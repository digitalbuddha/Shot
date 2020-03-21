package com.karumi.shot

import com.karumi.shot.android.Commander
import com.karumi.shot.domain.Config
import com.karumi.shot.tasks.{
  PullVerifyScreenshots,
  RecordScreenshotTests,
  RemoveScreenshotsTask,
  CompareScreenshotTask
}
import org.gradle.api.artifacts.{
  Dependency,
  DependencyResolutionListener,
  ResolvableDependencies
}
import org.gradle.api.{Plugin, Project}


object ShotPlugin {
  private val minGradleVersionSupportedMajorNumber = 3
  private val minGradleVersionSupportedMinorNumber = 4
}

class ShotPlugin extends Plugin[Project] {

  override def apply(project: Project): Unit = {
    addExtensions(project)
    addAndroidTestDependency(project)
    project.afterEvaluate { project => {
      configureAdb(project)
      addTasks(project)
    }
    }
  }

  private def configureAdb(project: Project): Unit = {
    val adbPath = AdbPathExtractor.extractPath(project)
    Commander.adbBinaryPath = adbPath
  }

  private def addTasks(project: Project): Unit = {

    project.getExtensions.getByType[ShotExtension](classOf[ShotExtension])
    project.getTasks
      .create(RemoveScreenshotsTask.name, classOf[RemoveScreenshotsTask])
    project.getTasks.create(RecordScreenshotTests.name, classOf[RecordScreenshotTests])

    val compareScreenshots = project.getTasks
      .create(CompareScreenshotTask.name, classOf[CompareScreenshotTask])
    val pullVerifyScreenshots = project.getTasks
      .create(PullVerifyScreenshots.name, classOf[PullVerifyScreenshots])

    compareScreenshots.dependsOn(pullVerifyScreenshots)
  }

  private def addExtensions(project: Project): Unit = {
    val name = ShotExtension.name
    project.getExtensions.add(name, new ShotExtension())
  }

  private def addAndroidTestDependency(project: Project): Unit = {

    project.getGradle.addListener(new DependencyResolutionListener() {

      override def beforeResolve(
                                  resolvableDependencies: ResolvableDependencies): Unit = {
        var facebookDependencyHasBeenAdded = false

        project.getConfigurations.forEach(config => {
          facebookDependencyHasBeenAdded |= config.getAllDependencies
            .toArray(new Array[Dependency](0))
            .exists(
              dependency =>
                Config.androidDependencyGroup == dependency.getGroup
                  && Config.androidDependencyName == dependency.getName
                  && Config.androidDependencyVersion == dependency.getVersion)
        })

        if (!facebookDependencyHasBeenAdded) {
          val dependencyMode = Config.androidDependencyMode
          val dependencyName = Config.androidDependency
          val dependenciesHandler = project.getDependencies

          val dependencyToAdd = dependenciesHandler.create(dependencyName)
          Option(project.getPlugins.findPlugin(Config.androidPluginName))
            .map(_ =>
              project.getDependencies.add(dependencyMode, dependencyToAdd))
          project.getGradle.removeListener(this)
        }
      }

      override def afterResolve(resolvableDependencies: ResolvableDependencies): Unit = {}
    })
  }
}
