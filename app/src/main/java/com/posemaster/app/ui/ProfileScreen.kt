package com.posemaster.app.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.posemaster.app.data.AppPreferences
import com.posemaster.app.services.DailyInspiration

/** A device-local profile: role, language, appearance and reminders do not need an account. */
@Composable
fun ProfileScreen(
    preferences: AppPreferences,
    onDeleteAll: () -> Unit,
    onPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by preferences.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val am = settings.language == "am"
    fun t(english: String, amharic: String) = if (am) amharic else english
    var page by rememberSaveable { mutableStateOf("settings") }
    var deleteDialog by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var notificationsPermitted by remember { mutableStateOf(DailyInspiration.permitted(context)) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsPermitted = granted && DailyInspiration.permitted(context)
        preferences.setNotifications(notificationsPermitted)
        if (notificationsPermitted) {
            DailyInspiration.schedule(context)
            message = t("Daily inspiration is enabled. Delivery is around 9am local time.", "የዕለቱ አነሳሽ ሐሳብ ነቅቷል። በአካባቢዎ ሰዓት በጠዋት 9 ሰዓት አካባቢ ይደርሳል።")
        } else {
            DailyInspiration.cancel(context)
            message = t("Notifications were not allowed. You can still find your daily pose on Home.", "ማሳወቂያዎች አልተፈቀዱም። የዕለቱን አቀማመጥ በመነሻ ገጽ ማየት ይችላሉ።")
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsPermitted = DailyInspiration.permitted(context)
                if (!notificationsPermitted && preferences.state.value.notificationsEnabled) {
                    preferences.setNotifications(false)
                    DailyInspiration.cancel(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    BackHandler(page != "settings") { page = "settings" }
    when (page) {
        "privacy" -> LegalPage(t("Privacy policy", "የግላዊነት ፖሊሲ"), am, privacySections, modifier) { page = "settings" }
        "terms" -> LegalPage(t("Terms of use", "የአጠቃቀም ውሎች"), am, termsSections, modifier) { page = "settings" }
        "feedback" -> FeedbackPage(am, modifier) { page = "settings" }
        else -> LazyColumn(
            modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(t("YOUR CREATIVE SPACE", "የፈጠራ ቦታዎ"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                Text(t("Make it yours.", "የራስዎ ያድርጉት።"), fontFamily = FontFamily.Serif, fontSize = 34.sp)
                Text(t("Your guides, your pace. No account required.", "የእርስዎ መመሪያዎች፣ በእርስዎ ፍጥነት። መለያ አያስፈልግም።"), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                SettingsCard {
                    Icon(Icons.Outlined.PersonOutline, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(t("Your perspective", "የእርስዎ ምርጫ"), style = MaterialTheme.typography.titleMedium)
                    SettingsChoice(t("I use Hello Photography as", "Hello Photography የምጠቀመው እንደ"), settings.role ?: "everyone", listOf(
                        "photographer" to t("I'm a Photographer", "ፎቶ አንሺ ነኝ"),
                        "model" to t("I'm a Model", "ሞዴል ነኝ"),
                        "everyone" to t("Just for Me (Everyone)", "ለራሴ (ለሁሉም)")
                    ), preferences::setRole)
                    Text(t("Changing your role updates recommendations and keeps your favorites and lessons.", "ሚናዎን መቀየር ምክሮችን ያዘምናል፤ ተወዳጆችዎና ትምህርቶችዎ ይቀመጣሉ።"), style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                SettingsCard {
                    Text(t("Language & appearance", "ቋንቋ እና ገጽታ"), style = MaterialTheme.typography.titleMedium)
                    SettingsChoice(t("App language", "የመተግበሪያ ቋንቋ"), settings.language, listOf("en" to "English", "am" to "አማርኛ"), preferences::setLanguage)
                    Text(t("Navigation, settings, pose instructions and Academy lessons support English and Amharic. Some additional photography tips remain in English.", "የመተግበሪያ አሰሳ፣ ቅንብሮች፣ የአቀማመጥ መመሪያዎችና የአካዳሚ ትምህርቶች በእንግሊዝኛና በአማርኛ ይገኛሉ። አንዳንድ ተጨማሪ የፎቶግራፍ ምክሮች በእንግሊዝኛ ናቸው።"), style = MaterialTheme.typography.bodySmall)
                    SettingsChoice(t("Appearance", "ገጽታ"), settings.theme, listOf(
                        "System" to t("Match device", "ከመሣሪያው ጋር ይዛመድ"), "Light" to t("Light", "ብርሃን"), "Dark" to t("Dark", "ጨለማ")
                    ), preferences::setTheme)
                }
            }
            item {
                SettingsCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(t("Daily inspiration", "የዕለቱ አነሳሽ ሐሳብ"), style = MaterialTheme.typography.titleMedium)
                            Text(t("One local reminder around 9am. No internet needed.", "በጠዋት 9 ሰዓት አካባቢ አንድ ማሳሰቢያ። ኢንተርኔት አያስፈልግም።"), style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(modifier = Modifier.semantics { contentDescription = t("Daily inspiration notifications", "የዕለቱ አነሳሽ ሐሳብ ማሳወቂያዎች") }, checked = settings.notificationsEnabled && notificationsPermitted, onCheckedChange = { enabled ->
                            if (!enabled) {
                                preferences.setNotifications(false)
                                DailyInspiration.cancel(context)
                            } else if (DailyInspiration.permitted(context)) {
                                preferences.setNotifications(true)
                                DailyInspiration.schedule(context)
                                message = t("Daily reminder enabled. Android may delay delivery to save battery.", "የዕለቱ ማሳሰቢያ ነቅቷል። አንድሮይድ ባትሪ ለመቆጠብ ሊያዘገየው ይችላል።")
                            } else if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                message = t("Allow Hello Photography notifications in Android settings, then turn this on.", "በአንድሮይድ ቅንብሮች የHello Photography ማሳወቂያዎችን ይፍቀዱ፤ ከዚያ ይህን ያብሩ።")
                            }
                        })
                    }
                    if (!notificationsPermitted) TextButton(onClick = {
                        context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                    }) { Text(t("Android notification settings", "የአንድሮይድ ማሳወቂያ ቅንብሮች")) }
                    message?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                }
            }
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(t("A little more possibility.", "ተጨማሪ እድሎች።"), fontFamily = FontFamily.Serif, fontSize = 25.sp)
                        Text(t("Explore premium plans and what is ready in this development build.", "የፕሪሚየም ዕቅዶችንና በዚህ የሙከራ ስሪት የተዘጋጁትን ይመልከቱ።"))
                        Button(onClick = onPremium) { Text(t("View premium plans", "የፕሪሚየም ዕቅዶችን ይመልከቱ")) }
                    }
                }
            }
            item {
                SettingsCard {
                    SettingsLink(Icons.Outlined.RateReview, t("Feedback & rating", "አስተያየት እና ደረጃ")) { page = "feedback" }
                    HorizontalDivider()
                    SettingsLink(Icons.Outlined.PrivacyTip, t("Privacy policy", "የግላዊነት ፖሊሲ")) { page = "privacy" }
                    HorizontalDivider()
                    SettingsLink(Icons.Outlined.Description, t("Terms of use", "የአጠቃቀም ውሎች")) { page = "terms" }
                    HorizontalDivider()
                    SettingsLink(Icons.Outlined.Code, t("Project & support on GitHub", "ፕሮጀክት እና ድጋፍ በGitHub")) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Saymii/hello-photography/issues"))
                        try { context.startActivity(intent) }
                        catch (_: android.content.ActivityNotFoundException) {
                            message = t("No browser is available. Project: github.com/Saymii/hello-photography", "አሳሽ የለም። ፕሮጀክት፦ github.com/Saymii/hello-photography")
                        }
                    }
                }
            }
            item {
                SettingsCard {
                    Text(t("Your data stays in your hands", "ውሂብዎ በእጅዎ ነው"), style = MaterialTheme.typography.titleMedium)
                    Text(t("Favorites, collections, lesson progress, recently viewed guides and preferences are saved on this device. Deleting them cannot be undone.", "ተወዳጆች፣ ስብስቦች፣ የትምህርት ሂደት፣ በቅርቡ የታዩ መመሪያዎችና ቅንብሮች በዚህ መሣሪያ ይቀመጣሉ። ከተሰረዙ መመለስ አይቻልም።"), style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = { deleteDialog = true }) {
                        Icon(Icons.Outlined.DeleteOutline, null)
                        Text(t("Delete all local data", "ሁሉንም የአካባቢ ውሂብ ሰርዝ"), Modifier.padding(start = 8.dp))
                    }
                }
            }
            item {
                Text("HELLO PHOTOGRAPHY", style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
                Text(t("Made for every body and every perspective. Original pose-study illustrations; no personal photos are uploaded.", "ለሁሉም አካልና አመለካከት። ኦሪጂናል የአቀማመጥ ምሳሌዎች፤ የግል ፎቶዎች አይላኩም።"), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    if (deleteDialog) AlertDialog(
        onDismissRequest = { deleteDialog = false },
        title = { Text(t("Delete your local data?", "የአካባቢ ውሂብዎ ይሰረዝ?")) },
        text = { Text(t("This permanently removes your favorites, collections, learning progress, history and settings from this device. Daily reminders will stop. Bundled guides remain available; you will choose your role again.", "ይህ ተወዳጆችዎን፣ ስብስቦችዎን፣ የትምህርት ሂደትዎን፣ ታሪክዎንና ቅንብሮችዎን ከዚህ መሣሪያ በቋሚነት ይሰርዛል። የዕለቱ ማሳሰቢያዎች ይቆማሉ። የተካተቱት መመሪያዎች ይቀጥላሉ፤ ሚናዎን እንደገና ይመርጣሉ።")) },
        confirmButton = { TextButton(onClick = { deleteDialog = false; DailyInspiration.cancel(context); onDeleteAll() }) { Text(t("Delete data", "ውሂብ ሰርዝ"), color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { deleteDialog = false }) { Text(t("Keep my data", "ውሂቤን አስቀምጥ")) } }
    )
}

@Composable private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable private fun SettingsChoice(label: String, value: String, options: List<Pair<String, String>>, change: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(options.find { it.first == value }?.second ?: value, Modifier.weight(1f))
                Icon(Icons.Outlined.ExpandMore, null)
            }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (key, title) -> DropdownMenuItem(text = { Text(title) }, onClick = { expanded = false; change(key) }) }
            }
        }
    }
}

@Composable private fun SettingsLink(icon: ImageVector, title: String, click: () -> Unit) {
    TextButton(onClick = click, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 8.dp)) {
        Icon(icon, null, Modifier.padding(end = 12.dp))
        Text(title, Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable private fun FeedbackPage(am: Boolean, modifier: Modifier, back: () -> Unit) {
    val context = LocalContext.current
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var feedback by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    fun t(en: String, translated: String) = if (am) translated else en
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { TextButton(onClick = back) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null); Text(t("Back to Profile", "ወደ መገለጫ ተመለስ")) } }
        item { Text(t("Help shape the next frame.", "የሚቀጥለውን ምስል ያሻሽሉ።"), fontFamily = FontFamily.Serif, fontSize = 30.sp); Text(t("How is Hello Photography working for you?", "Hello Photography እንዴት እየሰራልዎ ነው?")) }
        item {
            Text(t("Your rating", "የእርስዎ ደረጃ"), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..5).forEach { score -> FilterChip(selected = rating == score, onClick = { rating = score }, label = { Text("$score ★") }) }
            }
            Text(t("This is private feedback, not a Google Play review.", "ይህ የግል አስተያየት ነው፤ የGoogle Play ግምገማ አይደለም።"), style = MaterialTheme.typography.bodySmall)
        }
        item { OutlinedTextField(value = feedback, onValueChange = { feedback = it.take(3000) }, label = { Text(t("What should we improve?", "ምን ማሻሻል አለብን?")) }, minLines = 5, modifier = Modifier.fillMaxWidth()); Text("${feedback.length}/3000", style = MaterialTheme.typography.labelSmall) }
        item {
            Button(onClick = {
                if (feedback.isBlank() && rating == 0) error = t("Select a rating or add a message first.", "መጀመሪያ ደረጃ ይምረጡ ወይም መልዕክት ይጻፉ።")
                else {
                    val text = "Hello Photography feedback\nRating: ${if (rating == 0) "Not specified" else "$rating/5"}\n\n${feedback.trim()}"
                    val share = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_SUBJECT, "Hello Photography feedback"); putExtra(Intent.EXTRA_TEXT, text) }
                    try { context.startActivity(Intent.createChooser(share, t("Share feedback", "አስተያየት ያጋሩ"))); error = null }
                    catch (_: android.content.ActivityNotFoundException) { error = t("No sharing app is installed. You can post feedback on the project's GitHub Issues page.", "የማጋሪያ መተግበሪያ አልተጫነም። በፕሮጀክቱ GitHub Issues ገጽ አስተያየት መስጠት ይችላሉ።") }
                }
            }) { Icon(Icons.Outlined.Share, null); Text(t("Share feedback", "አስተያየት ያጋሩ"), Modifier.padding(start = 8.dp)) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Text(t("You choose where to send this message. Nothing is submitted automatically; avoid including private photos or sensitive information.", "መልዕክቱን የት እንደሚልኩ እርስዎ ይመርጣሉ። በራስ ሰር ምንም አይላክም፤ የግል ፎቶዎችንና ስሱ መረጃዎችን አያካትቱ።"), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private data class LegalSection(val title: String, val amTitle: String, val text: String, val amText: String)

@Composable private fun LegalPage(title: String, am: Boolean, sections: List<LegalSection>, modifier: Modifier, back: () -> Unit) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { TextButton(onClick = back) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null); Text(if (am) "ወደ መገለጫ ተመለስ" else "Back to Profile") } }
        item { Text(title, fontFamily = FontFamily.Serif, fontSize = 32.sp); Text(if (am) "የሙከራ ስሪት · ጥቅምት 2026" else "Development edition · October 2026", style = MaterialTheme.typography.labelSmall) }
        sections.forEach { section -> item { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(if (am) section.amTitle else section.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary); Text(if (am) section.amText else section.text, lineHeight = 25.sp); HorizontalDivider(Modifier.padding(top = 8.dp)) } } }
    }
}

