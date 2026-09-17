# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.pogotcghelper.app.**$$serializer { *; }
-keepclassmembers class com.pogotcghelper.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.pogotcghelper.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
