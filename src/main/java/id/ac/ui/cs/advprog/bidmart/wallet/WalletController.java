package id.ac.ui.cs.advprog.bidmart.wallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping
    public String viewWallet(Principal principal, Model model) {
        String userId = (principal != null) ? principal.getName() : "user-123";

        model.addAttribute("wallet", walletService.getWallet(userId));
        model.addAttribute("history", walletService.getHistory(userId));
        return "wallet/index";
    }

    @PostMapping("/topup")
    public String topUp(Principal principal, @RequestParam Long amount) {
        String userId = (principal != null) ? principal.getName() : "user-123";

        walletService.topUp(userId, amount);
        return "redirect:/wallet";
    }
}