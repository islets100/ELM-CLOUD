package team.tjusw.elm.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import team.tjusw.elm.po.DeliveryAddress;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DeliveryAddressServiceTest {

    @Autowired
    private DeliveryAddressService deliveryAddressService;

    @Test
    @DisplayName("测试根据用户ID查询地址列表")
    void testListDeliveryAddressByUserId() {
        List<DeliveryAddress> list = deliveryAddressService.listDeliveryAddressByUserId("user1001");

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("user1001", list.get(0).getUserId());
        assertEquals(1, list.get(0).getValid());
    }

    @Test
    @DisplayName("测试新增地址")
    void testSaveDeliveryAddress() {
        DeliveryAddress address = new DeliveryAddress();
        address.setContactName("李四");
        address.setContactSex(0);
        address.setContactTel("13800000002");
        address.setAddress("天津市南开区白堤路2号");
        address.setUserId("user1001");

        int result = deliveryAddressService.saveDeliveryAddress(address);
        List<DeliveryAddress> list = deliveryAddressService.listDeliveryAddressByUserId("user1001");

        assertEquals(1, result);
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("测试更新地址时旧地址失效并新增新地址")
    void testUpdateDeliveryAddress() {
        DeliveryAddress updated = new DeliveryAddress();
        updated.setDaId(1);
        updated.setContactName("张三-新");
        updated.setContactSex(1);
        updated.setContactTel("13800009999");
        updated.setAddress("天津市河西区友谊路99号");
        updated.setUserId("user1001");

        int result = deliveryAddressService.updateDeliveryAddress(updated);
        List<DeliveryAddress> list = deliveryAddressService.listDeliveryAddressByUserId("user1001");
        DeliveryAddress oldAddress = deliveryAddressService.getDeliveryAddressById(1);

        assertEquals(1, result);
        assertNotNull(oldAddress);
        assertEquals(0, oldAddress.getValid());
        assertEquals(1, list.size());
        assertEquals("user1001", list.get(0).getUserId());
        assertNotEquals(Integer.valueOf(1), list.get(0).getDaId());
    }

    @Test
    @DisplayName("测试删除地址")
    void testRemoveDeliveryAddress() {
        int result = deliveryAddressService.removeDeliveryAddress(1);
        List<DeliveryAddress> list = deliveryAddressService.listDeliveryAddressByUserId("user1001");
        DeliveryAddress removed = deliveryAddressService.getDeliveryAddressById(1);

        assertEquals(1, result);
        assertNotNull(removed);
        assertEquals(0, removed.getValid());
        assertTrue(list.isEmpty());
    }
}
