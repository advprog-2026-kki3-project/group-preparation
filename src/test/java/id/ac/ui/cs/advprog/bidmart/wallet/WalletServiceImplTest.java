package id.ac.ui.cs.advprog.bidmart.wallet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

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
        verify(transactionRepository, times(1)).save(any(WalletTransaction.class));
    }

    @Test
    void testHoldFundsSuccess() {
        Wallet mockWallet = new Wallet("user-123");
        mockWallet.addBalance(100000L);

        when(walletRepository.findByUserId("user-123")).thenReturn(mockWallet);
        when(walletRepository.save(any(Wallet.class))).thenReturn(mockWallet);

        walletService.holdFunds("user-123", 40000L);

        assertEquals(60000L, mockWallet.getAvailableBalance());
        assertEquals(40000L, mockWallet.getHeldBalance());
        verify(walletRepository, times(1)).save(mockWallet);
        verify(transactionRepository, times(1)).save(any(WalletTransaction.class));
    }

    @Test
    void testHoldFundsThrowsExceptionWhenInsufficient() {
        Wallet mockWallet = new Wallet("user-123");
        mockWallet.addBalance(20000L);

        when(walletRepository.findByUserId("user-123")).thenReturn(mockWallet);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            walletService.holdFunds("user-123", 50000L);
        });

        assertEquals("Insufficient available balance to place this bid.", exception.getMessage());

        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never()).save(any(WalletTransaction.class));
    }
}