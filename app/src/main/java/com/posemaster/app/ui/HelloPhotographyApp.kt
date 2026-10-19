package com.posemaster.app.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.posemaster.app.data.*
import com.posemaster.app.domain.*

@Composable fun HelloPhotographyApp(initialPoseId:Int?=null,notificationEvent:Int=0,vm:PoseViewModel=viewModel()) {
 val context=LocalContext.current
 val preferences=remember {AppPreferences(context)}
 val settings by preferences.state.collectAsStateWithLifecycle()
 val academy=remember {AcademyStore(context)}
 DisposableEffect(academy){onDispose{academy.close()}}
 val progress by academy.state.collectAsStateWithLifecycle()
 val translations=remember {PoseTranslations(context)}
 val language=settings.language
 val dark=settings.theme=="Dark" || settings.theme=="System" && isSystemInDarkTheme()
 // Android 15/16 draw behind system bars. Keep their icons readable when the
 // app theme differs from the device theme, including after a settings change.
 SideEffect {
  (context as? ComponentActivity)?.enableEdgeToEdge(
   statusBarStyle=SystemBarStyle.auto(android.graphics.Color.TRANSPARENT,android.graphics.Color.TRANSPARENT){dark},
   navigationBarStyle=SystemBarStyle.auto(0xE6FFFFFF.toInt(),0x801B1B1B.toInt()){dark}
  )
 }
 val palette=if(dark) darkColorScheme(
  primary=HelloGold,onPrimary=Color(0xFF231D12),primaryContainer=Color(0xFF4A442F),onPrimaryContainer=Color(0xFFF9E9BE),
  secondary=Color(0xFFB4CABB),onSecondary=Color(0xFF1A3727),secondaryContainer=Color(0xFF2C3A31),onSecondaryContainer=Color(0xFFDEEADF),
  background=Color(0xFF141B19),surface=Color(0xFF1F2925),onSurface=Color(0xFFF5F3EA),surfaceVariant=Color(0xFF2D3932),onSurfaceVariant=Color(0xFFBECABE),
  surfaceContainer=Color(0xFF223029),surfaceContainerLow=Color(0xFF1B251F),surfaceContainerHigh=Color(0xFF2A372E),surfaceContainerHighest=Color(0xFF334035)
 ) else lightColorScheme(
  primary=Color(0xFF705426),onPrimary=Color.White,primaryContainer=Color(0xFFEADFBE),onPrimaryContainer=Color(0xFF302713),
  secondary=Color(0xFF456251),onSecondary=Color.White,secondaryContainer=Color(0xFFE7ECDD),onSecondaryContainer=Color(0xFF263A2C),
  background=Color(0xFFF9F6EE),surface=Color(0xFFFFFCF5),onSurface=Color(0xFF202A24),surfaceVariant=Color(0xFFE5E9DD),onSurfaceVariant=Color(0xFF4A554D),
  surfaceContainer=Color(0xFFEDEFE3),surfaceContainerLow=Color(0xFFF3F3E9),surfaceContainerHigh=Color(0xFFE8EBDF),surfaceContainerHighest=Color(0xFFE1E5D9)
 )
 var consumedNotification by rememberSaveable {mutableIntStateOf(-1)}
 var tab by rememberSaveable {mutableIntStateOf(0)}
 var selectedId by rememberSaveable {mutableStateOf<Int?>(null)}
 var showAssistant by rememberSaveable {mutableStateOf(false)}
 var showPremium by rememberSaveable {mutableStateOf(false)}
 var query by rememberSaveable {mutableStateOf("")}
 var category by rememberSaveable {mutableStateOf("All")}
 var finder by remember {mutableStateOf(Finder())}
 val saved by vm.saved.collectAsStateWithLifecycle()
 val loading by vm.loading.collectAsStateWithLifecycle()
 val snack=remember {SnackbarHostState()}
 val openPose:(Int)->Unit={id->preferences.recordView(id);selectedId=id;showAssistant=false;showPremium=false}
 val localize:(Pose)->Pose={translations.localize(it,language)}
 LaunchedEffect(vm){vm.errors.collect{snack.showSnackbar(it)}}
 LaunchedEffect(notificationEvent,settings.role!=null){if(settings.role!=null && initialPoseId!=null && consumedNotification!=notificationEvent && vm.poses.any{it.id==initialPoseId}) {openPose(initialPoseId);consumedNotification=notificationEvent}}
 MaterialTheme(colorScheme=palette) {
  Surface(Modifier.fillMaxSize()) {
   if(settings.role==null) WelcomeScreen(language,preferences::setLanguage,preferences::setRole)
   else Scaffold(snackbarHost={SnackbarHost(snack)},bottomBar={
    NavigationBar {
     val labels=listOf(tr(language,"Home","መነሻ"),tr(language,"Explore Poses","አቋሞች"),tr(language,"Learn","ትምህርት"),tr(language,"Favorites","ተወዳጆች"),tr(language,"Profile","መገለጫ"))
     val icons=listOf(Icons.Outlined.Home,Icons.Outlined.Search,Icons.Outlined.School,Icons.Outlined.FavoriteBorder,Icons.Outlined.Person)
     labels.forEachIndexed {i,label->NavigationBarItem(selected=tab==i,onClick={tab=i;selectedId=null;showAssistant=false;showPremium=false},icon={Icon(icons[i],label)},label={Text(label,fontSize=10.sp,maxLines=1)})}
    }
   }){padding->
    BackHandler(enabled=selectedId!=null || showAssistant || showPremium){when {showPremium->showPremium=false;showAssistant->showAssistant=false;else->selectedId=null}}
    Box(Modifier.padding(padding).fillMaxSize(),contentAlignment=Alignment.TopCenter) {
     val pane=Modifier.widthIn(max=840.dp).fillMaxWidth()
     val current=vm.poses.find{it.id==selectedId}
     when {
      showPremium->SubscriptionScreen(language,{showPremium=false},{showPremium=false;tab=4},pane)
      current!=null->PoseDetailScreen(localize(current),vm.poses.filter{it.photo!=null}.map(localize),language,saved.find{it.poseId==current.id},
       savedLoading=loading,onBack={selectedId=null},onSave={name,download->vm.save(current.id,name,download)},onRemove={vm.remove(current.id)},
       onShare={shareText(context, "Hello Photography · ${localize(current).name}\n${localize(current).steps}\n${localize(current).hands}\n${localize(current).feet}")},
       onPrevious={openPose(vm.poses[(vm.poses.indexOf(current)-1+vm.poses.size)%vm.poses.size].id)},
       onNext={openPose(vm.poses[(vm.poses.indexOf(current)+1)%vm.poses.size].id)},modifier=pane)
      showAssistant->AssistantScreen(vm.poses,language,localize,openPose,{showAssistant=false},pane)
      tab==0->HomeScreen(vm.poses,saved.map{it.poseId},settings,progress.completed.size,progress.lastLesson!=null,language,localize,query,{query=it;tab=1},openPose,
       onCategory={category=it;query="";finder=Finder();tab=1},onExplore={category="All";query="";finder=Finder();tab=1},onAssistant={showAssistant=true},onLearn={tab=2},onFavorites={tab=3},onPremium={showPremium=true},modifier=pane)
      tab==1->ExploreScreen(vm.poses,language,query,{query=it},category,{category=it},finder,{finder=it},localize,openPose,{showAssistant=true},pane)
      tab==2->LearnScreen(modifier=pane,language=language,onPremium={showPremium=true})
      tab==3->FavoritesScreen(vm.poses,saved,loading,language,localize,openPose,pane)
      else->ProfileScreen(preferences=preferences,onDeleteAll={vm.clearUserData{academy.clear();preferences.clear();selectedId=null;showAssistant=false;showPremium=false;query="";category="All";tab=0}},onPremium={showPremium=true},modifier=pane)
     }
    }
   }
  }
 }
}

