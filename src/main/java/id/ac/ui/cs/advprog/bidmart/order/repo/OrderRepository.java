package id.ac.ui.cs.advprog.bidmart.order.repo;

import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    boolean existsByAuctionId(Long auctionId);

    Optional<OrderEntity> findByAuctionId(Long auctionId);

    List<OrderEntity> findByBuyerUsername(String buyerUsername);

    List<OrderEntity> findBySellerUsername(String sellerUsername);
}
