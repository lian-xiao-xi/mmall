package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.domain.SaltAndTokenVo;
import com.mmall.domain.backend.UserLoginVo;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manager/user")
public class UserManagerController {
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

  @RequestMapping(value = "login.do", method = RequestMethod.POST)
  @ResponseBody
  public ServerResponse<User> login(@RequestBody UserLoginVo loginVo, HttpSession session) {
    ServerResponse<User> response = iUserService.validateCredentials(loginVo.getUsername(), loginVo.getPassword(), session);
    if(response.isSuccess()) {
      User user = response.getData();
      Integer role = user.getRole();
      if(role == Const.Roles.ROLE_ADMIN) {
        session.setAttribute(Const.CURRENT_USER, user);
        return response;
      } else {
        return ServerResponse.createByError("不是管理员，无法登录");
      }
    }
    return response;
  }
}
