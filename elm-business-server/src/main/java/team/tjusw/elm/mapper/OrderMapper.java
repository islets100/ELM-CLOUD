package team.tjusw.elm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import team.tjusw.elm.po.Order;
import team.tjusw.elm.po.OrderDetail;

@Mapper
public interface OrderMapper {
	public int saveOrder(Order order);

	public int saveOrderDetail(OrderDetail orderDetail);

	@Select("select * from `order` where userId=#{userId} order by orderTime desc")
	public List<Order> listOrdersByUserId(String userId);

	@Select("select * from `order` where orderId=#{orderId}")
	public Order getOrderById(Integer orderId);

	@Select("select * from orderDetail where orderId=#{orderId}")
	public List<OrderDetail> listOrderDetailByOrderId(Integer orderId);

	@Select("select * from `order` where businessId=#{businessId} order by orderTime desc")
	public List<Order> listOrdersByBusinessId(Integer businessId);

	public int updateOrderStatus(Order order);

	public int updateOrderPayTime(Order order);

	public int updateOrderDeliveryTime(Order order);

	public int updateOrderCheckoutTime(Order order);
}
