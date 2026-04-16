package team.tjusw.elm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import team.tjusw.elm.po.Cart;
import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.service.CartService;

//@CrossOrigin("*") 
@RestController
@RequestMapping("/carts")
public class CartController {
	
	
	@Autowired
	private CartService cartService;

	@GetMapping("/user/{userId}")
	public CommonResult<List<Cart>> listCartByUserId(@PathVariable("userId") String userId) throws Exception {
		Cart cart = new Cart();
		cart.setUserId(userId);
		List<Cart> ret = cartService.listCart(cart);
		if(ret == null)
			return  new CommonResult<List<Cart>>(505,"购物车服务内部错误.",null);
		else
			return  new CommonResult<List<Cart>>(200,"成功获取购物车列表.",ret);
	}

	@GetMapping("/user/{userId}/business/{businessId}")
	public CommonResult<List<Cart>> listCartByUserIdAndBusinessId(
			@PathVariable("userId") String userId, 
			@PathVariable("businessId") Integer businessId) throws Exception {
		Cart cart = new Cart();
		cart.setUserId(userId);
		cart.setBusinessId(businessId);
		List<Cart> ret = cartService.listCart(cart);
		if(ret == null)
			return  new CommonResult<List<Cart>>(505,"购物车服务内部错误.",null);
		else
			return  new CommonResult<List<Cart>>(200,"成功获取购物车列表.",ret);
	}

	@DeleteMapping("/user/{userId}/business/{businessId}")
	public CommonResult<Integer> removeCartByUserIdAndBusinessId(
			@PathVariable("userId") String userId, 
			@PathVariable("businessId") Integer businessId) throws Exception {
		Cart cart = new Cart();
		cart.setUserId(userId);
		cart.setBusinessId(businessId);
		int ret = cartService.removeCart(cart);
		if(ret==0)
			return new CommonResult<Integer>(404,"没有符合的购物车.",ret); 
		else
			return new CommonResult<Integer>(200,"成功删除.",ret);
	}
	
	@DeleteMapping("/user/{userId}/business/{businessId}/food/{foodId}")
	public CommonResult<Integer> removeCartByUserIdAndBusinessIdAndFoodId(
			@PathVariable("userId") String userId, 
			@PathVariable("businessId") Integer businessId, 
			@PathVariable("foodId") Integer foodId) throws Exception {
		Cart cart = new Cart();
		cart.setUserId(userId);
		cart.setBusinessId(businessId);
		cart.setFoodId(foodId);
		int ret = cartService.removeCart(cart);
		if(ret==0)
			return new CommonResult<Integer>(404,"没有符合的购物车.",ret); 
		else
			return new CommonResult<Integer>(200,"成功删除.",ret);
	}
	
	@PostMapping
	public CommonResult<Integer> saveCart(@RequestBody Cart cart) throws Exception {
		int ret =  cartService.saveCart(cart);
		if(ret==0)
			return new CommonResult<Integer>(505,"未知错误，未能新增购物车.",ret); 
		else
			return new CommonResult<Integer>(201,"成功新增购物车.",ret);
	}

	@PutMapping("/user/{userId}/business/{businessId}/food/{foodId}")
	public CommonResult<Integer> updateCart(
			@PathVariable("userId") String userId, 
			@PathVariable("businessId") Integer businessId, 
			@PathVariable("foodId") Integer foodId, 
			@RequestBody Cart cart) throws Exception {
		cart.setUserId(userId);
		cart.setBusinessId(businessId);
		cart.setFoodId(foodId);
		int ret =  cartService.updateCart(cart);
		if(ret==0)
			return new CommonResult<Integer>(404,"没有符合的购物车.",ret); 
		else
			return new CommonResult<Integer>(200,"成功更改商品数量.",ret);
	}

}
