package id.ac.ui.cs.advprog.bidmart.order.repository;

import id.ac.ui.cs.advprog.bidmart.order.model.OrderEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void existsByAuctionId_returnsTrueWhenSaved() {
        orderRepository.save(new OrderEntity("auction-a", "buyer-a", "seller-a", "addr-a"));

        assertThat(orderRepository.existsByAuctionId("auction-a")).isTrue();
        assertThat(orderRepository.existsByAuctionId("missing")).isFalse();
    }

    @Test
    void findByAuctionId_returnsPersistedEntity() {
        orderRepository.save(new OrderEntity("auction-b", "buyer-b", "seller-b", "addr-b"));

        Optional<OrderEntity> found = orderRepository.findByAuctionId("auction-b");

        assertThat(found).isPresent();
        assertThat(found.get().getBuyerUsername()).isEqualTo("buyer-b");
    }

    @Test
    void findByBuyerUsername_returnsAllOrdersForBuyer() {
        orderRepository.save(new OrderEntity("auction-1", "buyer-x", "seller-1", "addr"));
        orderRepository.save(new OrderEntity("auction-2", "buyer-x", "seller-2", "addr"));
        orderRepository.save(new OrderEntity("auction-3", "buyer-y", "seller-3", "addr"));

        List<OrderEntity> buyerXOrders = orderRepository.findByBuyerUsername("buyer-x");

        assertThat(buyerXOrders).hasSize(2);
        assertThat(buyerXOrders).extracting(OrderEntity::getAuctionId)
                .containsExactlyInAnyOrder("auction-1", "auction-2");
    }

    @Test
    void findBySellerUsername_returnsAllOrdersForSeller() {
        orderRepository.save(new OrderEntity("auction-4", "buyer-1", "seller-z", "addr"));
        orderRepository.save(new OrderEntity("auction-5", "buyer-2", "seller-z", "addr"));

        List<OrderEntity> sellerOrders = orderRepository.findBySellerUsername("seller-z");

        assertThat(sellerOrders).hasSize(2);
    }
}