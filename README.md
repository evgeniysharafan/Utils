# Utils

Utils library for convenient use of the most frequent things in Android development.
It contains [logger](utils/src/main/java/com/evgeniysharafan/utils/L.java), [preferences utils](utils/src/main/java/com/evgeniysharafan/utils/PrefUtils.java), and other such stuff which writes any Android developer.

To init the library you should call Utils.init(this, BuildConfig.DEBUG); in your Application's onCreate() method.

To use logger you don't need a tag, just write L.d("some text"), it will be debug level. L class has methods for all log levels and disables verbose and debug levels for release builds. L adds information about location and able to duplicate all messages to a log file. In our case it will look like this: D/L: [ChatsFragment.onCreateView() : 51]: some text.
L has methods logIntent(Intent intent) and logBundle(Bundle bundle) to print all information about an Intent or a Bundle to debug level.

The library contains [TimeLogger](utils/src/main/java/com/evgeniysharafan/utils/TimeLogger.java) to measure an execution time of some methods. For example you can measure fow long your onBindViewHolder(ViewHolder holder, int position) method works and print the mean value or each measure to L.w() and L.e(). These log levels have been chosen because you don't need to use TimeLogger on a permanent basis, you just need to check places which may have issues with execution time and remove it.

[Toasts](utils/src/main/java/com/evgeniysharafan/utils/Toasts.java) class to show toasts with application's context.

[Res](utils/src/main/java/com/evgeniysharafan/utils/Res.java) class for getting resources with application's context.

[RandomUtils](utils/src/main/java/com/evgeniysharafan/utils/RandomUtils.java) class is useful for making mock objects with hardcoded data.

Examples of usage of this library can be found [here](https://github.com/evgeniysharafan/BaseProject).

-
It contains [DebugSettingsFragment](utils/src/main/java/com/evgeniysharafan/utils/ui/fragment/DebugSettingsFragment.java) which is helpful if you need to get logs or information about device on which your user has issues.

![](screenshots/device-2016-02-15-181258.png?raw=true) ![](screenshots/device-2016-02-15-181309.png?raw=true) ![](screenshots/device-2016-02-15-181324.png?raw=true)

To add it to your project you need to call [DebugSettingsActivity.addDebugSettingsIfNeeded(Activity activity, PreferenceScreen screen, boolean showInRelease, @Nullable String... emailsForSending);](utils/src/main/java/com/evgeniysharafan/utils/ui/activity/DebugSettingsActivity.java) in your settings fragment.

Or you can call [DebugSettingsActivity.launch(Activity activity, boolean writeInRelease, @Nullable String... emailsForSending);](utils/src/main/java/com/evgeniysharafan/utils/ui/activity/DebugSettingsActivity.java) to launch it.

To be able to write logs you need to add a permission:
```xml
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="18" />
```
We don't need such permission since API 19 because on Android 4.4+ we can write to our directory without it. Therefore we don't need to check it on Android 6+.

To be able to attach them to Gmail on Android 6+ devices you need a [FileProvider](http://developer.android.com/intl/ru/reference/android/support/v4/content/FileProvider.html).
Please add this to your AndroidManifest:
```xml
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="your.package.name.logsfileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_provider_paths" />
</provider>
```
Please notice you need to use **your.package.name**.logsfileprovider as authority to make it work, I expect it inside.
We have to use the file provider because of this [issue](https://code.google.com/p/android/issues/detail?id=190239).

-
The main module is utils, the other ones you can add if you need them. I don't want to add dependencies and permissions which can be useless, that's why I divide the library into modules.

```groovy
compile 'com.evgeniysharafan.utils:utils:1.0.12'
```
```groovy
compile 'com.evgeniysharafan.utils:recycler:1.0.12'
compile 'com.evgeniysharafan.utils:picasso:1.0.12'
```
