package id.ac.ui.cs.advprog.bidmart.wallet.service;

import id.ac.ui.cs.advprog.bidmart.wallet.model.Wallet;
import id.ac.ui.cs.advprog.bidmart.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.bidmart.wallet.repository.WalletRepository;
import id.ac.ui.cs.advprog.bidmart.wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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

    private final String testUserId = "user-123";
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = new Wallet(testUserId);
    }

    @Test
    void getWalletByUserId_ExistingWallet_ReturnsWallet() {
        when(walletRepository.findByUserId(testUserId)).thenReturn(testWallet);
        Wallet result = walletService.getWalletByUserId(testUserId);
        assertEquals(testUserId, result.getUserId());
    }

    @Test
    void getWalletByUserId_NewWallet_CreatesAndReturnsWallet() {
        when(walletRepository.findByUserId(testUserId)).thenReturn(null);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        Wallet result = walletService.getWalletByUserId(testUserId);

        assertNotNull(result);
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void topUp_AddsBalanceAndSavesTransaction() {
        when(walletRepository.findByUserId(testUserId)).thenReturn(testWallet);

        walletService.topUp(testUserId, 50000L);

        assertEquals(50000L, testWallet.getAvailableBalance());
        verify(walletRepository, times(1)).save(testWallet);
        verify(transactionRepository, times(1)).save(any(WalletTransaction.class));
    }

    @Test
    void holdFunds_Success_MovesToHeldBalance() {
        testWallet.addBalance(100000L);
        when(walletRepository.findByUserId(testUserId)).thenReturn(testWallet);

        walletService.holdFunds(testUserId, 40000L);

        assertEquals(60000L, testWallet.getAvailableBalance());
        assertEquals(40000L, testWallet.getHeldBalance());
    }

    @Test
    void holdFunds_WalletNotFound_ThrowsException() {
        when(walletRepository.findByUserId(testUserId)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            walletService.holdFunds(testUserId, 40000L);
        });
        assertEquals("Wallet not found", exception.getMessage());
    }

    @Test
    void holdFunds_InsufficientBalance_ThrowsException() {
        testWallet.addBalance(10000L);
        when(walletRepository.findByUserId(testUserId)).thenReturn(testWallet);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            walletService.holdFunds(testUserId, 50000L);
        });
        assertEquals("Insufficient available balance to place this bid.", exception.getMessage());
    }

    @Test
    void withdraw_Success_DeductsBalance() {
        testWallet.addBalance(100000L);
        when(walletRepository.findByUserId(testUserId)).thenReturn(testWallet);

        walletService.withdraw(testUserId, 50000L, "BCA-12345");

        assertEquals(50000L, testWallet.getAvailableBalance());
        verify(transactionRepository, times(1)).save(any(WalletTransaction.class));
    }

    @Test
    void withdraw_InsufficientBalance_ThrowsException() {
        testWallet.addBalance(10000L);
        when(walletRepository.findByUserId(testUserId)).thenReturn(testWallet);

        assertThrows(IllegalStateException.class, () -> {
            walletService.withdraw(testUserId, 50000L, "BCA-12345");
        });
    }

    @Test
    void getHistory_ReturnsTransactionList() {
        WalletTransaction tx = new WalletTransaction(testUserId, "TOP_UP", 50000L);
        when(transactionRepository.findByUserIdOrderByTimestampDesc(testUserId)).thenReturn(List.of(tx));

        List<WalletTransaction> history = walletService.getHistory(testUserId);

        assertFalse(history.isEmpty());
        assertEquals(1, history.size());
        verify(transactionRepository, times(1)).findByUserIdOrderByTimestampDesc(testUserId);
    }

    @Test
    void releaseFunds_Success_ReleasesHeldBalance() {
        testWallet.addBalance(100000L);
        testWallet.holdBalance(40000L);
        when(walletRepository.findByUserId(testUserId)).thenReturn(testWallet);

        walletService.releaseFunds(testUserId, 40000L);

        assertEquals(100000L, testWallet.getAvailableBalance());
        assertEquals(0L, testWallet.getHeldBalance());
        verify(walletRepository, times(1)).save(testWallet);
        verify(transactionRepository, times(1)).save(any(WalletTransaction.class));
    }

    @Test
    void releaseFunds_WalletNotFound_ThrowsException() {
        when(walletRepository.findByUserId(testUserId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.releaseFunds(testUserId, 40000L);
        });
    }

    @Test
    void commitPayment_Success_DeductsHeldBalance() {
        testWallet.addBalance(100000L);
        testWallet.holdBalance(40000L);
        when(walletRepository.findByUserId(testUserId)).thenReturn(testWallet);

        walletService.commitPayment(testUserId, 40000L);

        assertEquals(60000L, testWallet.getAvailableBalance());
        assertEquals(0L, testWallet.getHeldBalance());
        verify(walletRepository, times(1)).save(testWallet);
        verify(transactionRepository, times(1)).save(any(WalletTransaction.class));
    }

    @Test
    void commitPayment_WalletNotFound_ThrowsException() {
        when(walletRepository.findByUserId(testUserId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.commitPayment(testUserId, 40000L);
        });
    }

    @Test
    void withdraw_WalletNotFound_ThrowsException() {
        when(walletRepository.findByUserId(testUserId)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.withdraw(testUserId, 50000L, "BCA-123");
        });
    }
}