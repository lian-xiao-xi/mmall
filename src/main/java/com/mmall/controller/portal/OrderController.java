package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/order")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    @RequestMapping(value = "create.do", method = RequestMethod.POST)
    public ServerResponse createOrder(@RequestParam int shippingId, @RequestParam List<Integer> cartIds, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user ==null) return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        return iOrderService.createOrder(shippingId, cartIds, user.getId());
    }

    // 获取订单详情
    @RequestMapping(value = "get_order_cart_product.do", method = RequestMethod.GET)
    public ServerResponse getOrderCartProduct(@RequestParam List<Integer> cartIds, HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user ==null) return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        return null;
    }


    @RequestMapping(value = "pay.do", method = RequestMethod.GET)
    public ServerResponse pay(@RequestParam long orderNo, HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
//        String path = session.getServletContext().getRealPath("upload");
        return iOrderService.pay(orderNo, user.getId());
    }

    @RequestMapping("alipay_callback.do")
    public Object alipayCallback(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        HashMap<String, String> resultMap = new HashMap<>();
        for (Map.Entry<String, String[]> next : parameterMap.entrySet()) {
            String key = next.getKey();
            String[] values = next.getValue();
            int valLen = values.length;

//            String value = "";
//            if (valLen == 1) {
//                value= values[0];
//            } else if (valLen > 1) {
//                StringBuilder sb = new StringBuilder();
//                for (String val : values) {
//                    sb.append(",").append(val);
//                }
//                value = sb.toString().substring(1);
//            }

            StringBuilder stringBuilder = new StringBuilder();
            for (String val : values) {
                stringBuilder.append(",").append(val);
            }
            String value = stringBuilder.length() > 0 ? stringBuilder.toString().substring(1):stringBuilder.toString();
            resultMap.put(key, value);

        }
        logger.info("支付宝回调,sign:{},trade_status:{},参数:{}",resultMap.get("sign"),resultMap.get("trade_status"),resultMap.toString());
        try {
            // 支付宝回调验签，验证回调的正确性，是不是支付宝发过来的
            boolean alipayCallbackRSA2Check = AlipaySignature.rsaCheckV1(resultMap, Objects.requireNonNull(PropertiesUtil.getProperty("alipay.alipay_public_key")), "utf-8", PropertiesUtil.getProperty("alipay.sign_type"));
            logger.info("支付宝回调正确性：{}", alipayCallbackRSA2Check);
            if(!alipayCallbackRSA2Check) return "非法请求，回调验证不通过";
        } catch (AlipayApiException e) {
//            e.printStackTrace();
            logger.error("支付宝验证回调异常", e);
            return Const.AlipayCallbackResponseCode.RESPONSE_FAILED;
        }

//        验证各种订单相关数据
        ServerResponse<String> serverResponse = iOrderService.alipayCallback(resultMap);
        if(serverResponse.isSuccess()) return Const.AlipayCallbackResponseCode.RESPONSE_SUCCESS;
        else return Const.AlipayCallbackResponseCode.RESPONSE_FAILED;
    }

    // 查询订单状态
    @RequestMapping(value = "query_order_pay_status.do", method = RequestMethod.GET)
    public ServerResponse<Boolean> pay(@RequestParam long orderNo, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user ==null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse<String> serverResponse = iOrderService.queryOrderPayStatus(orderNo, user.getId());
        if(serverResponse.isSuccess()) return ServerResponse.createBySuccess(true);
        else return ServerResponse.createBySuccess(false);
    }
}
