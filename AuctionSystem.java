import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;

public class AuctionSystem {
    private static Scanner scanner = new Scanner(System.in);
    private static Auction currentAuction;
    private static Timer timer;
    private static int remainingSeconds;
    private static final String LOG_FILE = "auction_log.txt";
    private static final String URL = "jdbc:mysql://localhost:3306/auction_db";
    private static final String USER = "root";
    private static final String PASSWORD = "root123"; 

    public static void main(String[] args) {
        DatabaseUtil.createTables();
        
        boolean running = true;
        while (running) {
            System.out.println("\n=== Auction Management System ===");
            System.out.println("1. Create New Auction");
            System.out.println("2. Register Bidder");
            System.out.println("3. Place Bid");
            System.out.println("4. View Auction Status");
            System.out.println("5. Start Auction");
            System.out.println("6. End Auction");
            System.out.println("7. View All Auctions");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            System.out.println("You entered choice: " + choice);

            switch (choice) {
                case 1:
                    createAuction();
                    break;
                case 2:
                    registerBidder();
                    break;
                case 3:
                    placeBid();
                    break;
                case 4:
                    viewAuctionStatus();
                    break;
                case 5:
                    startAuction();
                    break;
                case 6:
                    endAuction();
                    break;
                case 7:
                    viewAllAuctions();
                    break;
                case 8:
                    running = false;
                    if (timer != null) {
                        timer.cancel();
                    }
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private static void logToFile(String message) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write(timestamp + " - " + message + "\n");
        } catch (IOException e) {
            System.out.println("Error writing to log file: " + e.getMessage());
        }
    }

    private static void startTimer(int durationMinutes) {
        if (timer != null) {
            timer.cancel();
        }
        remainingSeconds = durationMinutes * 60;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (remainingSeconds > 0) {
                    int minutes = remainingSeconds / 60;
                    int seconds = remainingSeconds % 60;
                    System.out.printf("\rTime remaining: %02d:%02d", minutes, seconds);
                    remainingSeconds--;
                } else {
                    System.out.println("\nAuction time has ended!");
                    timer.cancel();
                    if (currentAuction != null) {
                        endAuction();
                    }
                }
            }
        }, 0, 1000);
    }

    private static void createAuction() {
        System.out.println("\n=== Create New Auction ===");
        
        System.out.print("Enter Item ID: ");
        String itemId = scanner.nextLine();
        System.out.println("Item ID entered: " + itemId);
        
        System.out.print("Enter Item Name: ");
        String itemName = scanner.nextLine();
        System.out.println("Item Name entered: " + itemName);
        
        System.out.print("Enter Item Description: ");
        String itemDescription = scanner.nextLine();
        System.out.println("Item Description entered: " + itemDescription);
        
        System.out.print("Enter Starting Price: ");
        double startingPrice = scanner.nextDouble();
        scanner.nextLine(); 
        System.out.println("Starting Price entered: " + startingPrice);

        System.out.print("Enter Minimum Bid Increment: ");
        double minBidIncrement = scanner.nextDouble();
        System.out.println("Minimum Bid Increment entered: " + minBidIncrement);
        
        System.out.print("Enter Maximum Number of Bidders: ");
        int maxBidders = scanner.nextInt();
        System.out.println("Maximum Number of Bidders entered: " + maxBidders);
        
        System.out.print("Enter Auction Duration (minutes): ");
        int duration = scanner.nextInt();
        System.out.println("Auction Duration entered: " + duration + " minutes");
        
        System.out.print("Allow Proxy Bidding? (true/false): ");
        boolean allowProxy = scanner.nextBoolean();
        System.out.println("Proxy Bidding allowed: " + allowProxy);
        
        System.out.print("Set Reserve Price? (true/false): ");
        boolean hasReserve = scanner.nextBoolean();
        System.out.println("Reserve Price set: " + hasReserve);
        
        double reservePrice = 0;
        if (hasReserve) {
            System.out.print("Enter Reserve Price: ");
            reservePrice = scanner.nextDouble();
            System.out.println("Reserve Price entered: " + reservePrice);
        }
        scanner.nextLine(); 

        AuctionItem item = new AuctionItem(itemId, itemName, itemDescription, startingPrice);
        AuctionRules rules = new AuctionRules(minBidIncrement, maxBidders, duration, allowProxy, reservePrice, hasReserve);
        
        currentAuction = new Auction("AUCTION-" + System.currentTimeMillis(), item, rules);
        DatabaseUtil.saveAuction(currentAuction);
        
        System.out.println("\nAuction created successfully!");
        System.out.println("Auction ID: " + currentAuction.getAuctionId());
        System.out.println(item);
        System.out.println(rules);
        
        logToFile("New Auction Created - ID: " + currentAuction.getAuctionId() + 
                 ", Item: " + itemName + 
                 ", Starting Price: " + startingPrice);
    }

    private static void registerBidder() {
        if (currentAuction == null) {
            System.out.println("No auction exists. Please create an auction first.");
            return;
        }

        System.out.println("\n=== Register Bidder ===");
        System.out.print("Enter Bidder ID: ");
        String bidderId = scanner.nextLine();
        System.out.println("Bidder ID entered: " + bidderId);
        
        System.out.print("Enter Bidder Name: ");
        String bidderName = scanner.nextLine();
        System.out.println("Bidder Name entered: " + bidderName);
        
        System.out.print("Enter Maximum Bid Amount: ");
        double maxBidAmount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        System.out.println("Maximum Bid Amount entered: " + maxBidAmount);

        Bidder bidder = new Bidder(bidderId, bidderName, maxBidAmount);
        if (currentAuction.registerBidder(bidder)) {
            DatabaseUtil.saveBidder(bidder);
            System.out.println("Bidder with ID '" + bidderId + "' has been created.");
            System.out.println("Bidder registered successfully!");
            System.out.println(bidder);
            logToFile("Bidder Registered - ID: " + bidderId + 
                     ", Name: " + bidderName + 
                     ", Max Bid: " + maxBidAmount);
        } else {
            System.out.println("Failed to register bidder. Maximum number of bidders reached.");
            logToFile("Failed Bidder Registration - ID: " + bidderId + 
                     ", Name: " + bidderName + 
                     " - Maximum bidders reached");
        }
    }

    private static void placeBid() {
        if (currentAuction == null) {
            System.out.println("No auction exists. Please create an auction first.");
            return;
        }

        if (!currentAuction.isActive()) {
            System.out.println("Auction is not active. Please start the auction first.");
            return;
        }

        System.out.println("\n=== Place Bid ===");
        System.out.print("Enter Bidder ID: ");
        String bidderId = scanner.nextLine();
        System.out.println("Bidder ID entered: " + bidderId);
        
        System.out.print("Enter Bid Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine(); 
        System.out.println("Bid Amount entered: " + amount);

        if (currentAuction.placeBid(bidderId, amount)) {
            DatabaseUtil.saveBid(currentAuction.getAuctionId(), bidderId, amount);
            System.out.println("Bid placed successfully!");
            logToFile("Bid Placed - Bidder ID: " + bidderId + 
                     ", Amount: " + amount + 
                     ", Current Highest Bid: " + currentAuction.getItem().getCurrentPrice());
        } else {
            System.out.println("Failed to place bid. Please check the bid amount and bidder status.");
            logToFile("Failed Bid - Bidder ID: " + bidderId + 
                     ", Amount: " + amount + 
                     " - Bid rejected");
        }
    }

    private static void viewAuctionStatus() {
        if (currentAuction == null) {
            System.out.println("No auction exists. Please create an auction first.");
            return;
        }

        System.out.println("\n=== Auction Status ===");
        String status = currentAuction.getAuctionStatus();
        System.out.println(status);
        logToFile("Auction Status Check - " + status);
    }

    private static void startAuction() {
        if (currentAuction == null) {
            System.out.println("No auction exists. Please create an auction first.");
            return;
        }

        currentAuction.startAuction();
        System.out.println("Auction started successfully!");
        logToFile("Auction Started - ID: " + currentAuction.getAuctionId());
        startTimer(currentAuction.getRules().getDuration());
    }

    private static void endAuction() {
        if (currentAuction == null) {
            System.out.println("No auction exists. Please create an auction first.");
            return;
        }

        if (timer != null) {
            timer.cancel();
        }
        currentAuction.endAuction();
        System.out.println("Auction ended successfully!");
        String status = currentAuction.getAuctionStatus();
        System.out.println(status);
        
        logToFile("=== Auction Ended ===");
        logToFile("Auction ID: " + currentAuction.getAuctionId());
        logToFile("Item: " + currentAuction.getItem().getName());
        logToFile("Final Price: " + currentAuction.getItem().getCurrentPrice());
        logToFile("Winner: " + (currentAuction.getWinningBid() != null ? currentAuction.getWinningBid().getBidderId() : "No winner"));
        logToFile("Total Bids: " + currentAuction.getBids().size());
        logToFile("Total Bidders: " + currentAuction.getBidders().size());
        logToFile("===================");
    }

    private static void viewAllAuctions() {
        System.out.println("\n=== All Auctions ===");
        List<Auction> auctions = DatabaseUtil.getAllAuctions();
        if (auctions.isEmpty()) {
            System.out.println("No auctions found.");
        } else {
            for (Auction auction : auctions) {
                System.out.println("\nAuction ID: " + auction.getAuctionId());
                System.out.println("Item: " + auction.getItem().getName());
                System.out.println("Current Price: " + auction.getItem().getCurrentPrice());
                System.out.println("Status: " + (auction.isActive() ? "Active" : "Inactive"));
                System.out.println("------------------------");
            }
        }
    }
} 