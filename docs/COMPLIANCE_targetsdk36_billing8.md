# Комплаенс Google Play: targetSdk 36 + Billing 8 (ветка `claude/compliance-v2`)

> **Зачем:** после **31 августа 2026** Google Play не примет ни одного обновления
> приложения без `targetSdk 36` и Play Billing Library **8**. Эта ветка их вносит.
> **НЕ мержить в master, пока не пройден device-тест покупок** (ниже).

## Что уже сделано в коде (ветка `claude/compliance-v2`)
- `app/build.gradle.kts`: `compileSdk = 36`, `targetSdk = 36`.
- `gradle/libs.versions.toml`: `billing = "8.0.0"` (было 7.1.1).
- `gradle.properties`: `android.suppressUnsupportedCompileSdk=36` (AGP 8.7.3
  протестирован до compileSdk 35 — предупреждение подавлено, сборка работает).
- **Код `PlayBillingManager` менять НЕ пришлось** — он использует KTX-suspend API
  (`queryProductDetails` / `queryPurchasesAsync` / `acknowledgePurchase`), который
  в Billing 8 совместим. Breaking change Billing 8 (split fetched/unfetched в
  callback `queryProductDetailsAsync`) касается только кода на listener-API,
  которого здесь нет. Компиляция чистая, billing-warnings нет.

## ⚠️ Что ОБЯЗАТЕЛЬНО проверить перед мержем/релизом (device-тест)
Billing нельзя релизить без реальной проверки покупки — иначе риск сломать оплату.

1. **Собрать AAB с этой ветки**, поднять versionCode выше live и залить в
   **Internal testing** трек Play Console.
2. На реальном телефоне (тестовый аккаунт-покупатель):
   - [ ] Paywall открывается, показывает цены (queryProducts вернул продукты).
   - [ ] **Покупка PRO проходит** (launchBillingFlow → успех → PRO активируется).
   - [ ] **Восстановление покупок** (restore) работает после переустановки.
   - [ ] Acknowledge проходит (покупка не откатывается через 3 дня).
   - [ ] Если продукт не отдался — проверить, что `queryProducts` не падает и
         paywall не пустой (Billing 8 теперь возвращает «unfetched» отдельно).
3. **AGP:** если на compileSdk 36 всплывут проблемы сборки/рантайма — бампнуть
   AGP до 8.9+ (проверить совместимость с Gradle 9.4.1) и убрать
   `suppressUnsupportedCompileSdk`.
4. **targetSdk 36 поведение (Android 16):** прогнать smoke на устройстве с
   Android 16 (edge-to-edge, permissions, foreground service, уведомления).

## После успешного теста
- Поднять `versionCode`/`versionName` (это релиз ~1.27.0, ПОСЛЕ 1.26.1).
- Merge `claude/compliance-v2` → master → собрать релизный AAB → Production.

## Дедлайн
31 авг 2026. Начинать device-тест заранее — узкое место не код, а календарное
время на реальную тестовую покупку в Internal testing.
