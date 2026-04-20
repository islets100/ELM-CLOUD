package team.tjusw.elm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import team.tjusw.elm.mapper.CartMapper;
import team.tjusw.elm.po.Cart;
import team.tjusw.elm.service.CartService;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartMapper cartMapper;
	
	
	
	@Override
	public int saveCart(Cart cart) {
		// 先检查是否已存在
		Cart queryCart = new Cart();
		queryCart.setUserId(cart.getUserId());
		queryCart.setBusinessId(cart.getBusinessId());
		queryCart.setFoodId(cart.getFoodId());
		
		List<Cart> existingCarts = cartMapper.listCart(queryCart);
		
		if (existingCarts != null && !existingCarts.isEmpty()) {
			// 已存在，更新数量
			Cart existingCart = existingCarts.get(0);
			existingCart.setQuantity(existingCart.getQuantity() + cart.getQuantity());
			return cartMapper.updateCart(existingCart);
		} else {
			// 不存在，插入新记录
			return cartMapper.insertCart(cart);
		}
	}
	

	@Override
	public int removeCart(Cart cart) {
		return cartMapper.removeCart(cart);
	}


	@Override
	public List<Cart> listCart(Cart cart) {
		return cartMapper.listCart(cart);
	}
	
	


	@Override
	public int updateCart(Cart cart) {
		return cartMapper.updateCart(cart);
	}

}
