package id.ac.ui.cs.advprog.bidmart.catalogue.event;

public class ListingPublishedEvent {

    private String listingId;
    private String title;
    private double initialPrice;

    public ListingPublishedEvent() {}

    public ListingPublishedEvent(String listingId,
                                 String title,
                                 double initialPrice) {

        this.listingId = listingId;
        this.title = title;
        this.initialPrice = initialPrice;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(double initialPrice) {
        this.initialPrice = initialPrice;
    }
}