package de.joyn.kwadrant

import de.joyn.kwadrant.model.DependencyInspector
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.junit.Assert.assertEquals
import org.junit.Test

class MisalignedDependenciesTest {

    @Test
    fun `finds same libraries with different versions`() {

        val rootProject = buildProject("root")

        val libVersion1 = DefaultExternalModuleDependency("de.joyn", "kwadrant", "0.0.0")
        val child1 = buildProject("child1", rootProject).also {
            it.addImplementationDependency(libVersion1)
        }

        val libVersion2 = DefaultExternalModuleDependency("de.joyn", "kwadrant", "0.0.1")
        val child2 = buildProject("child2", rootProject).also {
            it.addImplementationDependency(libVersion2)
        }

        val result = DependencyInspector().getVersionMisalignedDependencies(rootProject).toList()

        assertEquals(2, result.size)
        val child1Dep = result[0]
        assertEquals(child1, child1Dep.second)
        assertEquals(libVersion1, child1Dep.first)
        val child2Dep = result[1]
        assertEquals(child2, child2Dep.second)
        assertEquals(libVersion2, child2Dep.first)
    }

    @Test
    fun `does not raise same library with same version`() {

        val rootProject = buildProject("root")

        val commonDep = DefaultExternalModuleDependency("de.joyn", "kwadrant", "0.0.0")

        buildProject("child1", rootProject).also {
            it.addImplementationDependency(commonDep)
        }

        buildProject("child2", rootProject).also {
            it.addImplementationDependency(commonDep)
        }

        val misalignedIo = DependencyInspector()
        val result = misalignedIo.getVersionMisalignedDependencies(rootProject).toList()
        assertEquals(0, result.size)
    }

}