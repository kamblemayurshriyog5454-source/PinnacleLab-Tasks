import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.io.File;

public class SetupDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String password = "root123";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            
            System.out.println("Connected to MySQL server.");

            File sqlFile = new File("sql/quiz_db.sql");
            BufferedReader reader = new BufferedReader(new FileReader(sqlFile));
            StringBuilder sb = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            String[] commands = sb.toString().split(";");

            for (String command : commands) {
                if (!command.trim().isEmpty()) {
                    try {
                        stmt.execute(command);
                        System.out.println("Executed: " + command.trim().substring(0, Math.min(command.trim().length(), 50)) + "...");
                    } catch (Exception e) {
                        System.out.println("Error executing command: " + command);
                        e.printStackTrace();
                    }
                }
            }
            
            System.out.println("Database setup completed.");
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to setup database. Please check your MySQL credentials.");
        }
    }
}
