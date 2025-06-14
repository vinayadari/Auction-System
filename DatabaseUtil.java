import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {
    
    public static void createTables() {
        String createAuctionTable = """
            CREATE TABLE IF NOT EXISTS auctions (
                auction_id VARCHAR(50) PRIMARY KEY,
                item_id VARCHAR(50) NOT NULL,
                item_name VARCHAR(100) NOT NULL,
                item_description TEXT,
                starting_price DECIMAL(10,2) NOT NULL,
                current_price DECIMAL(10,2) NOT NULL,
                min_bid_increment DECIMAL(10,2) NOT NULL,
                max_bidders INT NOT NULL,
                duration INT NOT NULL,
                status VARCHAR(20) DEFAULT 'PENDING',
                start_time DATETIME,
                end_time DATETIME,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        String createBiddersTable = """
            CREATE TABLE IF NOT EXISTS bidders (
                bidder_id VARCHAR(50) PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                max_bid_amount DECIMAL(10,2) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        String createBidsTable = """
            CREATE TABLE IF NOT EXISTS bids (
                bid_id INT AUTO_INCREMENT PRIMARY KEY,
                auction_id VARCHAR(50) NOT NULL,
                bidder_id VARCHAR(50) NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (auction_id) REFERENCES auctions(auction_id) ON DELETE CASCADE,
                FOREIGN KEY (bidder_id) REFERENCES bidders(bidder_id) ON DELETE CASCADE
            )
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Creating tables...");
            stmt.execute(createAuctionTable);
            stmt.execute(createBiddersTable);
            stmt.execute(createBidsTable);
            System.out.println("Tables created successfully!");
            
        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void saveAuction(Auction auction) {
        String sql = """
            INSERT INTO auctions (
                auction_id, item_id, item_name, item_description, 
                starting_price, current_price, min_bid_increment,
                max_bidders, duration, status, start_time, end_time
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
                    
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            System.out.println("Saving auction to database: " + auction.getAuctionId());
            pstmt.setString(1, auction.getAuctionId());
            pstmt.setString(2, auction.getItem().getItemId());
            pstmt.setString(3, auction.getItem().getName());
            pstmt.setString(4, auction.getItem().getDescription());
            pstmt.setDouble(5, auction.getItem().getStartingPrice());
            pstmt.setDouble(6, auction.getItem().getCurrentPrice());
            pstmt.setDouble(7, auction.getRules().getMinimumBidIncrement());
            pstmt.setInt(8, auction.getRules().getMaximumBidders());
            pstmt.setInt(9, auction.getRules().getDuration());
            pstmt.setString(10, auction.isActive() ? "ACTIVE" : "INACTIVE");
            pstmt.setTimestamp(11, Timestamp.valueOf(auction.getStartTime()));
            pstmt.setTimestamp(12, Timestamp.valueOf(auction.getEndTime()));
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Auction saved successfully! Rows affected: " + rowsAffected);
            
        } catch (SQLException e) {
            System.out.println("Error saving auction to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void saveBidder(Bidder bidder) {
        String sql = """
            INSERT INTO bidders (bidder_id, name, max_bid_amount) 
            VALUES (?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            System.out.println("Saving bidder to database: " + bidder.getBidderId());
            pstmt.setString(1, bidder.getBidderId());
            pstmt.setString(2, bidder.getName());
            pstmt.setDouble(3, bidder.getMaxBidAmount());
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Bidder saved successfully! Rows affected: " + rowsAffected);
            
        } catch (SQLException e) {
            System.out.println("Error saving bidder to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void saveBid(String auctionId, String bidderId, double amount) {
        String sql = """
            INSERT INTO bids (auction_id, bidder_id, amount) 
            VALUES (?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            System.out.println("Saving bid to database: Auction=" + auctionId + ", Bidder=" + bidderId + ", Amount=" + amount);
            pstmt.setString(1, auctionId);
            pstmt.setString(2, bidderId);
            pstmt.setDouble(3, amount);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Bid saved successfully! Rows affected: " + rowsAffected);
            
            // Update current price in auctions table
            String updateSql = """
                UPDATE auctions 
                SET current_price = ? 
                WHERE auction_id = ?
            """;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setDouble(1, amount);
                updateStmt.setString(2, auctionId);
                int updateRowsAffected = updateStmt.executeUpdate();
                System.out.println("Auction price updated successfully! Rows affected: " + updateRowsAffected);
            }
            
        } catch (SQLException e) {
            System.out.println("Error saving bid to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static List<Auction> getAllAuctions() {
        List<Auction> auctions = new ArrayList<>();
        String sql = """
            SELECT * FROM auctions 
            ORDER BY created_at DESC
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("Fetching all auctions...");
            while (rs.next()) {
                AuctionItem item = new AuctionItem(
                    rs.getString("item_id"),
                    rs.getString("item_name"),
                    rs.getString("item_description"),
                    rs.getDouble("starting_price")
                );
                
                AuctionRules rules = new AuctionRules(
                    rs.getDouble("min_bid_increment"),
                    rs.getInt("max_bidders"),
                    rs.getInt("duration"),
                    false,
                    0.0,
                    false
                );
                
                Auction auction = new Auction(
                    rs.getString("auction_id"),
                    item,
                    rules
                );
                
                auctions.add(auction);
            }
            System.out.println("Found " + auctions.size() + " auctions");
            
        } catch (SQLException e) {
            System.out.println("Error fetching auctions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return auctions;
    }

    public static void getAuctionSummary(String auctionId) {
        String sql = """
            SELECT 
                a.auction_id,
                a.item_name,
                a.starting_price,
                a.current_price,
                a.status,
                a.start_time,
                a.end_time,
                b.bidder_id,
                b.name as bidder_name,
                b.max_bid_amount,
                bi.amount as bid_amount,
                bi.bid_time
            FROM auctions a
            LEFT JOIN bids bi ON a.auction_id = bi.auction_id
            LEFT JOIN bidders b ON bi.bidder_id = b.bidder_id
            WHERE a.auction_id = ?
            ORDER BY bi.bid_time DESC
        """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, auctionId);
            ResultSet rs = pstmt.executeQuery();
            
            boolean firstRow = true;
            while (rs.next()) {
                if (firstRow) {
                    System.out.println("\n=== Auction Summary ===");
                    System.out.println("Item: " + rs.getString("item_name"));
                    System.out.println("Starting Price: $" + rs.getDouble("starting_price"));
                    System.out.println("Current Price: $" + rs.getDouble("current_price"));
                    System.out.println("Start Time: " + rs.getTimestamp("start_time"));
                    System.out.println("End Time: " + rs.getTimestamp("end_time"));
                    System.out.println("Status: " + rs.getString("status"));
                    System.out.println("\nBid History:");
                    firstRow = false;
                }
                
                if (rs.getString("bidder_id") != null) {
                    System.out.printf("Bidder: %s, Amount: $%.2f, Time: %s%n",
                        rs.getString("bidder_name"),
                        rs.getDouble("bid_amount"),
                        rs.getTimestamp("bid_time")
                    );
                }
            }
            
            if (firstRow) {
                System.out.println("No auction found with ID: " + auctionId);
            }
            
        } catch (SQLException e) {
            System.out.println("Error getting auction summary: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 