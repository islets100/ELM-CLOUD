package team.tjusw.elm.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import team.tjusw.elm.mapper.CartMapper;
import team.tjusw.elm.po.Cart;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private List<Cart> cartList;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        cart = new Cart();
        cart.setCartId(1);
        cart.setFoodId(1);
        cart.setBusinessId(1);
        cart.setUserId("user1");
        cart.setQuantity(2);

        cartList = new ArrayList<>();
        cartList.add(cart);
    }

    @Test
    void testSaveCart_NewItem() {
        // 模拟返回空列表，表示购物车中不存在该商品
        when(cartMapper.listCart(any(Cart.class))).thenReturn(new ArrayList<>());
        // 模拟插入操作
        when(cartMapper.insertCart(any(Cart.class))).thenReturn(1);

        // 执行测试方法
        int result = cartService.saveCart(cart);

        // 验证结果
        assertEquals(1, result);

        // 验证方法调用
        verify(cartMapper, times(1)).listCart(any(Cart.class));
        verify(cartMapper, times(1)).insertCart(any(Cart.class));
        verify(cartMapper, times(0)).updateCart(any(Cart.class));
    }

    @Test
    void testSaveCart_ExistingItem() {
        // 模拟返回购物车中已存在的商品
        when(cartMapper.listCart(any(Cart.class))).thenReturn(cartList);
        // 模拟更新操作
        when(cartMapper.updateCart(any(Cart.class))).thenReturn(1);

        // 执行测试方法
        int result = cartService.saveCart(cart);

        // 验证结果
        assertEquals(1, result);

        // 验证方法调用
        verify(cartMapper, times(1)).listCart(any(Cart.class));
        verify(cartMapper, times(1)).updateCart(any(Cart.class));
        verify(cartMapper, times(0)).insertCart(any(Cart.class));
    }

    @Test
    void testRemoveCart() {
        // 模拟删除操作
        when(cartMapper.removeCart(any(Cart.class))).thenReturn(1);

        // 执行测试方法
        int result = cartService.removeCart(cart);

        // 验证结果
        assertEquals(1, result);

        // 验证方法调用
        verify(cartMapper, times(1)).removeCart(any(Cart.class));
    }

    @Test
    void testListCart() {
        // 模拟返回购物车列表
        when(cartMapper.listCart(any(Cart.class))).thenReturn(cartList);

        // 执行测试方法
        List<Cart> result = cartService.listCart(cart);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(cart.getCartId(), result.get(0).getCartId());

        // 验证方法调用
        verify(cartMapper, times(1)).listCart(any(Cart.class));
    }

    @Test
    void testUpdateCart() {
        // 模拟更新操作
        when(cartMapper.updateCart(any(Cart.class))).thenReturn(1);

        // 执行测试方法
        int result = cartService.updateCart(cart);

        // 验证结果
        assertEquals(1, result);

        // 验证方法调用
        verify(cartMapper, times(1)).updateCart(any(Cart.class));
    }
}
