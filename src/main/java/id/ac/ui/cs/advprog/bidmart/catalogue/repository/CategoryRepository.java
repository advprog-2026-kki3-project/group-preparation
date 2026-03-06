package id.ac.ui.cs.advprog.bidmart.catalogue.repository;

import id.ac.ui.cs.advprog.bidmart.catalogue.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {
}
