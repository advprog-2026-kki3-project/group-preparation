package id.ac.ui.cs.advprog.bidmart.wallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository transactionRepository;

    @Override
    public Wallet getWallet(String userId) {
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet == null) {
            wallet = walletRepository.save(new Wallet(userId)); // Auto-create for the 25% slice demo
        }
        return wallet;
    }

    @Override
    public Wallet topUp(String userId, Long amount) {
        Wallet wallet = getWallet(userId);
        wallet.addBalance(amount);
        walletRepository.save(wallet);

        // Save to audit trail
        transactionRepository.save(new WalletTransaction(userId, "TOP_UP", amount));

        // Mock Event Contract for Milestone 25%
        System.out.println("Mock Event Published: WalletBalanceUpdated for user " + userId);

        return wallet;
    }

    @Override
    public List<WalletTransaction> getHistory(String userId) {
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}