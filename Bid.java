import java.time.LocalDateTime;

public class Bid {
    private String bidId;
    private String bidderId;
    private String itemId;
    private double amount;
    private LocalDateTime timestamp;
    private boolean isWinning;

    public Bid(String bidId, String bidderId, String itemId, double amount) {
        this.bidId = bidId;
        this.bidderId = bidderId;
        this.itemId = itemId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.isWinning = false;
    }

    public String getBidId() {
        return bidId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public String getItemId() {
        return itemId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isWinning() {
        return isWinning;
    }

    public void setWinning(boolean winning) {
        isWinning = winning;
    }

    @Override
    public String toString() {
        return "Bid ID: " + bidId + "\n" +
               "Bidder ID: " + bidderId + "\n" +
               "Item ID: " + itemId + "\n" +
               "Amount: $" + amount + "\n" +
               "Timestamp: " + timestamp + "\n" +
               "Status: " + (isWinning ? "Winning" : "Not Winning");
    }
} 