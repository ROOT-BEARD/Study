package com.example.study;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;

import com.example.study.Pages.TimerActivity;

import java.util.Locale;

public class TimerService extends Service {

    public static final String ACTION_START = "com.example.study.action.START";
    public static final String EXTRA_SESSION_MILLIS = "session_millis";
    public static final String EXTRA_SHORT_BREAK_MILLIS = "short_break_millis";
    public static final String EXTRA_LONG_BREAK_MILLIS = "long_break_millis";

    private static final String CHANNEL_ID = "pomodoro_timer";
    private static final int NOTIFICATION_ID = 1;
    private static final String PREFS_NAME = "pomodoro_timer_state";

    private enum Phase {
        SESSION, SHORT_BREAK, LONG_BREAK
    }

    /** State returned to the activity for displaying the timer. */
    public static final class TimerState {
        public final String phaseLabel;
        public final long remainingMillis;
        public final boolean running;
        public final int completedSessions;

        private TimerState(
                String phaseLabel,
                long remainingMillis,
                boolean running,
                int completedSessions) {
            this.phaseLabel = phaseLabel;
            this.remainingMillis = remainingMillis;
            this.running = running;
            this.completedSessions = completedSessions;
        }
    }

    /** Optional callback for the activity to receive timer updates. */
    public interface Listener {
        void onTimerStateChanged(TimerState state);
    }

    public class LocalBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    private final IBinder binder = new LocalBinder();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Phase phase = Phase.SESSION;
    private long sessionMillis = 25 * 60 * 1000L;
    private long shortBreakMillis = 5 * 60 * 1000L;
    private long longBreakMillis = 15 * 60 * 1000L;

    private long remainingMillis = sessionMillis;
    private long endTimeElapsed;
    private int completedSessions;
    private boolean running;
    private boolean foreground;
    private Listener listener;
    private ToneGenerator toneGenerator;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            remainingMillis = Math.max(
                    0,
                    endTimeElapsed - SystemClock.elapsedRealtime());

            if (remainingMillis == 0) {
                finishPhase();
                return;
            }

            saveState();
            publishState();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        loadState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // A foreground service must post its notification promptly after launch.
        ensureForeground();

        if (intent != null && ACTION_START.equals(intent.getAction())) {
            sessionMillis = intent.getLongExtra(
                    EXTRA_SESSION_MILLIS, sessionMillis);
            shortBreakMillis = intent.getLongExtra(
                    EXTRA_SHORT_BREAK_MILLIS, shortBreakMillis);
            longBreakMillis = intent.getLongExtra(
                    EXTRA_LONG_BREAK_MILLIS, longBreakMillis);

            saveState();
            startOrResume();
        } else if (running) {
            // The system may recreate a sticky service after killing its process.
            if (endTimeElapsed <= SystemClock.elapsedRealtime()) {
                finishPhase();
            } else {
                remainingMillis = endTimeElapsed - SystemClock.elapsedRealtime();
                scheduleTicker();
                publishState();
            }
        } else {
            publishState();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
        if (listener != null) {
            listener.onTimerStateChanged(getTimerState());
        }
    }

    public void clearListener() {
        listener = null;
    }

    /** Starts the selected phase, or resumes it if it was paused. */
    public void startOrResume() {
        if (running) return;

        if (remainingMillis <= 0) {
            remainingMillis = durationFor(phase);
        }
        if (remainingMillis <= 0) return;

        running = true;
        endTimeElapsed = SystemClock.elapsedRealtime() + remainingMillis;

        saveState();
        scheduleTicker();
        publishState();
    }

    public void pause() {
        if (!running) return;

        remainingMillis = Math.max(
                0,
                endTimeElapsed - SystemClock.elapsedRealtime());
        running = false;
        endTimeElapsed = 0;

        handler.removeCallbacks(ticker);
        saveState();
        publishState();
    }

    /** Resets to the session phase and clears the completed-session count. */
    public void reset() {
        handler.removeCallbacks(ticker);

        running = false;
        phase = Phase.SESSION;
        completedSessions = 0;
        remainingMillis = sessionMillis;
        endTimeElapsed = 0;

        saveState();
        publishState();
    }

    public TimerState getTimerState() {
        long remaining = running
                ? Math.max(0, endTimeElapsed - SystemClock.elapsedRealtime())
                : remainingMillis;

        return new TimerState(
                labelFor(phase),
                remaining,
                running,
                completedSessions);
    }

    private void finishPhase() {
        handler.removeCallbacks(ticker);
        running = false;
        endTimeElapsed = 0;
        remainingMillis = 0;

        if (toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 500);
        }

        if (phase == Phase.SESSION) {
            completedSessions++;
            phase = completedSessions % 4 == 0
                    ? Phase.LONG_BREAK
                    : Phase.SHORT_BREAK;
        } else {
            phase = Phase.SESSION;
        }

        // Match the original behavior: select the next phase, but wait for
        // the user to press Start before beginning it.
        remainingMillis = durationFor(phase);

        saveState();
        publishState();
    }

    private void scheduleTicker() {
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    private long durationFor(Phase phase) {
        switch (phase) {
            case SHORT_BREAK:
                return shortBreakMillis;
            case LONG_BREAK:
                return longBreakMillis;
            case SESSION:
            default:
                return sessionMillis;
        }
    }

    private String labelFor(Phase phase) {
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

    private void publishState() {
        TimerState state = getTimerState();
        updateNotification(state);

        if (listener != null) {
            listener.onTimerStateChanged(state);
        }
    }

    private void ensureForeground() {
        if (foreground) return;

        startForeground(NOTIFICATION_ID, buildNotification(getTimerState()));
        foreground = true;
    }

    private void updateNotification(TimerState state) {
        if (!foreground) return;

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(state));
        }
    }

    private Notification buildNotification(TimerState state) {
        Intent openAppIntent = new Intent(this, TimerActivity.class);
        openAppIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        String status = state.running ? "Timer running" : "Paused or ready";

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(state.phaseLabel + " · " + formatTime(state.remainingMillis))
                .setContentText(status)
                .setContentIntent(contentIntent)
                .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();
    }

    private String formatTime(long millis) {
        long totalSeconds = (millis + 999) / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pomodoro timer",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Shows the running Pomodoro timer");

            NotificationManager manager =
                    getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void saveState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString("phase", phase.name())
                .putLong("sessionMillis", sessionMillis)
                .putLong("shortBreakMillis", shortBreakMillis)
                .putLong("longBreakMillis", longBreakMillis)
                .putLong("remainingMillis", remainingMillis)
                .putLong("endTimeElapsed", endTimeElapsed)
                .putInt("completedSessions", completedSessions)
                .putBoolean("running", running)
                .apply();
    }

    private void loadState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        try {
            phase = Phase.valueOf(prefs.getString("phase", Phase.SESSION.name()));
        } catch (IllegalArgumentException e) {
            phase = Phase.SESSION;
        }

        sessionMillis = prefs.getLong("sessionMillis", sessionMillis);
        shortBreakMillis = prefs.getLong("shortBreakMillis", shortBreakMillis);
        longBreakMillis = prefs.getLong("longBreakMillis", longBreakMillis);
        remainingMillis = prefs.getLong("remainingMillis", sessionMillis);
        endTimeElapsed = prefs.getLong("endTimeElapsed", 0);
        completedSessions = prefs.getInt("completedSessions", 0);
        running = prefs.getBoolean("running", false);

        if (running) {
            remainingMillis = Math.max(
                    0,
                    endTimeElapsed - SystemClock.elapsedRealtime());
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(ticker);
        saveState();

        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }

        super.onDestroy();
    }
}
