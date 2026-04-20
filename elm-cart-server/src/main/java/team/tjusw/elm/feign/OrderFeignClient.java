package team.tjusw.elm.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.Order;
import team.tjusw.elm.po.OrderRequest;

@FeignClient(name = "elm-business-server")
public interface OrderFeignClient {
    
    @PostMapping("/orders")
    CommonResult<Order> createOrder(@RequestBody OrderRequest orderRequest);
}
