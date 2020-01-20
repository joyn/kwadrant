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
            val parentDeps = kwadrant.parent.libDependencies.filter { (_, parentDeps) ->
                parentDeps.contains(dep)
            }.map { (parent, _) ->
                parent
            }.toSet()
            if (parentDeps.isNotEmpty()) {
                acc.add(dep to parentDeps)
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
                this.map { it.second }.flatten().filter { allD ->
                    allD.group == localD.group && allD.name == localD.name && allD.version != localD.version
                }.isNotEmpty()
            }
            if (misAlignedDuplicates.isNotEmpty()) {
                misAlignedDuplicates.forEach {
                    acc.add(it to project)
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
            it to KwadrantInfo.create(it, rootProject).local.projectDependencies
        }
        transitiveParents.filter { (_, parentParents) ->
            directParents.any {
                parentParents.contains(it)
            }
        }.map { (parent, parentParents) ->
            parent to parentParents.filter { directParents.contains(it) }.toSet()
        }.toSet()
    }

}