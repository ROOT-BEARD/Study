package com.example.study.Pages;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.EditText;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;


import androidx.appcompat.app.AppCompatActivity;

import com.example.study.R;
import com.example.study.TimerService;
import com.example.study.databinding.ActivityTimerBinding;

import java.util.Locale;

public class TimerActivity extends AppCompatActivity {

    private ActivityTimerBinding binding;
    private TimerService timerService;
    private boolean bound;
    private ActivityResultLauncher<String> notificationPermissionLauncher;


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
            timerService = binder.getService();
            bound = true;

            timerService.setListener(TimerActivity.this::showTimerState);
            setControlsEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            timerService = null;
            bound = false;
            setControlsEnabled(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTimerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setControlsEnabled(false);

        binding.startStopButton.setOnClickListener(v -> {
            if (timerService == null) return;

            if (timerService.getTimerState().running) {
                timerService.pause();
            } else {
                requestNotificationPermissionThenStart();
            }

        });

        binding.timerBackButton.setOnClickListener(v-> onBackPressed());

        binding.resetButton.setOnClickListener(v -> {
            if (timerService != null) {
                timerService.reset();
            }
        });

        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    // Start the timer either way. If permission was denied,
                    // Android may not show its notification in the notification drawer.
                    startOrResumeService();
                });

    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent serviceIntent = new Intent(this, TimerService.class);
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (bound) {
            timerService.clearListener();
            unbindService(serviceConnection);
            timerService = null;
            bound = false;
        }

        super.onStop();
    }

    private void startOrResumeService() {
        long sessionMillis = readDurationMillis(binding.sessionInput);
        long shortBreakMillis = readDurationMillis(binding.breakInput);
        long longBreakMillis = readDurationMillis(binding.longBreakInput);

        if (sessionMillis <= 0 || shortBreakMillis <= 0 || longBreakMillis <= 0) {
            return;
        }

        Intent intent = new Intent(this, TimerService.class)
                .setAction(TimerService.ACTION_START)
                .putExtra(TimerService.EXTRA_SESSION_MILLIS, sessionMillis)
                .putExtra(TimerService.EXTRA_SHORT_BREAK_MILLIS, shortBreakMillis)
                .putExtra(TimerService.EXTRA_LONG_BREAK_MILLIS, longBreakMillis);

        ContextCompat.startForegroundService(this, intent);
    }

    private void showTimerState(TimerService.TimerState state) {
        binding.timerLabel.setText(state.phaseLabel);
        updateTimerDisplay(state.remainingMillis);

        binding.startStopButton.setText(
                state.running ? R.string._timer_pause : R.string._timer_start);
    }

    private void setControlsEnabled(boolean enabled) {
        if (binding == null) return;

        binding.startStopButton.setEnabled(enabled);
        binding.resetButton.setEnabled(enabled);
    }

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

    private void updateTimerDisplay(long millis) {
        long totalSeconds = (millis + 999) / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        binding.timerView.setText(String.format(
                Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    private void requestNotificationPermissionThenStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS);
        } else {
            startOrResumeService();
        }
    }
}
