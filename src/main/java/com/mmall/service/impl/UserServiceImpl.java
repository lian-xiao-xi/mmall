package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.domain.SaltAndTokenVo;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import com.mmall.util.SecurityUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

  @Autowired
  private UserMapper userMapper;

  @Override
  public User getByUsername(String username) {
    return userMapper.selectByUsername(username);
  }

  @Override
  public SaltAndTokenVo beforeLogin(String username, HttpSession session) {
    String token = UUID.randomUUID().toString().replace("-", "");
    session.setAttribute(Const.CURRENT_USER_NAME, username);
    session.setAttribute(Const.CURRENT_USER_TOKEN, token);
    SaltAndTokenVo saltAndTokenVo = new SaltAndTokenVo();
    saltAndTokenVo.setToken(token);
    User user = userMapper.selectByUsername(username);
    if(user == null) {
      saltAndTokenVo.setSalt(UUID.randomUUID().toString().replace("-", ""));
    } else {
      saltAndTokenVo.setSalt(user.getSalt());
    }
    return saltAndTokenVo;
//    return null;
  }

  @Override
  public ServerResponse<User> validateCredentials(String username, String password, HttpSession session) {
    if(StringUtils.isBlank(username) || StringUtils.isBlank(password))
      return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    String token = (String) session.getAttribute(Const.CURRENT_USER_TOKEN);
    String userName = (String) session.getAttribute(Const.CURRENT_USER_NAME);
    if(token == null || userName == null) return ServerResponse.createByError("登录失败，请先获取盐");
    User user = userMapper.selectByUsername(username);
    if(user == null) return ServerResponse.createByError("用户不存在");
    String userCredentials = user.getPassword();
    String sha256HMAC = SecurityUtil.sha256HMAC(userCredentials + userName, token);
    boolean valid = StringUtils.equalsIgnoreCase(sha256HMAC.toLowerCase(), password) && StringUtils.isNotEmpty(userCredentials);
    if(!valid) return ServerResponse.createByError("用户名或密码错误");
    return ServerResponse.createBySuccess(user);
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
    User user = userMapper.selectByUsername(username);
    if(user == null) return ServerResponse.createByError("用户名不存在");
    String question = user.getQuestion();
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
  public <T> ServerResponse<T> isAdminRole(User user) {
    if(user ==null) return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    if(user.getRole() == Const.Roles.ROLE_ADMIN) {
      return ServerResponse.createBySuccess();
    }
    return ServerResponse.createByError(ResponseCode.PERMISSIONS_INSUFFICIENT.getCode(), ResponseCode.PERMISSIONS_INSUFFICIENT.getDesc());
  }

  // Todo 后期观察是否能使用此方法来替换 Service 层大量判断当前用户是否为管理员的重复代码
  @Override
  public <T> ServerResponse<T> isAdminRole(HttpSession session) {
    User user = (User) session.getAttribute(Const.CURRENT_USER);
    return this.isAdminRole(user);
  }

}
