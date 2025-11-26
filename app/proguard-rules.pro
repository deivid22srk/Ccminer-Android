# Add project specific ProGuard rules here.
-keep class com.verusminer.app.** { *; }
-keepclassmembers class * {
    native <methods>;
}
