package com.alikeremkol.ecommerce_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserVerifyDto {
    private String email;
    private String verificationCode;
}