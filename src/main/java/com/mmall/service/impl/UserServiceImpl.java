package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("userService")
public class UserServiceImpl implements IUserService {
  
  @Autowired
  private UserMapper userMapper;
  
  @Override
  public ServerResponse<User> login(String username, String password) {
    int count = userMapper.checkUsername(username);
    if(count == 0) {
      return ServerResponse.createByError("用户名不存在");
    }
    
    // todo 密码登录MD5加密
    
    User user = userMapper.selectLogin(username, password);
    if(user == null) {
      return ServerResponse.createByError("密码错误");
    }
    user.setPassword(StringUtils.EMPTY);
    return ServerResponse.createBySuccess("登录成功", user);
  }
}
