package team.tjusw.elm.feign;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import team.tjusw.elm.po.CommonResult;

@FeignClient(name = "elm-wallet-server")
public interface VirtualWalletClient {

	@GetMapping("/wallet/getBalance")
	public CommonResult<BigDecimal> getBalanceByUserId(@RequestParam("userId") String userId);

	@PostMapping("/wallet/transfer")
	public CommonResult<Integer> transfer(@RequestParam("fromUserId") String fromUserId, @RequestParam("toUserId") String toUserId, @RequestParam("amount") BigDecimal amount);

	@PostMapping("/wallet/freezeFunds")
	public CommonResult<Integer> freezeFunds(@RequestParam("orderId") Integer orderId, @RequestParam("userId") String userId, @RequestParam("businessUserId") String businessUserId, @RequestParam("amount") BigDecimal amount);

	@PostMapping("/wallet/releaseFrozenFunds")
	public CommonResult<Integer> releaseFrozenFunds(@RequestParam("orderId") Integer orderId);
}