private val privacySections = listOf(
    LegalSection("What is stored", "የሚቀመጥ መረጃ", "Your role, language, theme, reminder preference, favorites, collection names, recently viewed poses and learning progress are stored locally on this device. There is no sign-up requirement. Bundled guides work offline.", "ሚናዎ፣ ቋንቋዎ፣ ገጽታዎ፣ የማሳሰቢያ ምርጫዎ፣ ተወዳጆችዎ፣ የስብስብ ስሞች፣ በቅርቡ ያዩዋቸው አቀማመጦችና የትምህርት ሂደት በዚህ መሣሪያ ይቀመጣሉ። ምዝገባ አያስፈልግም። የተካተቱ መመሪያዎች ያለ ኢንተርኔት ይሰራሉ።"),
    LegalSection("Photos and permissions", "ፎቶዎችና ፈቃዶች", "The app does not request camera, microphone, location or photo-library access and does not upload your photos. Daily inspiration requests notification permission only when you turn it on. Reminders are scheduled on your device.", "መተግበሪያው የካሜራ፣ የማይክሮፎን፣ የቦታ ወይም የፎቶ ማከማቻ ፈቃድ አይጠይቅም፤ ፎቶዎችዎንም አይልክም። የዕለቱ ማሳሰቢያ ሲያበሩት ብቻ የማሳወቂያ ፈቃድ ይጠይቃል። ማሳሰቢያዎች በመሣሪያዎ ይዘጋጃሉ።"),
    LegalSection("Optional services", "አማራጭ አገልግሎቶች", "Cloud sign-in, photo uploads, analytics and AI API requests are not enabled in this development edition. Offline recommendations use structured rules. The AdMob SDK is bundled but normal app screens do not initialize or request ads. Google Play Billing and consent-gated test advertising are integration components. If enabled in a future build, Google may process device, advertising, network or purchase information under its own policies. No real subscription is unlocked without verified purchases. Production services require a revised published policy and accurate Play Data Safety disclosures.", "በዚህ የሙከራ ስሪት የደመና መግቢያ፣ የፎቶ ጭነት፣ ትንታኔና AI API ጥያቄዎች አልነቁም። ከመስመር ውጭ ምክሮች በደንብ የተመሠረቱ ናቸው። የAdMob SDK ተካትቷል፤ መደበኛ ገጾች ማስታወቂያ አያስጀምሩም ወይም አይጠይቁም። Google Play Billing እና በፈቃድ የሙከራ ማስታወቂያዎች በወደፊት ስሪት ሲነቁ Google የመሣሪያ፣ የማስታወቂያ፣ የአውታረ መረብ ወይም የግዢ መረጃን በራሱ ፖሊሲ ሊያስኬድ ይችላል። እውነተኛ ምዝገባ ያለተረጋገጠ ግዢ አይከፈትም።"),
    LegalSection("Sharing and feedback", "ማጋራትና አስተያየት", "Sharing a pose or feedback opens Android's share chooser. Nothing is sent until you choose a receiving app and complete its send action. The receiving app and GitHub support links have their own privacy policies. Feedback drafts are not automatically uploaded.", "አቀማመጥ ወይም አስተያየት ማጋራት የአንድሮይድ ማጋሪያ ይከፍታል። ተቀባይ መተግበሪያ እስኪመርጡና እስኪልኩ ድረስ ምንም አይላክም። ተቀባይ መተግበሪያዎችና GitHub የራሳቸው የግላዊነት ፖሊሲ አላቸው። የአስተያየት ረቂቆች በራስ ሰር አይላኩም።"),
    LegalSection("Delete and control", "ሰርዝና ተቆጣጠር", "Use Profile → Delete all local data to remove saved guides, progress, viewing history and settings and stop reminders. Android's Clear storage or uninstall also removes this app's local data. There is no cloud account to delete in this edition. Android device backups are disabled for the app. Questions and privacy concerns can be raised at github.com/Saymii/hello-photography/issues; avoid posting personal information publicly.", "በመገለጫ → ሁሉንም የአካባቢ ውሂብ ሰርዝ በመጠቀም የተቀመጡ መመሪያዎችን፣ ሂደትን፣ ታሪክንና ቅንብሮችን ያስወግዱ፤ ማሳሰቢያዎችም ይቆማሉ። የአንድሮይድ ማከማቻ ማጽዳት ወይም ማራገፍም ውሂቡን ያስወግዳል። የደመና መለያ የለም። የመሣሪያ ምትኬ ተሰናክሏል። ጥያቄዎችን በ github.com/Saymii/hello-photography/issues ያቅርቡ፤ የግል መረጃ በይፋ አያስቀምጡ።")
)

