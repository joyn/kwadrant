package de.joyn.kwadrant.tasks

import de.joyn.kwadrant.util.shortId
import de.joyn.kwadrant.util.shortString

open class DuplicatedLibrariesTask : KwadrantTask() {

    override val taskName = "Duplicated Libraries"

    override fun execute() {
        rootProject.allprojects { childProject ->
            val kwadrant = getKwadrantForChild(childProject.name)
            kwadrant?.let {
                val duplicatedDependencies = inspector.getDuplicatedDependencies(it)

                val result = when (duplicatedDependencies.isNotEmpty()) {
                    true ->
                        duplicatedDependencies.map { (dependency, parents) ->
                            "${dependency.shortId()} is contained already in ${parents.shortString()}"
                        }
                    false -> listOf("-")
                }

                console.putInfo(childProject.name)
                console.putNormal(result)
                console.putNewLine()
            }
        }
    }


}