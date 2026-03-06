package id.ac.ui.cs.advprog.bidmart.order.event;

public record OrderCreatedEvent(Long orderId, Long auctionId, String winnerUsername) {
}
