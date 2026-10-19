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
                    Text(t("Navigation, settings, the original 48 guides and Academy lessons support English and Amharic. New AI photo guides currently use English instructions.", "የመተግበሪያ አሰሳ፣ ቅንብሮች፣ የአቀማመጥ መመሪያዎችና የአካዳሚ ትምህርቶች በእንግሊዝኛና በአማርኛ ይገኛሉ። አንዳንድ ተጨማሪ የፎቶግራፍ ምክሮች በእንግሊዝኛ ናቸው።"), style = MaterialTheme.typography.bodySmall)
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
                Text(t("Made for every body and every perspective. AI-generated reference images and original pose illustrations work offline; your personal photos are never uploaded.", "ለሁሉም አካልና አመለካከት። በAI የተፈጠሩ ማጣቀሻ ምስሎችና ኦሪጂናል ምሳሌዎች ከመስመር ውጭ ይሠራሉ፤ የግል ፎቶዎችዎ አይላኩም።"), style = MaterialTheme.typography.bodySmall)
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
        item { Text(title, fontFamily = FontFamily.Serif, fontSize = 32.sp); Text(if (am) "የሚሠራበት ቀን፦ 2026-10-19" else "Effective 19 October 2026", style = MaterialTheme.typography.labelSmall) }
        sections.forEach { section -> item { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(if (am) section.amTitle else section.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary); Text(if (am) section.amText else section.text, lineHeight = 25.sp); HorizontalDivider(Modifier.padding(top = 8.dp)) } } }
    }
}

