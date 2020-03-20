package com.karumi.shot.tasks

import com.karumi.shot.android.Adb
import com.karumi.shot.domain.Config
import com.karumi.shot.reports.{ConsoleReporter, ExecutionReporter}
import com.karumi.shot.screenshots.ScreenshotsSaver
import com.karumi.shot.ui.Console
import com.karumi.shot.{Files, Shot, ShotExtension}
import org.gradle.api.{DefaultTask}
import org.gradle.api.tasks.TaskAction

abstract class ShotTask() extends DefaultTask {

  private val console = new Console
  protected val shot: Shot =
    new Shot(
      new Adb,
      new Files,
      new ScreenshotsSaver,
      console,
      new ExecutionReporter,
      new ConsoleReporter(console)
    )
  protected val shotExtension: ShotExtension =
    getProject.getExtensions.findByType(classOf[ShotExtension])

  setGroup("shot")

}

object ExecuteScreenshotTests {
  val name = "pullShots"
}

class ExecuteScreenshotTests extends ShotTask {

  setDescription(
    "Pulls screenshots from device to be verified")

  @TaskAction
  def executeScreenshotTests(): Unit = {
    val project = getProject
    val recordScreenshots = project.hasProperty("record")
    val printBase64 = project.hasProperty("printBase64")
    val projectFolder = project.getProjectDir.getAbsolutePath
    val projectName = project.getName
    val buildFolder = project.getBuildDir.getAbsolutePath
    val appId = shotExtension.getAppId
    if (recordScreenshots) {
      shot.recordScreenshots(appId, buildFolder, projectFolder, projectName)
    } else {
      shot.verifyScreenshots(appId,
        buildFolder,
        projectFolder,
        project.getName,
        printBase64)
    }
  }
}

object RecordScreenshotTests {
  val name = "recordShots"
}

class RecordScreenshotTests extends ShotTask {

  setDescription(
    "Records the user interface tests screenshots")

  @TaskAction
  def executeScreenshotTests(): Unit = {
    val project = getProject
    val projectFolder = project.getProjectDir.getAbsolutePath
    val projectName = project.getName
    val buildFolder = project.getBuildDir.getAbsolutePath
    val appId = shotExtension.getAppId

    shot.recordScreenshots(appId, buildFolder, projectFolder, projectName)
  }
}


object DownloadScreenshotsTask {
  val name = "downloadScreenshots"
}

class DownloadScreenshotsTask extends ShotTask {

  setDescription(
    "Retrieves the screenshots stored into the Android device where the tests were executed.")

  @TaskAction
  def downloadScreenshots(): Unit = {
    val projectFolder = getProject.getProjectDir.getAbsolutePath
    val appId = shotExtension.getOptionAppId
    shot.downloadScreenshots(projectFolder, appId)
  }
}


object RemoveScreenshotsTask {
  val name = "removeScreenshots"
}

class RemoveScreenshotsTask extends ShotTask {

  setDescription(
    "Removes the screenshots recorded during the tests execution from the Android device where the tests were executed.")

  @TaskAction
  def clearScreenshots(): Unit = {
    val appId = shotExtension.getOptionAppId
    shot.removeScreenshots(appId)
  }
}

object CompareScreenshotTask {
  val name = "shots"
}

class CompareScreenshotTask extends ShotTask {

  setDescription(
    "Runs the comparison tool .")

  @TaskAction
  def compareScreenshots(): Unit = {
    val project = getProject
    val projectFolder = project.getProjectDir.getAbsolutePath
    val diffFolder = projectFolder + Config.differFolder
    val buildFolder = project.getBuildDir.getAbsolutePath

    shot.executeDiffer(diffFolder, projectFolder, buildFolder+Config.reportFolder)
  }
}