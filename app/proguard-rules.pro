# Android application v2 2.3.3
# The Android application build system will automatically add content to this file,
# including the default rules, when you do a release build. You can use this file
# to override the default rules or to add your own.
# Learn more about the Android application build system at
# http://developer.android.com/tools/building/index.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class * {
#    @android.webkit.JavascriptInterface <methods>;
#}

# Ignore warnings for unused classes that are kept for reflection
-dontwarn com.squareup.okhttp.**
-dontwarn retrofit2.**
-dontwarn okio.**

# Keep classes for Retrofit
-keep class retrofit2.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep interface retrofit2.** { *; }

# Keep annotations used by Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# Keep classes for GSON
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.** { *; }