private val privacySections = listOf(
    LegalSection("Publisher and contact", "አሳታሚ እና አድራሻ", "Hello Photography is provided by Simon Solomon. Contact simonsolomon436@gmail.com with support questions, privacy concerns or deletion requests. This policy describes the offline Google Play release. We will update it before enabling services that change how information is handled.", "Hello Photography በSimon Solomon ይቀርባል። ለድጋፍ፣ ለግላዊነት ጥያቄዎች ወይም ለስረዛ ጥያቄዎች simonsolomon436@gmail.com ያነጋግሩ። ይህ ፖሊሲ ከመስመር ውጭ የሚሠራውን የGoogle Play ስሪት ይገልጻል። የመረጃ አያያዝን የሚቀይሩ አገልግሎቶች ከመንቃታቸው በፊት ፖሊሲው ይዘምናል።"),
    LegalSection("What is stored", "የሚቀመጥ መረጃ", "Your role, language, theme, reminder preference, favorites, collection names, recently viewed poses and learning progress are stored in this app's private storage on your device. There is no sign-up requirement or cloud synchronization. Bundled guides work offline. Search and pose-assistant inputs are processed on your device using structured rules; they are not sent to an AI model or API.", "ሚናዎ፣ ቋንቋዎ፣ ገጽታዎ፣ የማሳሰቢያ ምርጫዎ፣ ተወዳጆችዎ፣ የስብስብ ስሞች፣ በቅርቡ ያዩዋቸው አቀማመጦችና የትምህርት ሂደት በመሣሪያዎ በመተግበሪያው የግል ማከማቻ ይቀመጣሉ። ምዝገባና የደመና ማመሳሰል የለም። የተካተቱ መመሪያዎች ያለ ኢንተርኔት ይሰራሉ። ፍለጋና የአቀማመጥ ረዳት ግቤቶች በመሣሪያዎ በደንብ ይሰራሉ፤ ወደ AI ሞዴል ወይም API አይላኩም።"),
    LegalSection("Photos and permissions", "ፎቶዎችና ፈቃዶች", "The app does not request camera, microphone, location, contacts or photo-library access and does not capture, import or upload your photos. Posing references include labeled AI-generated images supplied for the app and original illustrations. They are bundled for offline viewing; no AI image generation runs on your device or server. Daily inspiration requests notification permission only when you turn it on. Reminders are scheduled on your device and can be disabled in Profile or Android settings.", "መተግበሪያው የካሜራ፣ የማይክሮፎን፣ የቦታ፣ የእውቂያ ወይም የፎቶ ማከማቻ ፈቃድ አይጠይቅም፤ ፎቶዎችዎንም አያነሳም፣ አያስገባም ወይም አይልክም። የአቀማመጥ ማጣቀሻዎች በAI የተፈጠሩ ምስሎችንና ኦሪጂናል ምሳሌዎችን ያካትታሉ። ለኦፍላይን እይታ ተካተዋል፤ መተግበሪያው አዲስ የAI ምስል አይፈጥርም። የዕለቱ ማሳሰቢያ ሲያበሩት ብቻ የማሳወቂያ ፈቃድ ይጠይቃል። ማሳሰቢያዎች በመሣሪያዎ ይዘጋጃሉ፤ በመገለጫ ወይም በአንድሮይድ ቅንብሮች ማጥፋት ይችላሉ።"),
    LegalSection("Google Play and optional services", "Google Play እና አማራጭ አገልግሎቶች", "Cloud sign-in, photo uploads and AI API requests are not enabled. Premium items may appear as locked previews; purchases are not enabled in this release. If product IDs are configured, Google Play Billing can query product availability and localized prices. Google handles its store and account information under its own privacy policy; the publisher does not receive payment card details from a catalog query. A future purchase service requires secure verification and updated disclosures before activation.", "የደመና መግቢያ፣ የፎቶ ጭነትና AI API ጥያቄዎች አልነቁም። የፕሪሚየም ይዘት እንደ ተቆለፈ ቅድመ እይታ ሊታይ ይችላል፤ በዚህ ስሪት ግዢዎች አልነቁም። የምርት መለያዎች ከተዋቀሩ Google Play Billing የምርት አቅርቦትና የአካባቢ ዋጋዎችን ሊጠይቅ ይችላል። Google የመደብርና የመለያ መረጃን በራሱ ፖሊሲ ያስተናግዳል፤ አሳታሚው ከካታሎግ ጥያቄ የክፍያ ካርድ ዝርዝር አያገኝም። የወደፊት ግዢ አገልግሎት ከመንቃቱ በፊት ደህንነቱ የተጠበቀ ማረጋገጫና የዘመኑ መግለጫዎችን ይፈልጋል።"),
    LegalSection("Advertising and analytics", "ማስታወቂያና ትንታኔ", "The Google Play release does not display ads and excludes the AdMob and UMP advertising/consent SDKs. It does not include publisher-operated analytics, crash reporting or tracking services. Development builds may contain isolated test-ad integration components; those are not the release described here. Adding advertising or analytics requires a revised policy and accurate Play disclosures first.", "የGoogle Play ልቀት ማስታወቂያ አያሳይም፤ AdMob እና UMP የማስታወቂያና የፈቃድ SDK አያካትትም። በአሳታሚው የሚሠሩ የትንታኔ፣ የብልሽት ሪፖርት ወይም የክትትል አገልግሎቶች የሉም። የሙከራ ስሪቶች የሙከራ ማስታወቂያ ክፍሎችን ሊያካትቱ ይችላሉ፤ ይህ ፖሊሲ የልቀት ስሪቱን ይገልጻል። ማስታወቂያ ወይም ትንታኔ ከመጨመሩ በፊት ፖሊሲውና የPlay መግለጫዎች መዘመን አለባቸው።"),
    LegalSection("Sharing, links and feedback", "ማጋራት፣ አገናኞችና አስተያየት", "Sharing opens Android's share chooser. The receiving app you select receives the text you choose to share and handles it under its own policy. Feedback drafts are not automatically submitted. Opening a support link starts a browser or another app; those services may receive technical information such as an IP address and have their own policies. Avoid sharing private information in public GitHub issues.", "ማጋራት የአንድሮይድ ማጋሪያ ይከፍታል። የሚመርጡት ተቀባይ መተግበሪያ የሚያጋሩትን ጽሑፍ ይቀበላል፤ በራሱም ፖሊሲ ያስተናግዳል። የአስተያየት ረቂቆች በራስ ሰር አይላኩም። የድጋፍ አገናኝ አሳሽ ወይም ሌላ መተግበሪያ ይከፍታል፤ እነዚህ አገልግሎቶች እንደ IP አድራሻ ያለ ቴክኒካዊ መረጃ ሊቀበሉ ይችላሉ፤ የራሳቸውም ፖሊሲ አላቸው። በይፋ በGitHub የግል መረጃ አያስቀምጡ።"),
    LegalSection("Support correspondence", "የድጋፍ መልዕክቶች", "If you email us, we use the information you provide to respond. We retain correspondence only as long as necessary to handle your request and meet legal obligations. You can request deletion at simonsolomon436@gmail.com; your and our email providers also process messages under their own policies. We respond to privacy requests in accordance with applicable law.", "ኢሜይል ከላኩልን የሰጡትን መረጃ ለመመለስ እንጠቀማለን። መልዕክቶችን ጥያቄዎን ለማስተናገድና ሕጋዊ ግዴታዎችን ለማሟላት እስከሚያስፈልግ ድረስ ብቻ እናቆያለን። በsimonsolomon436@gmail.com ስረዛ መጠየቅ ይችላሉ፤ የእርስዎና የእኛ የኢሜይል አቅራቢዎችም በራሳቸው ፖሊሲ መልዕክቶችን ያስተናግዳሉ። የግላዊነት ጥያቄዎችን በሚመለከተው ሕግ መሠረት እንመልሳለን።"),
    LegalSection("Delete and control", "ሰርዝና ተቆጣጠር", "Use Profile → Delete all local data to remove favorites, collections, progress, viewing history and settings and stop reminders. Android's Clear storage or uninstall also removes this app's local data. There is no cloud account to delete. Automatic Android backup is disabled and supported backup/device-transfer rules exclude app data. Deletion cannot remove copies you shared to other apps or recover data already deleted; we cannot erase or recover local app data remotely.", "በመገለጫ → ሁሉንም የአካባቢ ውሂብ ሰርዝ በመጠቀም ተወዳጆችን፣ ስብስቦችን፣ ሂደትን፣ ታሪክንና ቅንብሮችን ያስወግዱ፤ ማሳሰቢያዎችም ይቆማሉ። የአንድሮይድ ማከማቻ ማጽዳት ወይም ማራገፍም ውሂቡን ያስወግዳል። የሚሰረዝ የደመና መለያ የለም። ራስ ሰር የአንድሮይድ ምትኬ ተሰናክሏል፤ የምትኬና የመሣሪያ ማስተላለፍ ደንቦች የመተግበሪያውን ውሂብ ያስቀራሉ። ስረዛ ወደ ሌሎች መተግበሪያዎች ያጋሯቸውን ቅጂዎች አያጠፋም፤ የተሰረዘ ውሂብም አይመለስም። የአካባቢ ውሂብዎን ከሩቅ መሰረዝ ወይም መመለስ አንችልም።"),
    LegalSection("Security and audience", "ደህንነትና ታዳሚ", "Local data uses Android's app-private storage. No storage or transmission system offers absolute security. This app does not ask for age, create accounts or build children's profiles. Its intended audience is identified in the Play listing. A parent or guardian can contact us about information submitted to support. New services require a privacy and security review before activation.", "የአካባቢ ውሂብ በአንድሮይድ የመተግበሪያ የግል ማከማቻ ይቀመጣል። ማንኛውም የማከማቻ ወይም የማስተላለፍ ሥርዓት ፍጹም ደህንነት አያረጋግጥም። መተግበሪያው ዕድሜ አይጠይቅም፣ መለያ አይፈጥርም ወይም የሕፃናት መገለጫ አያዘጋጅም። የታሰበው ታዳሚ በPlay ዝርዝር ይገለጻል። ወላጅ ወይም አሳዳጊ ለድጋፍ ስለተላከ መረጃ ሊያነጋግረን ይችላል። አዲስ አገልግሎቶች ከመንቃታቸው በፊት የግላዊነትና የደህንነት ግምገማ ይፈልጋሉ።")
)

