package de.joyn.kwadrant

import de.joyn.kwadrant.model.KwadrantInfo
import de.joyn.kwadrant.model.DependencyInspector
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.junit.Assert.assertEquals
import org.junit.Test

class DuplicatedDependenciesTest {

    @Test
    fun `finds duplicated library`() {
        val rootProject = buildProject("root ")

        val commonDep = DefaultExternalModuleDependency("de.joyn", "kwadrant", "0.1")

        val firstLevelChild = buildProject("first_level_child", rootProject).also {
            it.addImplementationDependency(commonDep as Dependency)
        }

        val secondLevelChild = buildProject("second_level_child", rootProject).also {
            it.addImplementationDependency(commonDep)
            it.addProjectImplementationDependency(firstLevelChild)
        }

        val duplicationInfo = DependencyInspector().getDuplicatedDependencies(
            KwadrantInfo.create(secondLevelChild, rootProject)
        )

        assertEquals(1, duplicationInfo.size)
        val (duplicatedDep, duplicatingProjects) = duplicationInfo.toList()[0]
        assertEquals(commonDep, duplicatedDep)
        assertEquals(duplicatingProjects, setOf(firstLevelChild))
    }

}