package com.mrboomdev.awery.gradlePlugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.android.tools.r8.internal.Zi
import com.android.tools.r8.internal.pr
import com.mrboomdev.awery.gradlePlugin.addDirectoryContent
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files.exists

class AweryGradlePlugin: Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create<AweryGradlePluginExtension>("awery")
        
        // Apply third-party plugins
        project.pluginManager.apply(KotlinMultiplatformPluginWrapper::class)
        project.pluginManager.apply(AppPlugin::class) // Android application
        
        project.afterEvaluate {
            ext.apply { 
                if(!id.isPresent) {
                    throw IllegalArgumentException(
                        "awery.id isn't present!")
                }

                if(!main.isPresent) {
                    throw IllegalArgumentException(
                        "awery.main isn't present!")
                }
                
                if(!nsfw.isPresent) {
                    throw IllegalArgumentException(
                        "awery.nsfw should to be explicitly set!")
                }
                
                name.convention("Awery Extension")
                version.convention(project.version as? String ?: "1.0.0")
                platforms.convention(Constants.DEFAULT_PLATFORMS)
            }
            
            project.extensions.getByType<KotlinMultiplatformExtension>().apply {
                ext.platforms.get().forEach { 
                    it.installKmpTarget(this)
                }

                sourceSets {
                    commonMain.dependencies {
                        implementation(Constants.SDK_DEPENDENCY)
                    }
                }
            }
        }
        
        val generateAndroidFilesTask = project.tasks.register("aweryGenerateAndroidFiles") {
            group = "Awery"
            val androidManifest = BuildPaths.androidManifest(project)
            
            doLast {
                androidManifest.get().asFile.apply {
                    parentFile.mkdirs()
                    createNewFile()
                }.writeText("""
                    <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                        <application 
                            android:testOnly="true"
                            android:label="Awery Extension" />
                    </manifest>
                """.trimIndent())
            }
        }
        
        val zipTargetsTask = project.tasks.register("aweryZipTargets") {
            group = "Awery"
            
            val inputApk = BuildPaths.generatedApk(project)
            val outputZip = BuildPaths.outputExtension(project)
            val manifest = BuildPaths.extensionManifest(project)
            
            val iconFile = project.provider {
                ext.icon.orNull?.let {
                    project.layout.projectDirectory.file(it).asFile
                } ?: run {
                    val dir = project.layout.projectDirectory

                    for(fileType in Constants.SUPPORTED_ICON_TYPES) {
                        for(file in dir.asFile.listFiles() ?: emptyArray()) {
                            if(file.name.lowercase() == "icon.$fileType") {
                                return@run file
                            }
                        }
                    }

                    null
                }?.apply {
                    if(!exists()) {
                        throw FileNotFoundException(
                            "Specified icon was not found at $absolutePath!")
                    }
                }
            }
            
            doLast {
                val main = ext.main.get().let {
                    if(!it.startsWith(".")) {
                        return@let it
                    }
                    
                    ext.id.get() + it
                }
                
                manifest.get().asFile.apply { 
                    createNewFile()
                }.writeText(buildJson(
                    "id" to ext.id.get(),
                    "name" to ext.name.get(),
                    "version" to ext.version.get(),
                    "main" to main,
                    "lang" to ext.lang.orNull,
                    "nsfw" to ext.nsfw.get(),
                    "icon" to if(iconFile.isPresent) "icon.png" else null,
                    "webpage" to ext.webpage.orNull
                ))
                
                outputZip.get().asFile.apply { 
                    parentFile.mkdirs()
                    delete()
                    createNewFile()
                    
                    ZipFile(this).apply {
                        if(ext.platforms.get().contains(Platform.ANDROID)) {
                            addFile(inputApk.get().asFile, ZipParameters().apply {
                                fileNameInZip = "android.apk"
                            })
                        }
                        
                        addFile(manifest.get().asFile, ZipParameters().apply { 
                            fileNameInZip = "manifest.json"
                        })
                        
                        if(iconFile.isPresent) {
                            addFile(iconFile.get(), ZipParameters().apply { 
                                fileNameInZip = "icon.png"
                            })
                        }
                    }
                }
            }
        }
        
        project.tasks.register("aweryBuildExtension") {
            group = "Awery"
            dependsOn(zipTargetsTask)
        }

        project.extensions.getByType<ApplicationAndroidComponentsExtension>().finalizeDsl { android ->
            ext.android {
                targetSdk.convention(Constants.DEFAULT_ANDROID_TARGET_SDK)
                minSdk.convention(Constants.DEFAULT_ANDROID_MIN_SDK)
            }

            ext.jvm {
                version.convention(Constants.DEFAULT_JAVA_VERSION)
            }

            android.apply {
                namespace = "com.mrboomdev.awery.extension.compiled"
                compileSdk = ext.android.targetSdk.get()

                defaultConfig {
                    versionName = ext.version.get()
                    versionCode = 1
                    targetSdk = ext.android.targetSdk.get()
                    minSdk = ext.android.minSdk.get()
                }
                
                buildTypes {
                    debug {}
                }

                sourceSets.getByName("main") {
                    manifest.srcFile(BuildPaths.androidManifest(project))
                }

                buildFeatures {
                    aidl = false
                    buildConfig = false
                    dataBinding = false
                    mlModelBinding = false
                    prefab = false
                    renderScript = false
                    shaders = false
                    viewBinding = false
                    compose = false
                    resValues = false
                }

                compileOptions {
                    sourceCompatibility = ext.jvm.version.get()
                    targetCompatibility = ext.jvm.version.get()
                }
            }
            
            val assemble = project.tasks.getByName("assemble").apply {
                dependsOn(generateAndroidFilesTask)

                if(ext.platforms.get().contains(Platform.ANDROID)) {
                    zipTargetsTask.dependsOn(this)
                }
            }
            
            zipTargetsTask.dependsOn(assemble)
        }
    }
}