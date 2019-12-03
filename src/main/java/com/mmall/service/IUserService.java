package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.domain.SaltAndTokenVo;
import com.mmall.pojo.User;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

public interface IUserService {
  User getByUsername(String username);
  SaltAndTokenVo beforeLogin(@RequestParam String username, HttpSession session);
  ServerResponse<User> validateCredentials(String username, String password, HttpSession session);
  ServerResponse<String> register(User user);
  ServerResponse<String> checkValid(String str, String type);
  ServerResponse selectQuestion(String username);
  ServerResponse<String> checkAnswer(String username, String question,  String answer);
  ServerResponse<String> forgetRestPassword(String username, String passwordNew, String forgetToken);
  ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user);
  ServerResponse<User> updateUserInfo(User user);
  ServerResponse<User> getUserInfo(int userId);
  <T> ServerResponse<T> isAdminRole(User user);
  <T> ServerResponse<T> isAdminRole(HttpSession session);
}
