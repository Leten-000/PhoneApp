# Jarvis

Jarvis to minimalna prywatna aplikacja PWA na telefon. Po uruchomieniu pokazuje komunikat:

> aplikacja działa

## Link na telefon bez komputera

Po włączeniu GitHub Pages dla tego repozytorium aplikacja będzie dostępna jako zwykły link:

```text
https://<twoj-login-github>.github.io/<nazwa-repozytorium>/
```

Na telefonie otwórz ten link w przeglądarce, a następnie wybierz opcję dodania strony do ekranu głównego. Od tej chwili Jarvis będzie uruchamiał się jak aplikacja.

Workflow `.github/workflows/pages.yml` publikuje aplikację automatycznie po każdym pushu i dodatkowo zapisuje paczkę `jarvis-pwa-files` w zakładce Actions, jeśli potrzebujesz plików do pobrania.

## Dlaczego PWA?

PWA można dodać do ekranu głównego telefonu bez publikowania w sklepie. Po wdrożeniu nowej wersji na serwerze telefon automatycznie pobierze aktualizację przez przeglądarkę i service workera, bez ręcznego instalowania plików APK/IPA.

## Uruchomienie lokalne

```bash
npm run start
```

Następnie otwórz adres `http://localhost:4173` na komputerze albo adres IP komputera w tej samej sieci na telefonie.

## Wersja produkcyjna

```bash
npm run build
npm run preview
```
