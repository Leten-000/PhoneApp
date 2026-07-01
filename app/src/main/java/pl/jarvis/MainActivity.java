package pl.jarvis;

import android.app.Activity;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private static final String LATEST_RELEASE_API = "https://api.github.com/repos/Leten-000/PhoneApp/releases/tags/jarvis-latest";
    private static final String LATEST_RELEASE_PAGE = "https://github.com/Leten-000/PhoneApp/releases/tag/jarvis-latest";

    private TextView status;
    private TextView updateStatus;
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

        Button updateButton = new Button(this);
        updateButton.setText("Sprawdź aktualizację");
        updateButton.setAllCaps(false);
        updateButton.setTextSize(16);

        status = new TextView(this);
        status.setText("Wpisz polecenie, np. „minutnik za 10 minut” albo „za 30 minut włącz stoper”.");
        status.setTextColor(Color.rgb(203, 213, 225));
        status.setTextSize(16);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 24, 0, 0);

        updateStatus = new TextView(this);
        updateStatus.setText("Gdy będzie internet, Jarvis sprawdzi czy na GitHubie jest nowsza wersja APK. Bez internetu działa ostatnio zainstalowana wersja.");
        updateStatus.setTextColor(Color.rgb(148, 163, 184));
        updateStatus.setTextSize(14);
        updateStatus.setGravity(Gravity.CENTER);
        updateStatus.setPadding(0, 18, 0, 0);

        runButton.setOnClickListener((view) -> handleCommand());
        updateButton.setOnClickListener((view) -> checkForUpdates(true));
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
        layout.addView(updateButton, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layout.addView(status);
        layout.addView(updateStatus);
        setContentView(layout);
        checkForUpdates(false);
    }

    private void checkForUpdates(boolean openedByUser) {
        updateStatus.setText("Sprawdzam aktualizacje...");

        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(LATEST_RELEASE_API).openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("Accept", "application/vnd.github+json");

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    showUpdateMessage("Nie mogę sprawdzić aktualizacji. Jeśli repozytorium jest prywatne, GitHub nie pozwoli aplikacji pobrać aktualizacji bez logowania. Bez internetu używasz ostatniej zainstalowanej wersji.");
                    return;
                }

                String json = readResponse(connection);
                int remoteVersion = extractRemoteVersion(json);
                int currentVersion = getCurrentVersionCode();

                if (remoteVersion > currentVersion) {
                    showUpdateMessage("Jest nowsza wersja Jarvisa. Otwieram stronę pobierania APK.");
                    openLatestReleasePage();
                    return;
                }

                if (openedByUser) {
                    showUpdateMessage("Masz już najnowszą wersję Jarvisa.");
                } else {
                    showUpdateMessage("Jarvis działa w najnowszej znanej wersji. Bez internetu zostanie użyta ta zainstalowana wersja.");
                }
            } catch (Exception exception) {
                showUpdateMessage("Brak połączenia z internetem albo GitHub nie jest dostępny. Jarvis działa dalej w ostatniej zainstalowanej wersji.");
            }
        }).start();
    }

    private String readResponse(HttpURLConnection connection) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        reader.close();
        return builder.toString();
    }

    private int extractRemoteVersion(String json) {
        Matcher matcher = Pattern.compile("versionCode=(\\d+)").matcher(json);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return 0;
    }

    private int getCurrentVersionCode() throws Exception {
        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            return (int) packageInfo.getLongVersionCode();
        }

        return packageInfo.versionCode;
    }

    private void showUpdateMessage(String message) {
        runOnUiThread(() -> updateStatus.setText(message));
    }

    private void openLatestReleasePage() {
        runOnUiThread(() -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(LATEST_RELEASE_PAGE))));
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
