package id.ac.ui.cs.advprog.bidmart.wallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping
    public String viewWallet(Model model) {
        String mockUserId = "user-123"; // Mock auth for 25% milestone
        model.addAttribute("wallet", walletService.getWallet(mockUserId));
        model.addAttribute("history", walletService.getHistory(mockUserId));
        return "wallet/index";
    }

    @PostMapping("/topup")
    public String topUp(@RequestParam Long amount) {
        walletService.topUp("user-123", amount);
        return "redirect:/wallet";
    }
}