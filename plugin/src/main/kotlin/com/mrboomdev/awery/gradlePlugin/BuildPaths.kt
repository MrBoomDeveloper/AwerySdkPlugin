package com.mrboomdev.awery.gradlePlugin

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.File

object BuildPaths {
    fun androidManifest(project: Project): Provider<RegularFile> {
        return project.layout.buildDirectory.file("awery/AndroidManifest.xml")
    }

    fun extensionManifest(project: Project): Provider<RegularFile> {
        return project.layout.buildDirectory.file("awery/manifest.json")
    }

    fun outputExtension(project: Project): Provider<RegularFile> {
        return project.layout.buildDirectory.file("awery/extension.awery")
    }

    fun generatedApk(project: Project): Provider<RegularFile> {
        return project.layout.buildDirectory.file("outputs/apk/debug/${project.name}-debug.apk")
    }
}