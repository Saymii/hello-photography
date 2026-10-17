plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.kapt") }
android {
 namespace = "com.posemaster.app"
 compileSdk = 35
 buildToolsVersion = "35.0.0"
 defaultConfig { applicationId = "com.posemaster.app"; minSdk = 26; targetSdk = 35; versionCode = 2; versionName = "0.2.0"
  buildConfigField("String", "PLAY_MONTHLY_PRODUCT", "\"${providers.gradleProperty("HELLO_MONTHLY_PRODUCT").orElse("").get()}\"")
  buildConfigField("String", "PLAY_ANNUAL_PRODUCT", "\"${providers.gradleProperty("HELLO_ANNUAL_PRODUCT").orElse("").get()}\"")
 }
 testOptions { unitTests.isIncludeAndroidResources = true; unitTests.all { it.systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2") } }
 buildFeatures { compose = true; buildConfig = true }
 composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 buildTypes { release { isMinifyEnabled = false } }
}
dependencies {
 implementation(platform("androidx.compose:compose-bom:2024.12.01"))
 implementation("androidx.activity:activity-compose:1.9.3")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.material:material-icons-extended")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 implementation("androidx.room:room-runtime:2.6.1")
 implementation("androidx.room:room-ktx:2.6.1")
 kapt("androidx.room:room-compiler:2.6.1")
 implementation("io.coil-kt:coil-compose:2.7.0")
 implementation("com.android.billingclient:billing-ktx:7.1.1")
 implementation("com.google.android.gms:play-services-ads:23.6.0")
 implementation("com.google.android.ump:user-messaging-platform:3.1.0")
 testImplementation("junit:junit:4.13.2")
 testImplementation("org.robolectric:robolectric:4.14.1")
 testImplementation("androidx.test:core:1.6.1")
 testImplementation("androidx.compose.ui:ui-test-junit4")
 debugImplementation("androidx.compose.ui:ui-test-manifest")
}
