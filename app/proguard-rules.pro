# Hilt rules
-keep public class * extends android.app.Service
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View

-keep class com.google.dagger.hilt.** { *; }
-keep class dagger.hilt.** { *; }

# Room rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Compose rules
-keepclassmembers class * extends androidx.compose.ui.node.Owner {
   public *** get*();
   public void set*(***);
}
-keepclassmembernames class androidx.compose.ui.platform.AndroidComposeView {
   public *** get*();
   public void set*(***);
}

# General Android
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep ViewModel subclasses
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Keep Hilt generated classes
-keep class **_HiltModules* { *; }
-keep class **_HiltComponents* { *; }