@Composable private fun WelcomeScreen(language:String,onLanguage:(String)->Unit,onRole:(String)->Unit) {
 var selected by rememberSaveable {mutableStateOf<String?>(null)}
 LazyColumn(Modifier.fillMaxSize().safeDrawingPadding().testTag("welcome"),contentPadding=PaddingValues(24.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
  item {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("HELLO\nPHOTOGRAPHY",letterSpacing=2.sp,fontWeight=FontWeight.Bold);TextButton(onClick={onLanguage(if(language=="en")"am" else "en")}){Text(if(language=="en")"አማርኛ" else "English")}}}
  item {Box {PhotographyBackdrop(Modifier.fillMaxWidth().height(180.dp));Column(Modifier.padding(22.dp).align(Alignment.BottomStart)){Icon(Icons.Outlined.CameraAlt,null,tint=HelloGold,modifier=Modifier.size(40.dp));Text("A NEW PERSPECTIVE",color=Color.White,letterSpacing=3.sp,fontSize=11.sp,modifier=Modifier.padding(top=12.dp))}}}
  item {HpHeading(tr(language,"Welcome to Hello Photography","ወደ ሄሎ ፎቶግራፊ እንኳን ደህና መጡ"));Text(tr(language,"Learn to Pose. Capture Better Photos. Look Amazing.","አቋም ይማሩ። የተሻሉ ፎቶዎችን ያንሱ። በፎቶ ይዋቡ።"),Modifier.padding(top=10.dp),color=MaterialTheme.colorScheme.onSurfaceVariant)}
  val roles=listOf(
   Triple("photographer",tr(language,"I'm a Photographer","ፎቶግራፈር ነኝ"),tr(language,"Direct with confidence. Explore posing, composition, lighting, weddings, portraits, and business lessons.","በመተማመን ይምሩ። አቋም፣ ቅንብር፣ መብራት፣ ሰርግ፣ የፊት ፎቶ እና የንግድ ትምህርቶችን ያግኙ።")),
   Triple("model",tr(language,"I'm a Model","ሞዴል ነኝ"),tr(language,"Find your expression. Fashion, hands, body positioning, movement, confidence, and portfolio preparation.","የራስዎን ገጽታ ያግኙ። ፋሽን፣ የእጅ እና የሰውነት አቋም፣ እንቅስቃሴ፣ መተማመን እና ፖርትፎሊዮ ዝግጅት።")),
   Triple("everyone",tr(language,"Just for Me (Everyone)","ለራሴ (ለሁሉም)"),tr(language,"Make everyday moments memorable. Selfies, birthdays, graduation, travel, friends, and phone photography.","የዕለት ተዕለት ጊዜዎችን የማይረሱ ያድርጉ። ሴልፊ፣ ልደት፣ ምረቃ፣ ጉዞ፣ ጓደኞች እና የስልክ ፎቶግራፊ።"))
  )
  items(roles,key={it.first}){(id,title,description)->
   Card(onClick={selected=id},modifier=Modifier.fillMaxWidth().testTag("role_$id"),shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=if(selected==id)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
    Row(Modifier.padding(18.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.Top){Icon(if(id=="photographer")Icons.Outlined.CameraAlt else if(id=="model")Icons.Outlined.Person else Icons.Outlined.Smartphone,title,Modifier.size(28.dp));Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.Bold,fontSize=19.sp);Text(description,Modifier.padding(top=8.dp),style=MaterialTheme.typography.bodyMedium)};RadioButton(selected==id,{selected=id})}
   }
  }
  item {Button(onClick={selected?.let(onRole)},enabled=selected!=null,modifier=Modifier.fillMaxWidth().testTag("welcome_continue")){Text(tr(language,"Continue","ቀጥል"))};Text(tr(language,"Choose a role to continue. You can change it in Profile anytime.","ለመቀጠል የሚስማማዎትን ይምረጡ። በመገለጫ ውስጥ በማንኛውም ጊዜ መቀየር ይችላሉ።"),Modifier.padding(top=8.dp),style=MaterialTheme.typography.bodySmall)}
 }
}

