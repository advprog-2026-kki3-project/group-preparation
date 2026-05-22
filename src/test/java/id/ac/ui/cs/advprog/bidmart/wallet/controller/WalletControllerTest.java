package id.ac.ui.cs.advprog.bidmart.wallet.controller;

import id.ac.ui.cs.advprog.bidmart.wallet.model.Wallet;
import id.ac.ui.cs.advprog.bidmart.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        // The Golden Bullet: This builds the controller in total isolation,
        // completely bypassing Ayshia's global security configs and context loading!
        mockMvc = MockMvcBuilders.standaloneSetup(walletController).build();

        mockPrincipal = () -> "user-123";
    }

    @Test
    void getWallet_ReturnsOkAndWalletData() throws Exception {
        Wallet mockWallet = new Wallet("user-123");
        mockWallet.addBalance(150000L);
        when(walletService.getWalletByUserId("user-123")).thenReturn(mockWallet);

        mockMvc.perform(get("/api/wallet").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.availableBalance").value(150000));
    }

    @Test
    void topUp_ValidRequest_ReturnsOkWithMessage() throws Exception {
        String jsonRequest = "{\"amount\": 50000}";

        mockMvc.perform(post("/api/wallet/topup")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Top up successful"));

        verify(walletService, times(1)).topUp("user-123", 50000L);
    }

    @Test
    void withdraw_ValidRequest_ReturnsOkWithMessage() throws Exception {
        String jsonRequest = "{\"amount\": 50000, \"bankAccount\": \"BNI-999\"}";

        mockMvc.perform(post("/api/wallet/withdraw")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Withdrawal successful to BNI-999"));
    }

    @Test
    void withdraw_InsufficientFunds_ReturnsBadRequest() throws Exception {
        String jsonRequest = "{\"amount\": 9999999, \"bankAccount\": \"BNI-999\"}";

        doThrow(new IllegalStateException("Insufficient balance for withdrawal."))
                .when(walletService).withdraw("user-123", 9999999L, "BNI-999");

        mockMvc.perform(post("/api/wallet/withdraw")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient balance for withdrawal."));
    }

    @Test
    void getHistory_ReturnsOkAndTransactionList() throws Exception {
        // GIVEN: Fake a transaction history list
        id.ac.ui.cs.advprog.bidmart.wallet.model.WalletTransaction tx =
                new id.ac.ui.cs.advprog.bidmart.wallet.model.WalletTransaction("user-123", "TOP_UP", 50000L);

        when(walletService.getHistory("user-123")).thenReturn(java.util.List.of(tx));

        // WHEN & THEN: Perform the GET request and check the JSON array
        mockMvc.perform(get("/api/wallet/history").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionType").value("TOP_UP"))
                .andExpect(jsonPath("$[0].amount").value(50000));
    }
}