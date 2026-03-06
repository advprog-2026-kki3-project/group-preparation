package id.ac.ui.cs.advprog.bidmart.order.repo;

import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}
