package de.joyn.kwadrant.tasks

import de.joyn.kwadrant.util.shortName
import de.joyn.kwadrant.util.shortString

open class DuplicatedParentsTask : KwadrantTask() {

    override val taskName = "Duplicated Parents"

    override fun execute() {
        rootProject.allprojects { childProject ->

            val kwadrant = getKwadrantForChild(childProject.name)

            kwadrant?.let {
                val duplicatedParents = inspector.getDuplicatedParents(it, rootProject)
                val result = when (duplicatedParents.isNotEmpty()) {
                    true -> duplicatedParents.map { (parent, duplicatedParents) ->
                        "${parent.shortName()} is already contained in ${duplicatedParents.shortString()}"
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