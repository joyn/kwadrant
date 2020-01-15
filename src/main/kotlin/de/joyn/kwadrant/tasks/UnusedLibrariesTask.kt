package de.joyn.kwadrant.tasks

import de.joyn.kwadrant.model.KwadrantInfo
import de.joyn.kwadrant.util.SourceFileWalker
import de.joyn.kwadrant.util.not
import de.joyn.kwadrant.util.shortString
import org.gradle.api.Project

open class UnusedLibrariesTask : KwadrantTask() {

    override val taskName = "Unused Libraries"

    override fun execute() {
        rootProject.allprojects { childProject ->
            getKwadrantForChild(childProject.name)?.let {
                console.putInfo(childProject.name)
                val result = inspectChildProject(childProject, it)
                console.putNormal(result)
                console.putNewLine()
            }
        }
    }

    private fun inspectChildProject(childProject: Project, kwadrant: KwadrantInfo): List<String> {
        val imports = SourceFileWalker().getImports(childProject)
        val libraryDependencies = kwadrant.local.libDependencies
        val unusedLibs = libraryDependencies.filter { library ->
            library.group?.let { libraryGroup ->
                not(imports.any { it.startsWith(libraryGroup) })
            } ?: false
        }.toSet()
        return when (unusedLibs.isNotEmpty()) {
            true -> unusedLibs.shortString()
            false -> listOf("-")
        }
    }


}