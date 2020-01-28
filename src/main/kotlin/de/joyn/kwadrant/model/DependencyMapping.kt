package de.joyn.kwadrant.model

import de.joyn.kwadrant.util.DependencySet
import de.joyn.kwadrant.util.ProjectDependencySet
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency

enum class IntegrationType(val directive: String) {
    API("api"),
    IMPLEMENTATION("implementation")
}

enum class ArtifactType {
    PROJECT, LIB
}

data class KwadrantInfo(val project: Project, val local: Local, val parent: Parent) {

    data class Local(
        private val integrationDependencies: Map<IntegrationType, DependencySet>,
        private val artifactDependencies: Map<ArtifactType, DependencySet>
    ) {
        val apiDependencies: DependencySet = integrationDependencies[IntegrationType.API] ?: emptySet()
        val implDependencies: DependencySet = integrationDependencies[IntegrationType.IMPLEMENTATION] ?: emptySet()
        val projectDependencies: ProjectDependencySet =
            artifactDependencies[ArtifactType.PROJECT]?.filterIsInstance(DefaultProjectDependency::class.java)?.toSet()
                ?: emptySet()
        val libDependencies: DependencySet = artifactDependencies[ArtifactType.LIB] ?: emptySet()
    }

    data class Parent(
        val projects: Set<ParentProject>
        // val apiDependencies: Set<ProjectWithDependencies>,
        // val implDependencies: Set<ProjectWithDependencies>,
        // val projectDependencies: Set<Pair<Project, Set<DefaultProjectDependency>>>,
        // val libDependencies: Set<Pair<Project, Set<DefaultExternalModuleDependency>>>,
        // private val integrationDependencies: Map<IntegrationType, Set<ProjectWithDependencies>>,
        // private val artifactDependencies: Map<ArtifactType, Set<ProjectWithDependencies>>
    )

    data class ParentProject(
        val project: Project,
        private val integrationDependencies: Map<IntegrationType, DependencySet>,
        private val artifactDependencies: Map<ArtifactType, DependencySet>
    ) {
        val apiDependencies: DependencySet = integrationDependencies[IntegrationType.API] ?: emptySet()
        val implDependencies: DependencySet = integrationDependencies[IntegrationType.IMPLEMENTATION] ?: emptySet()
        val projectDependencies: DependencySet = artifactDependencies[ArtifactType.PROJECT] ?: emptySet()
        val libDependencies: DependencySet = artifactDependencies[ArtifactType.LIB] ?: emptySet()
    }

    companion object {
        fun create(project: Project, rootProject: Project) =
            (LocalMapper(project) to ParentMapper(project, rootProject)).let { (localMapper, parentMapper) ->
                KwadrantInfo(
                    project,
                    Local(
                        integrationDependencies = localMapper.integrationDependencies,
                        artifactDependencies = localMapper.artifactDependencies
                    ),
                    Parent(projects = parentMapper.parents)
                )
            }
    }

}

private class LocalMapper(project: Project) {

    val integrationDependencies: Map<IntegrationType, DependencySet>
    val artifactDependencies: Map<ArtifactType, DependencySet>

    val apiDependencies: DependencySet
    val implDependencies: DependencySet
    val projectDependencies: Set<DefaultProjectDependency>
    val libDependencies: Set<DefaultExternalModuleDependency>

    init {
        integrationDependencies = mutableMapOf<IntegrationType, Set<Dependency>>().apply {
            put(IntegrationType.API, project.getDepsByIntType(IntegrationType.API))
            put(IntegrationType.IMPLEMENTATION, project.getDepsByIntType(IntegrationType.IMPLEMENTATION))
        }
        artifactDependencies = mutableMapOf<ArtifactType, Set<Dependency>>().apply {
            put(ArtifactType.PROJECT, project.getDepsByArtifact(ArtifactType.PROJECT))
            put(ArtifactType.LIB, project.getDepsByArtifact(ArtifactType.LIB))
        }
        apiDependencies = integrationDependencies[IntegrationType.API] ?: emptySet()
        implDependencies = integrationDependencies[IntegrationType.IMPLEMENTATION] ?: emptySet()
        @Suppress("UNCHECKED_CAST")
        projectDependencies = artifactDependencies[ArtifactType.PROJECT] as Set<DefaultProjectDependency>
        @Suppress("UNCHECKED_CAST")
        libDependencies = artifactDependencies[ArtifactType.LIB] as Set<DefaultExternalModuleDependency>
    }

}

private class ParentMapper(project: Project, rootProject: Project) {

    val parents: Set<KwadrantInfo.ParentProject>

