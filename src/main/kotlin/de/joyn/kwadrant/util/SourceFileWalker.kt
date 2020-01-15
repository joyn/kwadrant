package de.joyn.kwadrant.util

import org.gradle.api.Project
import java.io.File

class SourceFileWalker {

    private companion object {
        const val KOTLIN_FILE_EXTENSION = "kt"
        const val JAVA_FILE_EXTENSION = "java"
        const val XML_FILE_EXTENSION = "xml"

        const val IMPORT_STATEMENT = "import "
        const val ANNOTATION_STATEMENT = "@"
        const val VIEW_ELEMENT_STATEMENT = "<"
        private val VIEW_ELEMENT_START_REGEX = Regex("<([A-Za-z]+)\\.+")

        private val targetFileExtensions = setOf(KOTLIN_FILE_EXTENSION, JAVA_FILE_EXTENSION, XML_FILE_EXTENSION)

        private val annotationImportMap: Map<String, String> = mapOf(
            // TODO add libraries only detectable by annotations (e.g Butterknife)
        )
    }

    fun getImports(project: Project): Set<String> {
        val rootDir = "${project.rootDir}/${project.name}"
        return File(rootDir).walkTopDown()
            .filter { file ->
                file.isFileWithPotentialImports()
            }.map { file ->
                val contents = file.readLines()
                contents.filter { line ->
                    line.isKotlinOrJavaImportStatement() || (line to file).isXmlViewStatement()
                            || line.startsWith(ANNOTATION_STATEMENT)
                }.map { importLine ->
                    when {
                        importLine.isKotlinOrJavaImportStatement() -> importLine.parseKotlinOrJavaImportStatement()
                        (importLine to file).isXmlViewStatement() -> importLine.parseXmlViewStatement()
                        else -> annotationImportMap.entries.filter { (_, statement) ->
                            importLine.startsWith(statement)
                        }.map { (lib, _) -> lib }.firstOrNull() ?: "[invalid]"
                    }
                }
            }.flatten().toSet()
    }

    private fun File.isFileWithPotentialImports() = extension in targetFileExtensions

    private fun String.isKotlinOrJavaImportStatement() = startsWith(IMPORT_STATEMENT)

    private fun Pair<String, File>.isXmlViewStatement() =
        (second.extension == XML_FILE_EXTENSION && VIEW_ELEMENT_START_REGEX.containsMatchIn(first))

    private fun String.parseKotlinOrJavaImportStatement() = split(" ")[1]

    private fun String.parseXmlViewStatement() = substring(indexOf(VIEW_ELEMENT_STATEMENT) + 1).trim()
}