package id.ac.ui.cs.advprog.bidmart.auction.model;

public enum AuctionStage {
    DRAFT,      // This is for when the listing is being created, not yet open for bids
    ACTIVE,     // This is when the Bidding is open
    EXTENDED,   // This is for when a late bid triggered the 2-minute extension
    CLOSED,     // This is for when the bidding time is up and evaluating the winner
    WON,        // Condition is reserve price met, winner determined
    UNSOLD      // Condition is reserve price not met
}