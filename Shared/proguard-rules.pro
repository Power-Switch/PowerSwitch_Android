# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in F:\Android SDK for Android Studio/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontobfuscate

# Lombok
-dontwarn lombok.**

# Logback Android
-keep class ch.qos.** { *; }
-keep class org.slf4j.** { *; }
-keepattributes *Annotation*
-dontwarn ch.qos.logback.core.net.*

# Crashlytics
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

## New rules for EventBus 3.0.x ##
# http://greenrobot.org/eventbus/documentation/proguard/
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# Courier
-keep class me.denley.courier.** { *; }
-dontwarn me.denley.courier.compiler.**
-keep class **$$Delivery { *; }
-keep class **DataMapPackager { *; }
-keepclasseswithmembernames class * {
    @me.denley.courier.* <fields>;
}
-keepclasseswithmembernames class * {
    @me.denley.courier.* <methods>;
}
-keep @me.denley.courier.* public class * { *; }