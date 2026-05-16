# Prevent R8 from renaming our Update models
-keep class com.arcanox.taskit.data.remote.model.** { *; }
-keepclassmembers class com.arcanox.taskit.data.remote.model.** { *; }

# Retrofit & Gson rules
-keepattributes Signature, InnerClasses, AnnotationDefault
-keepattributes *Annotation*
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }