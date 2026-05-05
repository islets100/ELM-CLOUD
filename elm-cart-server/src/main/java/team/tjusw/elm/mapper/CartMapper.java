package team.tjusw.elm.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import team.tjusw.elm.po.Cart;

@Mapper
public interface CartMapper {
	List<Cart> listCart(Cart cart);

	int insertCart(Cart cart);

	int updateCart(Cart cart);

	int removeCart(Cart cart);
}
