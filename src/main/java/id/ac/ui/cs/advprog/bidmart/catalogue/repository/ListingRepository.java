package id.ac.ui.cs.advprog.bidmart.catalogue.repository;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import id.ac.ui.cs.advprog.bidmart.catalogue.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, String> {

    List<Listing> findByTitleContainingIgnoreCase(String keyword);
    List<Listing> findByCategory(Category category);
    List<Listing> findByCurrentPriceBetween(double min, double max);
    List<Listing> findByActiveTrue();

    // All parameters are now strictly CAST so PostgreSQL never guesses their types
    @Query("SELECT l FROM Listing l WHERE l.active = true " +
            "AND (CAST(:keyword AS String) IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS String), '%'))) " +
            "AND (:category IS NULL OR l.category = :category) " +
            "AND (CAST(:minPrice AS Double) IS NULL OR l.currentPrice >= :minPrice) " +
            "AND (CAST(:maxPrice AS Double) IS NULL OR l.currentPrice <= :maxPrice) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR l.endTime <= :endDate)")
    List<Listing> searchListings(
            @Param("keyword") String keyword,
            @Param("category") Category category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("endDate") LocalDateTime endDate
    );
}