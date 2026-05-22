package id.ac.ui.cs.advprog.bidmart.wallet.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Test
    void addBalance_NegativeAmountExceedingBalance_ThrowsException() {
        // GIVEN: A wallet with 50,000 available
        Wallet wallet = new Wallet("user-123");
        wallet.addBalance(50000L);

        // WHEN & THEN: Try to add -60,000 (which would drop balance below 0)
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            wallet.addBalance(-60000L);
        });

        assertEquals("Balance cannot become negative", exception.getMessage());
    }
}