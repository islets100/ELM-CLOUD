package team.tjusw.elm.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import team.tjusw.elm.feign.BusinessClient;
import team.tjusw.elm.feign.CartClient;
import team.tjusw.elm.feign.PointClient;
import team.tjusw.elm.feign.VirtualWalletClient;
import team.tjusw.elm.lock.RedissonConfig;
import team.tjusw.elm.mapper.OrderDetailetMapper;
import team.tjusw.elm.mapper.OrdersMapper;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.OrderDetailet;
import team.tjusw.elm.po.Orders;
import team.tjusw.elm.service.OrdersService;
import team.tjusw.elm.util.CommonUtil;

@Service
public class OrdersServiceImpl implements OrdersService {

	@Autowired
	private OrdersMapper ordersMapper;

	@Autowired
	private OrderDetailetMapper orderDetailetMapper;

	@Autowired
	private CartClient cartClient;

	@Autowired
	private VirtualWalletClient virtualWalletClient;

	@Autowired
	private PointClient pointClient;

	@Autowired
	private BusinessClient businessClient;

	@Autowired
	private RedissonConfig redissonConfig;

	private final ConcurrentHashMap<Integer, Object> orderLocks = new ConcurrentHashMap<>();

	@Override
	@Transactional
	public int createOrders(Orders orders) {
		CommonResult<?> cartResult = cartClient.listCart(orders.getUserId(), orders.getBusinessId());
		List<LinkedHashMap<?, ?>> cartList = (List<LinkedHashMap<?, ?>>) cartResult.getResult();

		orders.setOrderDate(CommonUtil.getCurrentDate());
		ordersMapper.saveOrders(orders);
		int orderId = orders.getOrderId();

		List<OrderDetailet> list = new ArrayList<>();
		for (LinkedHashMap<?, ?> c : cartList) {
			OrderDetailet od = new OrderDetailet();
			od.setOrderId(orderId);
			od.setFoodId((Integer) c.get("foodId"));
			od.setQuantity((Integer) c.get("quantity"));
			list.add(od);
		}
		orderDetailetMapper.saveOrderDetailetBatch(list);

		cartClient.removeCart(orders.getUserId(), orders.getBusinessId());
		return orderId;
	}

	@Override
	public Orders getOrdersById(Integer orderId) {
		Orders order = ordersMapper.getOrdersById(orderId);
		if (order == null) {
			return null;
		}
		return attachOrderDetails(order);
	}

	@Override
	public List<Orders> listOrdersByUserId(String userId) {
		return attachOrderDetails(ordersMapper.listOrdersByUserId(userId));
	}

	@Override
	public List<Orders> listOrdersByBusinessId(Integer businessId) {
		return attachOrderDetails(ordersMapper.listOrdersByBusinessId(businessId));
	}

	@Override
	public Orders updateOrderState(Integer orderId, Integer orderState) {
		Orders existing = ordersMapper.getOrdersById(orderId);
		if (existing == null) {
			return null;
		}
		ordersMapper.updateOrderState(orderId, orderState);
		return getOrdersById(orderId);
	}

	@Override
	public Integer payByVirtualWallet(String userId, Integer orderId, int pointAmount) {
		Object lockObject = orderLocks.computeIfAbsent(orderId, k -> new Object());

		if (redissonConfig.redissonClient != null) {
			RLock orderLock = redissonConfig.redissonClient.getLock("order-" + orderId);
			orderLock.lock(redissonConfig.timeout, TimeUnit.MILLISECONDS);
			try {
				return processPayment(userId, orderId, pointAmount);
			} finally {
				orderLock.unlock();
			}
		}

		synchronized (lockObject) {
			return processPayment(userId, orderId, pointAmount);
		}
	}

	private Integer processPayment(String userId, Integer orderId, int pointAmount) {
		Orders order = ordersMapper.getOrdersById(orderId);
		if (order.getOrderState() == 1) {
			return 3;
		}

		String businessUserId = (String) ((LinkedHashMap<?, ?>) businessClient.getBusinessById(order.getBusinessId())
				.getResult()).get("userId");

		BigDecimal fromBalance = virtualWalletClient.getBalanceByUserId(userId).getResult();
		Integer pointBalance = pointClient.getBalanceByUserId(userId).getResult();

		if (fromBalance.compareTo(order.getOrderTotal()) < 0) {
			return 1;
		}
		if (pointBalance < pointAmount) {
			return 2;
		}

		BigDecimal deductedAmount = pointClient.getDeductedAmount(orderId, pointAmount).getResult();
		BigDecimal amount = order.getOrderTotal().subtract(deductedAmount);

		if (virtualWalletClient.transfer(userId, businessUserId, amount).getCode() != 200) {
			return 0;
		}
		pointClient.earnPointByOrder(orderId);
		ordersMapper.finishOrder(orderId);
		pointClient.deductOrder(orderId, pointAmount);
		return 200;
	}

	private List<Orders> attachOrderDetails(List<Orders> orders) {
		for (Orders order : orders) {
			attachOrderDetails(order);
		}
		return orders;
	}

	private Orders attachOrderDetails(Orders order) {
		order.setList(orderDetailetMapper.listOrderDetailetByOrderId(order.getOrderId()));
		return order;
	}
}
