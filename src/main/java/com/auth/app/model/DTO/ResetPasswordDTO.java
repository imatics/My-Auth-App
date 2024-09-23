package com.auth.app.model.DTO;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetPasswordDTO{
        private String otp;
        private String newPassword;
        private String phone;
}
