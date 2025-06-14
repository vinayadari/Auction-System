public class Bidder {
    private String id;
    private String name;
    private double maxBidAmount;
    private double currentBidAmount;
    private boolean isActive;

    public Bidder(String id, String name, double maxBidAmount) {
        this.id = id;
        this.name = name;
        this.maxBidAmount = maxBidAmount;
        this.currentBidAmount = 0;
        this.isActive = true;
    }

    public String getBidderId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getMaxBidAmount() {
        return maxBidAmount;
    }

    public double getCurrentBidAmount() {
        return currentBidAmount;
    }

    public void setCurrentBidAmount(double currentBidAmount) {
        this.currentBidAmount = currentBidAmount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean canPlaceBid(double amount) {
        return isActive && amount <= maxBidAmount && amount > currentBidAmount;
    }

    @Override
    public String toString() {
        return "Bidder ID: " + id + "\n" +
               "Name: " + name + "\n" +
               "Maximum Bid Amount: $" + maxBidAmount + "\n" +
               "Current Bid Amount: $" + currentBidAmount + "\n" +
               "Status: " + (isActive ? "Active" : "Inactive");
    }
} 