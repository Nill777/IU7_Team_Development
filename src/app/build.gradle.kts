import java.time.Duration

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.kbk"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kbk"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    jvmToolchain(21)
}

detekt {
    config.setFrom(files("$rootDir/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = true
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":presentation"))
    implementation(project(":keystroke-sdk"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.room.runtime)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation(libs.core.ktx)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

val avdName = "Pixel_2_API_27"

tasks.register<Exec>("startEmulator") {
    group = "Emulator"
    description = "Starts the Android emulator"

    // Получаем путь к утилите эмулятора из Android SDK
    val emulator = android.sdkDirectory.resolve("emulator/emulator")
    val adb = android.sdkDirectory.resolve("platform-tools/adb")

    doFirst {
        exec {
            commandLine(adb.absolutePath, "kill-server")
            isIgnoreExitValue = true
        }
    }

    println("Starting emulator $avdName...")

//    commandLine(
//        emulator.absolutePath,
//        "-avd", avdName,
//        "-no-snapshot-load", // Гарантирует "холодный", чистый запуск
//        "-no-window",        // Не показывать окно эмулятора
//        "-no-audio",         // Отключить звук
//        "-no-boot-anim"      // Отключить анимацию загрузки для ускорения
//    )

    // Запускаем shell, который, в свою очередь, запускает эмулятор в фоне
    commandLine(
        "sh", "-c",
        // Вся команда передается как одна строка.
        // `&` в конце — это команда шеллу запустить процесс в фоне.
        // `>/dev/null 2>&1` перенаправляет весь вывод в "никуда", чтобы он не засорял лог Gradle.
        "\"${emulator.absolutePath}\" -avd $avdName -no-snapshot-load -no-window -no-audio -no-boot-anim >/dev/null 2>&1 &"
    )
}

tasks.register<Exec>("waitForEmulator") {
    group = "Emulator"
    description = "Waits until the emulator is fully booted and ready"

    dependsOn(tasks.named("startEmulator"))

    val adb = android.sdkDirectory.resolve("platform-tools/adb")

    // Перезапускаем adb сервер с правами root перед ожиданием
    doFirst {
        exec {
            commandLine(adb.absolutePath, "root")
            isIgnoreExitValue = true
            standardOutput = System.out // Показываем вывод для отладки
        }
        exec {
            commandLine(adb.absolutePath, "wait-for-device")
            timeout.set(Duration.ofMinutes(1))
        }
    }

    // Используем 'sh -c' для выполнения цикла ожидания в шелле.
    // Скрипт опрашивает системное свойство 'sys.boot_completed' раз в 2 секунды
    // Как только свойство станет '1', цикл завершится.
    commandLine(
        "sh", "-c",
        "while [[ \"$( \"${adb.absolutePath}\" shell getprop sys.boot_completed | tr -d '\\r' )\" != \"1\" ]] ; do echo 'Waiting for emulator...'; sleep 2; done"
    )

    // Устанавливаем таймаут, чтобы не ждать вечно, если эмулятор не сможет запуститься
    timeout.set(Duration.ofMinutes(5))
}

tasks.register<Exec>("stopEmulator") {
    group = "Emulator"
    description = "Stops the running emulator"

    // Всегда выполняем, даже если тесты упали
//    mustRunAfter(tasks.named("runTestsAndPullReport"))

    val adb = android.sdkDirectory.resolve("platform-tools/adb")

    commandLine(adb.absolutePath, "emu", "kill")
    isIgnoreExitValue = true
}

tasks.register("runE2ETestsOnEmulator") {
    group = "Verification"
    description = "Runs ONLY E2E tests (com.distributedMessenger.e2e) and pulls their results"

    dependsOn(tasks.named("waitForEmulator"))
    dependsOn("installDebug", "installDebugAndroidTest")

    doLast {
        val adb = android.sdkDirectory.resolve("platform-tools/adb")
        val appId = android.defaultConfig.applicationId
        val instrumentationRunner = "${android.defaultConfig.applicationId}.test/${android.defaultConfig.testInstrumentationRunner}"
        val outputDir = project.layout.buildDirectory.get().asFile.resolve("allure-results-e2e")
        val tempTarFile = project.layout.buildDirectory.get().asFile.resolve("tmp/allure-results.tar")

        // Запуск ТОЛЬКО E2E тестов
        println("Running E2E tests...")
        exec {
            commandLine(
                adb.absolutePath, "shell", "am", "instrument", "-w",
//                "-e", "package", "com.distributedMessenger.e2e", // ФИЛЬТР ПО ПАКЕТУ
                instrumentationRunner
            )
            isIgnoreExitValue = true
        }
        println("E2E tests finished.")
    }
}

tasks.register("runE2ETestsWithEmulator") {
    group = "Verification"
    description = "Starts emulator, runs E2E tests and stops emulator"

    // Определяем строгий порядок выполнения
    dependsOn(tasks.named("waitForEmulator"))
    dependsOn(tasks.named("runE2ETestsOnEmulator").get().mustRunAfter(tasks.named("waitForEmulator")))

    // В самом конце, независимо от результата, всегда гасим эмулятор
    finalizedBy(tasks.named("stopEmulator"))
}

// отладка Android тестов
tasks.register<Exec>("debugAndroidTests") {
    group = "Verification"
    description = "Runs all instrumented tests and prints detailed logcat output."

    // Запускаем на уже подключенном/запущенном эмуляторе
    dependsOn("installDebug", "installDebugAndroidTest")

    val adb = android.sdkDirectory.resolve("platform-tools/adb")
    val appId = android.defaultConfig.applicationId
    val instrumentationRunner = "${android.defaultConfig.applicationId}.test/${android.defaultConfig.testInstrumentationRunner}"

    // Сначала очищаем logcat, чтобы видеть только ошибки от нашего запуска
    commandLine(adb.absolutePath, "logcat", "-c")
    doLast {
        println("\n--- RUNNING INSTRUMENTATION ---")
        // Запускаем тесты
        exec {
            commandLine(adb.absolutePath, "shell", "am", "instrument", "-w", instrumentationRunner)
            isIgnoreExitValue = true // Продолжаем, даже если тесты упали
        }

        println("\n--- CAPTURING LOGCAT ---")
        // Сразу после падения теста, выводим полный logcat в консоль
        exec {
            commandLine(adb.absolutePath, "logcat", "-d", "*:E") // "-d" - dump, "*:E" - все ошибки
        }
    }
}