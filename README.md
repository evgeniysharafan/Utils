# Utils

Utils library for convenient use of the most frequent things in Android development.
It contains [logger](utils/src/main/java/com/evgeniysharafan/utils/L.java), [preferences utils](utils/src/main/java/com/evgeniysharafan/utils/PrefUtils.java), and other such stuff which writes any Android developer.

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
We don't need such permission since API 19 because on Android 4.4+ we can write to our folder without it. Therefore we don't need to check it on Android 6+.

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
compile 'com.evgeniysharafan.utils:utils:1.0.8'
```
```groovy
compile 'com.evgeniysharafan.utils:recycler:1.0.8'
compile 'com.evgeniysharafan.utils:picasso:1.0.8'
```
