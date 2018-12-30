package com.leyou.user.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
public class UserController {

    @Autowired
    private UserService service;


    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean>  checkUser(@PathVariable("data") String data,@PathVariable("type") Integer type){
        //System.out.println("有没有到达");
        return ResponseEntity.ok(service.check(data,type));
    }

    @PostMapping("code")
    public ResponseEntity<Void> Sms(@RequestParam("phone") String phone){
        service.sms(phone);
        return  ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @PostMapping("register")
    public  ResponseEntity<Void> register(@Valid User user, @RequestParam("code") String code){
        service.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("query")
    public ResponseEntity<User> queryUser( @RequestParam("username") String username,
                                           @RequestParam("password") String password){

        User user =service.queryUser(username,password);
        return ResponseEntity.ok(user);

    }
 }
