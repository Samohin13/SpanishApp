# Как опубликовать Privacy Policy на GitHub Pages (10 минут)

Google Play требует **публичный URL** на политику конфиденциальности. Самый простой способ — GitHub Pages (бесплатно, навсегда).

## Шаги

### 1. Открой репозиторий на GitHub

https://github.com/Samohin13/SpanishApp

### 2. Settings → Pages

В шапке репо → **Settings** (шестерёнка) → в левом меню **Pages**.

### 3. Включи Pages

В блоке **Build and deployment**:
- **Source:** `Deploy from a branch`
- **Branch:** `master`, папка `/ (root)`
- Нажми **Save**.

GitHub запустит сборку (1–2 минуты). После этого страница покажет:
> Your site is live at https://samohin13.github.io/SpanishApp/

### 4. Проверь URL

Открой в браузере:
```
https://samohin13.github.io/SpanishApp/PRIVACY_POLICY
```

Должна открыться твоя политика. Если 404 — подожди 2 минуты и обнови.

### 5. Замени email и URL внутри политики

Открой `PRIVACY_POLICY.md` в Android Studio:
- Замени все `samohin13@example.com` на твой реальный email.
- Замени дату на актуальную.

### 6. Обнови ссылку в приложении

Открой `app/src/main/java/com/spanishapp/ui/settings/SettingsScreen.kt`, найди:
```kotlin
Uri.parse("https://github.com/Samohin13/SpanishApp/blob/master/PRIVACY_POLICY.md")
```
Замени на:
```kotlin
Uri.parse("https://samohin13.github.io/SpanishApp/PRIVACY_POLICY")
```
(в **двух местах** — для «Политики» и «Условий»).

### 7. Закоммить изменения

```
git add PRIVACY_POLICY.md app/src/main/java/com/spanishapp/ui/settings/SettingsScreen.kt
git commit -m "docs: publish privacy policy URL, replace contact email"
git push origin master
```

GitHub Pages автоматически пересоберёт страницу (1 минута).

### 8. В Google Play Console

Когда дойдёшь до загрузки приложения:
- **Store listing → Privacy Policy** → вставить:
  ```
  https://samohin13.github.io/SpanishApp/PRIVACY_POLICY
  ```

Готово.

---

## Альтернатива: Notion публичная страница

Если не хочешь GitHub Pages:

1. Открой Notion, создай новую страницу.
2. Вставь содержимое `PRIVACY_POLICY.md` (Notion сам распарсит markdown).
3. В правом верхнем углу нажми **Share → Publish to web**.
4. Скопируй полученный URL.
5. Используй его в Settings и Play Console.

---

## Что важно

- Privacy Policy **должна быть публичной** — Google Play проверяет.
- Если URL отдаст 404 во время review → отказ.
- Если изменишь содержимое — старый URL должен работать (не удалять страницу).
- Email в политике — **реальный**, иначе пользователи не смогут связаться по GDPR-запросам.
