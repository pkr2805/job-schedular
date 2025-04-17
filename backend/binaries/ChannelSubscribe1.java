/**
 * Subscribe to Channel 1 application.
 * A sample job that simulates subscribing to a channel at a scheduled time.
 */
public class ChannelSubscribe1 {
    public static void main(String[] args) {
        System.out.println("Starting Channel 1 subscription process...");
        System.out.println("Connecting to server...");
        
        try {
            // Simulate work
            Thread.sleep(2000);
            System.out.println("Connected to server");
            
            Thread.sleep(1000);
            System.out.println("Authenticating user...");
            
            Thread.sleep(1500);
            System.out.println("User authenticated successfully");
            
            Thread.sleep(1000);
            System.out.println("Subscribing to Channel 1...");
            
            Thread.sleep(2000);
            System.out.println("Successfully subscribed to Channel 1!");
            System.out.println("You will now receive notifications from Channel 1");
            
        } catch (InterruptedException e) {
            System.err.println("Subscription process was interrupted: " + e.getMessage());
            System.exit(1);
        }
        
        System.out.println("Channel 1 subscription process completed successfully");
    }
} 