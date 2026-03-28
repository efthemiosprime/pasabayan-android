# 18 — Localization (i18n)

**Phase:** 0 (foundation) + **applies to every feature**. All user-visible strings must go through the localization system from day one — never hardcode literals in Kotlin/Compose.

**iOS source of truth:** [`Pasabayan/Localization/`](../../Pasabayan/Localization/) (18 xcstrings files), [`Services/Localization/`](../../Pasabayan/Services/Localization/) (LanguageManager, Bundle swizzling, String extensions).

## Supported languages

| Code | Display name | Status |
|------|-------------|--------|
| `en` | English | Source language |
| `fr` | Français | Full translation |

English is the source/fallback language. Detection order: saved preference → system language (if supported) → English.

## iOS localization architecture

### String catalogs (18 xcstrings files — feature-organized)

| File | Feature | Size |
|------|---------|------|
| `Auth.xcstrings` | Authentication | 10 KB |
| `Bookings.xcstrings` | Bookings/matches | 300 KB |
| `Chat.xcstrings` | Chat | 18 KB |
| `Common.xcstrings` | Shared strings | 72 KB |
| `Dashboard.xcstrings` | Dashboard/shell | 60 KB |
| `Enums.xcstrings` | Enum display names | 33 KB |
| `Legal.xcstrings` | Legal | 5 KB |
| `Notifications.xcstrings` | Notifications | 14 KB |
| `Onboarding.xcstrings` | Onboarding | 41 KB |
| `Packages.xcstrings` | Packages | 147 KB |
| `Payments.xcstrings` | Payments | 68 KB |
| `Profile.xcstrings` | Profile | 53 KB |
| `Ratings.xcstrings` | Ratings | 17 KB |
| `Settings.xcstrings` | Settings | 20 KB |
| `Stats.xcstrings` | Analytics/stats | 1 KB |
| `Tabs.xcstrings` | Tab labels | 3 KB |
| `Trips.xcstrings` | Trips | 151 KB |
| `Verification.xcstrings` | Verification | 27 KB |

**Total:** ~1.1 MB of localization data across 18 feature-organized catalogs.

### String key convention

iOS uses dot-separated hierarchical keys with feature table prefix:

```swift
"bookings.list.title".localized(table: "Bookings")
"common.labels.error".localized(table: "Common")
"bookings.empty.descriptionFiltered".localized(table: "Bookings", with: filterName)
```

### Runtime language switching

iOS supports **runtime language switching without app restart** via:

1. **`LanguageManager`** ([`LanguageManager.swift`](../../Pasabayan/Services/Localization/LanguageManager.swift)) — singleton, `@Published currentLanguage`, persists to `UserDefaults` (key: `"app_language"`)
2. **`Bundle+Language`** ([`Bundle+Language.swift`](../../Pasabayan/Services/Localization/Bundle+Language.swift)) — swizzles `Bundle.main` to load from language-specific `.lproj` at runtime
3. **`String+Localized`** ([`String+Localized.swift`](../../Pasabayan/Services/Localization/String+Localized.swift)) — convenience extensions: `.localized`, `.localized(table:)`, `.localized(table:, with:)`
4. **View refresh** — `.refreshOnLanguageChange(languageManager)` modifier forces recomposition via `.id(currentLanguage)`

### WebView / HTML content localization

27 HTML articles under `Features/Support/Articles/`. French versions use `-fr.html` suffix convention:

```
getting-started.html          → English
getting-started-fr.html       → French
terms-of-service.html         → English
terms-of-service-fr.html      → French
```

WebView component checks `LanguageManager.shared.currentLanguage` and loads the `-fr` variant if French is selected and the file exists, otherwise falls back to the base file.

## Android equivalent architecture

### String resources (mirroring iOS xcstrings)

Android uses `res/values/strings.xml` (English) and `res/values-fr/strings.xml` (French).

**Organization:** Mirror iOS's feature-organized catalogs. Two approaches:

**Option A: Single strings.xml with section comments** (simpler)
```xml
<!-- Auth -->
<string name="auth_login_title">Sign In</string>
<string name="auth_login_google">Continue with Google</string>

<!-- Bookings -->
<string name="bookings_list_title">My Deliveries</string>
<string name="bookings_empty_description_filtered">No %1$s deliveries found</string>
```

