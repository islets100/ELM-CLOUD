package team.tjusw.elm.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import team.tjusw.elm.po.Cart;
import team.tjusw.elm.service.CartService;

@ExtendWith(MockitoExtension.class)
public class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

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

        // 初始化 MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
    }

    @Test
    void testListCartByUserId() throws Exception {
        // 模拟返回数据
        when(cartService.listCart(any(Cart.class))).thenReturn(cartList);

        // 执行测试
        mockMvc.perform(get("/carts/user/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功获取购物车列表."))
                .andExpect(jsonPath("$.result.length()").value(1));

        // 验证方法调用
        verify(cartService, times(1)).listCart(any(Cart.class));
    }

    @Test
    void testListCartByUserIdAndBusinessId() throws Exception {
        // 模拟返回数据
        when(cartService.listCart(any(Cart.class))).thenReturn(cartList);

        // 执行测试
        mockMvc.perform(get("/carts/user/user1/business/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功获取购物车列表."))
                .andExpect(jsonPath("$.result.length()").value(1));

        // 验证方法调用
        verify(cartService, times(1)).listCart(any(Cart.class));
    }

    @Test
    void testRemoveCartByUserIdAndBusinessId() throws Exception {
        // 模拟返回数据
        when(cartService.removeCart(any(Cart.class))).thenReturn(1);

        // 执行测试
        mockMvc.perform(delete("/carts/user/user1/business/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功删除."))
                .andExpect(jsonPath("$.result").value(1));

        // 验证方法调用
        verify(cartService, times(1)).removeCart(any(Cart.class));
    }

    @Test
    void testRemoveCartByUserIdAndBusinessIdAndFoodId() throws Exception {
        // 模拟返回数据
        when(cartService.removeCart(any(Cart.class))).thenReturn(1);

        // 执行测试
        mockMvc.perform(delete("/carts/user/user1/business/1/food/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功删除."))
                .andExpect(jsonPath("$.result").value(1));

        // 验证方法调用
        verify(cartService, times(1)).removeCart(any(Cart.class));
    }

    @Test
    void testSaveCart() throws Exception {
        // 模拟返回数据
        when(cartService.saveCart(any(Cart.class))).thenReturn(1);

        // 执行测试
        mockMvc.perform(post("/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(cart)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("成功新增购物车."))
                .andExpect(jsonPath("$.result").value(1));

        // 验证方法调用
        verify(cartService, times(1)).saveCart(any(Cart.class));
    }

    @Test
    void testUpdateCart() throws Exception {
        // 模拟返回数据
        when(cartService.updateCart(any(Cart.class))).thenReturn(1);

        // 执行测试
        mockMvc.perform(put("/carts/user/user1/business/1/food/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(cart)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功更改商品数量."))
                .andExpect(jsonPath("$.result").value(1));

        // 验证方法调用
        verify(cartService, times(1)).updateCart(any(Cart.class));
    }
}
