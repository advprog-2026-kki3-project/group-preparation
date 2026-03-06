package id.ac.ui.cs.advprog.bidmart.catalogue.model;

import jakarta.persistence.*;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    @ManyToOne
    private Category parentCategory;

    public Category() {}

    public Category(String name, Category parentCategory) {
        this.name = name;
        this.parentCategory = parentCategory;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public Category getParentCategory() { return parentCategory; }

    public void setName(String name) { this.name = name; }

    public void setParentCategory(Category parentCategory) {
        this.parentCategory = parentCategory;
    }
}