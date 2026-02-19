package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class QuizService {
    private List<Question> questions;
    private int currentQuestionIndex;
    private int score;

    public QuizService() {
        this.questions = new ArrayList<>();
        this.currentQuestionIndex = 0;
        this.score = 0;
        loadQuestionsFromDB();
    }

    private void loadQuestionsFromDB() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String query = "SELECT * FROM questions";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String qText = rs.getString("question_text");
                    String[] options = {
                        rs.getString("option_a"),
                        rs.getString("option_b"),
                        rs.getString("option_c"),
                        rs.getString("option_d")
                    };
                    int correct = rs.getInt("correct_option");
                    String explanation = rs.getString("explanation");

                    questions.add(new Question(qText, options, correct, explanation));
                }
            } else {
                System.out.println("Failed to connect to database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Question getCurrentQuestion() {
        if (currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex);
        }
        return null;
    }

    public boolean checkAnswer(int selectedOption) {
        Question q = getCurrentQuestion();
        if (q != null && q.getCorrectAnswer() == selectedOption) {
            score++;
            return true;
        }
        return false;
    }

    public void nextQuestion() {
        currentQuestionIndex++;
    }

    public boolean hasMoreQuestions() {
        return currentQuestionIndex < questions.size();
    }

    public int getScore() {
        return score;
    }

    public int totalQuestions() {
        return questions.size();
    }

    public List<Question> getAllQuestions() {
        return questions;
    }

    public void saveScore(String username, int score, int totalQuestions) {
        System.out.println("Attempting to save score for: " + username);
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String query = "INSERT INTO scores (username, score, total_questions) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, username);
                stmt.setInt(2, score);
                stmt.setInt(3, totalQuestions);
                int rows = stmt.executeUpdate();
                System.out.println("Score saved successfully. Rows affected: " + rows);
            } else {
                System.err.println("Database connection failed during saveScore.");
            }
        } catch (Exception e) {
            System.err.println("Error saving score: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> getTopScores() {
        List<String> highScores = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String query = "SELECT username, score, total_questions FROM scores ORDER BY score DESC, played_at DESC LIMIT 5";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                int rank = 1;
                while (rs.next()) {
                    String line = String.format("%d. %s: %d/%d", 
                        rank++, 
                        rs.getString("username"), 
                        rs.getInt("score"),
                        rs.getInt("total_questions"));
                    highScores.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return highScores;
    }
}
