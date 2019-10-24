package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
    // 不能将敏感信息传到前端
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
      String forgetToken = UUID.randomUUID().toString();
      TokenCache.setKey("token_"+username, forgetToken);
      return ServerResponse.createBySuccessMessage(forgetToken);
    }
    return ServerResponse.createByError("问题的答案错误");
  }

  @Override
  public ServerResponse<String> forgetRestPassword(String username, String passwordNew, String forgetToken) {
    if(StringUtils.isBlank(forgetToken)) {
      return ServerResponse.createByError("参数错误，token需要传递");
    }
    ServerResponse<String> checkUsername = this.checkValid(username, Const.USERNAME);
    if(checkUsername.isSuccess()) {
      return ServerResponse.createByError("用户不存在");
    }
    String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
    if(StringUtils.isBlank(token)) {
      return ServerResponse.createByError("token无效或者过期");
    }
    if (StringUtils.equals(forgetToken, token)) {
      String MD5passwordNew = MD5Util.MD5EncodeUtf8(passwordNew);
      int count = userMapper.updatePasswordByUsername(username, MD5passwordNew);
      if(count>0) {
        return ServerResponse.createBySuccessMessage("修改密码成功");
      }
    } else {
      return ServerResponse.createByError("token错误，请重新获取重置密码的token");
    }
    return ServerResponse.createByError("修改密码成功");
  }

  @Override
  public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
    // 防止横向越权，要校验一下这个用户的旧密码是不是所遇这个用户的
    int userCount = userMapper.checkPasswordByUserId(user.getId(), passwordOld);
    if(userCount <= 0) return ServerResponse.createByError("旧密码错误");
    user.setPassword(passwordNew);
    int updateCount = userMapper.updateByPrimaryKeySelective(user);
    // 更新用户的第二种方式
//    User updateUser = new User();
//    updateUser.setId(user.getId());
//    updateUser.setPassword(passwordNew);
//    int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
    if(updateCount <= 0) ServerResponse.createByError("更新密码失败");
    return ServerResponse.createBySuccessMessage("更新密码成功");
  }

  @Override
  public ServerResponse<User> updateUserInfo(User user) {
    // 不更新用户名
    // 校验email，要更新的新email是否已经被别的用户使用
    int emailCount = userMapper.checkEmailByUserId(user.getId(), user.getEmail());
    if(emailCount <= 0) return ServerResponse.createByError("email已经存在，请更换");
    User updateUser = new User();
    updateUser.setId(user.getId());
    updateUser.setAnswer(user.getAnswer());
    updateUser.setEmail(user.getEmail());
    updateUser.setPhone(user.getPhone());
    updateUser.setQuestion(user.getQuestion());
    int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
    if(updateCount <= 0) return ServerResponse.createByError("更新用户基本信息失败");
    return ServerResponse.createBySuccess("更新用户基本信息成功", updateUser);
  }
  
  @Override
  public ServerResponse<User> getUserInfo(int userId) {
    User user = userMapper.selectByPrimaryKey(userId);
    if(user == null) return ServerResponse.createByError("用户不存在");
    user.setPassword(StringUtils.EMPTY);
    return ServerResponse.createBySuccess(user);
  }

  // 当前用户是否为管理员角色
  // Todo 后期观察一下是否需要将返回值改为Boolean来重构此方法
  @Override
  public ServerResponse isAdminRole(User user) {
    if(user != null && user.getRole() == Const.Roles.ROLE_ADMIN) {
      return ServerResponse.createBySuccess();
    }
    return ServerResponse.createByError();
  }

}
