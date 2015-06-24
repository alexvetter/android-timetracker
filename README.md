# TimeTrackr

First off all, it's my first Android app. It was a project for a course at my university.

## About

With TimeTrackr you can track your working periods on daily bases. You can define your target hours
for each weekday and it will calcuate when you hit your target hours. This can be done manually or
you can register Bluetooth Beacons and the app with automatically add a new working period if you
are in range of one of that Beacons.

## Android

* SDK Version `22`

* Build Tools Version `22.0.1`

* Gradle Build Tools Version `1.2.3`

* Min. SDK Version `17`

* Target SDK Version `22`

## Libraries

* android support libraries `com.android.support:support-v4:22.2.0` & `com.android.support:support-v13:22.2.0`

* android v7 support libraries `com.android.support:appcompat-v7:22.2.0` & `com.android.support:cardview-v7:22.2.0` & `com.android.support:recyclerview-v7:22.2.0`

* swipelayout `com.daimajia.swipelayout:library:1.2.0@aar`

* sublime datetime picker [GitHub Repo Commit 48298aa](https://github.com/vikramkakkar/SublimePicker/tree/48298aa7694392984a3ab211490d7aa4ad81bb46)

* joda-time for android `net.danlew:android.joda:2.8.1@aar`
* joda-time `joda-time:joda-time:2.8.1`

* android beacon library `org.altbeacon:android-beacon-library:2.3@aar`

## Test Libraries

* junit `junit:junit:4.12`
* mockito `org.mockito:mockito-core:1.10.19`

## Screenshots

![Periods overview](/screencapture/shrinked/periods-overview-swipe.png)
![Period edit/create](/screencapture/shrinked/period-detail.png)
![Target hours](/screencapture/shrinked/target-hours.png)
![Registered beacons](/screencapture/shrinked/beacons-registered.png)
![Scan for beacons](/screencapture/shrinked/beacons-scan.png)