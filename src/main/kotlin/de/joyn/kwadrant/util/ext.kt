package de.joyn.kwadrant.util

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

typealias DependencySet = Set<Dependency>
typealias ProjectCollection = Collection<Project>

fun not(b: Boolean) = !b

fun Project.shortName() = ":$name"

fun ProjectCollection.shortString() = joinToString(prefix = "", postfix = "", separator = ", ") { it.shortName() }

fun DependencySet.shortString() = map { it.shortId() }

fun Dependency.shortId() = "$group:$name:$version"