private val photoCategories=listOf("Male poses","Female poses","Couple photography","Group photography","Casual & selfie poses")

private fun roleCategories(role:String?)=when(role){
 "photographer"->listOf("Portrait photography","Wedding photography","Studio photography","Professional business portraits","Couple photography")
 "model"->listOf("Fashion photography","Standing poses","Sitting poses","Walking poses","Female poses","Male poses")
 else->listOf("Casual & selfie poses","Selfie poses","Birthday photography","Graduation photography","Outdoor photography","Group photography","Instagram and TikTok poses")
}
@Composable private fun HomeScreen(poses:List<Pose>,favoriteIds:List<Int>,settings:UserSettings,completed:Int,hasResume:Boolean,language:String,localize:(Pose)->Pose,query:String,onQuery:(String)->Unit,onOpen:(Int)->Unit,onCategory:(String)->Unit,onExplore:()->Unit,onAssistant:()->Unit,onLearn:()->Unit,onFavorites:()->Unit,onPremium:()->Unit,modifier:Modifier) {
 val photos=poses.filter{it.photo!=null}
 val recommendations=poses.filter{it.category in roleCategories(settings.role)}.sortedBy{it.photo==null}
 val featuredOptions=recommendations.filter{it.photo!=null}.ifEmpty{photos}.ifEmpty{recommendations}.ifEmpty{poses}
 val featured=featuredOptions[(java.time.LocalDate.now().dayOfYear-1)%featuredOptions.size]
 LazyColumn(modifier.testTag("home_list"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
  item {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("HELLO\nPHOTOGRAPHY",fontWeight=FontWeight.Bold,letterSpacing=2.sp);AssistChip(onClick=onPremium,label={Text(tr(language,"✦ Premium","✦ ፕሪሚየም"))})}}
  item {Text(tr(language,when(settings.role){"photographer"->"WELCOME, PHOTOGRAPHER";"model"->"YOUR NEXT GREAT FRAME";else->"MAKE A MOMENT YOURS"},when(settings.role){"photographer"->"ፎቶግራፈር፣ እንኳን ደህና መጡ";"model"->"ቀጣዩ ምርጥ ፎቶዎ";else->"ጊዜውን የራስዎ ያድርጉ"}),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary);HpHeading(tr(language,when(settings.role){"photographer"->"See the light.\nTell the story.";"model"->"Find your angle.\nOwn the frame.";else->"A little direction.\nA beautiful memory."},when(settings.role){"photographer"->"ብርሃኑን ይመልከቱ።\nታሪኩን ይንገሩ።";"model"->"አንግልዎን ያግኙ።\nበፎቶ ይተማመኑ።";else->"ትንሽ መመሪያ።\nውብ ትዝታ።"}),Modifier.padding(top=10.dp))}
  item {SearchBox(language,query,onQuery)}
  item {Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick=onExplore,modifier=Modifier.weight(1f)){Text(tr(language,"Explore poses","አቋሞችን ያስሱ"))};OutlinedButton(onClick=onLearn,modifier=Modifier.weight(1f)){Text(tr(language,"Start learning","መማር ይጀምሩ"))}}}
  item {HpSection(tr(language,"Today's featured pose","የዛሬ ልዩ አቋም"),tr(language,"A daily edit for your interests","ለፍላጎትዎ የተመረጠ የዕለቱ ሃሳብ"));Spacer(Modifier.height(12.dp));HpPoseCard(localize(featured),language,true,onOpen)}
  if(photos.isNotEmpty()) {
   item {HpSection(tr(language,"AI pose photo library","በAI የተፈጠሩ የአቋም ፎቶዎች"),tr(language,"Practice with clear visual references, available offline.","ከመስመር ውጭ በሚገኙ ግልጽ ማጣቀሻዎች ይለማመዱ።"))}
   items(photoCategories,key={"photo_category_$it"}){c->
    val categoryPhotos=photos.filter{it.category==c}
    if(categoryPhotos.isNotEmpty()) Card(onClick={onCategory(c)},shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth().testTag("photo_category_${photoCategories.indexOf(c)}")) {
     Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
      PoseReferenceImage(localize(categoryPhotos.first()),Modifier.width(84.dp).height(112.dp),showLabel=false,language=language)
      Column(Modifier.weight(1f)){Text(categoryLabel(c,language),style=MaterialTheme.typography.titleMedium);Text(tr(language,"${categoryPhotos.size} photo guides · Open collection →","${categoryPhotos.size} የፎቶ መመሪያዎች፤ ስብስቡን ክፈት →"),style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=6.dp))}
     }
    }
   }
  }
  item {HpNotice(tr(language,"AI Pose Assistant","የAI አቋም ረዳት"),tr(language,"Tell us the occasion, outfit, and location. Get useful offline recommendations using transparent rules.","ዝግጅቱን፣ ልብሱን እና ቦታውን ይንገሩን። በግልጽ መመሪያዎች ላይ የተመሠረቱ ከመስመር ውጭ ምክሮችን ያግኙ።"));TextButton(onClick=onAssistant){Text(tr(language,"Find my pose →","የእኔን አቋም ፈልግ →"))}}
  item {HpSection(tr(language,"Popular categories","ተወዳጅ ምድቦች"));Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){roleCategories(settings.role).forEach{c->SuggestionChip(onClick={onCategory(c)},label={Text(categoryLabel(c,language))})}}}
  item {HpSection(tr(language,"Trending posing ideas","የተመረጡ የአቋም ሃሳቦች"),tr(language,"Editor's picks · curated, not live popularity rankings","የአዘጋጁ ምርጫዎች፤ በቀጥታ የሕዝብ ድምፅ ውጤት አይደሉም"))}
  items(recommendations.filter{it.id!=featured.id}.take(3),key={"recommended_${it.id}"}){HpPoseCard(localize(it),language,onOpen=onOpen)}
  item {HpSection(tr(language,"Recommended tutorials","የተመከሩ ትምህርቶች"));Card(onClick=onLearn,shape=RoundedCornerShape(20.dp)){Column(Modifier.fillMaxWidth().padding(20.dp)){Icon(Icons.Outlined.School,null,tint=MaterialTheme.colorScheme.primary);Text(tr(language,when(settings.role){"model"->"Direction, expression & confidence";"everyone"->"Better photos with your phone";else->"Light, composition & client direction"},when(settings.role){"model"->"መመሪያ፣ ገጽታ እና መተማመን";"everyone"->"በስልክዎ የተሻሉ ፎቶዎች";else->"ብርሃን፣ ቅንብር እና ደንበኛን መምራት"}),Modifier.padding(top=10.dp),fontWeight=FontWeight.Bold);Text(tr(language,"Open the Photography Academy →","የፎቶግራፊ አካዳሚውን ክፈት →"),Modifier.padding(top=8.dp))}}}
  item {HpSection(tr(language,"Continue learning","ትምህርቱን ይቀጥሉ"));Text(tr(language,"$completed lessons completed. Your progress is saved on this device.","$completed ትምህርቶች ተጠናቀዋል። እድገትዎ በዚህ መሣሪያ ላይ ይቀመጣል።"));TextButton(onClick=onLearn){Text(tr(language,if(hasResume)"Resume last lesson →" else "Choose your first course →",if(hasResume)"የመጨረሻውን ትምህርት ቀጥል →" else "የመጀመሪያዎን ኮርስ ይምረጡ →"))}}
  if(settings.recentIds.isNotEmpty()) {
   item {HpSection(tr(language,"Recently viewed","በቅርቡ የታዩ"))}
   items(settings.recentIds.take(3).mapNotNull{id->poses.find{it.id==id}},key={"recent_${it.id}"}){HpPoseCard(localize(it),language,onOpen=onOpen)}
  }
  item {HpSection(tr(language,"Your favorites","የእርስዎ ተወዳጆች"));Text(tr(language,"${favoriteIds.size} saved guides · available offline","${favoriteIds.size} የተቀመጡ መመሪያዎች፤ ከመስመር ውጭ ይገኛሉ"));TextButton(onClick=onFavorites){Text(tr(language,"Open my collection →","ስብስቤን ክፈት →"))}}
  item {HpNotice(tr(language,"Made for every body","ለሁሉም ሰው"),tr(language,"Explore ${poses.size} guides offline. References are labeled as illustrations or photos, with photo credits shown on each guide. Premium collections require future verified purchases.","${poses.size} መመሪያዎችን ከመስመር ውጭ ያስሱ። ማጣቀሻዎች ምሳሌ ወይም ፎቶ መሆናቸው ይገለጻል፤ የፎቶ ባለቤት በመመሪያው ይታያል። የፕሪሚየም ስብስቦች የተረጋገጠ ግዢ ይፈልጋሉ።"))}
 }
}

