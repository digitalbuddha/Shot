package com.karumi.shot.android

import com.karumi.shot.domain.model.{AppId, Folder}

import scala.sys.process._

object Adb {
  var adbBinaryPath: String = ""
}

class Adb {
  val differCommand: String = "/blink-diff --output output.png 1.png 4.png --threshold-type percent --threshold .0001 --verbose --h-shift 1 --v-shift 1 --base-dir "

  def devices: List[String] = {
    executeAdbCommandWithResult("devices").split('\n').toList.drop(1).map {
      line =>
        line.split('\t').toList.head
    }
  }

  def pullScreenshots(device: String,
                      screenshotsFolder: Folder,
                      appId: AppId): Unit =
    executeAdbCommandWithResult(
      s"-s $device pull /sdcard/screenshots/$appId.test/screenshots-default/ $screenshotsFolder")

  def clearScreenshots(device: String, appId: AppId): Unit =
    executeAdbCommand(
      s"-s $device shell rm -r /sdcard/screenshots/$appId.test/screenshots-default/")



  def executeDiffer(differDir: String, projectFolder:String) = s"${differDir}${differCommand} ${differDir} --recorded-dir ${projectFolder}/"!


  private def executeAdbCommand(command: String): Int =
    s"${Adb.adbBinaryPath} $command".!

  private def executeAdbCommandWithResult(command: String): String =
    s"${Adb.adbBinaryPath} $command".!!

}
