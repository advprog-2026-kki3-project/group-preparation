package id.ac.ui.cs.advprog.bidmart.wallet;

import java.util.List;

public interface WalletService {
    Wallet getWallet(String userId);
    Wallet topUp(String userId, Long amount);
    List<WalletTransaction> getHistory(String userId);
}