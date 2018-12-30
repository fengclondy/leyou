package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService service;

    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody List<Cart> carts){
        Cart cart = carts.get(0);
        service.addCart(cart);
        return ResponseEntity.ok().build();
    }
    @GetMapping
    public ResponseEntity<List<Cart>> queryCart(){
        List<Cart> carts=service.queryCart();
        return  ResponseEntity.ok(carts);
    }
    @PutMapping
    public ResponseEntity<Void> updateNum(@RequestBody Cart carts) {
        service.updateNum(carts);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleCart(@PathVariable("id") String id) {
        service.deleCart(id);
        return ResponseEntity.ok().build();
    }


}
