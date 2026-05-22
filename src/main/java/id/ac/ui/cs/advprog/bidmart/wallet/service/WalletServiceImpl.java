package id.ac.ui.cs.advprog.bidmart.wallet.service;

import id.ac.ui.cs.advprog.bidmart.wallet.model.Wallet;
import id.ac.ui.cs.advprog.bidmart.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.bidmart.wallet.repository.WalletRepository;
import id.ac.ui.cs.advprog.bidmart.wallet.repository.WalletTransactionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final Counter walletTopUpSuccesses;
    private final Counter walletTopUpFailures;
    private final Counter walletWithdrawSuccesses;
    private final Counter walletWithdrawFailures;
    private final Counter walletPaymentSuccesses;
    private final Counter walletPaymentFailures;

    public WalletServiceImpl(
        WalletRepository walletRepository,
        WalletTransactionRepository transactionRepository,
        MeterRegistry meterRegistry
    ) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.walletTopUpSuccesses = Counter.builder("wallet_topup_total")
            .description("Total successful wallet top ups")
            .tag("result", "success")
            .register(meterRegistry);
        this.walletTopUpFailures = Counter.builder("wallet_topup_total")
            .description("Total failed wallet top ups")
            .tag("result", "failure")
            .register(meterRegistry);
        this.walletWithdrawSuccesses = Counter.builder("wallet_withdraw_total")
            .description("Total successful wallet withdrawals")
            .tag("result", "success")
            .register(meterRegistry);
        this.walletWithdrawFailures = Counter.builder("wallet_withdraw_total")
            .description("Total failed wallet withdrawals")
            .tag("result", "failure")
            .register(meterRegistry);
        this.walletPaymentSuccesses = Counter.builder("wallet_payment_total")
            .description("Total successful wallet payments")
            .tag("result", "success")
            .register(meterRegistry);
        this.walletPaymentFailures = Counter.builder("wallet_payment_total")
            .description("Total failed wallet payments")
            .tag("result", "failure")
            .register(meterRegistry);
    }

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