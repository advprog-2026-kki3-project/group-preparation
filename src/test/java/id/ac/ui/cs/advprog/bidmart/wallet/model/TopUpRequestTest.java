package id.ac.ui.cs.advprog.bidmart.wallet.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TopUpRequestTest {

    @Test
    void testTopUpRequestGettersAndSetters() {
        // This tests the empty constructor (Line 7)
        TopUpRequest request = new TopUpRequest();

        // This tests the setter (Lines 13-15)
        request.setAmount(50000L);

        // This tests the getter (Lines 9-11)
        assertEquals(50000L, request.getAmount());
    }
}