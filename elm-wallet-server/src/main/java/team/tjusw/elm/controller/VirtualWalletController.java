package team.tjusw.elm.controller;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.service.VirtualWalletService;

@RestController
@RequestMapping("/wallet")
public class VirtualWalletController {
	
	@Autowired
	private VirtualWalletService virtualWalletService;
	
	@RequestMapping(value = "/getBalance", method = RequestMethod.GET)
	public CommonResult<BigDecimal> getBalanceByUserId(@RequestParam String userId) {
		BigDecimal balance = virtualWalletService.getBalanceByUserId(userId);
		return new CommonResult<>(200, "success", balance);
	}
	
	@RequestMapping(value = "/transfer", method = RequestMethod.POST)
	public CommonResult<Integer> transfer(@RequestParam String fromUserId, @RequestParam String toUserId, @RequestParam BigDecimal amount) {
		int result = virtualWalletService.transfer(fromUserId, toUserId, amount);
		return new CommonResult<>(200, "success", result);
	}
	
	// 熔断降级处理
	@RequestMapping("/walletFallback")
	public CommonResult<String> walletFallback() {
		return new CommonResult<>(500, "wallet service fallback", null);
	}
}