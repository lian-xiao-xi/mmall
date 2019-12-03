package com.mmall.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaltAndTokenVo {
    private String salt;
    private String token;
}
