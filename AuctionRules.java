public class AuctionRules {
    private final double minimumBidIncrement;
    private final int maximumBidders;
    private final int auctionDurationMinutes;
    private final boolean allowProxyBidding;
    private final double reservePrice;
    private final boolean hasReservePrice;

    public AuctionRules(double minimumBidIncrement, int maximumBidders, int auctionDurationMinutes,
                       boolean allowProxyBidding, double reservePrice, boolean hasReservePrice) {
        this.minimumBidIncrement = minimumBidIncrement;
        this.maximumBidders = maximumBidders;
        this.auctionDurationMinutes = auctionDurationMinutes;
        this.allowProxyBidding = allowProxyBidding;
        this.reservePrice = reservePrice;
        this.hasReservePrice = hasReservePrice;
    }

    public double getMinimumBidIncrement() {
        return minimumBidIncrement;
    }

    public int getMaximumBidders() {
        return maximumBidders;
    }

    public int getAuctionDurationMinutes() {
        return auctionDurationMinutes;
    }

    public boolean isAllowProxyBidding() {
        return allowProxyBidding;
    }

    public double getReservePrice() {
        return reservePrice;
    }

    public boolean hasReservePrice() {
        return hasReservePrice;
    }

    public int getDuration() {
        return auctionDurationMinutes;
    }

    public boolean isValidBid(double currentPrice, double newBid) {
        if (newBid <= currentPrice) {
            return false;
        }
        if (hasReservePrice && newBid < reservePrice) {
            return false;
        }
        return (newBid - currentPrice) >= minimumBidIncrement;
    }

    @Override
    public String toString() {
        return """
               Auction Rules:
               Minimum Bid Increment: $""" + minimumBidIncrement + "\n" +
               "Maximum Bidders: " + maximumBidders + "\n" +
               "Auction Duration: " + auctionDurationMinutes + " minutes\n" +
               "Proxy Bidding: " + (allowProxyBidding ? "Allowed" : "Not Allowed") + "\n" +
               "Reserve Price: " + (hasReservePrice ? "$" + reservePrice : "No Reserve Price");
    }
} 