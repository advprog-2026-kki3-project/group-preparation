package id.ac.ui.cs.advprog.bidmart.wallet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository; // Mock the new audit trail repo

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void testTopUpIncreasesBalance() {
        Wallet mockWallet = new Wallet("user-123");
        when(walletRepository.findByUserId("user-123")).thenReturn(mockWallet);
        when(walletRepository.save(any(Wallet.class))).thenReturn(mockWallet);

        walletService.topUp("user-123", 50000L);

        assertEquals(50000L, mockWallet.getAvailableBalance());
        verify(walletRepository, times(1)).save(mockWallet);
        verify(transactionRepository, times(1)).save(any(WalletTransaction.class)); // Verifies the audit trail was saved
    }
}