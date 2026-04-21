package team.tjusw.elm.feign;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import team.tjusw.elm.po.CommonResult;

@FeignClient(name = "elm-point-server")
public interface PointClient {
	@GetMapping("/points/balance/{userId}")
	public CommonResult<Integer> getBalanceByUserId(@PathVariable("userId") String userId);
	
	@GetMapping("/points/deducted/{orderId}/{pointAmount}")
	public CommonResult<BigDecimal> getDeductedAmount(@PathVariable("orderId") Integer orderId, @PathVariable("pointAmount") Integer pointAmount);
	
	@GetMapping("/points/earn/{orderId}")
	public CommonResult<Integer> earnPointByOrder(@PathVariable("orderId") Integer orderId);
	
	@GetMapping("/points/deduct/{orderId}/{pointAmount}")
	CommonResult<Integer> deductOrder(@PathVariable("orderId") Integer orderId, @PathVariable("pointAmount") int pointAmount);
	
}
