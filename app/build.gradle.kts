plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.kapt") }
val uploadVariables=listOf("HELLO_UPLOAD_KEYSTORE","HELLO_UPLOAD_STORE_PASSWORD","HELLO_UPLOAD_KEY_ALIAS","HELLO_UPLOAD_KEY_PASSWORD")
val uploadSigning=uploadVariables.associateWith {providers.environmentVariable(it).orNull}
val hasUploadSigning=uploadSigning.values.all {!it.isNullOrBlank()}
val checkUploadSigning=tasks.register("checkUploadSigning") {
 doLast {
  val missing=uploadSigning.filterValues {it.isNullOrBlank()}.keys
  check(missing.isEmpty()) {"Release signing is required. Configure these environment variables securely: ${missing.joinToString()}. See docs/PLAY_PUBLISHING_STEPS.md"}
  check(file(uploadSigning.getValue("HELLO_UPLOAD_KEYSTORE")!!).isFile) {"Upload keystore file is unavailable"}
 }
}
android {
 namespace = "com.posemaster.app"
 compileSdk = 36
 buildToolsVersion = "36.0.0"
 defaultConfig { applicationId = "com.saymii.hellophotography"; minSdk = 26; targetSdk = 36; versionCode = 3; versionName = "1.0.0"
  buildConfigField("String", "PLAY_MONTHLY_PRODUCT", "\"${providers.gradleProperty("HELLO_MONTHLY_PRODUCT").orElse("").get()}\"")
  buildConfigField("String", "PLAY_ANNUAL_PRODUCT", "\"${providers.gradleProperty("HELLO_ANNUAL_PRODUCT").orElse("").get()}\"")
 }
 testOptions { unitTests.isIncludeAndroidResources = true; unitTests.all { it.systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2") } }
 buildFeatures { compose = true; buildConfig = true }
 composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 signingConfigs {
  create("upload") {
   if(hasUploadSigning) {
    storeType="PKCS12"
    storeFile=file(uploadSigning.getValue("HELLO_UPLOAD_KEYSTORE")!!)
    storePassword=uploadSigning.getValue("HELLO_UPLOAD_STORE_PASSWORD")
    keyAlias=uploadSigning.getValue("HELLO_UPLOAD_KEY_ALIAS")
    keyPassword=uploadSigning.getValue("HELLO_UPLOAD_KEY_PASSWORD")
   }
  }
 }
 buildTypes {
  debug { applicationIdSuffix=".debug"; versionNameSuffix="-dev" }
  release {
   isDebuggable=false
   isMinifyEnabled=true
   isShrinkResources=true
   signingConfig=signingConfigs.getByName("upload")
   proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro")
  }
 }
 packaging { jniLibs { useLegacyPackaging=false } }
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
 implementation("com.android.billingclient:billing:9.1.0")
 debugImplementation("com.google.android.gms:play-services-ads:23.6.0")
 debugImplementation("com.google.android.ump:user-messaging-platform:3.1.0")
 testImplementation("junit:junit:4.13.2")
 testImplementation("org.robolectric:robolectric:4.14.1")
 testImplementation("androidx.test:core:1.6.1")
 testImplementation("androidx.compose.ui:ui-test-junit4")
 debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Never silently produce an unsigned release bundle when credentials are absent.
tasks.configureEach {
 if(name in setOf("validateSigningRelease","packageReleaseBundle","signReleaseBundle","bundleRelease","assembleRelease")) dependsOn(checkUploadSigning)
}
