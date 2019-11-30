package com.mmall.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.ExtendParams;
import com.alipay.api.domain.GoodsDetail;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.BigDecimalUtil;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.domain.CreateOrderVo;
import com.mmall.domain.OrderProductInfoVo;
import com.mmall.domain.PageVo;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    // 这里的事务注解不管用（问题重现：把 updateProductStock2 方法中的 productMapper.batchUpdate 对应的 mybatis xml 文件的 sql 语句故意改为错误语法，使程序报错，可以发现仍然会在 order 表中插入一条记录）
    @Transactional
    public ServerResponse createOrder(CreateOrderVo vo, int userId) {
        Integer shippingId = vo.getShippingId();
        List<OrderProductInfoVo> productList = vo.getProductList();
        if(productList.isEmpty()) return ServerResponse.createByError("error 订单中没有商品");

        ServerResponse<List<OrderItem>> orderItemsResponse = this.getOrderItemList(userId, productList);
        if(!orderItemsResponse.isSuccess()) return orderItemsResponse;

        List<OrderItem> orderItemList = orderItemsResponse.getData();
        // 订单总价
        BigDecimal orderTotalPrice = this.getOrderTotalPrice(orderItemList);

        // 生成订单
        ServerResponse<Order> orderResponse = this.generateOrder(userId, shippingId, orderTotalPrice);
        if(!orderResponse.isSuccess()) return orderResponse;
        Order order = orderResponse.getData();

        // 检测收获地址
        Shipping shipping = shippingMapper.selectByIdAndUserId(order.getShippingId(), userId);
        if(shipping == null) return ServerResponse.createByError("创建订单异常，收货地址不存在");

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
        List<Integer> productIds = productList.stream().map(OrderProductInfoVo::getProductId).collect(Collectors.toList());
        int deleteCartNum = cartMapper.deleteByUserIdAndProductIds(userId, productIds);
        if(deleteCartNum != productIds.size()) return ServerResponse.createByError("清空购物车异常，系统异常");

        // 组合Vo返回
        OrderVo orderVo = this.assembleOrderVo(order, shipping, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse getOrderCartProduct(List<Integer> cartIds, int userId) {
        // 产生订单的购物车列表
        List<Cart> cartList = cartMapper.selectByUserIdAndIds(userId, cartIds);
        if(cartList.isEmpty()) return ServerResponse.createByError("没有选择任何购物车商品");
        if(cartList.size() != cartIds.size()) return ServerResponse.createByError("选择的一些购物车不存在了");
        List<OrderProductInfoVo> productInfoVos = cartList.stream().map(cart -> {
            OrderProductInfoVo productInfoVo = new OrderProductInfoVo();
            productInfoVo.setProductId(cart.getProductId());
            productInfoVo.setNum(cart.getQuantity());
            return productInfoVo;
        }).collect(Collectors.toList());
        ServerResponse<List<OrderItem>> serverResponse = this.getOrderItemList(userId, productInfoVos);
        if(!serverResponse.isSuccess()) return serverResponse;
        List<OrderItem> orderItemList = serverResponse.getData();
        List<OrderItemVo> orderItemVoList = orderItemList.stream().map(this::assembleOrderItemVo).collect(Collectors.toList());
        return ServerResponse.createBySuccess(orderItemVoList);
    }

    /*
     * Todo 记录一个问题：用户 “我的订单” 订单列表、订单详情接口中，如果当时那个订单的收货地址已经删除了，那么就无法显示这个订单的收货地址信息了。
     *  目前能想到的解决方案，订单表中冗余订单的收货地址信息，在创建订单时将收货地址信息写入订单表。但是个人觉得这个方案不很好，因为基本上要把收货地址表中的字段全都冗余到订单表 =_=
     * */
    @Override
    public ServerResponse<PageInfo<OrderVo>> userOrderList(PageVo page, int userId) {
        PageHelper.startPage(page.getPageNum(), page.getPageSize(), "create_time desc");
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = orderList.stream().map(order -> this.assembleOrderVo(order, userId)).collect(Collectors.toList());
        PageInfo<OrderVo> orderVoPageInfo = new PageInfo<>(orderVoList);
        return ServerResponse.createBySuccess(orderVoPageInfo);
    }

    @Override
    public ServerResponse<OrderVo> userOrderDetail(Long orderNo, Integer userId) {
        Order order = orderMapper.selectByUserIdAndOrderNo(orderNo, userId);
        if(order == null) return ServerResponse.createByError("找不到该订单");
        OrderVo orderVo = this.assembleOrderVo(order, userId);
        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    @Transactional
    public ServerResponse<String> cancelOrder(Long orderNo, Integer userId) {
        Order order = orderMapper.selectByUserIdAndOrderNo(orderNo, userId);
        if(order == null) return ServerResponse.createByError("找不到该订单");
        if(order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) return ServerResponse.createByError("订单已付款，无法取消");
        order.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int i = orderMapper.updateByPrimaryKey(order);
        if(i <= 0) return ServerResponse.createByError("更新订单失败");
        return ServerResponse.createBySuccess("取消订单成功");
    }

    // 产生子订单列表
    private ServerResponse<List<OrderItem>> getOrderItemList(int userId, List<OrderProductInfoVo> productList) {
        ArrayList<OrderItem> orderItemList = new ArrayList<>();

        //校验产品的数据,包括产品的状态和数量
        for (OrderProductInfoVo productInfo : productList) {
            Product product = productMapper.selectByPrimaryKey(productInfo.getProductId());
            if(product == null) return ServerResponse.createByError("订单中部分产品不存在，请刷新后重试");
            String productName = product.getName();
            // 检测产品是否为在线售卖状态
            if(Const.ProductStatus.ON_SALE.getCode() != product.getStatus()) return ServerResponse.createByError(productName+"不是在线售卖状态");
            // 检测产品库存
            if(product.getStock() < productInfo.getNum()) {
                return ServerResponse.createByError(productName+"库存不足");
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(userId);
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setQuantity(productInfo.getNum());
            orderItem.setTotalPrice(BigDecimalUtil.mul(productInfo.getNum(), product.getPrice().doubleValue()));
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
//            Product product = new Product();
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
//            product.setId(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            products.add(product);
        }
        int i = productMapper.batchUpdate(products);
        return i;
    }

    // 构造返回前端的Vo
    private OrderVo assembleOrderVo(Order order, Shipping shipping, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();

//        orderVo.setOrderNo(order.getOrderNo());
//        orderVo.setPayment(order.getPayment());
//        orderVo.setPaymentType(order.getPaymentType());
//        orderVo.setPostage(order.getPostage());
//        orderVo.setStatus(order.getStatus());
//        orderVo.setPaymentTime(order.getPaymentTime());
//        orderVo.setSendTime(order.getSendTime());
//        orderVo.setEndTime(order.getEndTime());
//        orderVo.setCloseTime(order.getCloseTime());
//        orderVo.setCreateTime(order.getCreateTime());

        BeanUtils.copyProperties(order, orderVo);

        ShippingVo shippingVo = this.assembleShippingVo(shipping);
        orderVo.setShippingVo(shippingVo);

        List<OrderItemVo> orderItemVoList = orderItemList.stream().map(this::assembleOrderItemVo).collect(Collectors.toList());
        orderVo.setOrderItemVoList(orderItemVoList);

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return orderVo;
    }

    // 根据order生成orderVo
    private OrderVo assembleOrderVo(Order order, Integer userId) {
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(userId, order.getOrderNo());
        Shipping shipping = shippingMapper.selectByIdAndUserId(order.getShippingId(), userId);
        return this.assembleOrderVo(order, shipping, orderItemList);
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        if(orderItem == null) return null;
        OrderItemVo orderItemVo = new OrderItemVo();
        BeanUtils.copyProperties(orderItem, orderItemVo);
        return orderItemVo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping) {
        if(shipping == null) return null;
        ShippingVo shippingVo = new ShippingVo();
        BeanUtils.copyProperties(shipping, shippingVo);
        return shippingVo;
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
