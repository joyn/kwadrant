package de.joyn.kwadrant.model

import de.joyn.kwadrant.buildProject
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.junit.Assert.assertEquals
import org.junit.Test

internal class DependencyInspectorTest {

    @Test
    fun `finds duplicated dependency`() {
        val rootProject = buildProject("root ")
        val commonDependency = DefaultExternalModuleDependency("de.joyn", "kwadrant", "0.1")
        val distinctDependency1 = DefaultExternalModuleDependency("abc.de", "kwadrant", "0.1")
        val distinctDependency2 = DefaultExternalModuleDependency("fgh.ij", "kwadrant", "0.1")

        val firstLevelChild = buildProject("first_level_child", rootProject,
            libraryDependencies = setOf(commonDependency, distinctDependency1)
        )
        val secondLevelChild = buildProject("second_level_child", rootProject,
            libraryDependencies = setOf(commonDependency, distinctDependency2),
            projectDependencies = setOf(firstLevelChild)
        )

        val inspector = DependencyInspector()

        val kwadrantInfo1stChild = KwadrantInfo.create(firstLevelChild, rootProject)
        val duplicationInfo1stChild = inspector.getDuplicatedDependencies(kwadrantInfo1stChild)
        val kwadrantInfo2ndChild = KwadrantInfo.create(secondLevelChild, rootProject)
        val duplicationInfo2ndChild = inspector.getDuplicatedDependencies(kwadrantInfo2ndChild)

        assertEquals(0, duplicationInfo1stChild.size)
        assertEquals(1, duplicationInfo2ndChild.size)
        val (duplicatedDependency, containingProjects) = duplicationInfo2ndChild.first()
        assertEquals(duplicatedDependency, commonDependency)
        assertEquals(1, containingProjects.size)
        val containingProject = containingProjects.first()
        assertEquals(firstLevelChild, containingProject)
    }

    @Test
    fun `find misaligned versions`() {
        val rootProject = buildProject("root")

        val commonLib = DefaultExternalModuleDependency("de.joyn", "kwadrant_alpha", "1")

        val diffLibVersion1 = DefaultExternalModuleDependency("de.joyn", "kwadrant", "0.0.0")
        val child1 = buildProject("child1", rootProject,
            libraryDependencies = setOf(diffLibVersion1, commonLib)
        )

        val diffLibVersion2 = DefaultExternalModuleDependency("de.joyn", "kwadrant", "0.0.1")
        val child2 = buildProject("child2", rootProject,
            libraryDependencies = setOf(diffLibVersion2, commonLib)
        )

        val inspector = DependencyInspector()
        val result = inspector.getVersionMisalignedDependencies(rootProject).toList()

        assertEquals(2, result.size)
        val (resultDiffLib1, resultProjects1) = result[0]
        val (resultDiffLib2, resultProjects2) = result[1]

        assertEquals(diffLibVersion1, resultDiffLib1)
        assertEquals(diffLibVersion2, resultDiffLib2)
        assertEquals(child1, resultProjects1)
        assertEquals(child2, resultProjects2)
    }

    @Test
    fun `find duplicated parents`() {
        val rootProject = buildProject("root")

        val commonParent = buildProject("commonparent", rootProject)
        val distinctParent1 = buildProject("distinctparent1", rootProject)
        val distinctParent2 = buildProject("distinctparent2", rootProject)

        val child1 = buildProject("child1", rootProject,
            projectDependencies = setOf(commonParent, distinctParent1)
        )
        val child2 = buildProject("child2", rootProject,
            projectDependencies = setOf(child1, commonParent, distinctParent2)
        )

        val inspector = DependencyInspector()
        val result = inspector.getDuplicatedParents(
            KwadrantInfo.create(child2, rootProject),rootProject
        ).toList()

        assertEquals(1, result.size)
        val child2DuplicateInfo = result[0]
        val (duplicatingProject, duplicatedParents) = child2DuplicateInfo
        assertEquals(child1, duplicatingProject)
        assertEquals(1, duplicatedParents.size)
        val duplicatedParent = duplicatedParents.first()
        assertEquals(commonParent, duplicatedParent.dependencyProject)
    }

}