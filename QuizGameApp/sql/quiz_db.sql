CREATE DATABASE IF NOT EXISTS quiz_app;
USE quiz_app;

CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_text VARCHAR(255) NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_option INT NOT NULL, -- 0 for A, 1 for B, 2 for C, 3 for D
    explanation TEXT
);

CREATE TABLE IF NOT EXISTS scores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    score INT NOT NULL,
    total_questions INT NOT NULL,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, explanation) 
SELECT * FROM (SELECT 'What is the size of int in Java?', '16 bit', '32 bit', '64 bit', '8 bit', 1, 'In Java, int is a 32-bit signed two''s complement integer.') AS tmp
WHERE NOT EXISTS (
    SELECT question_text FROM questions WHERE question_text = 'What is the size of int in Java?'
) LIMIT 1;

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, explanation) 
SELECT * FROM (SELECT 'Which keyword is used to define a class in Java?', 'class', 'Class', 'define', 'struct', 0, 'The keyword "class" is used to declare a class in Java.') AS tmp
WHERE NOT EXISTS (
    SELECT question_text FROM questions WHERE question_text = 'Which keyword is used to define a class in Java?'
) LIMIT 1;

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, explanation) 
SELECT * FROM (SELECT 'What is the default value of a boolean variable?', 'true', 'false', 'null', '0', 1, 'The default value of a boolean instance variable is false.') AS tmp
WHERE NOT EXISTS (
    SELECT question_text FROM questions WHERE question_text = 'What is the default value of a boolean variable?'
) LIMIT 1;

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, explanation) 
SELECT * FROM (SELECT 'Which method is the entry point of a Java program?', 'start()', 'run()', 'main()', 'init()', 2, 'The main() method is the entry point of any standalone Java application.') AS tmp
WHERE NOT EXISTS (
    SELECT question_text FROM questions WHERE question_text = 'Which method is the entry point of a Java program?'
) LIMIT 1;
