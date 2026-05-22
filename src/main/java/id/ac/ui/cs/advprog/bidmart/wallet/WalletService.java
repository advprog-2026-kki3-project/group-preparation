package id.ac.ui.cs.advprog.bidmart.wallet;

import java.util.List;

public interface WalletService {
    Wallet getWalletByUserId(String userId);
    List<WalletTransaction> getHistory(String userId);
    void topUp(String userId, Long amount);

    void holdFunds(String userId, Long amount);
    void releaseFunds(String userId, Long amount);
    void commitPayment(String userId, Long amount);

    void withdraw(String userId, Long amount, String bankAccount);
}