import backend.DatabaseInitializer;
import backend.SimpleWebServer;
import frontend.QuizUI;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.net.URI;

public class Main {
    public static void main(String[] args) {
        // Initialize Database
        boolean dbSuccess = DatabaseInitializer.initialize();

        if (!dbSuccess) {
            JOptionPane.showMessageDialog(null, 
                "Failed to connect to MySQL Database!\n" +
                "Please make sure MySQL is running and credentials are correct.\n" +
                "(Check terminal for details)",
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Check if user wants Web Mode or Desktop Mode
        // For this task "make this like a localhost", we default to Web Mode.
        // Or we can ask. Let's just launch Web Mode as requested.
        
        try {
            int port = 8080;
            SimpleWebServer.start(port);
            
            String url = "http://localhost:" + port;
            System.out.println("Web App running at: " + url);
            
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
            
            // Keep the main thread alive? HttpServer does that.
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to start Web Server: " + e.getMessage());
        }
    }
}