@Composable private fun SearchBox(language:String,query:String,onQuery:(String)->Unit) {OutlinedTextField(query,onQuery,placeholder={Text(tr(language,"Search poses, moods, and moments","አቋሞችን እና ሃሳቦችን ይፈልጉ"))},leadingIcon={Icon(Icons.Outlined.Search,null)},singleLine=true,modifier=Modifier.fillMaxWidth().testTag("pose_search"),shape=RoundedCornerShape(18.dp))}
@Composable private fun ExploreScreen(poses:List<Pose>,language:String,query:String,onQuery:(String)->Unit,category:String,onCategory:(String)->Unit,finder:Finder,onFinder:(Finder)->Unit,localize:(Pose)->Pose,onOpen:(Int)->Unit,onAssistant:()->Unit,modifier:Modifier) {
 var filters by rememberSaveable {mutableStateOf(false)}
 var photosOnly by rememberSaveable {mutableStateOf(false)}
 val matching=PoseFilter.apply(poses,"",category,finder).filter{(!photosOnly || it.photo!=null) && (query.isBlank() || "${localize(it).name} ${localize(it).steps} ${categoryLabel(it.category,language)} ${it.name} ${it.category}".contains(query,true))}.sortedBy{it.photo==null}
 LazyColumn(modifier.testTag("explore_list"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
  item {HpHeading(tr(language,"Find your next\nfavorite pose.","ቀጣዩን ተወዳጅ\nአቋምዎን ያግኙ።"));Text(tr(language,"Every occasion. Every body. Your perspective.","ለማንኛውም ዝግጅት። ለሁሉም ሰው። የራስዎ እይታ።"),Modifier.padding(top=8.dp))}
  item {SearchBox(language,query,onQuery)}
  if(poses.any{it.photo!=null}) item {FilterChip(selected=photosOnly,onClick={photosOnly=!photosOnly},label={Text(tr(language,"AI photo guides only","በAI የተፈጠሩ የፎቶ መመሪያዎች ብቻ"))},modifier=Modifier.testTag("photo_guides_filter"))}
  item {Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){(listOf("All")+photoCategories.filter{c->poses.any{it.photo!=null && it.category==c}}+poses.map{it.category}.distinct()).distinct().forEach{c->FilterChip(selected=category==c,onClick={onCategory(c)},label={Text(if(c=="All")tr(language,"All","ሁሉም") else categoryLabel(c,language))})}}}
  item {Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick={filters=!filters},modifier=Modifier.weight(1f)){Icon(Icons.Outlined.Tune,null);Text(tr(language," Filters"," ማጣሪያዎች"))};OutlinedButton(onClick=onAssistant,modifier=Modifier.weight(1f)){Text(tr(language,"Pose assistant","አቋም ረዳት"))}}
   if(filters){
    HpChoice(tr(language,"Subject","ተነሺ"),finder.subject,listOf("Any","Individual","Couple","Family","Group"),{onFinder(finder.copy(subject=it))})
    HpChoice(tr(language,"Style","ዘይቤ"),finder.style,listOf("Any","Casual","Professional","Romantic","Fashion","Creative"),{onFinder(finder.copy(style=it))})
    HpChoice(tr(language,"Location","ቦታ"),finder.location,listOf("Any","Studio","Street","Home","Park","Café","Outdoor"),{onFinder(finder.copy(location=it))})
    HpChoice(tr(language,"Position","አቋም"),finder.position,listOf("Any","Standing","Sitting","Walking","Close-up"),{onFinder(finder.copy(position=it))})
    HpChoice(tr(language,"Experience","ልምድ"),finder.experience,listOf("Any","Beginner","Intermediate","Professional"),{onFinder(finder.copy(experience=it))})
   }
   if(query.isNotBlank() || category!="All" || finder!=Finder() || photosOnly) TextButton(onClick={onQuery("");onCategory("All");onFinder(Finder());photosOnly=false}){Text(tr(language,"Reset search and filters","ፍለጋውን እና ማጣሪያዎችን አጽዳ"))}
  }
  item {Text(tr(language,"${matching.size} guides to explore","ለማሰስ ${matching.size} መመሪያዎች"),style=MaterialTheme.typography.labelLarge)}
  if(matching.isEmpty()) item {HpNotice(tr(language,"No matches yet","የሚስማማ ውጤት የለም"),tr(language,"Try fewer filters or a shorter search. The offline sample library does not cover every combination.","ጥቂት ማጣሪያዎችን ወይም አጭር ፍለጋ ይሞክሩ። ከመስመር ውጭ ያለው ስብስብ ሁሉንም ጥምረት አይሸፍንም።"))}
  items(matching,key={it.id}){HpPoseCard(localize(it),language,onOpen=onOpen)}
 }
}

