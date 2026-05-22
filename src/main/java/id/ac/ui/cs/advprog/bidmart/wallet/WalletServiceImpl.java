package id.ac.ui.cs.advprog.bidmart.wallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository transactionRepository;

    @Override
    public Wallet getWalletByUserId(String userId) {
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet == null) {
            wallet = new Wallet(userId);
            return walletRepository.save(wallet);
        }
        return wallet;
    }

    @Override
    public List<WalletTransaction> getHistory(String userId) {
        getWalletByUserId(userId);
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    @Override
    @Transactional
    public void topUp(String userId, Long amount) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.addBalance(amount);
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, "TOP_UP", amount));
    }

    @Override
    @Transactional
    public void holdFunds(String userId, Long amount) {
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet not found");
        }

        wallet.holdBalance(amount);
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, "HOLD", amount));
    }

    @Override
    @Transactional
    public void releaseFunds(String userId, Long amount) {
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet not found");
        }

        wallet.releaseBalance(amount);
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, "RELEASE", amount));
    }

    @Override
    @Transactional
    public void commitPayment(String userId, Long amount) {
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet not found");
        }

        wallet.deductHeldBalance(amount);
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, "PAYMENT", amount));
    }

    @Override
    @Transactional
    public void withdraw(String userId, Long amount, String bankAccount) {
        Wallet wallet = walletRepository.findByUserId(userId);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet not found");
        }

        wallet.withdrawBalance(amount);
        walletRepository.save(wallet);
        transactionRepository.save(new WalletTransaction(userId, "WITHDRAW", amount));
    }
}