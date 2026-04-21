package team.tjusw.elm.service;

import java.util.List;

import team.tjusw.elm.po.Orders;

public interface OrdersService {
	public int createOrders(Orders orders);

	public Orders getOrdersById(Integer orderId);

	public List<Orders> listOrdersByUserId(String userId);

	public List<Orders> listOrdersByBusinessId(Integer businessId);

	public Orders updateOrderState(Integer orderId, Integer orderState);

	public Integer payByVirtualWallet(String userId, Integer orderId, int pointAmount);
}
