import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Auction {
    private String auctionId;
    private AuctionItem item;
    private AuctionRules rules;
    private List<Bidder> bidders;
    private List<Bid> bids;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isActive;
    private Bid winningBid;
    private Map<String, Double> proxyBids;

    public Auction(String auctionId, AuctionItem item, AuctionRules rules) {
        this.auctionId = auctionId;
        this.item = item;
        this.rules = rules;
        this.bidders = new ArrayList<>();
        this.bids = new ArrayList<>();
        this.isActive = false;
        this.winningBid = null;
        this.proxyBids = new HashMap<>();
    }

    public boolean registerBidder(Bidder bidder) {
        if (bidders.size() >= rules.getMaximumBidders()) {
            return false;
        }
        for (Bidder existingBidder : bidders) {
            if (existingBidder.getBidderId().equals(bidder.getBidderId())) {
                return false;
            }
        }
        bidders.add(bidder);
        return true;
    }

    public boolean placeBid(String bidderId, double amount) {
        if (!isActive) {
            return false;
        }

        if (LocalDateTime.now().isAfter(endTime)) {
            endAuction();
            return false;
        }

        Bidder bidder = findBidder(bidderId);
        if (bidder == null || !bidder.isActive()) {
            return false;
        }

        if (!rules.isValidBid(item.getCurrentPrice(), amount)) {
            return false;
        }

        if (!bidder.canPlaceBid(amount)) {
            return false;
        }

        String bidId = "BID-" + System.currentTimeMillis();
        Bid newBid = new Bid(bidId, bidderId, item.getItemId(), amount);
        bids.add(newBid);
        item.setCurrentPrice(amount);
        bidder.setCurrentBidAmount(amount);

        if (winningBid == null || amount > winningBid.getAmount()) {
            if (winningBid != null) {
                winningBid.setWinning(false);
            }
            winningBid = newBid;
            winningBid.setWinning(true);
        }

        if (rules.isAllowProxyBidding()) {
            proxyBids.put(bidderId, amount);
            processProxyBids();
        }

        return true;
    }

    private void processProxyBids() {
        if (!rules.isAllowProxyBidding()) {
            return;
        }

        double currentHighestBid = item.getCurrentPrice();

        for (Map.Entry<String, Double> entry : proxyBids.entrySet()) {
            String bidderId = entry.getKey();
            double proxyAmount = entry.getValue();

            if (proxyAmount > currentHighestBid) {
                Bidder bidder = findBidder(bidderId);
                if (bidder != null && bidder.isActive()) {
                    double nextBidAmount = currentHighestBid + rules.getMinimumBidIncrement();
                    if (nextBidAmount <= proxyAmount) {
                        placeBid(bidderId, nextBidAmount);
                    }
                }
            }
        }
    }

    public void startAuction() {
        if (!isActive) {
            startTime = LocalDateTime.now();
            endTime = startTime.plusMinutes(rules.getAuctionDurationMinutes());
            isActive = true;
            System.out.println("Auction started at: " + startTime);
            System.out.println("Auction will end at: " + endTime);
        }
    }

    public void endAuction() {
        if (isActive) {
            isActive = false;
            if (winningBid != null) {
                item.setSold(true);
                System.out.println("\nAuction ended! Winner: " + findBidder(winningBid.getBidderId()).getName() + 
                                 " with bid: $" + winningBid.getAmount());
                AuctionHistory.recordWinner(item.getName(), findBidder(winningBid.getBidderId()).getName(), winningBid.getAmount());
            } else {
                System.out.println("\nAuction ended with no bids.");
            }
        }
    }

    private Bidder findBidder(String bidderId) {
        for (Bidder bidder : bidders) {
            if (bidder.getBidderId().equals(bidderId)) {
                return bidder;
            }
        }
        return null;
    }

    public String getAuctionStatus() {
        StringBuilder status = new StringBuilder();
        status.append("Auction ID: ").append(auctionId).append("\n");
        status.append("Item: ").append(item.getName()).append("\n");
        status.append("Current Price: $").append(item.getCurrentPrice()).append("\n");
        status.append("Status: ").append(isActive ? "Active" : "Ended").append("\n");
        status.append("Time Remaining: ");
        
        if (isActive) {
            long minutesRemaining = java.time.Duration.between(LocalDateTime.now(), endTime).toMinutes();
            status.append(minutesRemaining).append(" minutes\n");
        } else {
            status.append("Auction ended\n");
        }
        
        status.append("Number of Bidders: ").append(bidders.size()).append("\n");
        status.append("Number of Bids: ").append(bids.size()).append("\n");
        
        if (winningBid != null) {
            Bidder winningBidder = findBidder(winningBid.getBidderId());
            if (winningBidder != null) {
                status.append("Winning Bidder: ").append(winningBidder.getName()).append("\n");
                status.append("Winning Amount: $").append(winningBid.getAmount()).append("\n");
            }
        }
        
        if (rules.hasReservePrice()) {
            status.append("Reserve Price: $").append(rules.getReservePrice()).append("\n");
            if (winningBid != null) {
                status.append("Reserve Price Met: ").append(winningBid.getAmount() >= rules.getReservePrice()).append("\n");
            }
        }
        
        return status.toString();
    }

    public String getAuctionId() {
        return auctionId;
    }

    public AuctionItem getItem() {
        return item;
    }

    public AuctionRules getRules() {
        return rules;
    }

    public List<Bidder> getBidders() {
        return bidders;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public Bid getWinningBid() {
        return winningBid;
    }

    public boolean isReservePriceMet() {
        if (!rules.hasReservePrice()) {
            return true;
        }
        return winningBid != null && winningBid.getAmount() >= rules.getReservePrice();
    }
} 