package backend;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL Driver Loaded");

            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/quiz_app",
                "root",
                "root123"
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
