package de.joyn.kwadrant.util

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency

typealias DependencySet = Set<Dependency>
typealias ProjectCollection = Collection<Project>
typealias ProjectDependencySet = Set<DefaultProjectDependency>

fun not(b: Boolean) = !b

fun Project.shortName() = ":$name"

fun ProjectCollection.shortString() = joinToString(prefix = "", postfix = "", separator = ", ") { it.shortName() }

fun DependencySet.shortString() = map { it.shortId() }

fun Dependency.shortId() = "$group:$name:$version"
