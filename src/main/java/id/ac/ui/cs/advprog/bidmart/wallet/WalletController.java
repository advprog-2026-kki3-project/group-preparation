package id.ac.ui.cs.advprog.bidmart.wallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping
    public ResponseEntity<Wallet> getWallet(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // principal.getName() returns the email/username of the logged-in user
        Wallet wallet = walletService.getWallet(principal.getName());
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/history")
    public ResponseEntity<List<WalletTransaction>> getHistory(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<WalletTransaction> history = walletService.getHistory(principal.getName());
        return ResponseEntity.ok(history);
    }

    @PostMapping("/topup")
    public ResponseEntity<Map<String, String>> topUp(Principal principal, @RequestBody TopUpRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        walletService.topUp(principal.getName(), request.getAmount());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Top up successful");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }
}