**Option B: Separate string resource files per feature** (mirrors iOS better)
```
res/values/strings_auth.xml
res/values/strings_bookings.xml
res/values/strings_common.xml
...
res/values-fr/strings_auth.xml
res/values-fr/strings_bookings.xml
...
```

**Recommendation:** Option B for 1:1 parity — each iOS `.xcstrings` file maps to one Android `strings_<feature>.xml` file. This makes cross-referencing easier.

### String key naming convention

Convert iOS dot-separated keys to Android underscore-separated:

| iOS key | Android name |
|---------|-------------|
| `bookings.list.title` | `bookings_list_title` |
| `common.labels.error` | `common_labels_error` |
| `common.buttons.ok` | `common_buttons_ok` |
| `trips.create.success` | `trips_create_success` |

### Usage in Compose

```kotlin
// Simple string
Text(text = stringResource(R.string.bookings_list_title))

// With arguments (format strings)
Text(text = stringResource(R.string.bookings_empty_description_filtered, filterName))

// In ViewModel (non-Compose context)
context.getString(R.string.common_labels_error)
```

**Rule:** Never use `"hardcoded string"` in composables or ViewModels for user-visible text. Always use `stringResource()` or `context.getString()`.

### `LanguageManager` (runtime language switching)

Create in `:core:common` or `:core:localization`:

```kotlin
@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SharedPreferences
) {
    enum class AppLanguage(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        FRENCH("fr", "Français")
    }

    private val _currentLanguage = MutableStateFlow(loadSavedLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("app_language", language.code).apply()
        _currentLanguage.value = language
        applyLocale(language)
    }

    private fun applyLocale(language: AppLanguage) {
        val locale = Locale(language.code)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        // For API 33+: use AppCompatDelegate.setApplicationLocales()
    }

    private fun loadSavedLanguage(): AppLanguage {
        val saved = prefs.getString("app_language", null)
        if (saved != null) return AppLanguage.entries.find { it.code == saved } ?: AppLanguage.ENGLISH

        // Fall back to system language if supported
        val systemLang = Locale.getDefault().language
        return AppLanguage.entries.find { it.code == systemLang } ?: AppLanguage.ENGLISH
    }
}
```

### Per-app language (Android 13+ / API 33+)

For Android 13+, use **per-app language preferences** via `AppCompatDelegate`:

```kotlin
// Set language
AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.code))

// Get current
val current = AppCompatDelegate.getApplicationLocales()
```

For API < 33, use `context.createConfigurationContext()` with the selected locale, and recreate the activity.

### `locales_config.xml` (declare supported locales)

```xml
<!-- res/xml/locales_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<locale-config xmlns:android="http://schemas.android.com/apk/res/android">
    <locale android:name="en" />
    <locale android:name="fr" />
</locale-config>
```

Reference in `AndroidManifest.xml`:
```xml
<application android:localeConfig="@xml/locales_config" ...>
```

### Persistence

| Key | Type | Notes |
|-----|------|-------|
| `app_language` | String | `"en"` or `"fr"`; mirrors iOS UserDefaults key |

### WebView HTML localization

Mirror iOS approach: bundle HTML articles in `assets/articles/` with `-fr` suffix:

```
assets/articles/getting-started.html
assets/articles/getting-started-fr.html
assets/articles/terms-of-service.html
assets/articles/terms-of-service-fr.html
```

Load in `WebView` composable:
```kotlin
val filename = if (languageManager.currentLanguage == FRENCH) {
    val frenchName = "${baseName}-fr.html"
    if (context.assets.list("articles")?.contains(frenchName) == true) frenchName
    else "${baseName}.html"
} else "${baseName}.html"

webView.loadUrl("file:///android_asset/articles/$filename")
```

### HTML articles to bundle (27 files)

