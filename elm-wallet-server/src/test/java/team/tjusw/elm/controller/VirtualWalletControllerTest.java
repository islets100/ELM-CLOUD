package team.tjusw.elm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import team.tjusw.elm.po.CommonResult;
import team.tjusw.elm.po.VirtualWalletPO;
import team.tjusw.elm.service.VirtualWalletService;

@ExtendWith(MockitoExtension.class)
class VirtualWalletControllerTest {

    @Mock
    private VirtualWalletService walletService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private VirtualWalletController controller;

    @Test
    void getCurrentWalletShouldResolveUserIdFromJwtSubject() {
        VirtualWalletPO wallet = new VirtualWalletPO();
        wallet.setUserId("user123");
        wallet.setBalance(BigDecimal.ZERO);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + createToken("user123"));
        when(walletService.getVirtualWallet("user123")).thenReturn(wallet);

        CommonResult<VirtualWalletPO> response = controller.getCurrentWallet(request);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("user123", response.getResult().getUserId());
        verify(walletService).getVirtualWallet("user123");
    }

    private String createToken(String userId) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"sub\":\"" + userId + "\"}").getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".signature";
    }
}
