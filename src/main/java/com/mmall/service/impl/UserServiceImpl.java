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
    int count = userMapper.checkUsername(user.getUsername());
    if(count > 0) {
      return ServerResponse.createByError("用户已存在");
    }
    count = userMapper.checkEmail(user.getEmail());
    if(count > 0) {
      return ServerResponse.createByError("邮箱已存在");
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
  
}
