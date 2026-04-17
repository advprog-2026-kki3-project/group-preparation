package id.ac.ui.cs.advprog.bidmart.catalogue.model;

import jakarta.persistence.*;

@Entity
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String title;
    private String description;
    private double initialPrice;
    private double currentPrice;

    private int bidCount = 0;
    private boolean active = true;

    @ManyToOne
    private Category category;

    public Listing() {}

    public Listing(String title, String description, double initialPrice, Category category) {
        this.title = title;
        this.description = description;
        this.initialPrice = initialPrice;
        this.currentPrice = initialPrice;
        this.category = category;
    }

    public String getId() { return id; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public double getInitialPrice() { return initialPrice; }

    public double getCurrentPrice() { return currentPrice; }

    public int getBidCount() { return bidCount; }

    public boolean isActive() { return active; }

    public Category getCategory() { return category; }

    public void setTitle(String title) { this.title = title; }

    public void setDescription(String description) { this.description = description; }

    public void setInitialPrice(double initialPrice) { this.initialPrice = initialPrice; }

    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public void setBidCount(int bidCount) { this.bidCount = bidCount; }

    public void setActive(boolean active) { this.active = active; }

    public void setCategory(Category category) { this.category = category; }
}