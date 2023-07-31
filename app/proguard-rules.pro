# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# for FragmentContainerView error
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.fragment.app.Fragment{}
-keep class androidx.lifecycle.** {*;}
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class com.noisefit_commans.data.model.** { *; }
-keep class com.noisefit_commans.data.response.** { *; }
-keep class com.noisefit.data.remote.base.** { *; }
-keep class com.noisefit.data.remote.request.** { *; }

-keep public enum com.noisefit.** { *; }


#Commons
-keep class com.noisefit_commans.models.** { *; }
-keep class com.noisefit_commans.response.** { *; }
-keep public enum com.noisefit_commans.** { *; }
-keep class com.google.protobuf.** { *; }


#CF2
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keep class org.greenrobot.greendao.** { *; }
-keep class com.veryfit.** { *; }
-keep class com.ido.ble.** { *; }

#Hybrid
-keep class cn.appscomm.** { *; }

#CFPro
-keep class com.crrepa.** { *; }

#Evolve2
-keep public enum com.noisefit_evolve2.** { *; }

#NavPlus
-keep public enum com.noisefit_nav_plus.** { *; }
-keep class com.zjw.** { *; }


-printmapping outputfile.txt

#ZhSDK
-keep public enum com.noisefit_zhsdk.** { *; }
-keep class com.zhapp.** { *; }
-keep class com.zh.** { *; }


-keep class * extends com.google.gson.reflect.TypeToken

#https://stackoverflow.com/questions/52677638/module-with-main-dispatcher-is-missing
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

#insider
-keep class com.useinsider.insider.Insider { *; }
-keep interface com.useinsider.insider.InsiderCallback { *; }
-keep class com.useinsider.insider.InsiderUser { *; }
-keep class com.useinsider.insider.InsiderProduct { *; }
-keep class com.useinsider.insider.InsiderEvent { *; }
-keep class com.useinsider.insider.InsiderCallbackType { *; }
-keep class com.useinsider.insider.InsiderGender { *; }
-keep class com.useinsider.insider.InsiderIdentifiers { *; }

-keep interface com.useinsider.insider.RecommendationEngine$SmartRecommendation { *; }
-keep interface com.useinsider.insider.MessageCenterData { *; }
-keep class com.useinsider.insider.ContentOptimizerDataType { *; }
-keep class org.openudid.** { *; }
