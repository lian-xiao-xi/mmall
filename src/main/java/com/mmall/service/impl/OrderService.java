package com.mmall.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.ExtendParams;
import com.alipay.api.domain.GoodsDetail;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.OrderItemMapper;
import com.mmall.dao.OrderMapper;
import com.mmall.dao.PayInfoMapper;
import com.mmall.pojo.Order;
import com.mmall.pojo.OrderItem;
import com.mmall.pojo.PayInfo;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public ServerResponse<OrderVo> createOrder(int shippingId, List<Integer> cartIds, int userId) {


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
