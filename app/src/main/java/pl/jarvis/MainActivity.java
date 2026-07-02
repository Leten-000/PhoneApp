package pl.jarvis;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private static final String LATEST_RELEASE_API = "https://api.github.com/repos/Leten-000/PhoneApp/releases/tags/jarvis-latest";
    private static final String LATEST_RELEASE_PAGE = "https://github.com/Leten-000/PhoneApp/releases/tag/jarvis-latest";
    private static final String APK_FILE_NAME = "Jarvis.apk";
    private static final String APK_MIME_TYPE = "application/vnd.android.package-archive";
    private static final String GEMINI_MODEL = "gemini-3.5-flash";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_MODEL + ":generateContent";

    private TextView status;
    private TextView updateStatus;
    private EditText commandInput;
    private long updateDownloadId = -1;
    private BroadcastReceiver downloadReceiver;

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
        commandInput.setHint("Wpisz polecenie dla Jarvisa");
        commandInput.setSingleLine(false);
        commandInput.setMinLines(2);
        commandInput.setTextColor(Color.WHITE);
        commandInput.setHintTextColor(Color.rgb(148, 163, 184));
        commandInput.setTextSize(18);
        commandInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        commandInput.setGravity(Gravity.CENTER_VERTICAL);
        commandInput.setPadding(28, 18, 28, 18);
        GradientDrawable inputBackground = new GradientDrawable();
        inputBackground.setColor(Color.rgb(15, 23, 42));
        inputBackground.setCornerRadius(24);
        inputBackground.setStroke(2, Color.rgb(56, 189, 248));
        commandInput.setBackground(inputBackground);

        Button runButton = new Button(this);
        runButton.setText("Wykonaj");
        runButton.setAllCaps(false);
        runButton.setTextSize(18);

        Button askAiButton = new Button(this);
        askAiButton.setText("Zapytaj AI");
        askAiButton.setAllCaps(false);
        askAiButton.setTextSize(18);

        Button updateButton = new Button(this);
        updateButton.setText("Sprawdź aktualizację");
        updateButton.setAllCaps(false);
        updateButton.setTextSize(16);

        status = new TextView(this);
        status.setText("");
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
        askAiButton.setOnClickListener((view) -> showAskAiDialog());
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
        layout.addView(askAiButton, new LinearLayout.LayoutParams(
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
        registerDownloadReceiver();
        checkForUpdates(false);
    }


    private void showAskAiDialog() {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(32, 16, 32, 0);

        EditText questionInput = new EditText(this);
        questionInput.setHint("O co chcesz spytać AI?");
        questionInput.setSingleLine(false);
        questionInput.setMinLines(3);
        questionInput.setTextColor(Color.rgb(15, 23, 42));
        questionInput.setTextSize(16);
        questionInput.setGravity(Gravity.TOP | Gravity.START);

        TextView answerView = new TextView(this);
        answerView.setText("Wpisz pytanie i naciśnij „Wyślij”. Klucz API jest dodawany podczas budowania aplikacji z sekretu GitHub API_KEY.");
        answerView.setTextColor(Color.rgb(15, 23, 42));
        answerView.setTextSize(16);
        answerView.setPadding(0, 24, 0, 0);

        ScrollView answerScroll = new ScrollView(this);
        answerScroll.addView(answerView);

        dialogLayout.addView(questionInput, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        dialogLayout.addView(answerScroll, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            420
        ));

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
            .setTitle("Zapytaj AI")
            .setView(dialogLayout)
            .setPositiveButton("Wyślij", null)
            .setNegativeButton("Zamknij", null)
            .create();

        dialog.setOnShowListener((dialogInterface) -> {
            Button sendButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            sendButton.setOnClickListener((buttonView) -> {
                String question = questionInput.getText().toString().trim();

                if (BuildConfig.API_KEY.isEmpty()) {
                    answerView.setText("Brakuje sekretu API_KEY w zbudowanej aplikacji. Dodaj API_KEY w GitHub Secrets i poczekaj na nowy APK z GitHub Actions.");
                    return;
                }

                if (question.isEmpty()) {
                    answerView.setText("Najpierw wpisz pytanie do AI.");
                    return;
                }

                sendButton.setEnabled(false);
                answerView.setText("Pytam AI...");
                askGemini(question, answerView, sendButton);
            });
        });

        dialog.show();
    }

    private void askGemini(String question, TextView answerView, Button sendButton) {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                JSONObject textPart = new JSONObject().put("text", question);
                JSONArray parts = new JSONArray().put(textPart);
                JSONObject content = new JSONObject().put("parts", parts);
                JSONObject requestBody = new JSONObject().put("contents", new JSONArray().put(content));

                connection = (HttpURLConnection) new URL(GEMINI_API_URL).openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("x-goog-api-key", BuildConfig.API_KEY);
                connection.setDoOutput(true);
                connection.getOutputStream().write(requestBody.toString().getBytes("UTF-8"));

                int responseCode = connection.getResponseCode();
                String response = responseCode >= 200 && responseCode < 300
                    ? readResponse(connection)
                    : readErrorResponse(connection);

                if (responseCode < 200 || responseCode >= 300) {
                    showAiAnswer(answerView, sendButton, "AI zwróciło błąd (" + responseCode + "). Spróbuj ponownie później.");
                    return;
                }

                String answer = parseGeminiAnswer(response);
                showAiAnswer(answerView, sendButton, answer.isEmpty() ? "AI nie zwróciło odpowiedzi." : answer);
            } catch (Exception exception) {
                showAiAnswer(answerView, sendButton, "Nie udało się połączyć z AI. Sprawdź internet i spróbuj ponownie.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private String readErrorResponse(HttpURLConnection connection) throws Exception {
        if (connection.getErrorStream() == null) {
            return "";
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        reader.close();
        return builder.toString();
    }

    private String parseGeminiAnswer(String json) throws Exception {
        JSONObject response = new JSONObject(json);
        JSONArray candidates = response.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) {
            return "";
        }

        JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
        if (content == null) {
            return "";
        }

        JSONArray parts = content.optJSONArray("parts");
        if (parts == null) {
            return "";
        }

        StringBuilder answer = new StringBuilder();
        for (int index = 0; index < parts.length(); index++) {
            String text = parts.getJSONObject(index).optString("text", "");
            if (!text.isEmpty()) {
                if (answer.length() > 0) {
                    answer.append("\n\n");
                }
                answer.append(text);
            }
        }

        return answer.toString().trim();
    }

    private void showAiAnswer(TextView answerView, Button sendButton, String answer) {
        runOnUiThread(() -> {
            answerView.setText(answer);
            sendButton.setEnabled(true);
        });
    }

    @Override
    protected void onDestroy() {
        if (downloadReceiver != null) {
            unregisterReceiver(downloadReceiver);
        }

        super.onDestroy();
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
                ReleaseInfo releaseInfo = parseReleaseInfo(json);
                int currentVersion = getCurrentVersionCode();

                if (releaseInfo.versionCode > currentVersion) {
                    if (releaseInfo.apkUrl.isEmpty()) {
                        showUpdateMessage("Jest nowsza wersja Jarvisa, ale nie znalazłem pliku APK w release. Otwieram stronę pobierania.");
                        openLatestReleasePage();
                        return;
                    }

                    showUpdateMessage("Jest nowsza wersja Jarvisa. Pobieram aktualizację automatycznie...");
                    downloadAndInstallUpdate(releaseInfo.apkUrl);
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

    private ReleaseInfo parseReleaseInfo(String json) throws Exception {
        JSONObject release = new JSONObject(json);
        int versionCode = extractRemoteVersion(release.optString("body", "") + " " + release.optString("name", ""));
        String apkUrl = "";
        JSONArray assets = release.optJSONArray("assets");

        if (assets != null) {
            for (int index = 0; index < assets.length(); index++) {
                JSONObject asset = assets.getJSONObject(index);
                String assetName = asset.optString("name", "");

                if (APK_FILE_NAME.equals(assetName) || assetName.toLowerCase(Locale.ROOT).endsWith(".apk")) {
                    apkUrl = asset.optString("browser_download_url", "");
                    break;
                }
            }
        }

        return new ReleaseInfo(versionCode, apkUrl);
    }

    private int extractRemoteVersion(String text) {
        Matcher matcher = Pattern.compile("versionCode=(\\d+)").matcher(text);
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

    private void registerDownloadReceiver() {
        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == updateDownloadId) {
                    installDownloadedUpdate(downloadId);
                }
            }
        };

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(downloadReceiver, filter);
        }
    }

    private void downloadAndInstallUpdate(String apkUrl) {
        runOnUiThread(() -> {
            try {
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl))
                    .setTitle("Aktualizacja Jarvisa")
                    .setDescription("Pobieram najnowszy plik APK")
                    .setMimeType(APK_MIME_TYPE)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalFilesDir(this, null, APK_FILE_NAME);

                updateDownloadId = downloadManager.enqueue(request);
                updateStatus.setText("Pobieram aktualizację. Po pobraniu Android pokaże ekran instalacji — wystarczy zatwierdzić.");
            } catch (Exception exception) {
                updateStatus.setText("Nie udało się rozpocząć pobierania aktualizacji. Otwieram stronę pobierania APK.");
                openLatestReleasePage();
            }
        });
    }

    private void installDownloadedUpdate(long downloadId) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri apkUri = downloadManager.getUriForDownloadedFile(downloadId);

        if (apkUri == null) {
            showUpdateMessage("Nie udało się znaleźć pobranego APK. Otwieram stronę pobierania.");
            openLatestReleasePage();
            return;
        }

        Intent installIntent = new Intent(Intent.ACTION_VIEW)
            .setDataAndType(apkUri, APK_MIME_TYPE)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        showUpdateMessage("Aktualizacja pobrana. Otwieram instalator Androida — zatwierdź instalację.");
        startActivity(installIntent);
    }

    private static class ReleaseInfo {
        private final int versionCode;
        private final String apkUrl;

        private ReleaseInfo(int versionCode, String apkUrl) {
            this.versionCode = versionCode;
            this.apkUrl = apkUrl;
        }
    }

    private void handleCommand() {
        String command = commandInput.getText().toString().trim();
        String normalized = command.toLowerCase(Locale.ROOT);

        if (command.isEmpty()) {
            status.setText("Najpierw wpisz, co Jarvis ma zrobić.");
            return;
        }

        if (containsWebSearchRequest(normalized)) {
            if (trySearchWeb(command, normalized)) {
                return;
            }
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

        if (containsCalendarRequest(normalized)) {
            if (tryAddCalendarEvent(command, normalized)) {
                return;
            }
        }

        status.setText("Nie rozumiem jeszcze tego polecenia. Spróbuj: „nastaw alarm na 7:30”, „minutnik za 10 minut”, „dodaj wydarzenie jutro o 15:00” albo „znajdź mi hulajnogę do 400 zł”.");
    }

    private boolean containsWebSearchRequest(String normalizedCommand) {
        return normalizedCommand.startsWith("znajdź")
            || normalizedCommand.startsWith("znajdz")
            || normalizedCommand.startsWith("wyszukaj")
            || normalizedCommand.startsWith("poszukaj")
            || normalizedCommand.startsWith("sprawdź w internecie")
            || normalizedCommand.startsWith("sprawdz w internecie")
            || normalizedCommand.startsWith("szukaj");
    }

    private boolean trySearchWeb(String originalCommand, String normalizedCommand) {
        String query = buildSearchQuery(originalCommand, normalizedCommand);

        if (query.isEmpty()) {
            status.setText("Napisz, czego mam poszukać, np. „znajdź mi hulajnogę do 400 zł”.");
            return true;
        }

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + encodedQuery));
            startActivity(intent);
            status.setText("Szukam w internecie: „" + query + "”.");
        } catch (Exception exception) {
            status.setText("Nie udało się otworzyć wyszukiwarki internetowej.");
        }

        return true;
    }

    private String buildSearchQuery(String originalCommand, String normalizedCommand) {
        String query = originalCommand.trim();
        String lowerQuery = normalizedCommand.trim();
        String[] prefixes = {
            "sprawdź w internecie",
            "sprawdz w internecie",
            "znajdź mi",
            "znajdz mi",
            "znajdź",
            "znajdz",
            "wyszukaj",
            "poszukaj",
            "szukaj"
        };

        for (String prefix : prefixes) {
            if (lowerQuery.startsWith(prefix)) {
                query = query.substring(prefix.length()).trim();
                break;
            }
        }

        return query;
    }

    private boolean containsCalendarRequest(String normalizedCommand) {
        return normalizedCommand.contains("kalendarz")
            || normalizedCommand.contains("wydarzenie")
            || normalizedCommand.contains("spotkanie")
            || normalizedCommand.contains("zadanie")
            || normalizedCommand.contains("przypomnienie");
    }

    private boolean tryAddCalendarEvent(String originalCommand, String normalizedCommand) {
        Calendar startTime = extractCalendarStartTime(normalizedCommand);

        if (startTime == null) {
            status.setText("Podaj datę i godzinę, np. „dodaj wydarzenie jutro o 15:00” albo „dodaj zadanie w kalendarzu na 12.07 o 9:00”.");
            return true;
        }

        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 1);

        Intent intent = new Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, buildCalendarTitle(originalCommand))
            .putExtra(CalendarContract.Events.DESCRIPTION, originalCommand)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.getTimeInMillis())
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
            .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        try {
            startActivity(intent);
            status.setText(String.format(Locale.ROOT, "Otwieram kalendarz, żeby dodać wpis na %02d.%02d.%04d o %02d:%02d.",
                startTime.get(Calendar.DAY_OF_MONTH),
                startTime.get(Calendar.MONTH) + 1,
                startTime.get(Calendar.YEAR),
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE)));
        } catch (ActivityNotFoundException exception) {
            status.setText("Nie znalazłem aplikacji kalendarza, która może dodać wydarzenie.");
        }
        return true;
    }

    private Calendar extractCalendarStartTime(String normalizedCommand) {
        Matcher timeMatcher = Pattern.compile("(?:o\\s*)?(\\d{1,2})(?:[:.](\\d{2}))").matcher(normalizedCommand);
        if (!timeMatcher.find()) {
            return null;
        }

        int hour = Integer.parseInt(timeMatcher.group(1));
        int minute = Integer.parseInt(timeMatcher.group(2));

        if (hour > 23 || minute > 59) {
            return null;
        }

        Calendar startTime = Calendar.getInstance();
        startTime.setLenient(false);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);
        startTime.set(Calendar.HOUR_OF_DAY, hour);
        startTime.set(Calendar.MINUTE, minute);

        if (normalizedCommand.contains("pojutrze")) {
            startTime.add(Calendar.DAY_OF_YEAR, 2);
        } else if (normalizedCommand.contains("jutro")) {
            startTime.add(Calendar.DAY_OF_YEAR, 1);
        } else {
            Matcher dateMatcher = Pattern.compile("(\\d{1,2})[.-](\\d{1,2})(?:[.-](\\d{2,4}))?").matcher(normalizedCommand);
            if (dateMatcher.find()) {
                int day = Integer.parseInt(dateMatcher.group(1));
                int month = Integer.parseInt(dateMatcher.group(2));
                int year = dateMatcher.group(3) == null ? startTime.get(Calendar.YEAR) : Integer.parseInt(dateMatcher.group(3));

                if (year < 100) {
                    year += 2000;
                }

                if (month < 1 || month > 12 || day < 1 || day > 31) {
                    return null;
                }

                startTime.set(Calendar.YEAR, year);
                startTime.set(Calendar.MONTH, month - 1);
                startTime.set(Calendar.DAY_OF_MONTH, day);
            } else if (!normalizedCommand.contains("dzisiaj") && !normalizedCommand.contains("dziś")) {
                return null;
            }
        }

        try {
            startTime.getTime();
        } catch (IllegalArgumentException exception) {
            return null;
        }

        return startTime;
    }

    private String buildCalendarTitle(String originalCommand) {
        return originalCommand
            .replaceFirst("(?i)^(dodaj|utwórz|stwórz|zapisz)\\s+", "")
            .replaceFirst("(?i)\\s+w kalendarzu.*$", "")
            .trim();
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
            .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        startActivity(intent);
        status.setText(String.format(Locale.ROOT, "Gotowe — ustawiłem alarm na %02d:%02d.", hour, minute));
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
            .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        startActivity(intent);
        status.setText("Gotowe — ustawiłem minutnik dla polecenia: „" + originalCommand + "”.");
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
