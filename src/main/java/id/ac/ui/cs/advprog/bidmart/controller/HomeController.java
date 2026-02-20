package id.ac.ui.cs.advprog.bidmart.controller;

import id.ac.ui.cs.advprog.bidmart.model.DummyItem;
import id.ac.ui.cs.advprog.bidmart.repository.DummyItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private DummyItemRepository dummyItemRepository;

    @GetMapping("/")
    public String home(Model model) {
        // Create a dummy item if the database is empty so we have something to show
        if (dummyItemRepository.count() == 0) {
            dummyItemRepository.save(new DummyItem());
        }

        // Fetch it and send it to the HTML page
        List<DummyItem> items = dummyItemRepository.findAll();
        model.addAttribute("items", items);

        return "index"; // This tells Spring to look for index.html
    }
}