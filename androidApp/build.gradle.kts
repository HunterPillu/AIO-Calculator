import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class VerifyReleaseSigningTask : DefaultTask() {
    @get:Input
    abstract val signingConfigured: Property<Boolean>

    @TaskAction
    fun verify() {
        check(signingConfigured.get()) {
            "Release signing is not configured. Supply android.release.* Gradle properties " +
                "or ANDROID_RELEASE_* environment variables through a secure secret store."
        }
    }
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

val releaseStoreFile = providers.gradleProperty("android.release.storeFile")
    .orElse(providers.environmentVariable("ANDROID_RELEASE_STORE_FILE"))
    .orNull
val releaseStorePassword = providers.gradleProperty("android.release.storePassword")
    .orElse(providers.environmentVariable("ANDROID_RELEASE_STORE_PASSWORD"))
    .orNull
val releaseKeyAlias = providers.gradleProperty("android.release.keyAlias")
    .orElse(providers.environmentVariable("ANDROID_RELEASE_KEY_ALIAS"))
    .orNull
val releaseKeyPassword = providers.gradleProperty("android.release.keyPassword")
    .orElse(providers.environmentVariable("ANDROID_RELEASE_KEY_PASSWORD"))
    .orNull
val releaseSigningConfigured = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { !it.isNullOrBlank() }

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "app.prinkal.calculator"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    val configuredVersionName = providers.gradleProperty("app.versionName").orElse("1.0.0").get()
    val configuredVersionCode = providers.gradleProperty("app.versionCode").orElse("1").get().toInt()

    defaultConfig {
        applicationId = "app.prinkal.calculator"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = configuredVersionCode
        versionName = configuredVersionName
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            if (releaseSigningConfigured) {
                storeFile = file(requireNotNull(releaseStoreFile))
                storePassword = requireNotNull(releaseStorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    tasks.register<VerifyReleaseSigningTask>("verifyReleaseSigning") {
        signingConfigured.set(releaseSigningConfigured)
    }

    tasks.matching { task ->
        task.name == "assembleRelease" || task.name == "bundleRelease"
    }.configureEach {
        dependsOn("verifyReleaseSigning")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}