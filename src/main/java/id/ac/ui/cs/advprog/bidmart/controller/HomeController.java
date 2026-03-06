package id.ac.ui.cs.advprog.bidmart.controller;

import id.ac.ui.cs.advprog.bidmart.order.dto.CreateOrderRequest;
import id.ac.ui.cs.advprog.bidmart.order.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {
    private final OrderService orderService;

    public HomeController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("orders", orderService.findAllOrders());
        model.addAttribute("createOrderRequest", new CreateOrderRequest());
        return "index";
    }

    @PostMapping("/orders")
    public String createOrder(@ModelAttribute CreateOrderRequest createOrderRequest, RedirectAttributes redirectAttributes) {
        orderService.createOrder(createOrderRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Order created and winner notification published.");
        return "redirect:/";
    }
}
