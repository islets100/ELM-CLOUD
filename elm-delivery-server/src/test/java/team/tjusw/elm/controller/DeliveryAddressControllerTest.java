package team.tjusw.elm.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.DeliveryAddress;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DeliveryAddressControllerTest {

    @Autowired
    private DeliveryAddressController deliveryAddressController;

    @Test
    @DisplayName("测试查询用户地址列表接口")
    void testListDeliveryAddressByUserId() throws Exception {
        CommonResult<List<DeliveryAddress>> result = deliveryAddressController.listDeliveryAddressByUserId("user1001");

        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertEquals(1, result.getResult().size());
    }

    @Test
    @DisplayName("测试查询单个地址接口")
    void testGetDeliveryAddressById() throws Exception {
        CommonResult<DeliveryAddress> result = deliveryAddressController.getDeliveryAddressById(1);

        assertEquals(200, result.getCode());
        assertNotNull(result.getResult());
        assertEquals("user1001", result.getResult().getUserId());
    }

    @Test
    @DisplayName("测试新增地址接口")
    void testSaveDeliveryAddress() throws Exception {
        DeliveryAddress address = new DeliveryAddress();
        address.setContactName("王五");
        address.setContactSex(1);
        address.setContactTel("13800000003");
        address.setAddress("天津市河北区金钟河大街3号");
        address.setUserId("user1001");

        CommonResult<Integer> result = deliveryAddressController.saveDeliveryAddress(address);

        assertEquals(201, result.getCode());
        assertEquals(1, result.getResult());
    }
}
