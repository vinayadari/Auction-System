public class AuctionItem {
    private String id;
    private String name;
    private String description;
    private double startingPrice;
    private double currentPrice;
    private boolean isSold;

    public AuctionItem(String id, String name, String description, double startingPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.isSold = false;
    }

    public String getItemId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public boolean isSold() {
        return isSold;
    }

    public void setSold(boolean sold) {
        isSold = sold;
    }

    @Override
    public String toString() {
        return "Item ID: " + id + "\n" +
               "Name: " + name + "\n" +
               "Description: " + description + "\n" +
               "Starting Price: $" + startingPrice + "\n" +
               "Current Price: $" + currentPrice + "\n" +
               "Status: " + (isSold ? "Sold" : "Available");
    }
} 