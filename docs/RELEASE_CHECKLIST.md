# ESPEAK — чек-лист релиза в Google Play

> Финальный пошаговый план. Отмечай выполненное `[x]`.

## ✅ Уже сделано (автоматически)

- [x] Имя приложения = ESPEAK
- [x] Permissions очищены (нет лишнего CAMERA)
- [x] Backup rules настроены
- [x] ProGuard правила прописаны
- [x] R8 minification + resource shrinking включены
- [x] APK 230 МБ → 15.6 МБ (сжатие word_images)
- [x] release.keystore создан и подключён
- [x] Подпись V2 валидная
- [x] AAB собирается (`gradlew bundleRelease`)
- [x] Privacy Policy шаблон в репо
- [x] Cloudflare Worker готов к деплою
- [x] Firestore rules написаны

## 🔴 Осталось сделать тебе

### Шаг 1. Тест на телефоне (15 мин)
- [ ] Установить `app-release.apk` на телефон
- [ ] Пройти онбординг
- [ ] Проверить AI-чат
- [ ] Открыть 3–4 игры, увидеть что rating растёт
- [ ] Зажать слово в Libros — увидеть перевод
- [ ] Открыть Settings → «Политика конфиденциальности» (откроется браузер)

Если что-то падает — пиши, чиним.

### Шаг 2. Опубликовать Privacy Policy (10 мин)
См. [`docs/PUBLISH_PRIVACY_POLICY.md`](PUBLISH_PRIVACY_POLICY.md)

### Шаг 3. Firebase Console (5 мин)
- [ ] Открой https://console.firebase.google.com/project/spanishapp-35092/authentication/providers
- [ ] Sign-in method → **Anonymous** → **Enable** → **Save**
- [ ] Открой https://console.firebase.google.com/project/spanishapp-35092/firestore
- [ ] Если БД нет — создай (region: `eur3` или ближайший)
- [ ] Rules → скопируй содержимое `backend/firestore.rules` → Publish

### Шаг 4. Cloudflare Worker для Gemini (30 мин)
См. [`backend/cloudflare-worker/README.md`](../backend/cloudflare-worker/README.md)

После деплоя обнови `AiChatRepository.kt` на новый URL (см. README шаги 6.1–6.4).

### Шаг 5. Google Play Console (1–2 часа)

#### 5.1 Создать аккаунт разработчика
- [ ] https://play.google.com/console — единоразовый платёж $25
- [ ] Заполнить профиль разработчика

#### 5.2 Создать приложение
- [ ] **Create app** → название `ESPEAK`
- [ ] Тип: **App** + Free
- [ ] Declarations — заполнить (Developer Program Policies, US export laws)

#### 5.3 Контент-рейтинг
- [ ] Заполнить опросник: образование, нет насилия, нет секс. контента → получишь PEGI 3 / Everyone

#### 5.4 Целевая аудитория
- [ ] Минимальный возраст: 6+ или 12+ (для всех возрастов)
- [ ] Не нацелено на детей (если выбрал 12+)

#### 5.5 Реклама и платежи
- [ ] Реклама: **No** (у нас нет рекламы)
- [ ] In-app purchases: **No**

#### 5.6 Privacy и data safety
- [ ] Privacy Policy URL: `https://samohin13.github.io/SpanishApp/PRIVACY_POLICY`
- [ ] Data safety form — указать что собирается:
  - Personal info: name, email
  - App activity: in-app actions (для рейтинга)
  - Audio: только во время использования (произношение), не покидает устройство

#### 5.7 Store listing
- [ ] **App name:** ESPEAK
- [ ] **Short description** (80 символов): пример → `Учи испанский: карточки, игры, AI-репетитор и рассказы — для русскоговорящих.`
- [ ] **Full description** (4000 символов): расписать про функции
- [ ] **App icon** (512×512 PNG)
- [ ] **Feature graphic** (1024×500 PNG/JPG)
- [ ] **Screenshots** минимум 2, рекомендуется 8 (телефон 16:9 или 9:16, 1080×1920)
  - Главный экран
  - Карточка слова
  - Урок A1
  - Игра Crucigrama
  - AI-чат
  - Профиль с рейтингом
  - Лидерборд
  - Рассказ Libros
- [ ] **Category:** Education

#### 5.8 Загрузка AAB
- [ ] **Production → Create new release**
- [ ] Upload `app/build/outputs/bundle/release/app-release.aab`
- [ ] **Release notes** на 1–2 языках (en + ru): `Первая версия ESPEAK! Карточки, 8 игр, AI-репетитор.`
- [ ] **Save** → **Review release** → **Start rollout**

#### 5.9 Ждать review
- Обычно 2–7 дней. Могут отказать с конкретной причиной — исправляешь и подаёшь снова.

---

## 📦 Команды сборки

| Что нужно | Команда |
|---|---|
| Debug APK для теста | `gradlew assembleDebug` |
| Signed release APK | `gradlew assembleRelease` |
| **Signed AAB для Play** | `gradlew bundleRelease` |
| Очистить кэш | `gradlew clean` |

Файлы появляются в `app/build/outputs/`.

---

## 🆘 Если что-то идёт не так

- **Build падает с SDK location** → проверь `local.properties` без BOM (UTF-8 без BOM).
- **Подпись не валидна** → проверь пути в `keystore.properties` — `storeFile=release.keystore` (относительно корня проекта).
- **Play отклоняет** → читай причину, обычно текст четкий: «add Privacy Policy URL», «target API level too low», и т.п.
- **AAB слишком большой** → запусти `python scripts/compress_word_images.py` если перегенерировал картинки.

---

## 🎯 Финальные размеры

| | Сейчас | Лимит Play |
|---|---|---|
| Release APK | 15.6 МБ | 100 МБ (APK) |
| Release AAB | ~14 МБ | 200 МБ (AAB) |

С запасом.
