package id.ac.ui.cs.advprog.bidmart.order.event;

public record OrderShippedEvent(Long orderId, String buyerUsername, String trackingNumber) {
}