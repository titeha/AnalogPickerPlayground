# AnalogPickerPlayground

Настраиваемый **аналоговый выбор времени** для Android на Jetpack Compose: циферблат со
стрелками вместо стандартных «крутилок». Стрелки перетаскиваются пальцем, циферблат
полностью кастомизируется — цвета, фон, стрелки, цифры и шрифты, — и есть готовые темы.

Репозиторий состоит из двух модулей:

- `analogtimepicker` — библиотека (публикуемый артефакт);
- `app` — демо-приложение.

## Возможности

- Перетаскивание минутной и часовой стрелок; умный выбор стрелки по близости к её линии.
- Опциональная привязка минут к шагу 5.
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
  implementation("com.github.titeha.AnalogPickerPlayground:analogtimepicker:v0.1.0")
}
```

Чтобы версия появилась на JitPack: запушьте тег в GitHub
(`git tag v0.1.0 && git push origin v0.1.0`) и откройте сборку на jitpack.io.

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
