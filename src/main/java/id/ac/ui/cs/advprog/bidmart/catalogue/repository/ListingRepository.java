package id.ac.ui.cs.advprog.bidmart.catalogue.repository;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingRepository extends JpaRepository<Listing, String> {
}