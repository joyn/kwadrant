package de.joyn.kwadrant.model

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency

typealias DependencyWithIncludingProjects = Pair<Dependency, Set<Project>>
typealias ExternalDependencyWithIncludingProject = Pair<ExternalModuleDependency, Project>
typealias ProjectWithDuplicatedParentProjects = Pair<Project, Set<DefaultProjectDependency>>

class DependencyInspector {

    fun getDuplicatedDependencies(kwadrant: KwadrantInfo):  Set<DependencyWithIncludingProjects> =
        kwadrant.local.libDependencies.fold(mutableSetOf()) { acc, dep ->
            kwadrant.parent.projects.forEach { parent ->
                val localDeps = parent.libDependencies
                if (localDeps.contains(dep)) {
                    acc.putOrAdd(dep to parent.project)
                }
            }
            acc
        }

    fun getVersionMisalignedDependencies(rootProject: Project): Set<ExternalDependencyWithIncludingProject> = with(
        rootProject.allprojects.map {
            it to KwadrantInfo.create(it, rootProject)
        }.map {
            it.first to it.second.local.libDependencies
        }
    ) {
        fold(mutableSetOf()) { acc, d ->
            val (project, localDeps) = d
            val misAlignedDuplicates = localDeps.filter { localD ->
                this.map { it.second }.flatten().any { allD ->
                    allD.group == localD.group && allD.name == localD.name && allD.version != localD.version
                }
            }
            if (misAlignedDuplicates.isNotEmpty()) {
                misAlignedDuplicates.forEach {
                    acc.add((it as ExternalModuleDependency) to project)
                }
            }
            acc
        }
    }

    fun getDuplicatedParents(
        kwadrant: KwadrantInfo,
        rootProject: Project
    ): Set<ProjectWithDuplicatedParentProjects> = kwadrant.run {
        val directParents = kwadrant.local.projectDependencies
        val transitiveParents = kwadrant.parent.projects.map {
            it to KwadrantInfo.create(it.project, rootProject).local.projectDependencies
        }
        transitiveParents.filter { (_, parentParents) ->
            directParents.any {
                parentParents.contains(it)
            }
        }.map { (parent, parentParents) ->
            parent.project to parentParents.filter { directParents.contains(it) }.toSet()
        }.toSet()
    }

    private fun <T, V> MutableSet<Pair<T, Set<V>>>.putOrAdd(v: Pair<T, V>) =
        when (val entry = find { it.first == v.first } ) {
            null -> apply { add(v.first to setOf(v.second)) }
            else -> {
                remove(entry)
                val newSet = entry.second.toMutableSet().apply {
                    add(v.second)
                }
                apply {
                    add(entry.copy(second = newSet))
                }
            }
        }

}