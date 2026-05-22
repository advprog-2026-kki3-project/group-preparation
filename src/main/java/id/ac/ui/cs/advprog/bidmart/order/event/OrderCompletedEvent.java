package id.ac.ui.cs.advprog.bidmart.order.event;

public record OrderCompletedEvent(Long orderId, String buyerUsername) {
}
