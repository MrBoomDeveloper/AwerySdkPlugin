plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "com.mrboomdev.awery"
version = "1.0.0"

dependencies {
    // Binary builders
    implementation("com.android.tools.build:gradle:8.11.0")
    implementation("org.jetbrains.kotlin.multiplatform:org.jetbrains.kotlin.multiplatform.gradle.plugin:2.2.0")
    
    // Utils
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
}

gradlePlugin {
    plugins {
        create("awery") {
            id = "com.mrboomdev.awery.extension"
            displayName = "Awery Extension Plugin"
            implementationClass = "com.mrboomdev.awery.gradlePlugin.AweryGradlePlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = "extension-gradle-plugin"
            version = version
            from(components["java"])
        }
    }
}