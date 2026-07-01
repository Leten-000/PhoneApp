# Co zrobić dalej na telefonie

Jesteś już w dobrym miejscu: **Actions → Build Jarvis APK**. Teraz trzeba poczekać, aż budowanie się skończy.

## 1. Poczekaj na koniec budowania

Na ekranie widać status **In progress**. Odśwież stronę po chwili albo dotknij uruchomionego wpisu workflow.

- Jeśli pojawi się zielony znaczek, przejdź do następnego kroku.
- Jeśli pojawi się czerwony krzyżyk, otwórz ten wpis, zrób zrzut ekranu błędu i podeślij go dalej.

## 2. Pobierz APK

Po wejściu w zakończony workflow przewiń stronę na dół do sekcji **Artifacts**.

Pobierz artefakt:

```text
Jarvis-apk
```

GitHub pobierze paczkę ZIP. W środku będzie plik:

```text
Jarvis.apk
```

## 3. Zainstaluj na Androidzie

1. Otwórz pobrany ZIP w aplikacji **Pliki** albo **Menedżer plików**.
2. Wypakuj albo otwórz plik `Jarvis.apk`.
3. Jeśli Android pokaże ostrzeżenie, wybierz ustawienie pozwalające instalować aplikacje z tej przeglądarki lub menedżera plików.
4. Wróć do instalacji i dotknij **Zainstaluj**.
5. Po instalacji uruchom aplikację **Jarvis**.

Po uruchomieniu zobaczysz pole wpisywania poleceń. Przykłady:

```text
nastaw alarm na 7:30
minutnik za 10 minut
za 30 minut włącz stoper
```

## 4. Jak aktualizować później

Po kolejnej zmianie aplikacji wejdź ponownie w **Actions → Build Jarvis APK**, pobierz najnowszy artefakt **Jarvis-apk** i zainstaluj nowy plik `Jarvis.apk` na telefonie.
