package team.tjusw.elm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.Order;
import team.tjusw.elm.po.OrderDetail;
import team.tjusw.elm.po.OrderRequest;
import team.tjusw.elm.service.OrderService;

//@CrossOrigin("*") 
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public CommonResult<Order> createOrder(@RequestBody OrderRequest orderRequest) throws Exception {
        Order order = orderService.createOrder(
                orderRequest.getUserId(),
                orderRequest.getBusinessId(),
                orderRequest.getDaId(),
                orderRequest.getOrderDetails()
        );
        if (order == null) {
            return new CommonResult<Order>(505, "订单创建失败", null);
        } else {
            return new CommonResult<Order>(201, "订单创建成功", order);
        }
    }

    @GetMapping("/user/{userId}")
    public CommonResult<List<Order>> listOrdersByUserId(@PathVariable("userId") String userId) throws Exception {
        List<Order> orders = orderService.listOrdersByUserId(userId);
        return new CommonResult<List<Order>>(200, "获取订单列表成功", orders);
    }

    @GetMapping("/business/{businessId}")
    public CommonResult<List<Order>> listOrdersByBusinessId(@PathVariable("businessId") Integer businessId) throws Exception {
        List<Order> orders = orderService.listOrdersByBusinessId(businessId);
        return new CommonResult<List<Order>>(200, "获取商家订单列表成功", orders);
    }

    @GetMapping("/{orderId}")
    public CommonResult<Order> getOrderById(@PathVariable("orderId") Integer orderId) throws Exception {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return new CommonResult<Order>(404, "订单不存在", null);
        } else {
            return new CommonResult<Order>(200, "获取订单详情成功", order);
        }
    }

    @GetMapping("/{orderId}/details")
    public CommonResult<List<OrderDetail>> listOrderDetailByOrderId(@PathVariable("orderId") Integer orderId) throws Exception {
        List<OrderDetail> orderDetails = orderService.listOrderDetailByOrderId(orderId);
        return new CommonResult<List<OrderDetail>>(200, "获取订单详情成功", orderDetails);
    }

    @PutMapping("/{orderId}/pay")
    public CommonResult<Integer> payOrder(@PathVariable("orderId") Integer orderId) throws Exception {
        int result = orderService.payOrder(orderId);
        if (result == 0) {
            return new CommonResult<Integer>(404, "订单不存在", result);
        } else {
            return new CommonResult<Integer>(200, "支付成功", result);
        }
    }

    @PutMapping("/{orderId}/deliver")
    public CommonResult<Integer> deliverOrder(@PathVariable("orderId") Integer orderId) throws Exception {
        int result = orderService.deliverOrder(orderId);
        if (result == 0) {
            return new CommonResult<Integer>(404, "订单不存在", result);
        } else {
            return new CommonResult<Integer>(200, "开始配送", result);
        }
    }

    @PutMapping("/{orderId}/checkout")
    public CommonResult<Integer> checkoutOrder(@PathVariable("orderId") Integer orderId) throws Exception {
        int result = orderService.checkoutOrder(orderId);
        if (result == 0) {
            return new CommonResult<Integer>(404, "订单不存在", result);
        } else {
            return new CommonResult<Integer>(200, "订单完成", result);
        }
    }
}

