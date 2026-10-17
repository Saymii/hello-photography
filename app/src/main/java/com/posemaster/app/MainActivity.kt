package com.posemaster.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.posemaster.app.domain.*
import com.posemaster.app.ui.PoseViewModel

private val Gold = Color(0xFFD5B879)
class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { PoseMaster() } }
}
@Composable fun PoseMaster(vm: PoseViewModel = viewModel()) {
 val context = LocalContext.current
 val prefs = remember { context.getSharedPreferences("preferences", 0) }
 var onboarded by rememberSaveable { mutableStateOf(prefs.getBoolean("onboarded", false)) }
 var onboardingPage by rememberSaveable { mutableIntStateOf(0) }
 var theme by rememberSaveable { mutableStateOf(prefs.getString("theme", "System") ?: "System") }
 val dark = theme == "Dark" || theme == "System" && isSystemInDarkTheme()
 val colors = if(dark) darkColorScheme(primary=Gold, background=Color(0xFF171917), surface=Color(0xFF222521), onPrimary=Color(0xFF20221E)) else lightColorScheme(primary=Color(0xFF785D28), background=Color(0xFFF8F6F0), surface=Color.White)
 var tab by rememberSaveable { mutableIntStateOf(0) }
 var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }
 var query by rememberSaveable { mutableStateOf("") }
 var category by rememberSaveable { mutableStateOf("All") }
 var finder by remember { mutableStateOf(Finder()) }
 var showFinder by rememberSaveable { mutableStateOf(false) }
 var premium by rememberSaveable { mutableStateOf(false) }
 var interests by rememberSaveable { mutableStateOf(prefs.getString("interests", "Portrait photography") ?: "Portrait photography") }
 val saved by vm.saved.collectAsStateWithLifecycle()
 val savedLoading by vm.loading.collectAsStateWithLifecycle()
 val snackbar = remember { SnackbarHostState() }
 LaunchedEffect(vm) { vm.errors.collect { snackbar.showSnackbar(it) } }
 MaterialTheme(colorScheme=colors) {
 Surface(Modifier.fillMaxSize()) {
 if(!onboarded) {
  Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp), verticalArrangement=Arrangement.Center) {
   Text("P O S E M A S T E R", color=MaterialTheme.colorScheme.primary, letterSpacing=3.sp)
   Spacer(Modifier.height(24.dp))
   PoseArt(onboardingPage, Modifier.fillMaxWidth().height(260.dp))
   Spacer(Modifier.height(24.dp))
   Text(listOf("Your perfect pose,\nevery time.","Find your own rhythm.","Create with confidence.")[onboardingPage], fontSize=36.sp, fontFamily=FontFamily.Serif)
   Text(listOf("A little direction. A beautiful difference. Discover thoughtful guides for every body.","48 original guides, clear direction, and photography lessons. All sample guides are bundled offline.","Choose an interest. No account required. Cloud sync and purchases are not enabled in this development build.")[onboardingPage], Modifier.padding(vertical=16.dp))
   if(onboardingPage==2) Choice("Your interest", interests, vm.poses.map{it.category}.distinct()) { interests=it }
   Button(onClick={ if(onboardingPage<2) onboardingPage++ else { prefs.edit().putBoolean("onboarded",true).putString("interests",interests).apply(); onboarded=true } }, Modifier.fillMaxWidth()) { Text(if(onboardingPage==2) "Start exploring" else "Continue") }
   Text("${onboardingPage+1} / 3 · Private by design", Modifier.padding(top=12.dp), style=MaterialTheme.typography.labelMedium)
  }
 } else Scaffold(snackbarHost={SnackbarHost(snackbar)}, bottomBar={
  NavigationBar { listOf("Home","Explore","Camera Guide","Favorites","Profile").forEachIndexed { index, label ->
   val icon = listOf(Icons.Outlined.Home,Icons.Outlined.Search,Icons.Outlined.CameraAlt,Icons.Outlined.FavoriteBorder,Icons.Outlined.Person)[index]
   NavigationBarItem(selected=tab==index, onClick={tab=index; selectedId=null}, icon={Icon(icon,label)}, label={Text(label, maxLines=1, fontSize=10.sp)})
  } }
 }) { padding ->
  val pose = vm.poses.find { it.id==selectedId }
  BackHandler(enabled=pose!=null) { selectedId=null }
  if(pose!=null) {
   var collection by remember(pose.id) { mutableStateOf(saved.find {it.poseId==pose.id}?.collection ?: "My favorites") }
   val entry = saved.find {it.poseId==pose.id}
   LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding=PaddingValues(20.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
    item { TextButton(onClick={selectedId=null}) { Text("← Back to collection") } }
    item { PoseArt(pose.id,Modifier.fillMaxWidth().height(290.dp)); Text("Original placeholder illustration · replace with licensed photo",style=MaterialTheme.typography.labelSmall) }
    item { Text(pose.category.uppercase(), color=MaterialTheme.colorScheme.primary, style=MaterialTheme.typography.labelMedium); Heading(pose.name); Text("${pose.difficulty} · ${pose.position} · ${if(pose.premium) "Premium preview" else "Free guide"}") }
    if(pose.premium) item { Notice("Premium preview", "Sample instructions are available in development. Purchases and premium entitlements are not active.") }
    item { GuideBlock("01  Body positioning",pose.steps); GuideBlock("02  Head & expression",pose.face); GuideBlock("03  Hands",pose.hands); GuideBlock("04  Feet & support",pose.feet); GuideBlock("05  Camera angle & height",pose.angle); GuideBlock("06  Lighting",pose.lighting); GuideBlock("Common mistakes",pose.mistakes); GuideBlock("For photographers",pose.photographerTip); GuideBlock("For models",pose.modelTip) }
    item { OutlinedTextField(collection,{collection=it},label={Text("Collection name")},modifier=Modifier.fillMaxWidth(),singleLine=true)
     Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
      Button(onClick={vm.save(pose.id,collection,entry?.downloaded ?: false)}) { Text(if(entry!=null) "Update favorite" else "Save favorite") }
      OutlinedButton(onClick={context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {type="text/plain"; putExtra(Intent.EXTRA_TEXT,"PoseMaster: ${pose.name}\n${pose.steps}\nHands: ${pose.hands}\nFeet: ${pose.feet}")},"Share pose guide"))}) { Text("Share") }
     }
     TextButton(onClick={vm.save(pose.id,collection,entry?.downloaded != true)}) { Text(if(entry?.downloaded==true) "Remove offline bookmark" else "Keep in offline guides") }
     Text("All sample text and illustrations are bundled offline. This marks a guide for your offline list; it does not download a photo.",style=MaterialTheme.typography.bodySmall)
     if(entry!=null) TextButton(onClick={vm.remove(pose.id)}) { Text("Remove from favorites") }
    }
    item { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { OutlinedButton(onClick={selectedId=vm.poses[(vm.poses.indexOf(pose)-1+vm.poses.size)%vm.poses.size].id}) {Text("← Previous")}; OutlinedButton(onClick={selectedId=vm.poses[(vm.poses.indexOf(pose)+1)%vm.poses.size].id}) {Text("Next →")} } }
   }
  } else when(tab) {
   0,1 -> LazyColumn(Modifier.padding(padding), contentPadding=PaddingValues(20.dp), verticalArrangement=Arrangement.spacedBy(18.dp)) {
    item { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) { Text("POSEMASTER",letterSpacing=3.sp,fontWeight=FontWeight.Bold); AssistChip(onClick={premium=true},label={Text("✦  PREMIUM")}) }
     Text(if(tab==0) "A little inspiration.\nA remarkable frame." else "Find your next\nfavorite pose.",fontFamily=FontFamily.Serif,fontSize=34.sp,lineHeight=39.sp,modifier=Modifier.padding(vertical=16.dp))
     Text(if(tab==0) "YOUR PERFECT POSE, EVERY TIME." else "48 ORIGINAL GUIDES · EVERY BODY WELCOME",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
    }
    item { OutlinedTextField(query,{query=it},placeholder={Text("Search poses, moods, and moments")},leadingIcon={Icon(Icons.Outlined.Search,null)},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp)) }
    item { Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) { (listOf("All")+vm.poses.map{it.category}.distinct()).forEach { c -> FilterChip(category==c,{category=c;tab=1},label={Text(c)}) } } }
    item { OutlinedButton(onClick={showFinder=!showFinder},modifier=Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Tune,null); Text("  Smart pose finder · offline") }
     if(showFinder) {
      Choice("Subject",finder.subject,listOf("Any","Individual","Couple","Family","Group")){finder=finder.copy(subject=it)}
      Choice("Style",finder.style,listOf("Any","Casual","Professional","Romantic","Fashion","Creative")){finder=finder.copy(style=it)}
      Choice("Location",finder.location,listOf("Any","Studio","Street","Home","Park","Café","Outdoor")){finder=finder.copy(location=it)}
      Choice("Position",finder.position,listOf("Any","Standing","Sitting","Walking","Close-up")){finder=finder.copy(position=it)}
      Choice("Experience",finder.experience,listOf("Any","Beginner","Intermediate","Professional")){finder=finder.copy(experience=it)}
      TextButton(onClick={finder=Finder();query="";category="All"}) {Text("Reset filters")}
     }
    }
    if(tab==0 && query.isBlank() && category=="All" && !showFinder) {
     item { SectionTitle("The daily edit", "A moment worth making") }
     item { PoseCard(vm.poses[(java.time.LocalDate.now().dayOfYear-1)%vm.poses.size],true) {selectedId=it} }
     item { SectionTitle("Selected for you", interests) }
     items(vm.poses.filter {it.category==interests}) {p -> PoseCard(p,false){selectedId=it} }
     item { SectionTitle("Start with confidence", "Easy ideas. Beautiful results.") }
     items(vm.poses.filter {it.difficulty=="Beginner"}.take(3)) {p -> PoseCard(p,false){selectedId=it} }
     item { Notice("The editor’s collection", "Explore fashion and studio premium previews. No payment required in development."); TextButton(onClick={premium=true}) { Text("Discover premium →") } }
     item { SectionTitle("From the field notes", "Learn to see the light"); GuideBlock("Open shade, open possibilities", "Place your subject just inside the shade, facing open sky. Check for bright patches across the face."); TextButton(onClick={tab=2}) {Text("Read camera guides →")} }
    } else {
     val results=PoseFilter.apply(vm.poses,query,category,if(showFinder) finder else Finder())
     item { SectionTitle("${results.size} poses to explore", "Choose comfort. Make it yours.") }
     if(results.isEmpty()) item { Notice("No matching poses", "Try a broader combination or reset your filters. The sample library does not cover every combination.") }
     items(results,key={it.id}) {p -> PoseCard(p,false){selectedId=it} }
    }
   }
   2 -> LazyColumn(Modifier.padding(padding),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
    item { Heading("Behind the lens"); Text("Small lessons for stronger photographs.",color=MaterialTheme.colorScheme.primary) }
    item { ThirdsIllustration() }
    items(lessons) {(title,copy) -> GuideBlock(title,copy)}
   }
   3 -> {
    var collectionFilter by rememberSaveable {mutableStateOf("All")}
    var offlineOnly by rememberSaveable {mutableStateOf(false)}
    val entries=saved.filter {(collectionFilter=="All" || it.collection==collectionFilter) && (!offlineOnly || it.downloaded)}
    LazyColumn(Modifier.padding(padding),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
     item { Heading("Your inspiration,\ncollected."); Text("Saved on this device with Room. No account needed."); Choice("Collection",collectionFilter,listOf("All")+saved.map{it.collection}.distinct()){collectionFilter=it}; Row(verticalAlignment=Alignment.CenterVertically) { Switch(offlineOnly,{offlineOnly=it}); Text("Offline bookmarks only") } }
     if(savedLoading) item { CircularProgressIndicator(Modifier.semantics { contentDescription="Loading saved guides" }) }
     if(!savedLoading && entries.isEmpty()) item { Notice("Room for inspiration", "Save a pose to build your first collection. Your favorites stay here after restarting the app.") }
     items(entries,key={it.poseId}) {e -> vm.poses.find{it.id==e.poseId}?.let {p -> PoseCard(p,false){selectedId=it}; Text(e.collection,style=MaterialTheme.typography.labelSmall) } }
    }
   }
   4 -> LazyColumn(Modifier.padding(padding),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
    item { Heading("Make it yours."); Text("Guest photographer",color=MaterialTheme.colorScheme.primary); Notice("Offline & private", "No account, photo uploads, tracking analytics, or cloud synchronization is enabled. You can explore without signing in.") }
    item { Choice("Appearance",theme,listOf("System","Light","Dark")){theme=it;prefs.edit().putString("theme",it).apply()}; Choice("Your inspiration",interests,vm.poses.map{it.category}.distinct()){interests=it;prefs.edit().putString("interests",it).apply()} }
    item { Notice("PoseMaster Premium", "Monthly and annual subscriptions are planned. Prices come from Google Play when configured. No products or payment backend are connected."); Button(onClick={premium=true}) {Text("View premium plans")} }
    item { GuideBlock("Privacy", "Favorites, collections, interests, and appearance are stored only on this device. No camera or photo permission is requested. Clear app storage or uninstall to erase local data. Ads are disabled until consent and production configuration are implemented."); GuideBlock("Support & policy links", "Owner support and hosted privacy-policy URLs must be supplied before release. This development build does not link to invented contact details."); GuideBlock("Accounts", "Login and account deletion are not enabled. A future account service must provide authenticated deletion, export, and sync controls."); GuideBlock("About", "PoseMaster 0.1.0 · English\nFuture translations: Amharic, Tigrinya, Arabic, Spanish, French.\nAll reference art is clearly marked original placeholder illustration.") }
   }
  }
 }
 if(premium) AlertDialog(onDismissRequest={premium=false}, title={Text("More room to create.",fontFamily=FontFamily.Serif)},text={Text("Premium will include exclusive collections, advanced filters, and ad-free offline guides.\n\nMonthly · Not configured\nAnnual · Not configured\n\nPurchases are unavailable in this development build. No entitlement is granted locally.")},confirmButton={TextButton(onClick={premium=false}) {Text("Keep exploring")}})
 }
 }
}
@Composable private fun Heading(text:String) {Text(text,fontFamily=FontFamily.Serif,fontSize=32.sp,lineHeight=38.sp)}
@Composable private fun SectionTitle(title:String,subtitle:String) {Column {Text(title,fontFamily=FontFamily.Serif,fontSize=25.sp);Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
@Composable private fun Notice(title:String,copy:String) {Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer),shape=RoundedCornerShape(20.dp)) {Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {Text(title,fontWeight=FontWeight.SemiBold);Text(copy,style=MaterialTheme.typography.bodyMedium)}}}
@Composable private fun GuideBlock(title:String,copy:String) {Column(Modifier.fillMaxWidth().padding(vertical=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {Text(title,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.primary);Text(copy,lineHeight=24.sp);HorizontalDivider(Modifier.padding(top=12.dp))}}
@Composable private fun Choice(label:String,value:String,options:List<String>,change:(String)->Unit) {
 var expanded by remember {mutableStateOf(false)}
 Box(Modifier.fillMaxWidth().padding(vertical=4.dp)) {OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth()) {Text("$label: $value",modifier=Modifier.weight(1f));Icon(Icons.Outlined.ExpandMore,null)}; DropdownMenu(expanded,{expanded=false}) {options.forEach {option->DropdownMenuItem(text={Text(option)},onClick={change(option);expanded=false})}}}
}
@Composable private fun PoseCard(p:Pose,featured:Boolean,onClick:(Int)->Unit) {
 Card(onClick={onClick(p.id)},shape=RoundedCornerShape(22.dp)) {
  if(featured) PoseArt(p.id,Modifier.fillMaxWidth().height(245.dp))
  Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
   if(!featured) PoseArt(p.id,Modifier.size(82.dp))
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {Text(p.category.uppercase(),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary);Text(p.name,fontFamily=FontFamily.Serif,fontSize=22.sp);Text("${p.difficulty} · ${if(p.premium) "Premium preview" else "Free guide"}",style=MaterialTheme.typography.bodySmall)}
   Icon(Icons.AutoMirrored.Outlined.ArrowForward,"Open ${p.name}")
  }
 }
}
@Composable private fun PoseArt(seed:Int,modifier:Modifier) {
 Box(modifier.background(Color(0xFF333D35),RoundedCornerShape(20.dp)).semantics {contentDescription="Original pose placeholder illustration; not a photographic reference"}) {
  Canvas(Modifier.fillMaxSize()) {
   drawRect(Brush.linearGradient(listOf(Color(0xFF5B6756),Color(0xFF222D28))))
   val x=size.width*.52f; val y=size.height*.26f; val r=size.minDimension*.075f
   drawCircle(Color(0xFFE6D8BB),r,Offset(x,y))
   val shoulder=Offset(x,y+r*1.8f); val hip=Offset(x+if(seed%2==0) r else -r,y+r*5.2f)
   fun line(a:Offset,b:Offset,width:Float=r*.65f) {drawLine(Color(0xFFD1B782),a,b,width,StrokeCap.Round)}
   line(shoulder,hip,r*1.5f)
   line(shoulder,Offset(x-r*2,y+r*4));line(Offset(x-r*2,y+r*4),Offset(x-r*1.2f,y+r*5))
   line(shoulder,Offset(x+r*2,y+r*3));line(Offset(x+r*2,y+r*3),Offset(x+r*1.8f,y+r*1.2f))
   line(hip,Offset(x-r*1.8f,size.height*.87f));line(hip,Offset(x+r*2,size.height*.88f))
   drawLine(Color(0xFF7A8774),Offset(size.width*.1f,size.height*.91f),Offset(size.width*.9f,size.height*.91f),2f)
  }
  Text("POSE STUDY / ${seed.toString().padStart(2,'0')}",Modifier.align(Alignment.TopStart).padding(14.dp),color=Color(0xFFF2EAD9),fontSize=10.sp,letterSpacing=2.sp)
  Text("ILLUSTRATION · PLACEHOLDER",Modifier.align(Alignment.BottomStart).padding(12.dp),color=Color.White,fontSize=9.sp)
 }
}
@Composable private fun ThirdsIllustration() {
 Column {Canvas(Modifier.fillMaxWidth().height(200.dp).background(Color(0xFF354438),RoundedCornerShape(16.dp)).semantics {contentDescription="Rule of thirds: place the face near an intersection, leaving space in the direction of the gaze"}) {
  for(i in 1..2) {drawLine(Gold.copy(alpha=.5f),Offset(size.width*i/3,0f),Offset(size.width*i/3,size.height),2f);drawLine(Gold.copy(alpha=.5f),Offset(0f,size.height*i/3),Offset(size.width,size.height*i/3),2f)}
  drawCircle(Gold,19f,Offset(size.width/3,size.height/3));drawLine(Gold,Offset(size.width/3,size.height/3+30),Offset(size.width/3,size.height*.85f),18f)
 };Text("Place the eyes near an intersection. Leave room toward the gaze.",style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=8.dp))}
}
private val lessons=listOf(
 "Portrait composition" to "Start with a clean frame. Check the edges for cut-off fingers and distracting objects. Leave breathing room around the head.",
 "Rule of thirds" to "Imagine two vertical and two horizontal lines. Place the eyes near an intersection. Treat the grid as a starting point; centered portraits can also work.",
 "Camera angles" to "Begin at the subject’s eye level. A lower angle can emphasize stature; a high angle changes body proportions. Show the subject a test image and adapt together.",
 "Natural lighting" to "Use a large window or open shade for soft transitions. Turn the face slowly and watch where shadows fall. Avoid mixed orange room light and blue daylight.",
 "Golden hour" to "Work near sunrise or sunset for softer directional light. Keep the sun behind or beside the subject and expose for the face. Never look directly into the sun.",
 "Phone photography" to "Clean the lens, tap the face to focus, and lower exposure if highlights clip. Step back rather than using an ultra-wide lens close to a face.",
 "DSLR & mirrorless" to "Start around 1/250 second for gentle movement, select eye autofocus if available, and raise ISO when needed. Check sharpness rather than relying only on the rear-screen overview.",
 "Portrait focal lengths" to "About 50–85 mm on full frame gives comfortable working distances for many portraits. On crop sensors use the equivalent field of view. Perspective depends on camera distance.",
 "Background selection" to "Look for simple color and separation. Move sideways to remove poles behind heads. Increase subject-to-background distance for softer detail.",
 "Directing a model" to "Give one instruction at a time, demonstrate it, and ask what feels comfortable. Ask permission before touch. Replace rigid commands with small actions and allow breaks.",
 "Common mistakes" to "Watch for missed focus, blown highlights, tense hands, uneven horizons, and inaccessible locations. Make one adjustment at a time and review with the subject."
)
