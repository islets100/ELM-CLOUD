package team.tjusw.elm.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.OrderInfo;

@FeignClient(name = "elm-order-server")
public interface OrderClient {

	@GetMapping("/orders/{orderId}")
	CommonResult<OrderInfo> getOrderById(@PathVariable("orderId") Integer orderId);
}
