/**
 * Wake-up reminder application.
 * A sample recurring job that sends a wake-up reminder.
 */
public class WakeUpReminder {
    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("⏰ WAKE UP REMINDER ⏰");
        System.out.println("=====================================");
        System.out.println("Good morning! It's time to wake up!");
        System.out.println("Current time: " + java.time.LocalDateTime.now());
        System.out.println("Today's tasks:");
        System.out.println("1. Morning exercise");
        System.out.println("2. Breakfast");
        System.out.println("3. Check your calendar for meetings");
        System.out.println("-------------------------------------");
        System.out.println("Remember to drink water and have a great day!");
        System.out.println("=====================================");
        
        // If args are provided for weather information
        if (args.length > 0) {
            System.out.println("Weather forecast: " + args[0]);
        }
        
        System.out.println("Wake-up reminder sent successfully.");
    }
} 