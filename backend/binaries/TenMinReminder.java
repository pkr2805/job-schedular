/**
 * 10-minute reminder application.
 * A sample recurring job that sends a reminder every 10 minutes.
 */
public class TenMinReminder {
    public static void main(String[] args) {
        System.out.println("----------------------------------------");
        System.out.println("⏱️ 10-MINUTE REMINDER ⏱️");
        System.out.println("----------------------------------------");
        System.out.println("Take a break! It's been 10 minutes of work.");
        System.out.println("Current time: " + java.time.LocalDateTime.now());
        System.out.println("Suggested break activities:");
        System.out.println("- Stand up and stretch");
        System.out.println("- Look away from screen (20-20-20 rule)");
        System.out.println("- Take a few deep breaths");
        System.out.println("- Drink some water");
        System.out.println("----------------------------------------");
        
        // If args are provided for specific reminder message
        if (args.length > 0) {
            System.out.println("Custom reminder: " + args[0]);
        }
        
        System.out.println("10-minute reminder sent successfully.");
    }
} 