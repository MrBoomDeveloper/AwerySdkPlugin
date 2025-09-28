package com.mrboomdev.awery.gradlePlugin

import org.gradle.api.JavaVersion

internal object Constants {
    const val SDK_DEPENDENCY = "ru.mrboomdev.awery:sdk:2.0.0"
    const val DEFAULT_ANDROID_MIN_SDK = 25
    const val DEFAULT_ANDROID_TARGET_SDK = 36
    val DEFAULT_JAVA_VERSION = JavaVersion.VERSION_1_8
    val DEFAULT_PLATFORMS = setOf(Platform.ANDROID)
    
    val SUPPORTED_ICON_TYPES = arrayOf(
        "png", "jpg", "jpeg", "webp", "gif"
    )
}