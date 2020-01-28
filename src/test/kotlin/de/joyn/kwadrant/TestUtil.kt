package de.joyn.kwadrant

import com.nhaarman.mockitokotlin2.mock
import de.joyn.kwadrant.model.IntegrationType
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.initialization.ProjectAccessListener
import org.gradle.testfixtures.ProjectBuilder

fun buildProject(name: String, parent: Project? = null, libraryDependencies: Set<Dependency> = emptySet(),
                 projectDependencies: Set<Project> = emptySet()): Project =
    buildProject(name, parent).also { project ->
        libraryDependencies.forEach {
            project.addImplementationDependency(it)
        }
        projectDependencies.forEach {
            project.addProjectImplementationDependency(it)
        }
    }

fun buildProject(name: String, parent: Project? = null): Project =
    ProjectBuilder.builder()
        .withName(name)
        .also { builder ->
            parent?.let {
                builder.withParent(it)
            }
        }.build()

fun Project.addImplementationDependency(dependency: Dependency) = also {
    if (!it.configurations.names.contains(IntegrationType.IMPLEMENTATION.directive)) {
        it.configurations.create(IntegrationType.IMPLEMENTATION.directive)
    }
    it.configurations.getByName(IntegrationType.IMPLEMENTATION.directive).dependencies.add(dependency)
}

fun Project.addApiDependency(dependency: Dependency) = also {
    if (!it.configurations.names.contains(IntegrationType.API.directive)) {
        it.configurations.create(IntegrationType.API.directive)
    }
    it.configurations.getByName(IntegrationType.API.directive).dependencies.add(dependency)
}

fun Project.addProjectImplementationDependency(projectDependency: Project) = also {
    val dependency = DefaultProjectDependency(
        projectDependency as ProjectInternal, "default", mock{}, true
    )
    it.addImplementationDependency(dependency)
}

fun Project.addProjectApiDependency(projectDependency: Project) = also {
    val dependency = DefaultProjectDependency(
        projectDependency as ProjectInternal, "default", mock {}, true
    )
    it.addApiDependency(dependency)
}