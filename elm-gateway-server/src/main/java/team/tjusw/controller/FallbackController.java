package team.tjusw.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import team.tjusw.po.CommonResult;

@RestController
public class FallbackController {
	@RequestMapping("/globalFallback")
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public CommonResult<Map<String, Object>> globalFallback(ServerWebExchange exchange)
	{
		return buildFallbackResult("global", "Gateway触发了全局熔断降级方法.", exchange);
	}

	@RequestMapping("/userFallback")
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public CommonResult<Map<String, Object>> userFallback(ServerWebExchange exchange)
	{
		return buildFallbackResult("user", "Gateway触发了用户微服务的熔断降级方法.", exchange);
	}

	@RequestMapping("/businessFallback")
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public CommonResult<Map<String, Object>> businessFallback(ServerWebExchange exchange)
	{
		return buildFallbackResult("business", "Gateway触发了商家微服务的熔断降级方法.", exchange);
	}

	@RequestMapping("/cartFallback")
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public CommonResult<Map<String, Object>> cartFallback(ServerWebExchange exchange)
	{
		return buildFallbackResult("cart", "Gateway触发了购物车微服务的熔断降级方法.", exchange);
	}

	@RequestMapping("/foodFallback")
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public CommonResult<Map<String, Object>> foodFallback(ServerWebExchange exchange)
	{
		return buildFallbackResult("food", "Gateway触发了食品微服务的熔断降级方法.", exchange);
	}

	@RequestMapping("/deliveryFallback")
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public CommonResult<Map<String, Object>> deliveryFallback(ServerWebExchange exchange)
	{
		return buildFallbackResult("delivery", "Gateway触发了配送微服务的熔断降级方法.", exchange);
	}

	@RequestMapping("/orderFallback")
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public CommonResult<Map<String, Object>> orderFallback(ServerWebExchange exchange)
	{
		return buildFallbackResult("order", "Gateway触发了订单微服务的熔断降级方法.", exchange);
	}

	@RequestMapping("/walletFallback")
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public CommonResult<Map<String, Object>> walletFallback(ServerWebExchange exchange)
	{
		return buildFallbackResult("wallet", "Gateway触发了钱包微服务的熔断降级方法.", exchange);
	}

	@RequestMapping("/pointFallback")
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public CommonResult<Map<String, Object>> pointFallback(ServerWebExchange exchange)
	{
		return buildFallbackResult("point", "Gateway触发了积分微服务的熔断降级方法.", exchange);
	}

	private CommonResult<Map<String, Object>> buildFallbackResult(String service, String message, ServerWebExchange exchange) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("fallback", true);
		metadata.put("service", service);
		metadata.put("retryable", true);
		metadata.put("fallbackKey", service + "Fallback");
		metadata.put("path", exchange.getRequest().getURI().getRawPath());
		metadata.put("method", exchange.getRequest().getMethodValue());
		return new CommonResult<>(403, message, metadata);
	}
}
