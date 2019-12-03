package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.domain.SaltAndTokenVo;
import com.mmall.domain.protal.UserLoginVo;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {

  @Autowired
  private IUserService iUserService;

  @RequestMapping(value = "before_login.do", method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<SaltAndTokenVo> beforeLogin(@RequestParam String username, HttpSession session) {
    if(StringUtils.isBlank(username))
      return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());

    SaltAndTokenVo saltAndTokenVo = iUserService.beforeLogin(username, session);
    return ServerResponse.createBySuccess(saltAndTokenVo);
  }

  // 登录
  @RequestMapping(value = "login.do", method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<User> login(@RequestBody UserLoginVo loginVo, HttpSession session) {
    ServerResponse<User> validateCredentials = iUserService.validateCredentials(loginVo.getUsername(), loginVo.getPassword(), session);
    if(!validateCredentials.isSuccess()) return validateCredentials;
    User user = validateCredentials.getData();
    session.setAttribute(Const.CURRENT_USER, user);
    return ServerResponse.createBySuccess(user);
  }

  // 退出登录
  @RequestMapping(value = "logout.do", method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<String> logout(HttpSession session) {
    session.removeAttribute(Const.CURRENT_USER);
    return ServerResponse.createBySuccess();
  }

  // 注册
  @RequestMapping(value = "register.do", method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<String> register(User user) {
    return iUserService.register(user);
  }

  // 用户邮箱、手机号校验
  @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<String> checkValid(String str, String type) {
    return iUserService.checkValid(str, type);
  }

  // 获取当前用户信息
  @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
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
  @RequestMapping(value = "question_is_true", method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<String> uIsTrue(String username, String question,  String answer) {
    return iUserService.checkAnswer(username, question, answer);
  }

  // 忘记密码中的重置密码
  @RequestMapping(value = "forget_reset_password", method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<String> forgetRestPassword(String username, String passwordNew, String forgetToken) {
    return iUserService.forgetRestPassword(username, passwordNew, forgetToken);
  }

  // 登录状态下的重置密码
  @RequestMapping(value = "reset_password", method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, HttpSession session) {
    User user = (User) session.getAttribute(Const.CURRENT_USER);
    if(user == null) return ServerResponse.createByError("用户未登录");
    return iUserService.resetPassword(passwordOld, passwordNew, user);
  }

  // 更新用户个人基本信息(email,phone等，不包含用户名、密码、角色)
  @RequestMapping(value = "update_user_info", method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<User> updateUserInfo(User user, HttpSession session) {
    User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
    if(currentUser == null) return ServerResponse.createByError("用户未登录");
    // 前端传过来的user不含用户id
    user.setId(currentUser.getId());
    user.setUsername(currentUser.getUsername());
    ServerResponse<User> userServerResponse = iUserService.updateUserInfo(user);
    if(userServerResponse.isSuccess()) {
      User data = userServerResponse.getData();
      data.setUsername(user.getUsername());
      session.setAttribute(Const.CURRENT_USER, data);
    }
    return userServerResponse;
  }

  // 获取用户个人基本信息
  @RequestMapping(value = "get_user_info", method = RequestMethod.GET)
  @ResponseBody
  public ServerResponse<User> get_user_info(HttpSession session) {
    User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
    if(currentUser == null) return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(), "用户尚未登录，请先登录");
    return iUserService.getUserInfo(currentUser.getId());
  }
}
