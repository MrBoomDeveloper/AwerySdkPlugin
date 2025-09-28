package com.mrboomdev.awery.gradlePlugin

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

enum class Platform {
    ANDROID {
        override fun installKmpTarget(kmpExtension: KotlinMultiplatformExtension) {
            kmpExtension.androidTarget()
        }
    },
    
    JVM {
        override fun installKmpTarget(kmpExtension: KotlinMultiplatformExtension) {
            kmpExtension.jvm()
        }
    },
    
    JS {
        @OptIn(ExperimentalWasmDsl::class)
        override fun installKmpTarget(kmpExtension: KotlinMultiplatformExtension) {
            kmpExtension.js(kmpExtension.IR) {
                browser()
                nodejs()
            }
            
            kmpExtension.wasmJs {
                browser()
                nodejs()
            }
        }
    },
    
    IOS {
        override fun installKmpTarget(kmpExtension: KotlinMultiplatformExtension) {
            listOf(
                kmpExtension.iosX64(),
                kmpExtension.iosArm64(),
                kmpExtension.iosSimulatorArm64()
            ).forEach { iosTarget ->
                iosTarget.binaries.framework {
                    baseName = "aweryBackend"
                    isStatic = true
                }
            }
        }
    };
    
    abstract fun installKmpTarget(kmpExtension: KotlinMultiplatformExtension)
}