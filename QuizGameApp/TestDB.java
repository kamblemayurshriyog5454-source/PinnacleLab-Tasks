import backend.DBConnection;
import backend.QuizService;
import backend.Question;
import java.sql.Connection;

public class TestDB {

    public static void main(String[] args) {
        try {
            System.out.println("Starting DB test...");

            // Test Connection
            Connection con = DBConnection.getConnection();
            if (con == null) {
                System.out.println("Connection failed");
                return;
            }
            System.out.println("Connection successful!");
            con.close();

            // Test QuizService
            System.out.println("Testing QuizService...");
            QuizService service = new QuizService();
            if (service.totalQuestions() > 0) {
                System.out.println("Questions loaded successfully! Count: " + service.totalQuestions());
                Question q = service.getCurrentQuestion();
                System.out.println("First Question: " + q.getQuestion());
            } else {
                System.out.println("No questions loaded.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
