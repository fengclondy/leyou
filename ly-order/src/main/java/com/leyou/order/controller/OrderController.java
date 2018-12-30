package com.leyou.order.controller;

import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import com.leyou.search.pojo.PageResult;
import com.leyou.utils.PayHelper;
import com.leyou.utils.PayState;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PayHelper payHelper;

    /**
     * 添加订单
     * @param order
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody @Valid Order order){
        Long id = this.orderService.createOrder(order);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }
    /**
     * 查询订单
     * @param id 订单编号
     * @return 订单对象
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id){
        Order order = this.orderService.queryOrderById(id);
        if (order == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(order);
    }

    /**
     * 分页查询当前已经登录的用户订单
     * @param page 页数
     * @param rows 每页大小
     * @param status 订单状态
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<PageResult<Order>> queryUserOrderList(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "status",required = false)Integer status
    ){

        PageResult<Order> result = this.orderService.queryUserOrderList(page,rows,status);
        if (result == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }


    /**
     * 更新订单状态
     * @param id
     * @param status
     * @return
     */
    @PutMapping("{id}/{status}")
    public ResponseEntity<Boolean> updateOrderStatus(@PathVariable("id") Long id,@PathVariable("status") Integer status){
        Boolean result = this.orderService.updateOrderStatus(id,status);
        if (result == null){
            //返回400
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        //返回204
        return new ResponseEntity<>(result,HttpStatus.NO_CONTENT);
    }


    /**
     * 根据订单id生成付款链接
     * @param orderId
     * @return
     */
    @GetMapping("url/{id}")
    public ResponseEntity<String> generateUrl(@PathVariable("id") Long orderId){
        Order order = orderService.queryOrderById(orderId);
        if (1!=order.getStatus()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String url = this.payHelper.createPayUrl(orderId);
        if (StringUtils.isNotBlank(url)){
            return ResponseEntity.ok(url);
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }




    /**
     * 查询付款状态
     * @param orderId
     * @return
     */
    @GetMapping("state/{id}")
    public ResponseEntity<Integer> queryPayState(@PathVariable("id") Long orderId){
        PayState payState = this.payHelper.queryOrder(orderId);
        return ResponseEntity.ok(payState.getValue());
    }
}
