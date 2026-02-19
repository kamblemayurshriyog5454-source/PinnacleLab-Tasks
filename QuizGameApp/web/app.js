let questions = [];
let currentQuestionIndex = 0;
let score = 0;
let username = "Guest";
let userAnswers = [];

async function startQuiz() {
    const nameInput = document.getElementById('username');
    if (nameInput.value.trim() !== "") {
        username = nameInput.value.trim();
    }

    try {
        const response = await fetch('/api/questions');
        questions = await response.json();
        
        if (questions.length > 0) {
            document.getElementById('start-screen').classList.remove('active');
            document.getElementById('quiz-screen').classList.add('active');
            loadQuestion();
        } else {
            alert("No questions found in database!");
        }
    } catch (error) {
        console.error("Error fetching questions:", error);
        alert("Failed to connect to server.");
    }
}

function loadQuestion() {
    const q = questions[currentQuestionIndex];
    document.getElementById('question-text').innerText = q.question;
    
    const container = document.getElementById('options-container');
    container.innerHTML = '';

    q.options.forEach((opt, index) => {
        const btn = document.createElement('button');
        btn.innerText = opt;
        btn.className = 'option-btn';
        btn.onclick = () => selectOption(index, btn);
        container.appendChild(btn);
    });
}

let selectedOptionIndex = -1;

function selectOption(index, btn) {
    selectedOptionIndex = index;
    // Visually select
    document.querySelectorAll('.option-btn').forEach(b => b.classList.remove('selected'));
    btn.classList.add('selected');
}

function nextQuestion() {
    if (selectedOptionIndex === -1) {
        alert("Please select an option!");
        return;
    }

    // Record Answer
    userAnswers.push({
        questionId: currentQuestionIndex, // Simple index tracking
        selected: selectedOptionIndex
    });

    // Check Answer Client-Side for immediate feedback (Optional, but better UX for simple apps)
    // However, to be secure, we should send to server. For this task, we will verify at the end OR verify now.
    // Let's verify now to match previous Swing UX.
    
    // Note: The API I designed sends correct answer index for simplicity in this "localhost" port task.
    // In a real secure app, we wouldn't send correct_option to client.
    if (questions[currentQuestionIndex].correctAnswer === selectedOptionIndex) {
        score++;
        alert("Correct!\n" + questions[currentQuestionIndex].explanation);
    } else {
        alert("Wrong!\n" + questions[currentQuestionIndex].explanation);
    }

    currentQuestionIndex++;
    selectedOptionIndex = -1;

    if (currentQuestionIndex < questions.length) {
        loadQuestion();
    } else {
        finishQuiz();
    }
}

async function finishQuiz() {
    document.getElementById('quiz-screen').classList.remove('active');
    document.getElementById('result-screen').classList.add('active');
    
    document.getElementById('final-score').innerText = score;
    document.getElementById('total-questions').innerText = questions.length;

    // Submit Score
    await fetch('/api/submit', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            username: username,
            score: score,
            total: questions.length
        })
    });

    loadHighScores();
}

async function loadHighScores() {
    try {
        const res = await fetch('/api/scores');
        const scores = await res.json();
        
        const list = document.getElementById('high-scores-list');
        list.innerHTML = '';
        
        scores.forEach(s => {
            const li = document.createElement('li');
            li.innerText = `${s}`;
            list.appendChild(li);
        });
    } catch (e) {
        console.error(e);
    }
}
