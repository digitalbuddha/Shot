package com.karumi.shot.screenshots

import java.io.File

import com.karumi.shot.domain.Config
import com.karumi.shot.domain.model.{Folder, ScreenshotsSuite}
import org.apache.commons.io.FileUtils

class ScreenshotsSaver {

  def saveRecordedScreenshots(projectFolder: Folder,
                              screenshots: ScreenshotsSuite) = {
    deleteOldScreenshots(projectFolder)
    saveScreenshots(screenshots, projectFolder + Config.screenshotsFolderName)
  }

  private def deleteOldScreenshots(projectFolder: Folder) = {
    deleteFolder(projectFolder + Config.screenshotsFolderName)
  }

  private def deleteFolder(path: String): Unit = {
    val folder = new File(path)
    if (folder.exists()) {
      folder.delete()
    }
  }

  private def saveScreenshots(screenshots: ScreenshotsSuite, folder: Folder) = {
    val screenshotsFolder = new File(folder)
    if (!screenshotsFolder.exists()) {
      screenshotsFolder.mkdirs()
    }
    screenshots.par.foreach { screenshot =>
      val outputFile = new File(folder + screenshot.fileName)
      if (!outputFile.exists()) {
        outputFile.createNewFile()
      }
      val image = ScreenshotComposer.composeNewScreenshot(screenshot)
      image.output(outputFile)
    }
  }

}
