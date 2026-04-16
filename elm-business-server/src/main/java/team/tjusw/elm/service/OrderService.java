package team.tjusw.elm.service;

import java.util.List;

import team.tjusw.elm.po.Order;
import team.tjusw.elm.po.OrderDetail;

public interface OrderService {
	public Order createOrder(String userId, Integer businessId, Integer daId, List<OrderDetail> orderDetails);

	public List<Order> listOrdersByUserId(String userId);

	public Order getOrderById(Integer orderId);

	public List<OrderDetail> listOrderDetailByOrderId(Integer orderId);

	public List<Order> listOrdersByBusinessId(Integer businessId);

	public int updateOrderStatus(Integer orderId, Integer orderState);

	public int payOrder(Integer orderId);

	public int deliverOrder(Integer orderId);

	public int checkoutOrder(Integer orderId);
}
