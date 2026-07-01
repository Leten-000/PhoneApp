# Jarvis

Jarvis to minimalna prywatna aplikacja na Androida. Po uruchomieniu pokazuje pole tekstowe, w którym możesz wpisać czynność, np. „nastaw alarm na 7:30”, „minutnik za 10 minut” albo „za 30 minut włącz stoper”.

## Plik aplikacji do pobrania na telefon

Jeśli jesteś już na ekranie GitHub Actions na telefonie, wykonaj dokładne kroki z pliku [PHONE_INSTALL.md](PHONE_INSTALL.md).

Nie używamy już GitHub Pages, bo dla prywatnych repozytoriów może wymagać płatnego planu i link z placeholderem nie działał bez podstawienia prawdziwego loginu oraz nazwy repozytorium.

Zamiast tego repozytorium buduje zwykły plik APK:

1. Wejdź na GitHubie w zakładkę **Actions**.
2. Otwórz najnowszy workflow **Build Jarvis APK**.
3. Pobierz artefakt **Jarvis-apk**.
4. Rozpakuj paczkę na telefonie i uruchom plik `Jarvis.apk`.
5. Jeśli Android zapyta o zgodę, pozwól przeglądarce lub menedżerowi plików instalować aplikacje spoza sklepu.

Po instalacji na ekranie telefonu pojawi się aplikacja **Jarvis** z polem do wpisywania poleceń.

## Automatyczne aktualizacje

Po każdym pushu na GitHub workflow buduje nowy plik `Jarvis.apk` i publikuje go jako release `jarvis-latest`. Po uruchomieniu aplikacja sprawdza przez internet, czy w tym release jest nowszy `versionCode`; jeśli tak, otwiera stronę pobierania najnowszego APK. Android nadal wymaga potwierdzenia instalacji APK przez użytkownika, ale Jarvis sam wykrywa dostępność aktualizacji. Gdy nie ma internetu, aplikacja działa dalej w ostatnio zainstalowanej wersji.

## Budowanie lokalne

```bash
gradle :app:assembleDebug
```

Gotowy plik powstaje tutaj:

```text
app/build/outputs/apk/debug/app-debug.apk — w GitHub Actions ten plik jest kopiowany jako `Jarvis.apk`.
```

## Wersja PWA

W repozytorium nadal zostają proste pliki PWA (`index.html`, `manifest.webmanifest`, `service-worker.js`). Można ich użyć później, jeśli aplikacja będzie hostowana pod publicznym linkiem.
