package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {
  
  @Autowired
  private IUserService iUserService;
  
  // 登录
  @RequestMapping(value = "login.do", method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<User> login(String username, String password, HttpSession session) {
    ServerResponse<User> response = iUserService.login(username, password);
    if(response.isSuccess()) {
      session.setAttribute(Const.CURRENT_USER, response.getData());
    }
    return response;
  }
  
  // 退出登录
  @RequestMapping(value = "logout.do", method = RequestMethod.GET)
  @ResponseBody
  public ServerResponse<String> logout(HttpSession session) {
    session.removeAttribute(Const.CURRENT_USER);
    return ServerResponse.createBySuccess();
  }
  
  // 注册
  @RequestMapping(value = "register.do", method = RequestMethod.GET)
  @ResponseBody
  public ServerResponse<String> register(User user) {
    return iUserService.register(user);
  }
  
  // 用户邮箱、手机号校验
  @RequestMapping(value = "check_valid.do", method = RequestMethod.GET)
  @ResponseBody
  public ServerResponse<String> checkValid(String str, String type) {
    return iUserService.checkValid(str, type);
  }
  
  // 获取当前用户信息
  @RequestMapping(value = "get_user_info.do", method = RequestMethod.GET)
  @ResponseBody
  public ServerResponse<User> getUserInfo(HttpSession session) {
    User user = (User) session.getAttribute(Const.CURRENT_USER);
    if(user == null) {
      return ServerResponse.createByError("用户未登录，无法获取当前用户信息");
    }
    return ServerResponse.createBySuccess(user);
  }
  
  // 获取密码提示问题
  @RequestMapping(value = "forget_get_question.do", method = RequestMethod.GET)
  @ResponseBody
  public ServerResponse forgetGetQuestion(String username) {
    return iUserService.selectQuestion(username);
  }
  
  // 判读密码提示问题是否回答正确
  @RequestMapping(value = "question_is_true", method = RequestMethod.GET)
  @ResponseBody
  public ServerResponse<String> uIsTrue(String username, String question,  String answer) {
    return iUserService.checkAnswer(username, question, answer);
  }
  
}
