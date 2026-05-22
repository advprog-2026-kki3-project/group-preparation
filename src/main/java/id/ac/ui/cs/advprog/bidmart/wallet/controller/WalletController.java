package id.ac.ui.cs.advprog.bidmart.wallet.controller;

import id.ac.ui.cs.advprog.bidmart.auth.security.RequiresPermission;
import id.ac.ui.cs.advprog.bidmart.wallet.service.WalletService;
import id.ac.ui.cs.advprog.bidmart.wallet.model.WalletTransaction;
import id.ac.ui.cs.advprog.bidmart.wallet.model.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    // DTOs for clean incoming requests
    public record TopUpRequest(Long amount) {}
    public record WithdrawRequest(Long amount, String bankAccount) {}

    @GetMapping
    @RequiresPermission(allowed = "wallet:view")
    public ResponseEntity<Wallet> getWallet(Principal principal) {
        return ResponseEntity.ok(walletService.getWalletByUserId(principal.getName()));
    }

    @GetMapping("/history")
    @RequiresPermission(allowed = "wallet:view")
    public ResponseEntity<List<WalletTransaction>> getHistory(Principal principal) {
        return ResponseEntity.ok(walletService.getHistory(principal.getName()));
    }

    @PostMapping("/topup")
    @RequiresPermission(allowed = "wallet:create")
    public ResponseEntity<?> topUp(Principal principal, @RequestBody TopUpRequest request) {
        walletService.topUp(principal.getName(), request.amount());
        return ResponseEntity.ok(Map.of("message", "Top up successful"));
    }

    @PostMapping("/withdraw")
    @RequiresPermission(allowed = "wallet:create")
    public ResponseEntity<?> withdraw(Principal principal, @RequestBody WithdrawRequest request) {
        try {
            walletService.withdraw(principal.getName(), request.amount(), request.bankAccount());
            return ResponseEntity.ok(Map.of("message", "Withdrawal successful to " + request.bankAccount()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}