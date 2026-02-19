package backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseInitializer {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "quiz_app";
    private static final String USER = "root";
    private static final String PASS = "root123";

    public static boolean initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 1. Create Database if not exists
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 Statement stmt = conn.createStatement()) {
                
                String createDb = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
                stmt.executeUpdate(createDb);
                System.out.println("Database '" + DB_NAME + "' checked/created.");
            }

            // 2. Create Tables
            try (Connection conn = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASS);
                 Statement stmt = conn.createStatement()) {
                
                // Create Questions Table
                String createQuestionsTable = "CREATE TABLE IF NOT EXISTS questions (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "question_text VARCHAR(255) NOT NULL, " +
                        "option_a VARCHAR(255) NOT NULL, " +
                        "option_b VARCHAR(255) NOT NULL, " +
                        "option_c VARCHAR(255) NOT NULL, " +
                        "option_d VARCHAR(255) NOT NULL, " +
                        "correct_option INT NOT NULL, " +
                        "explanation TEXT)";
                stmt.executeUpdate(createQuestionsTable);

                // Create Scores Table
                String createScoresTable = "CREATE TABLE IF NOT EXISTS scores (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(100) NOT NULL, " +
                        "score INT NOT NULL, " +
                        "total_questions INT NOT NULL, " +
                        "played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
                stmt.executeUpdate(createScoresTable);

                System.out.println("Tables checked/created.");

                // 3. Seed Initial Questions if empty
                String countQuery = "SELECT COUNT(*) AS count FROM questions";
                ResultSet rs = stmt.executeQuery(countQuery);
                if (rs.next() && rs.getInt("count") == 0) {
                    seedQuestions(stmt);
                }
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Database Initialization Failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean dropDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 Statement stmt = conn.createStatement()) {
                String dropDb = "DROP DATABASE IF EXISTS " + DB_NAME;
                stmt.executeUpdate(dropDb);
                System.out.println("Database '" + DB_NAME + "' dropped.");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void seedQuestions(Statement stmt) throws Exception {
        String[] inserts = {
            "INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, explanation) VALUES ('What is the size of int in Java?', '16 bit', '32 bit', '64 bit', '8 bit', 1, 'In Java, int is a 32-bit signed two''s complement integer.')",
            "INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, explanation) VALUES ('Which keyword is used to define a class in Java?', 'class', 'Class', 'define', 'struct', 0, 'The keyword \"class\" is used to declare a class in Java.')",
            "INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, explanation) VALUES ('What is the default value of a boolean variable?', 'true', 'false', 'null', '0', 1, 'The default value of a boolean instance variable is false.')",
            "INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, explanation) VALUES ('Which method is the entry point of a Java program?', 'start()', 'run()', 'main()', 'init()', 2, 'The main() method is the entry point of any standalone Java application.')"
        };

        for (String sql : inserts) {
            stmt.executeUpdate(sql);
        }
        System.out.println("Seeded initial questions.");
    }
}
