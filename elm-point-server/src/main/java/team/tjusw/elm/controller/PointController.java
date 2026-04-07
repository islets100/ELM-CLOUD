package team.tjusw.elm.controller;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.service.PointService;

@RestController
@RequestMapping("/points")
public class PointController {
	
	@Autowired
	private PointService pointService;
	
	@GetMapping("/balance/{userId}")
	public CommonResult<Integer> getBalanceByUserId(@PathVariable("userId") String userId) {
		int balance = pointService.getBalanceByUserId(userId);
		return new CommonResult<Integer>(200, "成功获取积分余额", balance);
	}
	
	@GetMapping("/earn/{orderId}")
	public CommonResult<Integer> earnPointByOrder(@PathVariable("orderId") Integer orderId) {
		int result = pointService.earnPointByOrder(orderId);
		return new CommonResult<Integer>(200, "成功获取订单积分", result);
	}
	
	@GetMapping("/deduct/{orderId}/{pointAmount}")
	public CommonResult<Integer> deductOrder(@PathVariable("orderId") Integer orderId, @PathVariable("pointAmount") int pointAmount) {
		int result = pointService.deductOrder(orderId, pointAmount);
		return new CommonResult<Integer>(200, "成功扣除积分", result);
	}
	
	@GetMapping("/deducted/{orderId}/{pointAmount}")
	public CommonResult<BigDecimal> getDeductedAmount(@PathVariable("orderId") Integer orderId, @PathVariable("pointAmount") int pointAmount) {
		BigDecimal amount = pointService.getDeductedAmount(orderId, pointAmount);
		return new CommonResult<BigDecimal>(200, "成功获取抵扣金额", amount);
	}
}