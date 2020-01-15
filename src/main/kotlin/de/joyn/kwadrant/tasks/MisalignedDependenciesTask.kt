package de.joyn.kwadrant.tasks

import de.joyn.kwadrant.util.shortId
import de.joyn.kwadrant.util.shortName

open class MisalignedDependenciesTask : KwadrantTask() {
    override val taskName = "Misaligned Dependencies"

    override fun execute() {
        val misalignedDuplicates = inspector.getVersionMisalignedDependencies(rootProject)
        val result = when (misalignedDuplicates.isNotEmpty()) {
            true -> misalignedDuplicates.map { (dependency, containingProject) ->
                "${dependency.shortId()} is contained in ${containingProject.shortName()}"
            }
            false -> listOf("-")
        }
        console.putNormal(result)
        console.putNewLine()
    }
}