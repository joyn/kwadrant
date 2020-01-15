package de.joyn.kwadrant.tasks

import de.joyn.kwadrant.model.KwadrantInfo
import de.joyn.kwadrant.util.Console
import de.joyn.kwadrant.model.DependencyInspector
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.text.StyledTextOutputFactory

abstract class KwadrantTask : DefaultTask() {

    protected abstract val taskName: String

    protected lateinit var rootProject: Project

    protected val console = Console(
        services.get(StyledTextOutputFactory::class.java).create(javaClass)
    )

    private val kwadrantsMap: Map<String, KwadrantInfo> by lazy {
        initializeKwadrantMap(rootProject)
    }

    private fun initializeKwadrantMap(rootProject: Project): Map<String, KwadrantInfo> =
        rootProject.childProjects.mapValues { (_, childProject) ->
            KwadrantInfo.create(childProject, rootProject)
        }

    protected val inspector = DependencyInspector()

    @TaskAction
    fun abstractTaskExecution() {
        rootProject = project
        with (console) {
            putNewLine()
            putHeader(taskName)
            putNewLine()
        }
        execute()
    }

    protected fun getKwadrantForChild(childName: String): KwadrantInfo? =
        kwadrantsMap[childName]

    abstract fun execute()

}