private val termsSections = listOf(
    LegalSection("Educational use", "ትምህርታዊ አጠቃቀም", "Hello Photography, provided by Simon Solomon, offers posing and photography education. Results depend on your equipment, setting and practice; no qualification, income or particular result is promised. Adapt every movement for comfort and accessibility. Avoid unsafe positions and locations, and obtain consent before directing, touching or photographing anyone. Offline recommendations use structured rules, not a configured AI model.", "በSimon Solomon የሚቀርበው Hello Photography የአቀማመጥና የፎቶግራፍ ትምህርት ይሰጣል። ውጤቶች በመሣሪያ፣ በቦታና በልምምድ ይለያያሉ፤ ብቃት፣ ገቢ ወይም የተወሰነ ውጤት አይረጋገጥም። እንቅስቃሴዎችን ለምቾትና ለተደራሽነት ያስተካክሉ። አደገኛ አቀማመጥና ቦታ አይሞክሩ፤ ማንንም ከመምራት፣ ከመንካት ወይም ፎቶ ከማንሳት በፊት ፈቃድ ይጠይቁ። የኦፍላይን ምክሮች በተዋቀሩ ደንቦች ይሰራሉ፤ የተዋቀረ AI ሞዴል አይጠቀሙም።"),
    LegalSection("Content and attribution", "ይዘትና እውቅና", "The visual library includes labeled AI-generated reference images supplied by the publisher and original pose-study illustrations. AI-generated references are educational examples and may contain visual inaccuracies. Adapt the instructions to comfortable real-world movement. You may use the app for personal study and photography practice, and use the built-in sharing control to share its posing-tip text. Other reproduction or redistribution requires the relevant rights holder's permission unless allowed by law. Third-party software retains its own license terms. Obtain permissions and releases for your own photography.", "የምስል ስብስቡ በአሳታሚው የቀረቡ በAI የተፈጠሩ ማጣቀሻ ምስሎችንና ኦሪጂናል ምሳሌዎችን ያካትታል። የAI ምስሎች የትምህርት ምሳሌዎች ናቸው፤ ስህተት ሊኖራቸው ይችላል። እንቅስቃሴዎችን ለምቾትዎ ያስተካክሉ። ለግል ጥናትና ለፎቶግራፍ ልምምድ መጠቀምና በመተግበሪያው ማጋሪያ የምክር ጽሑፍ ማጋራት ይችላሉ። ሌላ ማባዛት ወይም ማሰራጨት በሕግ ካልተፈቀደ በስተቀር ከመብት ባለቤቱ ፈቃድ ይፈልጋል። የሶስተኛ ወገን ሶፍትዌር የራሱን ፈቃድ ያቆያል። ለራስዎ ፎቶግራፍ ተገቢ ፈቃዶችን ያግኙ።"),
    LegalSection("Premium and availability", "ፕሪሚየምና አቅርቦት", "Free guides and lessons are available as shown. Locked premium previews are not a purchase or active subscription; purchases are disabled in this release. Future real purchases require configured Google Play products and secure backend verification. Actual prices, billing periods, renewal terms, benefits and cancellation information must be shown before purchase. Refunds and cancellation must follow Google Play rules and applicable law. No unavailable paid service is promised.", "ነፃ መመሪያዎችና ትምህርቶች እንደሚታዩት ይገኛሉ። የተቆለፉ የፕሪሚየም ቅድመ እይታዎች ግዢ ወይም ንቁ ምዝገባ አይደሉም፤ በዚህ ስሪት ግዢዎች አልነቁም። የወደፊት እውነተኛ ግዢዎች የተዋቀሩ የGoogle Play ምርቶችና ደህንነቱ የተጠበቀ የአገልጋይ ማረጋገጫ ይፈልጋሉ። እውነተኛ ዋጋ፣ የክፍያ ጊዜ፣ የእድሳት ውሎች፣ ጥቅሞችና የመሰረዣ መረጃ ከግዢ በፊት መታየት አለባቸው። ተመላሽ ክፍያና ስረዛ የGoogle Play ደንቦችንና ሕግን ይከተላሉ። የማይገኝ የክፍያ አገልግሎት አይረጋገጥም።"),
    LegalSection("Local data and support", "የአካባቢ ውሂብና ድጋፍ", "Saved data belongs to this installation and is not synced across devices. Deleting local data is permanent. See the privacy policy for deletion controls. Contact simonsolomon436@gmail.com with bugs, accessibility concerns or questions about your rights. The project issue tracker is public; avoid posting personal information there. Features may change in later versions. Applicable consumer rights remain in force.", "የተቀመጠ ውሂብ በዚህ ጭነት ብቻ ይገኛል፤ በመሣሪያዎች መካከል አይመሳሰልም። የአካባቢ ውሂብ መሰረዝ ቋሚ ነው። የስረዛ መቆጣጠሪያዎችን በግላዊነት ፖሊሲው ይመልከቱ። ስህተቶች፣ የተደራሽነት ችግሮች ወይም የመብት ጥያቄዎች ካሉ simonsolomon436@gmail.com ያነጋግሩ። የፕሮጀክቱ የችግር መመዝገቢያ ይፋ ነው፤ የግል መረጃ አያስቀምጡ። ባህሪያት በወደፊት ስሪቶች ሊቀየሩ ይችላሉ። ተግባራዊ የሸማች መብቶች ይጠበቃሉ።")
)
