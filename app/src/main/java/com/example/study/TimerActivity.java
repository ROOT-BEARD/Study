package com.example.study;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * Runs a Pomodoro timer with focus sessions, short breaks, and long breaks.
 * A long break follows every fourth completed focus session.
 */
public class TimerActivity extends AppCompatActivity {

    /** The type of timer phase currently running or selected. */
    private enum Phase {
        SESSION, SHORT_BREAK, LONG_BREAK
    }

    private TextView timerView;
    private TextView timerLabel;
    private EditText sessionInput;
    private EditText breakInput;
    private EditText longBreakInput;
    private Button startStopButton;

    private CountDownTimer countDownTimer;
    private Phase phase = Phase.SESSION;
    private long remainingMillis;
    private int completedSessions;
    private boolean running;
    private boolean paused;
    private ToneGenerator toneGenerator;

    /**
     * Initializes the timer screen and connects its buttons to their actions.
     *
     * @param savedInstanceState previously saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        timerView = findViewById(R.id.timerView);
        timerLabel = findViewById(R.id.timerLabel);
        sessionInput = findViewById(R.id.sessionInput);
        breakInput = findViewById(R.id.breakInput);
        longBreakInput = findViewById(R.id.longBreakInput);
        startStopButton = findViewById(R.id.startStopButton);
        Button resetButton = findViewById(R.id.resetButton);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

        startStopButton.setText(R.string._timer_start);
        timerView.setText(R.string._session_default);

        startStopButton.setOnClickListener(v -> {
            if (running) {
                pauseTimer();
            } else if (paused) {
                startCountdown(remainingMillis);
            } else {
                startNewPhase(phase);
            }
        });

        resetButton.setOnClickListener(v -> resetTimer());
    }

    /**
     * Selects a timer phase, reads its duration, and starts its countdown.
     *
     * @param newPhase the phase to start
     */
    private void startNewPhase(Phase newPhase) {
        phase = newPhase;
        timerLabel.setText(getPhaseLabel(phase));

        EditText durationInput = getInputForPhase(phase);
        long duration = readDurationMillis(durationInput);

        if (duration <= 0) {
            remainingMillis = 0;
            timerView.setText("00:00");
            startStopButton.setText(R.string._timer_start);
            return;
        }

        remainingMillis = duration;
        startCountdown(remainingMillis);
    }

    /**
     * Starts or resumes a countdown for the specified duration.
     *
     * @param durationMillis countdown duration, in milliseconds
     */
    private void startCountdown(long durationMillis) {
        if (durationMillis <= 0) return;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        paused = false;
        running = true;
        startStopButton.setText(R.string._timer_pause);

        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;
                updateTimerDisplay(remainingMillis);
            }

            @Override
            public void onFinish() {
                running = false;
                paused = false;
                countDownTimer = null;

                toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 500);

                if (phase == Phase.SESSION) {
                    completedSessions++;
                    phase = completedSessions % 4 == 0
                            ? Phase.LONG_BREAK
                            : Phase.SHORT_BREAK;
                } else {
                    phase = Phase.SESSION;
                }

                timerLabel.setText(getPhaseLabel(phase));
                remainingMillis = readDurationMillis(getInputForPhase(phase));
                updateTimerDisplay(Math.max(remainingMillis, 0));
                startStopButton.setText(R.string._timer_start);
            }

        }.start();
    }

    /** Pauses the current countdown while preserving its remaining time. */
    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        running = false;
        paused = true;
        startStopButton.setText(R.string._timer_start);
    }

    /**
     * Cancels the countdown and resets the timer to the session duration.
     * Also clears the completed-session count.
     */
    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        running = false;
        paused = false;
        completedSessions = 0;
        phase = Phase.SESSION;
        timerLabel.setText(getPhaseLabel(phase));
        startStopButton.setText(R.string._timer_start);

        remainingMillis = readDurationMillis(sessionInput);
        updateTimerDisplay(Math.max(remainingMillis, 0));
    }

    /**
     * Gets the input field associated with a timer phase.
     *
     * @param phase the phase whose input field is needed
     * @return the duration input field for that phase
     */
    private EditText getInputForPhase(Phase phase) {
        switch (phase) {
            case SHORT_BREAK:
                return breakInput;
            case LONG_BREAK:
                return longBreakInput;
            case SESSION:
            default:
                return sessionInput;
        }
    }

    /**
     * Gets the display label for a timer phase.
     *
     * @param phase the phase to label
     * @return the label displayed for the phase
     */
    private String getPhaseLabel(Phase phase) {
        switch (phase) {
            case SHORT_BREAK:
                return "Break";
            case LONG_BREAK:
                return "Long Break";
            case SESSION:
            default:
                return "Session";
        }
    }

    /**
     * Parses a duration from an input field. Accepts MM:SS or a
     * number of minutes, such as 25.
     *
     * @param input the field containing the duration
     * @return the duration in milliseconds, or {@code 0} if invalid
     */
    private long readDurationMillis(EditText input) {
        String value = input.getText().toString().trim();

        try {
            String[] parts = value.split(":", -1);
            long minutes;
            long seconds = 0;

            if (parts.length == 2) {
                minutes = Long.parseLong(parts[0].trim());
                seconds = Long.parseLong(parts[1].trim());
            } else if (parts.length == 1) {
                minutes = Long.parseLong(parts[0].trim());
            } else {
                throw new NumberFormatException();
            }

            if (minutes < 0 || minutes > 9999 || seconds < 0 || seconds > 59) {
                throw new NumberFormatException();
            }

            long duration = (minutes * 60 + seconds) * 1000;
            if (duration == 0) {
                throw new NumberFormatException();
            }

            input.setError(null);
            return duration;

        } catch (NumberFormatException e) {
            input.setError("Enter a duration like 25:00");
            return 0;
        }
    }

    /**
     * Displays a duration as minutes and seconds in the timer text view.
     *
     * @param millis duration to display, in milliseconds
     */
    private void updateTimerDisplay(long millis) {
        long totalSeconds = (millis + 999) / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        timerView.setText(String.format(
                Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    /** Cancels the countdown when the activity is destroyed. */
    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
