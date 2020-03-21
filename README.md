
Shots is a [Gradle](https://gradle.org/) plugin that simplifies the execution of screenshot tests using [Screenshot Tests For Android by Facebook](http://facebook.github.io/screenshot-tests-for-android/). It is a fork of the http://github.com/karumi/shot

## What is this?

``Shot`` is a Gradle plugin thought to run screenshot tests for Android using the [screenshot testing Facebook SDK](http://facebook.github.io/screenshot-tests-for-android/).




Record your screenshots executing ``./gradlew recordShots``

And verify your tests executing ``./gradlew shots``

Shot outputs both composite images and a junit report

You can find the complete Facebook SDK documentation [here](https://facebook.github.io/screenshot-tests-for-android/).

## Getting started

Setup the Gradle plugin:

```groovy
  buildscript {
    // ...
    dependencies {
      // ...
      classpath 'com.karumi:shot:3.1.0'
    }
  }
  apply plugin: 'shot'

  shot {
    appId = 'YOUR_APPLICATION_ID'
  }
```

This plugin sets up a few convenience commands you can list executing ``./gradlew tasks`` and reviewing the ``Shot`` associated tasks:

**If you are using flavors update your shot configuration inside the ``build.gradle`` file as follows:**

```groovy
  shot {
    appId = 'YOUR_APPLICATION_ID'
    instrumentationTestTask = 'connected<FlavorName><BuildTypeName>AndroidTest'
  }
```



The screenshots library needs the ``WRITE_EXTERNAL_STORAGE`` permission. When testing a library, add this permission to the manifest of the instrumentation apk. If you are testing an application, add this permission to the app under test. To grant this permission you can create an ``AndroidManifest.xml`` file inside the ``androidTest`` folder. Here is an example:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="<YOUR_APP_ID>.test"
    android:sharedUserId="<YOUR_APP_ID>.uid">

    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

</manifest>
```

**You'll have to add the same ``android:sharedUserId="<YOUR_APP_ID>.uid"`` configuration to your ``app/AndroidManfiest.xml`` file in order to let the testing APK write into the SDCard.**. If you don't do this, you can end up facing a weird error with this message while running your tests:

```
java.lang.RuntimeException: Failed to create the directory /sdcard/screenshots/com.example.snapshottesting.test/screenshots-default for screenshots. Is your sdcard directory read-only?
```

Remember to configure the instrumentation test runner in your ``build.gradle`` as follows:

```groovy
android {
    // ...
    defaultConfig {
        // ...
        testInstrumentationRunner "com.myapp.ScreenshotTestRunner"
    }
    // ...
```

In order to do this, you'll have to create a class named ``ScreenshotTestRunner``, like the following one, inside your instrumentation tests source folder:

```java
public class ScreenshotTestRunner extends AndroidJUnitRunner {

    @Override
    public void onCreate(Bundle args) {
        super.onCreate(args);
        ScreenshotRunner.onCreate(this, args);
    }

    @Override
    public void finish(int resultCode, Bundle results) {
        ScreenshotRunner.onDestroy();
        super.finish(resultCode, results);
    }
}
```

Now you are ready to use the ``Screenshot`` API from your tests:

```java
@Test
public void theActivityIsShownProperly() {
        Activity mainActivity = startMainActivity();
       /*
         * Take the actual screenshot. At the end of this call, the screenshot
         * is stored on the device and the gradle plugin takes care of
         * pulling it and displaying it to you in nice ways.
         */
        Screenshot.snapActivity(activity).record();
}
```

Now you are ready to record and verify your screenshot tests! 

## Recording tests

You can record your screenshot tests executing this command:

```shell
./gradlew recordShots
```

This will execute all your integration tests and it will pull all the generated screenshots into your repository so you can easily add them to the version control system.

## Executing tests

Once you have a bunch of screenshot tests recorded you can easily verify if the behaviour of your app is the correct one executing this command:

```shell
./gradlew shots
```

**After executing your screenshot tests using the Gradle task ``shots`` a report with all your screenshots will be generated.**

![shotTasksHelp](./art/tasksDescription.png)


## CI Reporting

Shot generates a junit xml report you can review at the end of the verification build.
## Running only some tests

You can run a single test or test class, just add the `android.testInstrumentationRunnerArguments.class` parameter within your gradle call. This option works for both modes, verification and recording, just remember to add the `-Precord` if you want to do the latter.

**Running all tests in a class:**

```shell
./gradlew executeScreenshotTests -Pandroid.testInstrumentationRunnerArguments.class=com.your.package.YourClassTest
```

**Running a single test:**

```shell
./gradlew executeScreenshotTests -Pandroid.testInstrumentationRunnerArguments.class=com.your.package.YourClassTest#yourTest
```

## Custom dependencies

If you have included in your project a dependency to related to the dexmaker and you are facing this exception: ``com.android.dx.util.DexException: Multiple dex files define``, you can customize how the facebook SDK is added to your project and exclude the dexmaker library as follows:

 ```groovy
   androidTestCompile ('com.facebook.testing.screenshot:core:0.11.0') {
     exclude group: 'com.crittercism.dexmaker', module: 'dexmaker'
     exclude group: 'com.crittercism.dexmaker', module: 'dexmaker-dx'
   }
 ```
 
The Shot plugin automatically detects if you are including a compatible version of the screenshot facebook library in your project and, if it's present, it will not include it again.
 
**Disclaimer**: The only compatible version of the facebook library is 0.11.0 or any higher version right now, so if you are using any other version we highly encourage to match it with the one Shot is using to avoid problems.

## iOS support

If you want to apply the same testing technique on iOS you can use [Swift Snapshot Testing](https://github.com/pointfreeco/swift-snapshot-testing)

License
-------

    Copyright 2018 Karumi

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
