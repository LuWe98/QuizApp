plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs")
    id("kotlin-parcelize")
    id("realm-android")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.hfu.quizapp"
        minSdk = 23
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding =  true
    }

//    realm {
//        syncEnabled = true
//    }

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


    // Dagger Hilt
    val hiltVersion: String by project
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    val hiltAndroidXVersion: String by project
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:$hiltAndroidXVersion")
    kapt("androidx.hilt:hilt-compiler:$hiltAndroidXVersion")
    val hiltFragmentVersion: String by project
    implementation("androidx.hilt:hilt-navigation-fragment:$hiltFragmentVersion")


    //Paging 3
    val pagingVersion: String by project
    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
    testImplementation("androidx.paging:paging-common-ktx:$pagingVersion")


    //Coroutines
    val coroutineVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion")


    //Settings-Preferences
    val preferencesVersion: String by project
    implementation("androidx.preference:preference-ktx:$preferencesVersion")


    //Preference-Datastore
    val preferenceDatastoreVersion: String by project
    implementation("androidx.datastore:datastore-preferences:$preferenceDatastoreVersion")

    
    //Ktor
    val ktorVersion: String by project
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-gson:$ktorVersion")

    
    //Kotlin Serialisation
    val kotlinVersion: String by project
    implementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    val kotlinSerialisationVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerialisationVersion")


    //Testing
    val jUnitVersion: String by project
    testImplementation("junit:junit:$jUnitVersion")

    val androidJUnitVersion: String by project
    androidTestImplementation("androidx.test.ext:junit:$androidJUnitVersion")

    val espressoVersion: String by project
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
}