    init {
        parents = getParentDepsTransitively(project, rootProject).map { p ->
            val integrationDependencies = mutableMapOf<IntegrationType, DependencySet>().apply {
                put(IntegrationType.API, p.getDepsByIntType(IntegrationType.API))
                put(IntegrationType.IMPLEMENTATION, p.getDepsByIntType(IntegrationType.IMPLEMENTATION))
            }.toMap()
            val artifactDependencies = mutableMapOf<ArtifactType, Set<Dependency>>().apply {
                put(ArtifactType.PROJECT, p.getDepsByArtifact(ArtifactType.PROJECT))
                put(ArtifactType.LIB, p.getDepsByArtifact(ArtifactType.LIB))
            }
            KwadrantInfo.ParentProject(
                project = p,
                integrationDependencies = integrationDependencies,
                artifactDependencies = artifactDependencies
            )
        }.toSet()
    }

}

/*
private class ParentMapper(project: Project, rootProject: Project) {

    val integrationDependenciesParents: Map<IntegrationType, Set<Pair<Project, DependencySet>>>
    val artifactDependenciesParents: Map<ArtifactType, Set<Pair<Project, DependencySet>>>

    val apiDependenciesParents: Set<ProjectWithDependencies>
    val implDependenciesParents: Set<ProjectWithDependencies>
    val projectDependenciesParents: Set<Pair<Project, Set<DefaultProjectDependency>>>
    val libDependenciesParents: Set<Pair<Project, Set<DefaultExternalModuleDependency>>>

    val parentProjects: Set<Project> = getParentDepsTransitively(project, rootProject)

    init {
        integrationDependenciesParents = mutableMapOf<IntegrationType, Set<ProjectWithDependencies>>().apply {
            put(
                IntegrationType.API,
                parentProjects.map { it to it.getDepsByIntType(IntegrationType.API) }.toSet()
            )
            put(
                IntegrationType.IMPLEMENTATION,
                parentProjects.map { it to it.getDepsByIntType(IntegrationType.IMPLEMENTATION) }.toSet()
            )
        }
        artifactDependenciesParents = mutableMapOf<ArtifactType, Set<Pair<Project, Set<Dependency>>>>().apply {
            put(
                ArtifactType.PROJECT,
                parentProjects.map { it to it.getDepsByArtifact(ArtifactType.PROJECT) }.toSet()
            )
            put(
                ArtifactType.LIB,
                parentProjects.map { it to it.getDepsByArtifact(ArtifactType.LIB) }.toSet()
            )
        }
        apiDependenciesParents = integrationDependenciesParents[IntegrationType.API] ?: emptySet()
        implDependenciesParents = integrationDependenciesParents[IntegrationType.IMPLEMENTATION] ?: emptySet()
        @Suppress("UNCHECKED_CAST")
        projectDependenciesParents = artifactDependenciesParents[ArtifactType.PROJECT]
                as Set<Pair<Project, Set<DefaultProjectDependency>>>
        @Suppress("UNCHECKED_CAST")
        libDependenciesParents = artifactDependenciesParents[ArtifactType.LIB]
                as Set<Pair<Project, Set<DefaultExternalModuleDependency>>>
    }
}
 */

private fun Project.getDepsByIntType(integrationType: IntegrationType) =
    with(integrationType.directive) {
        when (configurations.names.contains(this)) {
            true -> configurations.getByName(this).dependencies
            false -> emptySet<Dependency>()
        }
    }

private fun Project.getDepsByArtifact(artifactType: ArtifactType) =
    (getDepsByIntType(IntegrationType.API).filterByArtifactType(artifactType) +
            getDepsByIntType(IntegrationType.IMPLEMENTATION).filterByArtifactType(artifactType)).toSet()

private fun Set<Dependency>.filterByArtifactType(artifactType: ArtifactType) =
    filterIsInstance(
        when (artifactType) {
            ArtifactType.PROJECT -> DefaultProjectDependency::class.java
            ArtifactType.LIB -> DefaultExternalModuleDependency::class.java
        }
    )

private fun getParentDepsTransitively(prj: Project, root: Project, acc: Set<Project> = mutableSetOf()): Set<Project> =
    with(prj.getDepsByArtifact(ArtifactType.PROJECT)) {
        val directProjectDeps = this
        when (isEmpty()) {
            true -> acc
            false -> {
                val directParentPrjs = root.allprojects.filter { it.name in directProjectDeps.map { p -> p.name } }
                directParentPrjs.map {
                    getParentDepsTransitively(it, root, acc.addAndReturn(directParentPrjs))
                }.flatten().toSet()
            }
        }
    }

private fun <T> Set<T>.addAndReturn(others: Collection<T>): Set<T> = mutableSetOf<T>().apply {
    addAll(this@addAndReturn)
    addAll(others)
}
