package com.karumi.shot

import com.karumi.shot.android.Adb
import com.karumi.shot.domain.Config
import com.karumi.shot.reports.{ConsoleReporter, ExecutionReporter}
import com.karumi.shot.screenshots.ScreenshotsSaver
import com.karumi.shot.tasks.{
  DownloadScreenshotsTask,
  PullVerifyScreenshots,
  RecordScreenshotTests,
  RemoveScreenshotsTask,
  CompareScreenshotTask
}
import com.karumi.shot.ui.Console
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

  private val console = new Console
  private lazy val shot: Shot =
    new Shot(
      new Adb,
      new Files,
      new ScreenshotsSaver,
      console,
      new ExecutionReporter,
      new ConsoleReporter(console)
    )

  override def apply(project: Project): Unit = {
    addExtensions(project)
    addAndroidTestDependency(project)
    project.afterEvaluate { project =>
      {
        configureAdb(project)
        addTasks(project)
      }
    }
  }

  private def configureAdb(project: Project): Unit = {
    val adbPath = AdbPathExtractor.extractPath(project)
    shot.configureAdbPath(adbPath)
  }

  private def addTasks(project: Project): Unit = {
    val extension =
      project.getExtensions.getByType[ShotExtension](classOf[ShotExtension])
    val removeScreenshots = project.getTasks
      .create(RemoveScreenshotsTask.name, classOf[RemoveScreenshotsTask])
    val compareScreenshots = project.getTasks
      .create(CompareScreenshotTask.name, classOf[CompareScreenshotTask])
    val downloadScreenshots = project.getTasks
      .create(DownloadScreenshotsTask.name, classOf[DownloadScreenshotsTask])
    val pullVerifyScreenshots = project.getTasks
      .create(PullVerifyScreenshots.name, classOf[PullVerifyScreenshots])
    val recordShots = project.getTasks
        .create(RecordScreenshotTests.name, classOf[RecordScreenshotTests])


//    pullVerifyScreenshots.dependsOn(downloadScreenshots)

    recordShots.dependsOn(downloadScreenshots)

    compareScreenshots.dependsOn(pullVerifyScreenshots)


    //    if (false) {
//      pullVerifyScreenshots.dependsOn(instrumentationTask)
//      pullVerifyScreenshots.dependsOn(removeScreenshots)
//      downloadScreenshots.mustRunAfter(instrumentationTask)
//      removeScreenshots.mustRunAfter(downloadScreenshots)
//    }
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

      override def afterResolve(
          resolvableDependencies: ResolvableDependencies): Unit = {}
    })
  }
}
