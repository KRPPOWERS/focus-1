package com.focusguard.app;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MindGameActivity extends AppCompatActivity {

    private static final String[] MOTIVATIONS = {
        "Excellent! Your mind is sharp and focused. Now channel that energy into your real work!",
        "Great job! You have proven you have the mental strength. Your goals need that strength now!",
        "Impressive! A focused mind creates an amazing life. Return to your work and make it happen!",
        "Well done! The same discipline you used here will take you to success. Keep going!",
        "You did it! Champions use their mental energy wisely. Your work is waiting for a champion!"
    };

    private enum GameType { MATH, MEMORY }

    private GameType currentGame;
    private Random rnd = new Random();
    private int mathQ = 0, mathCorrect = 0;
    private boolean gameComplete = false;
    private long gameStartMs;
    private static final long MIN_PLAY_MS = 60_000L;

    private LinearLayout llMath, llMemory, llResult;
    private TextView tvGameTitle, tvQuestion, tvScore, tvResult, tvTimer;
    private EditText etAnswer;
    private Button btnSubmit, btnDone;
    private TextView tvMemDisplay, tvMemPrompt;
    private EditText etMemAnswer;
    private Button btnMemSubmit;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mind_game);
        gameStartMs = System.currentTimeMillis();

        llMath       = findViewById(R.id.llMathGame);
        llMemory     = findViewById(R.id.llMemoryGame);
        llResult     = findViewById(R.id.llResult);
        tvGameTitle  = findViewById(R.id.tvGameTitle);
        tvQuestion   = findViewById(R.id.tvQuestion);
        tvScore      = findViewById(R.id.tvScore);
        tvResult     = findViewById(R.id.tvResult);
        etAnswer     = findViewById(R.id.etAnswer);
        btnSubmit    = findViewById(R.id.btnSubmit);
        tvTimer      = findViewById(R.id.tvTimer);
        tvMemDisplay = findViewById(R.id.tvMemDisplay);
        tvMemPrompt  = findViewById(R.id.tvMemPrompt);
        etMemAnswer  = findViewById(R.id.etMemAnswer);
        btnMemSubmit = findViewById(R.id.btnMemSubmit);
        btnDone      = findViewById(R.id.btnDone);

        GameType[] games = GameType.values();
        currentGame = games[rnd.nextInt(games.length)];

        startTimer();

        if (currentGame == GameType.MATH) {
            startMathGame();
        } else {
            startMemoryGame();
        }

        btnDone.setOnClickListener(v -> {
            goHome();
            finish();
        });
    }

    private void startTimer() {
        timer = new CountDownTimer(90_000L, 1000) {
            public void onTick(long ms) {
                tvTimer.setText("Time: " + (ms / 1000) + "s");
            }
            public void onFinish() {
                if (!gameComplete) finishGame(true);
            }
        }.start();
    }

    // ── MATH GAME ─────────────────────────────────────────────
    private int[] mathNums = new int[2];
    private char   mathOp;
    private int    mathAns;

    private void startMathGame() {
        llMath.setVisibility(View.VISIBLE);
        llMemory.setVisibility(View.GONE);
        tvGameTitle.setText("Math Challenge");
        mathQ = 0; mathCorrect = 0;
        showMathQuestion();
        btnSubmit.setOnClickListener(v -> checkMath());
    }

    private void showMathQuestion() {
        if (mathQ >= 8) { finishGame(false); return; }
        mathQ++;
        int type = rnd.nextInt(3);
        if (type == 0) {
            mathNums[0] = rnd.nextInt(50) + 1;
            mathNums[1] = rnd.nextInt(50) + 1;
            mathOp = '+'; mathAns = mathNums[0] + mathNums[1];
        } else if (type == 1) {
            mathNums[0] = rnd.nextInt(50) + 10;
            mathNums[1] = rnd.nextInt(mathNums[0] - 1) + 1;
            mathOp = '-'; mathAns = mathNums[0] - mathNums[1];
        } else {
            mathNums[0] = rnd.nextInt(12) + 2;
            mathNums[1] = rnd.nextInt(12) + 2;
            mathOp = 'x'; mathAns = mathNums[0] * mathNums[1];
        }
        tvQuestion.setText("Q" + mathQ + "/8:   " + mathNums[0] + " " + mathOp + " " + mathNums[1] + " = ?");
        tvScore.setText("Correct: " + mathCorrect);
        etAnswer.setText(""); etAnswer.requestFocus();
    }

    private void checkMath() {
        String txt = etAnswer.getText().toString().trim();
        if (txt.isEmpty()) return;
        try {
            int guess = Integer.parseInt(txt);
            if (guess == mathAns) {
                mathCorrect++;
                Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Wrong! Answer was " + mathAns, Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a number", Toast.LENGTH_SHORT).show();
            return;
        }
        showMathQuestion();
    }

    // ── MEMORY GAME ───────────────────────────────────────────
    private int memRound = 0;
    private List<Integer> memSeq = new ArrayList<>();
    private static final int[] MEM_LENGTHS = {4, 5, 6};

    private void startMemoryGame() {
        llMemory.setVisibility(View.VISIBLE);
        llMath.setVisibility(View.GONE);
        tvGameTitle.setText("Memory Challenge");
        memRound = 0;
        nextMemoryRound();
        btnMemSubmit.setOnClickListener(v -> checkMemory());
    }

    private void nextMemoryRound() {
        if (memRound >= 3) { finishGame(false); return; }
        int len = MEM_LENGTHS[memRound];
        memSeq.clear();
        for (int i = 0; i < len; i++) memSeq.add(rnd.nextInt(9) + 1);

        StringBuilder sb = new StringBuilder();
        for (int n : memSeq) sb.append(n).append("  ");
        tvMemDisplay.setText(sb.toString().trim());
        tvMemPrompt.setText("Round " + (memRound + 1) + "/3 - Memorise these " + len + " numbers!");
        tvMemDisplay.setVisibility(View.VISIBLE);
        etMemAnswer.setVisibility(View.GONE);
        btnMemSubmit.setVisibility(View.GONE);
        etMemAnswer.setText("");

        tvMemDisplay.postDelayed(() -> {
            tvMemDisplay.setVisibility(View.GONE);
            tvMemPrompt.setText("Now type the numbers in order (space-separated):");
            etMemAnswer.setVisibility(View.VISIBLE);
            btnMemSubmit.setVisibility(View.VISIBLE);
            etMemAnswer.requestFocus();
        }, 4000);
    }

    private void checkMemory() {
        String input = etMemAnswer.getText().toString().trim();
        String[] parts = input.split("\\s+");
        boolean ok = parts.length == memSeq.size();
        if (ok) {
            for (int i = 0; i < parts.length; i++) {
                try {
                    if (Integer.parseInt(parts[i]) != memSeq.get(i)) { ok = false; break; }
                } catch (Exception e) { ok = false; break; }
            }
        }
        StringBuilder ans = new StringBuilder();
        for (int n : memSeq) ans.append(n).append(" ");
        if (ok) {
            Toast.makeText(this, "Perfect memory!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sequence was: " + ans.toString().trim(), Toast.LENGTH_LONG).show();
        }
        memRound++;
        nextMemoryRound();
    }

    // ── FINISH ────────────────────────────────────────────────
    private void finishGame(boolean timedOut) {
        if (gameComplete) return;
        gameComplete = true;
        if (timer != null) timer.cancel();

        long elapsed = System.currentTimeMillis() - gameStartMs;
        if (elapsed < MIN_PLAY_MS && !timedOut) {
            long remaining = MIN_PLAY_MS - elapsed;
            tvTimer.postDelayed(this::showResult, remaining);
        } else {
            showResult();
        }
    }

    private void showResult() {
        llMath.setVisibility(View.GONE);
        llMemory.setVisibility(View.GONE);
        llResult.setVisibility(View.VISIBLE);
        tvResult.setText(MOTIVATIONS[rnd.nextInt(MOTIVATIONS.length)]);
        tvTimer.setText("Challenge Complete!");
        btnDone.setText("Return to Work");
    }

    private void goHome() {
        FocusAccessibilityService svc = FocusAccessibilityService.getInstance();
        if (svc != null) {
            svc.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME);
        }
    }

    @Override
    public void onBackPressed() {
        if (gameComplete) { goHome(); finish(); }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
