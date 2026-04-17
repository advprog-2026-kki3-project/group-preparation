package id.ac.ui.cs.advprog.bidmart.catalogue.repository;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import id.ac.ui.cs.advprog.bidmart.catalogue.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, String> {

    List<Listing> findByTitleContainingIgnoreCase(String keyword);

    List<Listing> findByCategory(Category category);

    List<Listing> findByCurrentPriceBetween(double min, double max);

    List<Listing> findByActiveTrue();
}