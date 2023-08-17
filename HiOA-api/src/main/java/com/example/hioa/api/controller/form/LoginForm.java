package com.example.hioa.api.controller.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Schema(description = "登录表单类")
public class LoginForm {
//    空的时候返回给前端的异常信息
    @NotBlank(message = "username不能为空")
//    需要符合正则表达式，前一个意思是字母，后一个意思是限制5-20个字符之间，不符合的时候返回前端的信息
    @Pattern(regexp = "^[a -zA -Z0 - 9]{5,20}$", message = "username内容不正确")
//    schema注解用于schem doc，为了让swagger接收提交的数据
    @Schema(description ="用户名")
    private String username;

    @NotBlank(message = "password不能为空")
    @Pattern(regexp = "^[a -zA -Z0 - 9]{6,20}$", message = "password内容不正确")
    @Schema(description ="密码")
    private String password;
}
