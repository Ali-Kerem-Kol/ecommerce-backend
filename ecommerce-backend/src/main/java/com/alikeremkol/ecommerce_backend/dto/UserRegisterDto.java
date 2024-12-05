package com.alikeremkol.ecommerce_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterDto {
    private String email;
    private String password;
    private String username;
}
