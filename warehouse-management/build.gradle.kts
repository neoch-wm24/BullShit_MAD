// 应用插件块，定义了当前模块所使用的 Gradle 插件
plugins {
    alias(libs.plugins.android.library)  // 应用 Android 库插件，适用于创建 Android 库模块
    alias(libs.plugins.kotlin.android)  // 应用 Kotlin Android 插件，支持在 Android 项目中使用 Kotlin 语言
    alias(libs.plugins.kotlin.compose)  // 应用 Kotlin Compose 插件，支持 Jetpack Compose 的使用
}

// Android 配置块，包含了与 Android 构建相关的各种配置选项
android {
    namespace = "com.example.warehouse_management"  // 定义应用的命名空间，通常与包名相同
    compileSdk = 36                                 // 设置编译时使用的 Android SDK 版本

    defaultConfig {                                        // 默认配置块，定义了应用的基本属性
        minSdk = 24                                                             // 设置应用支持的最低 Android SDK 版本

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"   // 指定用于运行仪器测试的测试运行器
        consumerProguardFiles("consumer-rules.pro")              // 指定 ProGuard 规则文件，用于混淆和优化代码
    }

    buildTypes {                                // 构建类型配置块，定义了不同的构建变体
        release {                                                         // 发布版本的构建类型
            isMinifyEnabled = false                                                         // 是否启用代码混淆和优化，通常在发布版本中启用
            proguardFiles(                                                                  // 指定 ProGuard 配置文件
                getDefaultProguardFile("proguard-android-optimize.txt"),    // 使用默认的 ProGuard 配置文件
                "proguard-rules.pro"                                                        // 使用自定义的 ProGuard 规则文件
            )
        }
    }

    compileOptions {                   // Java 编译选项
        sourceCompatibility = JavaVersion.VERSION_11    // 设置 Java 源代码的兼容版本
        targetCompatibility = JavaVersion.VERSION_11    // 设置编译后的 Java 字节码的目标版本
    }

    kotlinOptions {         // Kotlin 编译选项
        jvmTarget = "11"    // 设置 Kotlin 编译器生成的字节码的目标 JVM 版本
    }

    buildFeatures {     // 启用或禁用特定的构建特性
        compose = true                      // 启用 Jetpack Compose 支持
    }

    composeOptions {             // 配置与 Jetpack Compose 相关的选项
        kotlinCompilerExtensionVersion = "1.5.8"    // 设置 Kotlin 编译器扩展的版本
    }
}

// 依赖声明块，定义了当前模块所需的外部库和模块依赖
dependencies {
    implementation(libs.androidx.core.ktx)      // Android 核心库，提供了对 Android API 的 Kotlin 扩展支持
    implementation(libs.androidx.appcompat)     // 提供对旧版 Android 设备的兼容支持
    implementation(libs.material)               // Google 的 Material Design 组件库

    implementation(platform("androidx.compose:compose-bom:2024.02.00"))       // 使用 Compose 的 BOM（Bill of Materials）来管理 Compose 相关库的版本
    implementation("androidx.compose.ui:ui")                                            // Jetpack Compose 的核心 UI 库
    implementation("androidx.compose.material3:material3")                              // Jetpack Compose 的 Material 3 组件库
    implementation("androidx.compose.ui:ui-tooling-preview")                            // 提供预览功能的工具库
    implementation("androidx.activity:activity-compose:1.8.2")                          // 支持在 Activity 中使用 Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")                      // 支持在 Compose 中使用 Navigation 组件进行导航
    implementation(project(":core-ui"))                                                 // 引入项目中的 core-ui 模块，复用其中的 UI 组件和主题
    implementation("androidx.compose.material:material-icons-extended:1.7.5")
    implementation(project(":core-data")) // Added for RakInfo & RakManager

    debugImplementation("androidx.compose.ui:ui-tooling")                               // 提供调试工具的库
    debugImplementation("androidx.compose.ui:ui-test-manifest")                         // 提供测试清单的库

    testImplementation(libs.junit)                                                      // JUnit 测试框架，用于编写和运行单元测试
    androidTestImplementation(libs.androidx.junit)                                      // Android 版本的 JUnit 测试框架
    androidTestImplementation(libs.androidx.espresso.core)                              // Espresso 测试框架，用于编写和运行 UI 测试
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")                     // 支持在 Compose 中使用 JUnit4 进行 UI 测试
}