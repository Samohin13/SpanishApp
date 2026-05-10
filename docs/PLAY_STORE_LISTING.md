# Google Play Store — listing guide для ESPEAK

Что нужно подготовить и куда вставлять при заливке в Play Console.

---

## 1. Account / Setup (одноразово)

| Что | Где | Сколько |
|---|---|---|
| Developer account | https://play.google.com/console/signup | $25 разово |
| Bank / payments (для платных фич — но даже бесплатное приложение требует tax setup) | Console → Setup → Payments | 1-2 дня проверки Google |
| Tax info (W-8BEN для не-США) | Console → Setup → Tax | 10 минут |

---

## 2. Store Listing (главный пакет данных)

Console → Главный экран приложения → **Store presence → Main store listing**.

### 2.1 App name
> **ESPEAK — Испанский с нуля**

(До 30 символов. Можно потом A/B-тестить.)

### 2.2 Short description (до 80 символов)
> Учи испанский с играми, карточками и AI-репетитором. Бесплатно.

### 2.3 Full description (до 4000 символов)

```
🇪🇸 ESPEAK — учи испанский легко и интересно

Полнофункциональное приложение для русскоязычных учеников: от первых слов до уровня B2.

📚 ЧТО ВНУТРИ

• Курсы A1 → A2 → B1 → B2 — 75 уроков грамматики, 60 микро-уроков с интерактивом
• 1100+ слов в словаре — с примерами употребления, озвучкой, поиском по русскому/испанскому
• Карточки с алгоритмом интервального повторения SM-2 (как Anki)
• Тематические сеты Daily Sets (99 наборов): приветствия, семья, еда, путешествия, работа, эмоции
• 8 игр для закрепления: Crucigrama, Sopa de Letras, Articulos (el/la), Verbos, Math на слух, Speed-перевод, Anagrams, Palabra Maestra
• 50 рассказов уровня A1 для чтения с переводом по тапу
• Спряжения 159 глаголов во всех временах
• AI-чат на испанском (Gemini) — общайся, задавай вопросы, получай мягкие исправления

🏆 ГЕЙМИФИКАЦИЯ

• Skill Rating + 8 лиг «Путь до Мадрида» (Aldea perdida → Madrid)
• Weekly Leagues — недельные турниры по 30 человек
• Достижения, дневные цели, серия дней
• Лидерборд по странам и миру

🎯 ОСОБЕННОСТИ

• 100% офлайн (всё работает без интернета, кроме AI-чата)
• Бесплатно, без рекламы
• Тёмная тема
• Озвучка через системный TTS (8 голосов на выбор)
• Распознавание речи для тренировки произношения

🔒 ПРИВАТНОСТЬ

• Полный контроль над данными
• Опт-ин участия в лидерборде
• Анонимная авторизация по умолчанию
• Никакой продажи данных третьим лицам

Подходит для школьников, студентов, путешественников и всех, кто хочет освоить язык Сервантеса 🇪🇸
```

### 2.4 Categorization
- **App or game**: App
- **Application type**: Education
- **Category**: Education
- **Tags**: language learning, spanish, vocabulary, grammar

### 2.5 Contact details
- **Email**: es.espeak13@gmail.com
- **Website**: (опционально — можно пустое)
- **Phone**: пустое

### 2.6 Privacy Policy
- **URL**: `https://samohin13.github.io/SpanishApp/PRIVACY_POLICY.html` ✅ (уже работает)

---

## 3. Графические assets — что нужно сделать руками

### 3.1 App icon — 512×512 PNG

**Что**: квадратная иконка 512×512, формат PNG-32 (с альфа-каналом).

**Источник у нас**:
- В проекте: `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` (192×192) и `ic_launcher_foreground.xml`
- Используется бычий силуэт на оранжевом фоне

**Как сделать 512×512**:
- Открой в Android Studio: `app/src/main/res/mipmap` папку → правый клик → **New → Image Asset**
- Выбери **Launcher Icons (Adaptive and Legacy)**
- Source: твой текущий foreground SVG/PNG
- Studio автоматически генерирует все размеры включая 512×512 для Play
- Найдёшь итог в `app/build/intermediates/mipmaps_for_play_store/...` ИЛИ
- Просто экспортни через любой PNG editor (Photoshop, Figma, Photopea онлайн)

Альтернатива — мне сделать через Compose Canvas + рендер в bitmap, но проще через Studio.

### 3.2 Feature graphic — 1024×500 PNG/JPG

**Что**: горизонтальный баннер вверху страницы приложения в Play Store.

**Идея для нашего**:
- Левая половина: оранжевый градиент + большой бычок-логотип + надпись «ESPEAK»
- Правая половина: скриншот главного экрана (с premium тайлами)
- Подзаголовок снизу: «Испанский с играми и AI»

**Где сделать**:
- **Figma** (бесплатно): создай frame 1024×500 → перетащи иконку и скриншот
- **Canva** (бесплатно): шаблон «Feature graphic» уже есть
- **Photopea** (онлайн, как Photoshop)

### 3.3 Screenshots — минимум 2, рекомендую 6-8

**Что**: скриншоты телефона **минимум 320px по короткой стороне, максимум 3840px**, формат PNG/JPG, соотношение от 16:9 до 9:16.

**Самые продающие моменты для скриншотов**:
1. **Главный экран** — Stats Bar + Continue Pager + Word of Day с quiz (показывает геймификацию + контент в одном кадре)
2. **Bento 2×2 на главной** — крупно, с premium-стилем
3. **Карточка во время игры** — например Crucigrama или Sopa de Letras (показывает игровой характер)
4. **AI чат** — пара сообщений с подсветкой испанских слов
5. **Profile с трофеем + Path to Madrid** — геймификация
6. **Курс B2** с цветными блоками — полнота контента
7. **Bottom sheet «Как работает рейтинг»** — глубина системы
8. **Карточка слова в словаре** с примером + аудио — польза

