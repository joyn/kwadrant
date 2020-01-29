package de.joyn.kwadrant.model

import de.joyn.kwadrant.*
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class KwadrantInfoTest {

    @Test
    fun `test plain project`() {
        val root = buildProject("root")
        val target = buildProject("target", root)
        val kwadrant = KwadrantInfo.create(target, root)
        kwadrant.local.let { local ->
            assertTrue(local.projectDependencies.isEmpty())
            assertTrue(local.libDependencies.isEmpty())
            assertTrue(local.apiDependencies.isEmpty())
            assertTrue(local.implDependencies.isEmpty())
        }
        kwadrant.parent.let { parent ->
            val apiDeps = parent.projects.all { it.apiDependencies }
            val libDeps = parent.projects.all { it.libDependencies }
            assertTrue(parent.projects.isEmpty())
            assertTrue(apiDeps.isEmpty())
            assertTrue(libDeps.isEmpty())
        }
    }

    @Test
    fun `test single deps`() {

        val root = buildProject("root")
        val parentProjectImpl = buildProject("impl_prj", root)
        val parentProjectApi = buildProject("api_prj", root)
        val libImplDependency = DefaultExternalModuleDependency("com.joyn.foo", "bar.impl", "1.0")
        val libApiDependency = DefaultExternalModuleDependency("com.joyn.foo", "bar.api", "1.0")

        val target = buildProject("target", root).also {
            it.addImplementationDependency(libImplDependency)
            it.addApiDependency(libApiDependency)
            it.addProjectImplementationDependency(parentProjectImpl)
            it.addProjectApiDependency(parentProjectApi)
        }

        val kwadrant = KwadrantInfo.create(target, root)

        kwadrant.local.let { local ->
            assertTrue(local.projectDependencies.size == 2)
            assertTrue(local.libDependencies.size == 2)
            assertTrue(local.apiDependencies.size == 2)
            assertTrue(local.implDependencies.size == 2)
        }

        kwadrant.parent.let { parent ->
            assertTrue(parent.projects.all { it.apiDependencies }.isEmpty())
            assertTrue(parent.projects.all { it.libDependencies }.isEmpty())
            assertTrue(parent.projects.size == 2)
            val parentProjectList = parent.projects.map { it.project }
            assertEquals(parentProjectApi, parentProjectList[0])
            assertEquals(parentProjectImpl, parentProjectList[1])
        }


    }

    private fun <V, T : V> Set<T>.all(f: (T) -> Set<V>) = map { f(it) }.flatten()

}