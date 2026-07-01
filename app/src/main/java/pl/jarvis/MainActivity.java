package pl.jarvis;

import android.app.Activity;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private TextView status;
    private EditText commandInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);
        layout.setBackgroundColor(Color.rgb(2, 6, 23));

        TextView name = new TextView(this);
        name.setText("Jarvis");
        name.setTextColor(Color.rgb(56, 189, 248));
        name.setTextSize(18);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setLetterSpacing(0.18f);
        name.setGravity(Gravity.CENTER);

        TextView prompt = new TextView(this);
        prompt.setText("Co mam zrobić?");
        prompt.setTextColor(Color.rgb(226, 232, 240));
        prompt.setTextSize(34);
        prompt.setTypeface(Typeface.DEFAULT_BOLD);
        prompt.setGravity(Gravity.CENTER);
        prompt.setPadding(0, 24, 0, 18);

        commandInput = new EditText(this);
        commandInput.setHint("np. nastaw alarm na 7:30");
        commandInput.setSingleLine(false);
        commandInput.setMinLines(2);
        commandInput.setTextColor(Color.rgb(15, 23, 42));
        commandInput.setHintTextColor(Color.rgb(100, 116, 139));
        commandInput.setTextSize(18);
        commandInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        commandInput.setGravity(Gravity.CENTER_VERTICAL);
        commandInput.setPadding(28, 18, 28, 18);

        Button runButton = new Button(this);
        runButton.setText("Wykonaj");
        runButton.setAllCaps(false);
        runButton.setTextSize(18);

        status = new TextView(this);
        status.setText("Wpisz polecenie, np. „minutnik za 10 minut” albo „za 30 minut włącz stoper”.");
        status.setTextColor(Color.rgb(203, 213, 225));
        status.setTextSize(16);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 24, 0, 0);

        runButton.setOnClickListener((view) -> handleCommand());
        commandInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleCommand();
                return true;
            }

            return false;
        });

        layout.addView(name);
        layout.addView(prompt);
        layout.addView(commandInput, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layout.addView(runButton, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layout.addView(status);
        setContentView(layout);
    }

    private void handleCommand() {
        String command = commandInput.getText().toString().trim();
        String normalized = command.toLowerCase(Locale.ROOT);

        if (command.isEmpty()) {
            status.setText("Najpierw wpisz, co Jarvis ma zrobić.");
            return;
        }

        if (normalized.contains("alarm")) {
            if (trySetAlarm(command, normalized)) {
                return;
            }
        }

        if (normalized.contains("minutnik") || normalized.contains("timer") || normalized.contains("stoper")) {
            if (trySetTimer(command, normalized)) {
                return;
            }
        }

        status.setText("Nie rozumiem jeszcze tego polecenia. Spróbuj: „nastaw alarm na 7:30”, „minutnik za 10 minut” albo „za 30 minut włącz stoper”.");
    }

    private boolean trySetAlarm(String originalCommand, String normalizedCommand) {
        Matcher matcher = Pattern.compile("(\\d{1,2})(?:[:.](\\d{2}))?").matcher(normalizedCommand);

        if (!matcher.find()) {
            status.setText("Podaj godzinę alarmu, np. „nastaw alarm na 7:30”.");
            return true;
        }

        int hour = Integer.parseInt(matcher.group(1));
        int minute = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));

        if (hour > 23 || minute > 59) {
            status.setText("Godzina alarmu musi być w formacie 0:00–23:59.");
            return true;
        }

        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
            .putExtra(AlarmClock.EXTRA_HOUR, hour)
            .putExtra(AlarmClock.EXTRA_MINUTES, minute)
            .putExtra(AlarmClock.EXTRA_MESSAGE, originalCommand)
            .putExtra(AlarmClock.EXTRA_SKIP_UI, false);
        startActivity(intent);
        status.setText(String.format(Locale.ROOT, "Otwieram ustawienie alarmu na %02d:%02d.", hour, minute));
        return true;
    }

    private boolean trySetTimer(String originalCommand, String normalizedCommand) {
        int seconds = extractDurationSeconds(normalizedCommand);

        if (seconds <= 0) {
            status.setText("Podaj czas, np. „minutnik za 10 minut” albo „za 30 minut włącz stoper”.");
            return true;
        }

        Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER)
            .putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            .putExtra(AlarmClock.EXTRA_MESSAGE, originalCommand)
            .putExtra(AlarmClock.EXTRA_SKIP_UI, false);
        startActivity(intent);
        status.setText("Otwieram minutnik dla polecenia: „" + originalCommand + "”.");
        return true;
    }

    private int extractDurationSeconds(String normalizedCommand) {
        Matcher matcher = Pattern.compile("(\\d+)\\s*(godzin|godziny|godz|h|minut|minuty|min|m|sekund|sekundy|sek|s)").matcher(normalizedCommand);
        int totalSeconds = 0;

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            if (unit.startsWith("godz") || unit.equals("h")) {
                totalSeconds += value * 60 * 60;
            } else if (unit.startsWith("min") || unit.equals("m")) {
                totalSeconds += value * 60;
            } else {
                totalSeconds += value;
            }
        }

        return totalSeconds;
    }
}
