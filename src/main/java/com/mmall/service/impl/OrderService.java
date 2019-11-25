package com.mmall.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.ExtendParams;
import com.alipay.api.domain.GoodsDetail;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.mmall.common.BigDecimalUtil;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse createOrder(int shippingId, List<Integer> cartIds, int userId) {
        // 产生订单的购物车列表
        List<Cart> cartList = cartMapper.selectByUserIdAndIds(userId, cartIds);
        if(cartList.isEmpty()) return ServerResponse.createByError("购物车为空");
        if(cartList.size() != cartIds.size()) return ServerResponse.createByError("部分购物车不存在，系统异常");
        ServerResponse<List<OrderItem>> orderItemsResponse = this.getOrderItemList(userId, cartList);
        if(!orderItemsResponse.isSuccess()) return orderItemsResponse;

        List<OrderItem> orderItemList = orderItemsResponse.getData();
        // 订单总价
        BigDecimal orderTotalPrice = this.getOrderTotalPrice(orderItemList);

        // 生成订单
        ServerResponse<Order> orderResponse = this.generateOrder(userId, shippingId, orderTotalPrice);
        if(!orderResponse.isSuccess()) return orderResponse;
        Order order = orderResponse.getData();
        long orderNo = order.getOrderNo();

        // 设置子订单对应的订单号
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(orderNo);
        }

        // 批量生成子订单
        int batchInsert = orderItemMapper.batchInsert(orderItemList);
        if(batchInsert != orderItemList.size()) return ServerResponse.createByError("生成子订单异常，系统异常");

        // 更新产品库存
        int updateProductNum = this.updateProductStock2(orderItemList);
        if(updateProductNum != orderItemList.size()) return ServerResponse.createByError("更新产品库存异常，系统异常");

        // 清空所选购物车
        int deleteCartNum = cartMapper.deleteByUserIdAndIds(userId, cartIds);
        if(deleteCartNum != cartIds.size()) return ServerResponse.createByError("清空购物车异常，系统异常");

        // 组合Vo返回
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    // 产生子订单列表
    private ServerResponse<List<OrderItem>> getOrderItemList(int userId, List<Cart> cartList) {
        ArrayList<OrderItem> orderItemList = new ArrayList<>();

        //校验购物车的数据,包括产品的状态和数量
        for (Cart cart : cartList) {
            if(cart == null) return ServerResponse.createByError("部分购物车异常，请刷新后重试");
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if(product == null) return ServerResponse.createByError("购物车中部分产品不存在，请刷新后重试");
            String productName = product.getName();
            // 检测产品是否为在线售卖状态
            if(Const.ProductStatus.ON_SALE.getCode() != product.getStatus()) return ServerResponse.createByError(productName+"不是在线售卖状态");
            // 检测产品库存
            if(product.getStock() < cart.getQuantity()) {
                return ServerResponse.createByError(productName+"库存不足");
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(userId);
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(cart.getQuantity(), product.getPrice().doubleValue()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    // 结算订单总价
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        return orderItemList.stream().map(OrderItem::getTotalPrice).reduce(new BigDecimal("0"), BigDecimal::add);
    }

    // 生成订单
    private ServerResponse<Order> generateOrder(Integer userId, Integer shippingId, BigDecimal totalPrice) {
        Shipping shipping = shippingMapper.selectByIdAndUserId(shippingId, userId);
        if(shipping == null) return ServerResponse.createByError("生成订单异常，收货地址不存在");
        Order order = new Order();
        order.setShippingId(shippingId);
        long orderNo = this.generateOrderNo(userId);
        order.setOrderNo(orderNo);
        order.setPayment(totalPrice);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPostage(0);
        order.setUserId(userId);
        // 发货时间、付款时间以后设置
        int insert = orderMapper.insert(order);
        if(insert>0) return ServerResponse.createBySuccess(order);
        return ServerResponse.createByError("生成订单异常");
    }

    // 生成订单号
    private long generateOrderNo(Integer userId) {
        String yyMMddHHmmss = DateFormatUtils.format(new Date(), "yyMMddHHmmss");
        return Long.parseLong(yyMMddHHmmss + userId + new Random().nextInt(100));
    }

    // 更新产品库存
    private int updateProductStock(List<OrderItem> orderItemList) {
        int updateNum = 0;
        for (OrderItem orderItem:orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            int i = productMapper.updateByPrimaryKey(product);
            updateNum += i;
        }
        return updateNum;
    }

    // 更新产品库存
    private int updateProductStock2(List<OrderItem> orderItemList) {
        ArrayList<Product> products = new ArrayList<>();
        for (OrderItem orderItem:orderItemList) {
            Product product = new Product();
            product.setId(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            products.add(product);
        }
        int i = productMapper.batchUpdate(products);
        return i;
    }

    // Todo 构造返回的Vo
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        return null;
    }




    @Override
    public ServerResponse<Map<String, String>> pay(long orderNo, int userId) {
        Order order = orderMapper.selectByUserIdAndOrderNo(orderNo, userId);
        if(order == null) return ServerResponse.createByError("用户没有该订单");

        HashMap<String, String> resultMap = new HashMap<>();
        resultMap.put("orderNo", Long.toString(order.getOrderNo()));

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = "mmall 扫码支付，订单号："+outTradeNo;

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = "订单 "+ outTradeNo + "购买商品共 " + totalAmount + "元";

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(userId, orderNo);
        List<GoodsDetail> goodsDetailList = orderItemList.stream().map(item -> {
            GoodsDetail good = new GoodsDetail();
            good.setGoodsId(item.getProductId().toString());
            good.setGoodsName(item.getProductName());
            good.setPrice(item.getCurrentUnitPrice().toString());
            good.setQuantity(item.getQuantity().longValue());
            return good;
        }).collect(Collectors.toList());

        String detailString = "";
        String extendParamsString = "";
        ObjectMapper objectMapper = new ObjectMapper();
        // 请务必在序列化的时候将 GoodsDetail、ExtendParams等类的属性名由驼峰转换为下划线！！！这一个坑浪费了我两个多小时。。。
        /*
        * 说明：对比 支付宝支付文档 https://docs.open.alipay.com/api_1/alipay.trade.precreate/ 中请求参数部分的 goods_detail 参数说明和 alipay-sdk 源码 com.alipay.api.domain 包下的 GoodsDetail 类属性名
        * */
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        try {
            detailString = objectMapper.writeValueAsString(goodsDetailList);
            extendParamsString = objectMapper.writeValueAsString(extendParams);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 创建扫码支付请求builder，设置请求参数
        DefaultAlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipaydev.com/gateway.do", PropertiesUtil.getProperty("alipay.appid"), PropertiesUtil.getProperty("alipay.private_key"), "json", "utf-8", PropertiesUtil.getProperty("alipay.alipay_public_key"), "RSA2");

        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"));
        String requestContent = "{" +
                "\"out_trade_no\":\"mmall_" + outTradeNo + "\"," +
                "\"total_amount\":\""+totalAmount+"\"," +
                "\"seller_id\":\""+sellerId+"\"," +
                "\"subject\":\""+subject+"\"," +
                (StringUtils.isEmpty(detailString) ? "":  "\"goods_detail\":"+detailString+",") +
                "\"body\":\""+body+"\"," +
                "\"operator_id\":\""+operatorId+"\"," +
                "\"store_id\":\""+storeId+"\"," +
                (StringUtils.isEmpty(extendParamsString) ? "":  "\"extend_params\":"+extendParamsString+",") +
                "\"timeout_express\":\""+timeoutExpress+"\""+" }";

        request.setBizContent(requestContent);

        AlipayTradePrecreateResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            logger.error("预下单异常");
            return ServerResponse.createByError("预下单异常");
        }
        if(response.isSuccess()){
            logger.info("预下单成功");
            resultMap.put("qrUrl", response.getQrCode());
            resultMap.put("no", response.getOutTradeNo());
            return ServerResponse.createBySuccess(resultMap);
        } else {
            logger.error("预下单失败");
            return ServerResponse.createByError("预下单失败");
        }
    }

    @Override
//    @Transactional
//    验证支付宝回调中的数据正确性
    public ServerResponse<String> alipayCallback(Map<String, String> params) {
        long outTradeNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        Order order = orderMapper.selectByOrderNo(outTradeNo);
        if(order == null) return ServerResponse.createByError("系统中不存在该订单");
//        避免支付宝重复通知
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) return ServerResponse.createByError("支付宝重复调用");
        String tradeStatus = params.get("trade_status");
        if(StringUtils.equals(tradeStatus, Const.AlipayTradeStatus.TRADE_SUCCESS.name())) {
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            try {
                order.setPaymentTime(DateUtils.parseDate(params.get("gmt_payment"), "yyyy-MM-dd HH:mm:ss"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int i = orderMapper.updateByPrimaryKeySelective(order);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        int insert = payInfoMapper.insert(payInfo);
        return ServerResponse.createBySuccess();
    }

    @Override
    public ServerResponse<String> queryOrderPayStatus(long orderNo, int userId) {
        Order order = orderMapper.selectByUserIdAndOrderNo(orderNo, userId);
        if(order == null) return ServerResponse.createByError("用户没有该订单");
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode())
            return ServerResponse.createBySuccess();
        return ServerResponse.createByError();
    }
}
