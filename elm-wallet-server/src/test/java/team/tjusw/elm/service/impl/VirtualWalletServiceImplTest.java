package team.tjusw.elm.service.impl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import team.tjusw.elm.po.VirtualWalletPO;
import team.tjusw.elm.mapper.VirtualWalletMapper;
import team.tjusw.elm.mapper.OverdraftRecordMapper;
import team.tjusw.elm.feign.UserClient;

@ExtendWith(MockitoExtension.class)
public class VirtualWalletServiceImplTest {

    @Mock
    private VirtualWalletMapper walletMapper;
    @Mock
    private OverdraftRecordMapper overdraftMapper;
    @Mock
    private UserClient userClient;

    @InjectMocks
    private VirtualWalletServiceImpl walletService;

    @Test
    public void testGetBalance() {
        VirtualWalletPO wallet = new VirtualWalletPO();
        wallet.setBalance(new BigDecimal("100.00"));
        when(walletMapper.selectByUserId("user123")).thenReturn(wallet);

        BigDecimal balance = walletService.getBalance("user123");
        assertEquals(new BigDecimal("100.00"), balance);
    }
}