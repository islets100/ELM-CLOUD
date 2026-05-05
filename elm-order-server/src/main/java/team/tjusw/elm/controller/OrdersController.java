package team.tjusw.elm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.Orders;
import team.tjusw.elm.service.OrdersService;

@RestController
@RequestMapping("/orders")
public class OrdersController {
	@Autowired
	private OrdersService ordersService;

	@GetMapping("/user/{userId}")
	public CommonResult<List<Orders>> listOrdersByUserId(@PathVariable("userId") String userId) throws Exception {
		List<Orders> ret = ordersService.listOrdersByUserId(userId);
		return new CommonResult<List<Orders>>(200, "ok", ret);
	}

	@GetMapping("/business/{businessId}")
	public CommonResult<List<Orders>> listOrdersByBusinessId(@PathVariable("businessId") Integer businessId)
			throws Exception {
		List<Orders> ret = ordersService.listOrdersByBusinessId(businessId);
		return new CommonResult<List<Orders>>(200, "ok", ret);
	}

	@PostMapping(value = { "", "/" })
	public CommonResult<Integer> createOrders(@RequestBody Orders orders) throws Exception {
		int ret = ordersService.createOrders(orders);
		if (ret == 0) {
			return new CommonResult<Integer>(505, "create order failed", null);
		}
		return new CommonResult<Integer>(201, "created", ret);
	}

	@GetMapping("/{orderId}")
	public CommonResult<Orders> getOrdersById(@PathVariable("orderId") Integer orderId) throws Exception {
		Orders order = ordersService.getOrdersById(orderId);
		if (order == null) {
			return new CommonResult<Orders>(404, "order not found", null);
		}
		return new CommonResult<Orders>(200, "ok", order);
	}

	@PutMapping("/{orderId}/state")
	public CommonResult<Orders> updateOrderState(@PathVariable("orderId") Integer orderId,
			@RequestParam("orderState") Integer orderState) throws Exception {
		if (orderState == null || orderState < 0 || orderState > 4) {
			return new CommonResult<Orders>(400, "invalid orderState", null);
		}
		Orders ret = ordersService.updateOrderState(orderId, orderState);
		if (ret == null) {
			return new CommonResult<Orders>(404, "order not found", null);
		}
		return new CommonResult<Orders>(200, "updated", ret);
	}

	@PostMapping("/{orderId}/pay/virtual-wallet")
	public CommonResult<Integer> payByVirtualWallet(@PathVariable("orderId") Integer orderId,
			@RequestParam(value = "pointAmount", required = false) Integer pointAmount) {
		if (pointAmount == null) {
			pointAmount = 0;
		}
		Orders order = ordersService.getOrdersById(orderId);
		Integer ret = ordersService.payByVirtualWallet(order.getUserId(), orderId, pointAmount);
		if (ret == 1) {
			return new CommonResult<Integer>(402, "insufficient wallet balance", ret);
		} else if (ret == 2) {
			return new CommonResult<Integer>(402, "insufficient point balance", ret);
		} else if (ret == 3) {
			return new CommonResult<Integer>(409, "order already paid", ret);
		} else if (ret == 200) {
			return new CommonResult<Integer>(200, "ok", ret);
		}
		return new CommonResult<Integer>(505, "payment failed", ret);
	}

	@PutMapping("/{orderId}/confirm-receipt")
	public CommonResult<Orders> confirmReceipt(@PathVariable("orderId") Integer orderId) throws Exception {
		Orders ret = ordersService.confirmReceipt(orderId);
		if (ret == null) {
			return new CommonResult<Orders>(400, "confirm receipt failed", null);
		}
		return new CommonResult<Orders>(200, "confirmed", ret);
	}
}
