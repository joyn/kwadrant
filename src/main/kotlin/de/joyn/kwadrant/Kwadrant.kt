@file:Suppress("unused")

package de.joyn.kwadrant

import de.joyn.kwadrant.tasks.DuplicatedLibrariesTask
import de.joyn.kwadrant.tasks.DuplicatedParentsTask
import de.joyn.kwadrant.tasks.MisalignedDependenciesTask
import de.joyn.kwadrant.tasks.UnusedLibrariesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

private const val KW_UNUSED_LIBRARIES = "kwUnusedLibs"
private const val KW_DUPLICATED_DEPENDENCIES = "kwDuplicatedLibs"
private const val KW_DUPLICATED_PARENTS = "kwDuplicatedParents"
private const val KW_MISALIGNED_DEPENDENCIES = "kwMisalignedDeps"

// TODO filter plugin dependencies
// TODO prettify misaligned string
// TODO verify import checks in xml
// TODO verify annotation checks for imports

class Kwadrant : Plugin<Project> {

    override fun apply(project: Project) {
        project.afterEvaluate { rootProject ->

            if (null != rootProject.parent) {
                throw IllegalStateException("Plugin must only be applied to the root project.")
            }

            rootProject.tasks.create(
                KW_UNUSED_LIBRARIES, UnusedLibrariesTask::class.java
            )

            rootProject.tasks.create(
                KW_DUPLICATED_DEPENDENCIES, DuplicatedLibrariesTask::class.java
            )

            rootProject.tasks.create(
                KW_DUPLICATED_PARENTS, DuplicatedParentsTask::class.java
            )

            rootProject.tasks.create(
                KW_MISALIGNED_DEPENDENCIES, MisalignedDependenciesTask::class.java
            )
        }

    }

}