**Как снимать**:
1. На телефоне с подключенной dev-сборкой → пройди до нужного экрана → **Громкость вниз + Power** = скриншот
2. Скриншоты сохранятся в Photos / DCIM / Screenshots
3. Перенеси на компьютер по USB или через Google Photos
4. **НЕ редактируй** — Play Store любит «как-есть» скриншоты, без рамок телефона

**Бонус**: можно добавить «промо-обёртку» — скриншот в рамке Pixel/Galaxy с подписью сверху. Это делается за 2 мин в:
- https://shots.so (бесплатно)
- https://screenshots.pro

### 3.4 Promo video (опционально)
Рекомендую пропустить на v1 — потратишь 4-6 часов на запись/монтаж. Достаточно скриншотов.

---

## 4. App content (compliance — обязательно)

Console → Policy → **App content** — заполни все секции:

### 4.1 Privacy Policy
URL уже есть: `https://samohin13.github.io/SpanishApp/PRIVACY_POLICY.html`

### 4.2 App access
> «All functionality is available without restrictions» (всё бесплатно, без аккаунта)

ИЛИ если у тебя обязательная регистрация:
> «Required login: anonymous Firebase auth, no user input needed»

### 4.3 Ads
> **No ads** (у нас нет рекламы)

### 4.4 Content rating
- Заполни анкету — у нас **PEGI 3 / Everyone**
- Нет насилия, нет крови, нет неприличных слов в контенте

### 4.5 Target audience
- **Age groups**: 13+ (или «13 — Adult» если хочешь обоих)
- Нельзя ставить «Children under 13» — это требует COPPA-compliance, проще исключить

### 4.6 News app
> **No** (это не новостное приложение)

### 4.7 COVID-19 contact tracing
> **No**

### 4.8 Data safety
Заполни **Data safety** форму. Что у нас собирается:
- **Personal info**: Email (опционально, для Google sign-in), Name (display name)
- **App activity**: Page views, In-app actions (Firebase Analytics)
- **App info and performance**: Crash logs (Firebase Crashlytics)
- **Audio**: для распознавания речи (НЕ хранится)

Все эти данные:
- ✅ Encrypted in transit (yes)
- ✅ User can request deletion (через email или Settings → Удалить аккаунт)
- ❌ Not sold to third parties

### 4.9 Government apps
> **No**

### 4.10 Financial features
> **No**

---

## 5. Pricing & distribution

### 5.1 Price
> **Free**

### 5.2 Countries
- Минимум: Россия + СНГ (RU, BY, KZ, UA, KG, UZ)
- Можно сразу все доступные — никто не запрещает

### 5.3 Device categories
- **Phone** ✅
- **Tablet** ✅ (адаптивный layout уже есть)
- **Wear OS / TV / Auto / ChromeOS** — **No** (мы не оптимизировали)

---

## 6. Releases — заливка AAB

Console → Test and release → **Production** (или сначала **Internal testing**).

### 6.1 Рекомендую начать с Internal Testing
- Создай Internal track
- Залей AAB
- Добавь себя как тестера (свой Google email)
- Установи через спец-ссылку и протестируй ВСЁ
- Если ок — продвинь в Production

### 6.2 Загрузка AAB
- **App bundle**: `app/build/outputs/bundle/release/app-release.aab`
- Drag-and-drop в форму
- **Release notes** (для пользователей):
```
🎉 Первый публичный релиз ESPEAK!

• 75 уроков грамматики A1-B2
• 1100+ слов с озвучкой
• 8 мини-игр + рассказы для чтения
• AI-репетитор Gemini
• Геймификация: лиги, ачивки, недельные турниры
• 100% офлайн (кроме AI-чата)

Спасибо что попробовали!
```

### 6.3 First-time submission
Google Play проверит первый релиз вручную — обычно **1-3 дня**, иногда до 7. Если что-то не так — пришлют письмо со списком замечаний.

---

## 7. Quick checklist перед submit

- [ ] App icon 512×512 загружен
- [ ] Feature graphic 1024×500 загружен
- [ ] Минимум 2 скриншота (рекомендую 6-8)
- [ ] Full description (до 4000 символов)
- [ ] Short description (до 80 символов)
- [ ] Categorization: Education
- [ ] Contact email: es.espeak13@gmail.com
- [ ] Privacy Policy URL
- [ ] App content: все секции заполнены
- [ ] Data safety форма заполнена
- [ ] Content rating анкета пройдена
- [ ] Target audience: 13+
- [ ] Pricing: Free
- [ ] Countries выбраны
- [ ] AAB залит в Internal/Production track
- [ ] Release notes написаны

---

## 8. Что я уже подготовил для тебя

- ✅ Privacy Policy на публичном URL
- ✅ Email для контактов: `es.espeak13@gmail.com`
- ✅ Описание (выше)
- ✅ Release notes (выше)
- ✅ Data safety guidelines (выше)
- ✅ AAB сборка идёт в фоне → готовый файл будет в `app/build/outputs/bundle/release/app-release.aab`

## 9. Что тебе делать руками

1. Зарегистрироваться в Play Console ($25 разово)
2. Сделать иконку 512×512 (через Studio Image Asset)
3. Сделать feature graphic 1024×500 (Figma/Canva, 30 минут)
4. Снять 6-8 скриншотов с телефона
5. Создать Internal Test → залить AAB → потестить
6. Submit to Production

Любой пункт могу детальнее разобрать если непонятно.
