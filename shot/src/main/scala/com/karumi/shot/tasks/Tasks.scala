package com.karumi.shot.tasks

import com.karumi.shot.android.Commander
import com.karumi.shot.domain.Config
import com.karumi.shot.screenshots.ScreenshotsSaver
import com.karumi.shot.ui.Console
import com.karumi.shot.{Files, Shot, ShotExtension}
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class ShotTask() extends DefaultTask {

  private val console = new Console
  protected val shot: Shot =
    new Shot(
      new Commander,
      new Files,
      new ScreenshotsSaver,
      console
    )
  protected val shotExtension: ShotExtension =
    getProject.getExtensions.findByType(classOf[ShotExtension])

  setGroup("shot")

}

object PullVerifyScreenshots {
  val name = "pullShots"
}

class PullVerifyScreenshots extends ShotTask {

  setDescription(
    "Pulls screenshots from device to be verified")

  @TaskAction
  def pullVerifyScreenshots(): Unit = {
    val project = getProject
    val printBase64 = project.hasProperty("printBase64")
    val projectFolder = project.getProjectDir.getAbsolutePath
    val buildFolder = project.getBuildDir.getAbsolutePath
    val appId = shotExtension.getAppId
      shot.downloadNewScreenshots(appId,
        buildFolder,
        projectFolder,
        project.getName,
        printBase64)
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

    val optionAppId = shotExtension.getOptionAppId
    shot.downloadScreenshots(projectFolder, optionAppId)
    shot.recordScreenshots(appId, buildFolder, projectFolder, projectName)
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