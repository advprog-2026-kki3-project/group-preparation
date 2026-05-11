package id.ac.ui.cs.advprog.bidmart.wallet;

public class TopUpRequest {
    private Long amount;

    // Empty constructor for Spring Boot's JSON parser
    public TopUpRequest() {}

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}