package id.ac.ui.cs.advprog.bidmart.repository;

import id.ac.ui.cs.advprog.bidmart.model.DummyItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyItemRepository extends JpaRepository<DummyItem, Long> {
}