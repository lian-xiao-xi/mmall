package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
  
  @Autowired
  private UserMapper userMapper;
  
  @Override
  public ServerResponse<User> login(String username, String password) {
    int count = userMapper.checkUsername(username);
    if(count == 0) {
      return ServerResponse.createByError("用户名不存在");
    }
    
    // 密码登录MD5加密
    String md5password = MD5Util.MD5EncodeUtf8(password);
    User user = userMapper.selectLogin(username, md5password);
    if(user == null) {
      return ServerResponse.createByError("密码错误");
    }
    user.setPassword(StringUtils.EMPTY);
    return ServerResponse.createBySuccess("登录成功", user);
  }
  
  @Override
  public ServerResponse<String> register(User user) {
    ServerResponse<String> checkUsername = this.checkValid(user.getUsername(), Const.USERNAME);
    if(!checkUsername.isSuccess()) {
      return checkUsername;
    }
    ServerResponse<String> checkEmail = this.checkValid(user.getEmail(), Const.EMAIL);
    if(!checkEmail.isSuccess()) {
      return checkEmail;
    }
    user.setRole(Const.Roles.ROLE_CUSTOMER);
  
    // MD5加密密码
    user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
  
    int insertCount = userMapper.insert(user);
    if(insertCount == 0) {
      return ServerResponse.createByError("注册失败");
    }
    return ServerResponse.createBySuccessMessage("注册成功");
  }
  
  @Override
  public ServerResponse<String> checkValid(String str, String type) {
    if(StringUtils.isNotBlank(str)) {
      // 开始校验
      if(Const.USERNAME.equals(type)) {
        int count = userMapper.checkUsername(str);
        if(count>0) {
          return ServerResponse.createByError("用户名已存在");
        }
      }
      if(Const.EMAIL.equals(type)) {
        int count = userMapper.checkEmail(str);
        if(count>0) {
          return ServerResponse.createByError("邮箱已经存在");
        }
      }
    } else {
      return ServerResponse.createByError("参数错误");
    }
    return ServerResponse.createBySuccessMessage("校验成功");
  }
  
  @Override
  public ServerResponse selectQuestion(String username) {
    ServerResponse<String> checkUsername = this.checkValid(username, Const.USERNAME);
    if(!checkUsername.isSuccess()) {
      return ServerResponse.createByError("用户名不存在");
    }
    String question = userMapper.selectQuestionByUsername(username);
    if(StringUtils.isNotBlank(question)) {
      return ServerResponse.createBySuccess(question);
    }
    return ServerResponse.createByError("找回密码的问题为空");
  }
  
  @Override
  public ServerResponse<String> checkAnswer(String username, String question, String answer) {
    int count = userMapper.checkAnswer(username, question, answer);
    if(count>0) {
      // 说明问题是这个用户的，且回答正确
    }
    return null;
  }
  
  
}
