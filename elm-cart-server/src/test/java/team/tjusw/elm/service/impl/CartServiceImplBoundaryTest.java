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
public class CartServiceImplBoundaryTest {

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        cart = new Cart();
        cart.setCartId(1);
        cart.setFoodId(1);
        cart.setBusinessId(1);
        cart.setUserId("user1");
        cart.setQuantity(2);
    }

    @Test
    void testSaveCart_EmptyCart() {
        // 测试空购物车对象
        Cart emptyCart = new Cart();

        // 模拟返回空列表，表示购物车中不存在该商品
        when(cartMapper.listCart(any(Cart.class))).thenReturn(new ArrayList<>());
        // 模拟插入操作
        when(cartMapper.insertCart(any(Cart.class))).thenReturn(1);

        // 执行测试方法
        int result = cartService.saveCart(emptyCart);

        // 验证结果
        assertEquals(1, result);

        // 验证方法调用
        verify(cartMapper, times(1)).listCart(any(Cart.class));
        verify(cartMapper, times(1)).insertCart(any(Cart.class));
    }

    @Test
    void testSaveCart_ZeroQuantity() {
        // 测试数量为 0 的情况
        cart.setQuantity(0);

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
    }

    @Test
    void testRemoveCart_ItemNotFound() {
        // 模拟删除操作，返回 0 表示未找到
        when(cartMapper.removeCart(any(Cart.class))).thenReturn(0);

        // 执行测试方法
        int result = cartService.removeCart(cart);

        // 验证结果
        assertEquals(0, result);

        // 验证方法调用
        verify(cartMapper, times(1)).removeCart(any(Cart.class));
    }

    @Test
    void testListCart_EmptyList() {
        // 模拟返回空列表
        when(cartMapper.listCart(any(Cart.class))).thenReturn(new ArrayList<>());

        // 执行测试方法
        List<Cart> result = cartService.listCart(cart);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证方法调用
        verify(cartMapper, times(1)).listCart(any(Cart.class));
    }

    @Test
    void testUpdateCart_ItemNotFound() {
        // 模拟更新操作，返回 0 表示未找到
        when(cartMapper.updateCart(any(Cart.class))).thenReturn(0);

        // 执行测试方法
        int result = cartService.updateCart(cart);

        // 验证结果
        assertEquals(0, result);

        // 验证方法调用
        verify(cartMapper, times(1)).updateCart(any(Cart.class));
    }

    @Test
    void testUpdateCart_ZeroQuantity() {
        // 测试数量为 0 的情况
        cart.setQuantity(0);

        // 模拟更新操作
        when(cartMapper.updateCart(any(Cart.class))).thenReturn(1);

        // 执行测试方法
        int result = cartService.updateCart(cart);

        // 验证结果
        assertEquals(1, result);

        // 验证方法调用
        verify(cartMapper, times(1)).updateCart(any(Cart.class));
    }

    @Test
    void testSaveCart_ExistingItemWithZeroQuantity() {
        // 测试已有商品数量为 0 的情况
        Cart existingCart = new Cart();
        existingCart.setCartId(1);
        existingCart.setFoodId(1);
        existingCart.setBusinessId(1);
        existingCart.setUserId("user1");
        existingCart.setQuantity(0);

        List<Cart> existingCarts = new ArrayList<>();
        existingCarts.add(existingCart);

        // 模拟返回购物车中已存在的商品
        when(cartMapper.listCart(any(Cart.class))).thenReturn(existingCarts);
        // 模拟更新操作
        when(cartMapper.updateCart(any(Cart.class))).thenReturn(1);

        // 执行测试方法
        int result = cartService.saveCart(cart);

        // 验证结果
        assertEquals(1, result);

        // 验证方法调用
        verify(cartMapper, times(1)).listCart(any(Cart.class));
        verify(cartMapper, times(1)).updateCart(any(Cart.class));
    }
}
