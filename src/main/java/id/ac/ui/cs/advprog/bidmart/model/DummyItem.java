package id.ac.ui.cs.advprog.bidmart.model;

import jakarta.persistence.*;

@Entity
public class DummyItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message = "Hello World from BidMart Database!";

    public String getMessage() { return message; }
}