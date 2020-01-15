package de.joyn.kwadrant

import com.nhaarman.mockitokotlin2.mock
import de.joyn.kwadrant.model.IntegrationType
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder

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

fun Project.addProjectImplementationDependency(projectDependency: Project) = also {
    val dependency = DefaultProjectDependency(
        projectDependency as ProjectInternal, "default", mock{}, true
    )
    it.addImplementationDependency(dependency)
}