@Composable private fun PoseDetailScreen(pose:Pose,photoPoses:List<Pose>,language:String,entry:SavedPose?,savedLoading:Boolean,onBack:()->Unit,onSave:(String,Boolean)->Unit,onRemove:()->Unit,onShare:()->Unit,onPrevious:()->Unit,onNext:()->Unit,modifier:Modifier) {
 var showPhotoViewer by rememberSaveable(pose.id){mutableStateOf(false)}
 if(showPhotoViewer && pose.photo!=null) PosePhotoViewer(photoPoses,pose.id,language,onDismiss={showPhotoViewer=false})
 var collection by rememberSaveable(pose.id){mutableStateOf(entry?.collection ?: "My favorites")}
 var collectionEdited by rememberSaveable(pose.id){mutableStateOf(false)}
 LaunchedEffect(pose.id,entry?.collection){if(!collectionEdited && entry!=null) collection=entry.collection}
 LazyColumn(modifier.testTag("pose_detail"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
  item {TextButton(onClick=onBack){Text(tr(language,"← Back to collection","← ወደ ስብስቡ ተመለስ"))}}
  item {
   PoseReferenceImage(pose,Modifier.fillMaxWidth().height(if(pose.photo==null)320.dp else 420.dp).then(if(pose.photo!=null)Modifier.clickable(onClickLabel=tr(language,"View full-screen photo","ሙሉ ማያ ፎቶን ተመልከት")){showPhotoViewer=true} else Modifier),language=language)
   if(pose.photo!=null) OutlinedButton(onClick={showPhotoViewer=true},modifier=Modifier.fillMaxWidth().testTag("open_photo_viewer")){Icon(Icons.Outlined.Fullscreen,null);Text(tr(language," View full-screen photo"," ሙሉ ማያ ፎቶን ተመልከት"))}
   Text(if(pose.photo==null) tr(language,"Original reference illustration · no external photograph","የመጀመሪያ ማብራሪያ ሥዕል፤ ከውጭ የተገኘ ፎቶ አይደለም")
    else tr(language,"Photo credit: ${pose.photo.credit}","የፎቶ ባለቤት፦ ${pose.photo.credit}"),style=MaterialTheme.typography.labelSmall)
   pose.photo?.let {Text(tr(language,"Image rights: ${it.license}","የፎቶ መብት፦ ${it.license}"),style=MaterialTheme.typography.labelSmall)}
  }
  item {Text(categoryLabel(pose.category,language),color=MaterialTheme.colorScheme.primary);HpHeading(pose.name);Text("${pose.difficulty} · ${pose.position}",style=MaterialTheme.typography.labelMedium)}
  if(pose.premium) item {HpNotice(tr(language,"Collection preview","የስብስብ ቅድመ እይታ"),tr(language,"This original sample guide is free to read. Future premium collections and advanced courses require verified purchases.","ይህ የተዘጋጀ ናሙና መመሪያ ለማንበብ ነፃ ነው። የወደፊት ፕሪሚየም ስብስቦች እና ከፍተኛ ኮርሶች የተረጋገጠ ግዢ ይፈልጋሉ።"))}
  item {HpTip(tr(language,"01 · Body positioning","01 · የሰውነት አቋም"),pose.steps)}
  item {HpTip(tr(language,"02 · Head & expression","02 · ራስ እና ገጽታ"),pose.face)}
  item {HpTip(tr(language,"03 · Hands","03 · እጆች"),pose.hands)}
  item {HpTip(tr(language,"04 · Feet & support","04 · እግሮች እና ድጋፍ"),pose.feet)}
  item {HpTip(tr(language,"05 · Camera angle & height","05 · የካሜራ አንግል እና ከፍታ"),tr(language,pose.angle,if(pose.position=="Close-up")"ካሜራውን ከዓይን ጋር በእኩል ከፍታ ያድርጉ። ሌንሱ ከፊቱ ጋር ትይዩ ይሁን።" else "በደረት ከፍታ ይጀምሩ፤ አድማሱን ሳያዘነብሉ ያስተካክሉ።"))}
  item {HpTip(tr(language,"06 · Lighting","06 · ብርሃን"),tr(language,pose.lighting,"ለስላሳ የመስኮት ብርሃን ወይም ክፍት ጥላ ይጠቀሙ። ፊትን ወደ ብርሃኑ ያዙሩ፤ ፀሐይን በቀጥታ አይመልከቱ።"))}
  item {HpTip(tr(language,"Common mistakes","የተለመዱ ስህተቶች"),tr(language,pose.mistakes,"መገጣጠሚያዎችን አያጠንክሩ። የግድ ገጽታ፣ የተቆረጡ ጣቶች እና የተጨናነቀ ዳራ ያስወግዱ። ምቾትን ቅድሚያ ይስጡ።"));HpTip(tr(language,"For photographers","ለፎቶግራፈሮች"),tr(language,pose.photographerTip,"እንቅስቃሴውን ያሳዩ፤ ከማስተካከልዎ በፊት ፈቃድ ይጠይቁ። በእንቅስቃሴዎች መካከል ተከታታይ ፎቶ ያንሱ።"));HpTip(tr(language,"For models","ለሞዴሎች"),tr(language,pose.modelTip,"ትንፋሽ ያውጡ፣ ትከሻዎችን ያሳርፉ። ለእርስዎ የሚመች የእንቅስቃሴ መጠን ይምረጡ።"))}
  item {OutlinedTextField(collection,{collection=it;collectionEdited=true},label={Text(tr(language,"Collection name","የስብስብ ስም"))},singleLine=true,modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(10.dp));Button(onClick={onSave(collection,entry?.downloaded ?: false)},enabled=!savedLoading,modifier=Modifier.fillMaxWidth()){Text(tr(language,if(entry==null)"Save favorite" else "Update favorite",if(entry==null)"ወደ ተወዳጆች አስቀምጥ" else "ተወዳጁን አዘምን"))};OutlinedButton(onClick=onShare,modifier=Modifier.fillMaxWidth()){Icon(Icons.Outlined.Share,null);Text(tr(language," Share posing tip"," የአቋም ምክር አጋራ"))};TextButton(onClick={onSave(collection,entry?.downloaded!=true)},enabled=!savedLoading){Text(tr(language,if(entry?.downloaded==true)"Remove offline bookmark" else "Keep in offline guides",if(entry?.downloaded==true)"የኦፍላይን ምልክቱን አስወግድ" else "በኦፍላይን መመሪያዎች አስቀምጥ"))};Text(tr(language,"All guides and their reference images are bundled offline. No separate image download is needed.","ሁሉም መመሪያዎች እና ማጣቀሻ ምስሎች ከመስመር ውጭ ይገኛሉ። ተጨማሪ ምስል ማውረድ አያስፈልግም።"),style=MaterialTheme.typography.bodySmall);if(entry!=null) TextButton(onClick=onRemove){Text(tr(language,"Remove from favorites","ከተወዳጆች አስወግድ"))}}
  item {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){OutlinedButton(onClick=onPrevious){Text(tr(language,"← Previous","← ቀዳሚ"))};OutlinedButton(onClick=onNext){Text(tr(language,"Next →","ቀጣይ →"))}}}
 }
}

