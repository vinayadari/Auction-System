import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class AuctionHistory {
    private static final String HISTORY_FILE = "auction_history.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static void recordWinner(String itemName, String winnerName, double winningBid) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HISTORY_FILE, true))) {
            String timestamp = LocalDateTime.now().format(formatter);
            String record = String.format("[%s] Item: %s | Winner: %s | Winning Bid: $%.2f",
                    timestamp, itemName, winnerName, winningBid);
            writer.println(record);
        } catch (IOException e) {
            System.err.println("Error recording auction history: " + e.getMessage());
        }
    }
} 