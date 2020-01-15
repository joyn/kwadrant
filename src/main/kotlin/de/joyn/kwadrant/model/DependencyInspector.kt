package de.joyn.kwadrant.model

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency

class DependencyInspector {

    fun getDuplicatedDependencies(kwadrant: KwadrantInfo) =
        kwadrant.local.libDependencies.fold(mutableSetOf<Pair<Dependency, Set<Project>>>()) { acc, dep ->
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

    fun getVersionMisalignedDependencies(rootProject: Project) = with(
        rootProject.allprojects.map {
            it to KwadrantInfo.create(it, rootProject)
        }.map {
            it.first to it.second.local.libDependencies
        }
    ) {
        fold(mutableSetOf<Pair<ExternalModuleDependency, Project>>()) { acc, d ->
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

    fun getDuplicatedParents(kwadrant: KwadrantInfo, rootProject: Project) = kwadrant.run {
        val directParents = kwadrant.parent.projects
        val transitiveParents = directParents.map {
            it to KwadrantInfo.create(it, rootProject).parent.projects
        }
        val q = transitiveParents.filter { (_, parentParents) ->
            directParents.any { parentParents.contains(it) }
        }.map { (parent, parentParents) ->
            parent to parentParents.filter { directParents.contains(it) }
        }.toSet()
        q
    }

}