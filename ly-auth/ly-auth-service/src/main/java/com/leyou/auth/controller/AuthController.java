package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.CookieUtils;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private JwtProperties prop;


    @PostMapping("accredit")
    public ResponseEntity<Void> Login(@RequestParam("username") String username, @RequestParam("password") String password,
                                       HttpServletRequest request, HttpServletResponse response){
        try {
        String token = service.Login(username,password);
        CookieUtils.setCookie(request,response,prop.getCookieName(),token,prop.getCookieMaxAge(),null,true);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }catch (Exception e){
            throw new RuntimeException("登录失败");
        }
    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN") String token,
                                           HttpServletRequest request,
                                           HttpServletResponse response){
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            String newToken = JwtUtils.generateToken(userInfo,
                    prop.getPrivateKey(), prop.getExpire());
            CookieUtils.setCookie(request,response,prop.getCookieName(),newToken,prop.getCookieMaxAge(),null,true);
            return ResponseEntity.ok(userInfo);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
