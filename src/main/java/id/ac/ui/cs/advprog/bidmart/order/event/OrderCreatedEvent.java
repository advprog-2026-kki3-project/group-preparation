package id.ac.ui.cs.advprog.bidmart.order.event;

public record OrderCreatedEvent(Long orderId, String auctionId, String winnerUsername, String sellerUsername) {
    // Backward compatibility for modules still using buyer naming
    public String buyerUsername() {
        return winnerUsername;
    }
}