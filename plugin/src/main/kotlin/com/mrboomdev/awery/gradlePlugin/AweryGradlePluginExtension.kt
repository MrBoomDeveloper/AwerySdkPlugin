package com.mrboomdev.awery.gradlePlugin

import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Nested

interface AweryGradlePluginExtension {
    val platforms: SetProperty<Platform>
    val id: Property<String>
    val name: Property<String>
    val main: Property<String>
    val version: Property<String>
    val nsfw: Property<Boolean>
    val lang: Property<String?>
    val icon: Property<String?>
    val webpage: Property<String?>
    
    @get:Nested
    val android: AndroidExtension
    fun android(action: Action<AndroidExtension>) = action.execute(android)
    
    @get:Nested
    val jvm: JvmExtension
    fun jvm(action: Action<JvmExtension>) = action.execute(jvm)
    
    interface AndroidExtension {
        val targetSdk: Property<Int>
        val minSdk: Property<Int>
    }
    
    interface JvmExtension {
        val version: Property<JavaVersion>
    }
}