| English file | French file |
|-------------|-------------|
| `accepting-completing-deliveries.html` | `-fr.html` |
| `carrier-payouts-earnings.html` | `-fr.html` |
| `data-instructions.html` | (English only) |
| `getting-started.html` | `-fr.html` |
| `how-to-create-delivery-request.html` | `-fr.html` |
| `how-to-create-trip.html` | `-fr.html` |
| `liability-waiver.html` | `-fr.html` |
| `package-tracking-updates.html` | (English only) |
| `payment-methods-billing.html` | (English only) |
| `privacy-policy.html` | `-fr.html` |
| `safety-guidelines-for-carriers.html` | `-fr.html` |
| `service-agreement.html` | `-fr.html` |
| `switching-roles.html` | `-fr.html` |
| `terms-of-service.html` | `-fr.html` |
| `verifying-your-account.html` | `-fr.html` |

## Mandatory rules

1. **All user-visible strings** must use `stringResource()` in Compose or `context.getString()` in ViewModels — never hardcode.
2. **String resource files** organized per feature (mirror iOS xcstrings catalogs).
3. **Both `values/` and `values-fr/`** must be created for every new string file.
4. **New features** must add strings to both English and French from the start — not "translate later".
5. **Format strings** use Android positional args (`%1$s`, `%2$d`) — verify argument order matches iOS.
6. **Enum display names** go in `strings_enums.xml` (mirrors iOS `Enums.xcstrings`).
7. **Settings** must include a language picker (English / Français) — runtime switching like iOS.
8. **HTML articles** must have both English and French versions where iOS has them.

## Feature string file mapping (iOS → Android)

| iOS xcstrings | Android strings file | Feature module |
|--------------|---------------------|----------------|
| `Auth.xcstrings` | `strings_auth.xml` | `:feature:auth` |
| `Bookings.xcstrings` | `strings_bookings.xml` | `:feature:bookings` |
| `Chat.xcstrings` | `strings_chat.xml` | `:feature:chat` |
| `Common.xcstrings` | `strings_common.xml` | `:core:common` or `:app` |
| `Dashboard.xcstrings` | `strings_dashboard.xml` | `:app` |
| `Enums.xcstrings` | `strings_enums.xml` | `:core:common` |
| `Legal.xcstrings` | `strings_legal.xml` | `:feature:legal` |
| `Notifications.xcstrings` | `strings_notifications.xml` | `:feature:notifications` |
| `Onboarding.xcstrings` | `strings_onboarding.xml` | `:feature:onboarding` |
| `Packages.xcstrings` | `strings_packages.xml` | `:feature:packages` |
| `Payments.xcstrings` | `strings_payments.xml` | `:feature:payments` |
| `Profile.xcstrings` | `strings_profile.xml` | `:feature:profile` |
| `Ratings.xcstrings` | `strings_ratings.xml` | `:feature:ratings` |
| `Settings.xcstrings` | `strings_settings.xml` | `:feature:profile` or `:app` |
| `Stats.xcstrings` | `strings_stats.xml` | `:feature:analytics` |
| `Tabs.xcstrings` | `strings_tabs.xml` | `:app` |
| `Trips.xcstrings` | `strings_trips.xml` | `:feature:trips` |
| `Verification.xcstrings` | `strings_verification.xml` | `:feature:verification` |

## Integration points with other specs

| Spec | Localization impact |
|------|-------------------|
| [14-design-system.md](14-design-system.md) | `PButton` title, `EmptyStateView` text — all via `stringResource()` |
| [17-onboarding.md](17-onboarding.md) | Language picker in onboarding; journey step copy from `Onboarding.xcstrings` |
| [12-legal-support-misc.md](12-legal-support-misc.md) | HTML articles in both languages; legal agreement strings |
| [01-error-taxonomy.md](01-error-taxonomy.md) | `DomainError.userMessage()` should return localized strings |
| [13-ui-tab-explore.md](13-ui-tab-explore.md) | Tab labels from `Tabs.xcstrings` |

## TDD checklist

- [ ] `LanguageManager`: saved preference loads correctly; falls back to system then English.
- [ ] `setLanguage()` updates locale configuration and persists to preferences.
- [ ] `stringResource()` returns French strings when French is active.
- [ ] Format strings: argument substitution works for both languages.
- [ ] WebView: loads `-fr.html` when French, falls back to English when missing.
- [ ] Language picker in Settings: switching language updates all visible text.
- [ ] All feature string files have both `values/` and `values-fr/` versions.
- [ ] `DomainError.userMessage()` returns localized error messages.
- [ ] Tab labels change language on switch.
- [ ] `locales_config.xml` lists both `en` and `fr`.
