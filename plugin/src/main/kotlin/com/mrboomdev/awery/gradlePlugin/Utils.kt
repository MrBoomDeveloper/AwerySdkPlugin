package com.mrboomdev.awery.gradlePlugin

import com.android.tools.r8.internal.re
import com.android.tools.r8.internal.va
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import java.io.File

fun File.returnIfExists(): File? {
    return if(exists()) this else null
}

fun ZipFile.addDirectoryContent(
    zipDirectory: String = "/",
    directory: File
) {
    for(file in directory.listFiles()) {
        if(file.isDirectory) {
            addDirectoryContent(file.name, file)
            continue
        }
        
        addFile(file, ZipParameters().apply { 
            fileNameInZip = zipDirectory + file.name
        })
    }
}

fun buildJson(vararg values: Pair<String?, Any?>): String {
    return buildJsonObject { 
        for((key, value) in values) {
            if(key != null && value != null) {
                when(value) {
                    is String -> put(key, value)
                    is Boolean -> put(key, value)
                    is Number -> put(key, value)
                    
                    else -> throw IllegalArgumentException(
                        "Invalid json value type! ${value::class.qualifiedName}")
                }
            }
        }
    }.toString()
}