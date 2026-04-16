package id.ac.ui.cs.advprog.bidmart.auction.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuctionWebController {

    @GetMapping("/auction/view")
    public String viewAuctionDetail(Model model) {
        model.addAttribute("auctionId", "mock-auction-123");
        
        return "auction/detail";
    }
}