private val termsSections = listOf(
    LegalSection("Educational use", "ትምህርታዊ አጠቃቀም", "Hello Photography offers posing and photography education. Results depend on your equipment, setting and practice. Treat instructions as suggestions: adapt every movement for comfort and accessibility. Do not attempt unsafe positions or locations, and obtain consent before directing, touching or photographing anyone.", "Hello Photography የአቀማመጥና የፎቶግራፍ ትምህርት ይሰጣል። ውጤቶች በመሣሪያ፣ በቦታና በልምምድ ይለያያሉ። መመሪያዎችን እንደ ምክር ይውሰዱ፤ እንቅስቃሴዎችን ለምቾትዎ ያስተካክሉ። አደገኛ አቀማመጥና ቦታ አይሞክሩ፤ ማንንም ከመምራት፣ ከመንካት ወይም ፎቶ ከማንሳት በፊት ፈቃድ ይጠይቁ።"),
    LegalSection("Content and attribution", "ይዘትና እውቅና", "Bundled illustrations are original pose-study assets, clearly identified as illustrations rather than example photographs. No user-supplied reference photos are included. Respect the licenses and attribution requirements of any content you add or share. You are responsible for obtaining permission from subjects and rights holders for your own photographs.", "የተካተቱት ምሳሌዎች ኦሪጂናል የአቀማመጥ ጥናት ስራዎች ናቸው፤ ፎቶግራፎች እንዳልሆኑ በግልጽ ተለይተዋል። በተጠቃሚ የተላኩ ማጣቀሻ ፎቶዎች አልተካተቱም። የሚጨምሩትን ወይም የሚያጋሩትን ይዘት ፈቃድና እውቅና ያክብሩ። ለራስዎ ፎቶግራፎች ከተነሺዎችና ከመብት ባለቤቶች ፈቃድ ማግኘት የእርስዎ ኃላፊነት ነው።"),
    LegalSection("Premium and availability", "ፕሪሚየምና አቅርቦት", "Development previews do not constitute a paid subscription. Real purchases require configured Google Play products and secure backend verification. Prices, renewal terms and cancellation controls must be shown by Google Play before purchase. No unavailable service is promised, and offline pose recommendations do not use a configured AI model.", "የሙከራ ቅድመ እይታዎች የሚከፈልባቸው ምዝገባዎች አይደሉም። እውነተኛ ግዢዎች የተዋቀሩ Google Play ምርቶችንና ደህንነቱ የተጠበቀ ማረጋገጫ ይፈልጋሉ። ዋጋ፣ የእድሳት ውሎችና የመሰረዣ ቁጥጥሮች ከግዢ በፊት በGoogle Play መታየት አለባቸው። ከመስመር ውጭ ምክሮች የተዋቀረ AI ሞዴል አይጠቀሙም።"),
    LegalSection("Local data and support", "የአካባቢ ውሂብና ድጋፍ", "Saved data currently belongs to this installation and is not synced across devices. Deleting local data is permanent. Development features may change before release. Report bugs and accessibility problems at github.com/Saymii/hello-photography/issues. These development terms and the privacy information require owner review before public distribution.", "የተቀመጠ ውሂብ በዚህ ጭነት ብቻ ይገኛል፤ በመሣሪያዎች መካከል አይመሳሰልም። የአካባቢ ውሂብ መሰረዝ ቋሚ ነው። የሙከራ ባህሪዎች ከመለቀቃቸው በፊት ሊለወጡ ይችላሉ። ስህተቶችንና የተደራሽነት ችግሮችን በ github.com/Saymii/hello-photography/issues ያሳውቁ። እነዚህ ውሎችና የግላዊነት መረጃ ከሕዝብ ስርጭት በፊት የባለቤቱን ግምገማ ይፈልጋሉ።")
)
