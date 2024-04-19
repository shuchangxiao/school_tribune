package edu.hubu.controller;

import edu.hubu.entity.RestBean;
import edu.hubu.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    @Resource
    AccountService accountService;
    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam String email, @RequestParam String type, HttpServletRequest request){
        String X = request.getRemoteAddr();
        String message = accountService.registerEmailVerifyCode(type,email,request.getRemoteAddr());
        return message == null?RestBean.success():RestBean.failure(400,message);
    }
}
