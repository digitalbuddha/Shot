package com.karumi.shot.domain

import com.karumi.shot.domain.model.{
  FilePath,
  ScreenshotComparisionErrors,
  ScreenshotsSuite
}

object model {
  type ScreenshotsSuite = Seq[Screenshot]
  type FilePath = String
  type Folder = String
  type AppId = String
  type ScreenshotComparisionErrors = Seq[ScreenshotComparisionError]
}

object Config {
  val androidDependencyMode: FilePath = "androidTestImplementation"
  val androidDependencyGroup: String = "com.facebook.testing.screenshot"
  val androidDependencyName: String = "core"
  val androidDependencyVersion: String = "0.11.0"
  val androidDependency: FilePath =
    s"$androidDependencyGroup:$androidDependencyName:$androidDependencyVersion"
  val screenshotsFolderName: FilePath = "/screenshots/"
  val pulledScreenshotsFolder
    : FilePath = screenshotsFolderName + "screenshots-default/"
  val metadataFileName: FilePath = pulledScreenshotsFolder + "metadata.xml"
  val androidPluginName: FilePath = "com.android.application"
  val screenshotsTemporalRootPath: FilePath = "/tmp/shot/screenshot/"
  val defaultInstrumentationTestTask: String = "connectedAndroidTest"
  val defaultPackageTestApkTask: String = "packageDebugAndroidTest"
  val reportFolder: String = "/reports/shot/blink-diff/bin"
  val verificationReportFolder: String = reportFolder + "/verification"
  val recordingReportFolder: String = reportFolder + "/record"
}

case class Screenshot(name: String,
                      recordedScreenshotPath: String,
                      temporalScreenshotPath: String,
                      testClass: String,
                      testName: String,
                      tilesDimension: Dimension,
                      viewHierarchy: FilePath,
                      absoluteFileNames: Seq[FilePath],
                      relativeFileNames: Seq[FilePath],
                      recordedPartsPaths: Seq[FilePath],
                      screenshotDimension: Dimension) {
  val fileName: String =
    temporalScreenshotPath.substring(
      temporalScreenshotPath.lastIndexOf("/") + 1,
      temporalScreenshotPath.length)

  def getDiffScreenshotPath(basePath: String): String =
    s"${basePath}diff_$fileName"

}

case class Dimension(width: Int, height: Int) {
  override def toString: FilePath = width + "x" + height
}

sealed trait ScreenshotComparisionError

case class ScreenshotNotFound(screenshot: Screenshot)
    extends ScreenshotComparisionError

case class DifferentScreenshots(screenshot: Screenshot,
                                base64Diff: Option[String] = None)
    extends ScreenshotComparisionError

case class DifferentImageDimensions(screenshot: Screenshot,
                                    originalDimension: Dimension,
                                    newDimension: Dimension)
    extends ScreenshotComparisionError

case class ScreenshotsComparisionResult(errors: ScreenshotComparisionErrors,
                                        screenshots: ScreenshotsSuite) {
  val hasErrors: Boolean = errors.nonEmpty
}