@Composable private fun FavoritesScreen(poses:List<Pose>,saved:List<SavedPose>,loading:Boolean,language:String,localize:(Pose)->Pose,onOpen:(Int)->Unit,modifier:Modifier){
 var collection by rememberSaveable{mutableStateOf("All")};var offline by rememberSaveable{mutableStateOf(false)}
 val entries=saved.filter{(collection=="All" || it.collection==collection) && (!offline || it.downloaded)}
 LazyColumn(modifier.testTag("favorites_list"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
  item {HpHeading(tr(language,"Your inspiration,\ncollected.","የእርስዎ ሃሳቦች፣\nበአንድ ቦታ።"));Text(tr(language,"${saved.size} saved · Free plan: 20 favorites. Existing collections are preserved.","${saved.size} ተቀምጠዋል፤ ነፃ እቅድ፦ 20 ተወዳጆች። የነበሩ ስብስቦች ይጠበቃሉ።"),Modifier.padding(top=8.dp));HpChoice(tr(language,"Collection","ስብስብ"),collection,listOf("All")+saved.map{it.collection}.distinct(),{collection=it});Row(verticalAlignment=Alignment.CenterVertically){Switch(offline,{offline=it});Text(tr(language,"Offline bookmarks only","የኦፍላይን ምልክቶች ብቻ"))}}
  if(loading) item {CircularProgressIndicator()}
  if(!loading && entries.isEmpty()) item {HpNotice(tr(language,"Room for inspiration","ለአዲስ ሃሳብ ቦታ አለ"),tr(language,"Save a pose, give your collection a name, and return whenever you need it. All sample guides work offline.","አቋም ያስቀምጡ፣ ስብስቡን ይሰይሙ እና በፈለጉ ጊዜ ይመለሱ። ሁሉም ናሙናዎች ከመስመር ውጭ ይሠራሉ።"))}
  items(entries,key={it.poseId}){entry->poses.find{it.id==entry.poseId}?.let{HpPoseCard(localize(it),language,onOpen=onOpen);Text(entry.collection,style=MaterialTheme.typography.labelSmall)}}
 }
}

fun shareText(context:android.content.Context,text:String){
 try {context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {type="text/plain";putExtra(Intent.EXTRA_TEXT,text)},"Hello Photography").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))} catch(_:android.content.ActivityNotFoundException){android.widget.Toast.makeText(context,"No sharing app is available",android.widget.Toast.LENGTH_SHORT).show()}
}
