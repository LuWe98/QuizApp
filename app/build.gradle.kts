plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs")
    id("kotlin-parcelize")

    val kotlinVersion = "1.6.0"
    kotlin("plugin.serialization") version kotlinVersion
}

android {
    compileSdk = 31

    ndkVersion = "21.4.7075529"

    defaultConfig {
        applicationId = "com.hfu.quizapp"
        minSdk = 23
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            ndkBuild {
                cppFlags += ""
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    //TODO -> Hier noch schauen wie das geht
//    externalNativeBuild {
//        ndkBuild {
//            path = file("src/main/jni/Android.mk")
//        }
//        cmake {
//            path = file("src/main/jni/CMakeLists.txt")
//            version = "3.10.2"
//        }
//    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {
    //Base
    val jetBrainsAnnotationVersion: String by project
    implementation("org.jetbrains:annotations:$jetBrainsAnnotationVersion")

    val appCompatVersion: String by project
    implementation("androidx.appcompat:appcompat:$appCompatVersion")

    val androidXCoreVersion: String by project
    implementation("androidx.core:core-ktx:$androidXCoreVersion")


    //Material Design
    val materialDesignVersion: String by project
    implementation("com.google.android.material:material:$materialDesignVersion")


    //Constraint- and MotionLayout
    val constraintLayoutVersion: String by project
    implementation("androidx.constraintlayout:constraintlayout:$constraintLayoutVersion")


    //SwipeRefreshLayout
    val swipeRefreshLayoutVersion: String by project
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:$swipeRefreshLayoutVersion")


    //Dynamic animations -> List overscroll bounce etc.
    val dynamicAnimationVersion: String by project
    implementation("androidx.dynamicanimation:dynamicanimation:$dynamicAnimationVersion")


    //Fragments
    val fragmentVersion: String by project
    implementation("androidx.fragment:fragment-ktx:$fragmentVersion")


    //Livecycle
    val liveCycleVersion: String by project
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$liveCycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$liveCycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$liveCycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$liveCycleVersion")
    implementation("androidx.lifecycle:lifecycle-process:$liveCycleVersion")

    val liveCycleVersionOlder: String by project
    implementation("androidx.lifecycle:lifecycle-extensions:$liveCycleVersionOlder")


    //Room Database
    val roomVersion: String by project
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")


    //Navigation Component
    val navigationVersion: String by project
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navigationVersion")


    //Hilt
    val hiltVersion: String by project
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    val hiltViewModelVersion: String by project
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:$hiltViewModelVersion")
    val hiltAndroidXVersion: String by project
    kapt("androidx.hilt:hilt-compiler:$hiltAndroidXVersion")
    val hiltFragmentVersion: String by project
    implementation("androidx.hilt:hilt-navigation-fragment:$hiltFragmentVersion")


    //Paging 3
    val pagingMainVersion: String by project
    implementation("androidx.paging:paging-runtime-ktx:$pagingMainVersion")
    val pagingRoomVersion: String by project
    implementation("androidx.room:room-paging:$pagingRoomVersion")


    //Coroutines
    val coroutineVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion")


    //Preference-Datastore
    val preferenceDatastoreVersion: String by project
    implementation("androidx.datastore:datastore-preferences:$preferenceDatastoreVersion")


    //Ktor
    val ktorVersion: String by project
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    //implementation("io.ktor:ktor-auth-jwt:$ktorVersion")


    //AUTH 0 - JWT
    val auth0Version: String by project
    implementation("com.auth0:java-jwt:$auth0Version")


    //Kotlin Serialisation
    val kotlinSerialisationVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerialisationVersion")


    //BSON
    val bsonVersion: String by project
    implementation("org.mongodb:bson:$bsonVersion")


    //Splash Screens
    val splashScreenVersion: String by project
    implementation("androidx.core:core-splashscreen:$splashScreenVersion")



    //Testing
    val jUnitVersion: String by project
    testImplementation("junit:junit:$jUnitVersion")

    val androidJUnitVersion: String by project
    androidTestImplementation("androidx.test.ext:junit:$androidJUnitVersion")

    val espressoVersion: String by project
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")

}