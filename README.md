# AnalogPickerPlayground

Настраиваемый **аналоговый выбор времени** для Android на Jetpack Compose: циферблат со
стрелками вместо стандартных «крутилок». Стрелки перетаскиваются пальцем, циферблат
полностью кастомизируется — цвета, фон, стрелки, цифры и шрифты, — и есть готовые темы.

Репозиторий состоит из двух модулей:

- `analogtimepicker` — библиотека (публикуемый артефакт);
- `app` — демо-приложение.

## Возможности

- Перетаскивание минутной и часовой стрелок; умный выбор стрелки по близости к её линии.
- Опциональная привязка минут к шагу 5 (компактный чип) и кнопка «выставить текущее время».
- Текст выбранного времени сверху с контрастным к фону цветом (по умолчанию — из темы Material).
- Цифры: арабские, римские, арабо-индийские, только на четвертях, точки или без меток
  (плюс собственная стратегия).
- Фон циферблата: цвет, градиент, картинка (обрезается по кругу) или прозрачный.
- Стрелки: линия (с отдельным цветом для AM/PM) или картинка.
- Произвольный шрифт цифр.
- Готовые темы: Classic, Dark, Minimal, Roman.
- Локализация: все подписи задаются потребителем.

## Требования

- `minSdk` 26, Jetpack Compose (Material 3).

## Подключение

Через [JitPack](https://jitpack.io): добавьте репозиторий и зависимость, указав git-тег
как версию (например, `v0.1.0`).

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
  repositories {
    maven { url = uri("https://jitpack.io") }
  }
}

// build.gradle.kts модуля
dependencies {
  implementation("com.github.titeha:AnalogPickerPlayground:0.1.0")
}
```

JitPack отдаёт репозиторий одним артефактом по координате `com.github.titeha:AnalogPickerPlayground:<тег>`.

Чтобы появилась новая версия: запушьте тег в GitHub
(`git tag 0.1.1 && git push origin 0.1.1`) и откройте сборку на jitpack.io.

## Использование

### Базовый виджет

```kotlin
var time by remember { mutableStateOf(LocalTime.of(7, 30)) }

AnalogTimePicker(
  time = time,
  onTimeChange = { time = it },
)
```

### Диалог

```kotlin
if (showDialog) {
  AnalogTimePickerDialog(
    initialTime = time,
    onTimeSelected = { time = it; showDialog = false },
    onDismiss = { showDialog = false },
  )
}
```

### Только циферблат (ядро без текста и переключателя)

```kotlin
AnalogClockDial(
  time = time,
  onTimeChange = { time = it },
)
```

### Контролы и подписи (локализация)

Под циферблатом — компактные чипы: «5 мин» (привязка минут к шагу 5) и «Сейчас»
(выставить текущее время). Подписи задаёт потребитель — для русского интерфейса:

```kotlin
AnalogTimePicker(
  time = time,
  onTimeChange = { time = it },
  showSnapSwitch = true,        // показывать чип привязки к 5 минутам
  snapTo5Minutes = true,        // начальное состояние привязки
  snapLabel = "5 минут",        // подпись чипа привязки
  showNowButton = true,         // показывать чип «выставить текущее время»
  nowLabel = "Сейчас",          // подпись чипа текущего времени
  showTimeText = true,          // показывать текст выбранного времени сверху
)
```

`AnalogTimePickerDialog` принимает `snapLabel` и `nowLabel` и пробрасывает их в виджет.

## Кастомизация

Всё оформление задаётся через `TimePickerConfig`:

```kotlin
AnalogTimePicker(
  time = time,
  onTimeChange = { time = it },
  config = TimePickerConfig(
    numeralStyle = NumeralStyle.Roman,
    background = DialBackground.Gradient(
      Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF7E57C2)))
    ),
    hourHand = HandShape.Line(
      color = Color(0xFFFFEB3B), colorPm = Color(0xFFFF9800),
      widthPx = 14f, lengthFraction = 0.5f,
    ),
    textStyle = NumeralTextStyle(fontFamily = FontFamily.Serif),
  ),
)
```

**Стили цифр** (`NumeralStyle`): `Arabic`, `Roman`, `ArabicIndic`, `Dots`, `None`,
`QuartersOnly(base, off)`, `Custom { value, position -> NumeralCell }`.

**Фон** (`DialBackground`): `Solid(color)`, `Gradient(brush)`, `Image(painter, alpha)`, `None`.

**Стрелки** (`HandShape`): `Line(color, colorPm, widthPx, lengthFraction, cap)`,
`Image(painter, widthPx, lengthFraction)`. Картинка стрелки должна «смотреть вверх» (на 12 часов).

**Цвета** (`TimePickerColors` в `config.colors`): цвета делений, цифр, центра и текста
времени. `timeTextColor` по умолчанию `Color.Unspecified` — тогда берётся контрастный
`MaterialTheme.colorScheme.onSurface`; задайте явный цвет, чтобы переопределить.

### Готовые темы

```kotlin
AnalogTimePicker(time = time, onTimeChange = { time = it }, config = TimePickerThemes.Dark)
```

Доступны `TimePickerThemes.Classic / Dark / Minimal / Roman`. Любую тему можно донастроить
через `.copy(...)`, например `TimePickerThemes.Roman.copy(radius = 120.dp)`.

## Сборка

```bash
./gradlew :analogtimepicker:testDebugUnitTest assembleDebug
```

Для сборки нужен JDK 17+.
