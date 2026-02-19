package frontend;

import backend.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class QuizUI extends JFrame implements ActionListener {

    QuizService quizService = new QuizService();
    String username;

    JLabel questionLabel;
    JRadioButton[] options = new JRadioButton[4];
    ButtonGroup group;
    JButton nextBtn;

    public QuizUI() {
        setTitle("Interactive Java Quiz App");
        setSize(600, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Prompt for username
        username = JOptionPane.showInputDialog(this, "Enter your name to start the quiz:");
        if (username == null || username.trim().isEmpty()) {
            username = "Guest";
        }

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(questionLabel, BorderLayout.NORTH);

        JPanel optionPanel = new JPanel(new GridLayout(4, 1));
        group = new ButtonGroup();

        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            group.add(options[i]);
            optionPanel.add(options[i]);
        }

        add(optionPanel, BorderLayout.CENTER);

        nextBtn = new JButton("Next");
        nextBtn.addActionListener(this);
        add(nextBtn, BorderLayout.SOUTH);

        loadQuestion();
        setVisible(true);
    }

    private void loadQuestion() {
        Question q = quizService.getCurrentQuestion();
        questionLabel.setText(q.getQuestion());

        String[] opts = q.getOptions();
        for (int i = 0; i < opts.length; i++) {
            options[i].setText(opts[i]);
        }
        group.clearSelection();
    }

    public void actionPerformed(ActionEvent e) {
        int selected = -1;
        for (int i = 0; i < options.length; i++) {
            if (options[i].isSelected()) selected = i;
        }

        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select an option!");
            return;
        }

        boolean correct = quizService.checkAnswer(selected);
        Question q = quizService.getCurrentQuestion();

        JOptionPane.showMessageDialog(this,
                correct ? "Correct!\n" + q.getExplanation()
                        : "Wrong!\n" + q.getExplanation());

        quizService.nextQuestion();

        if (quizService.hasMoreQuestions()) {
            loadQuestion();
        } else {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        // Save Score
        quizService.saveScore(username, quizService.getScore(), quizService.totalQuestions());

        // Get High Scores
        List<String> topScores = quizService.getTopScores();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Quiz Completed!\n");
        sb.append("Player: ").append(username).append("\n");
        sb.append("Your Score: ").append(quizService.getScore()).append("/").append(quizService.totalQuestions()).append("\n\n");
        sb.append("--- High Scores ---\n");
        if (topScores.isEmpty()) {
            sb.append("No scores yet.");
        } else {
            for (String s : topScores) {
                sb.append(s).append("\n");
            }
        }

        JOptionPane.showMessageDialog(this, sb.toString());
        System.exit(0);
    }
}
