package com.posemaster.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.posemaster.app.BuildConfig
import com.posemaster.app.services.*

@Composable fun SubscriptionScreen(language:String,onBack:()->Unit,onPolicies:()->Unit,modifier:Modifier=Modifier) {
 val context=LocalContext.current
 var selected by remember {mutableStateOf("annual")}
 var monthly by remember {mutableStateOf<String?>(null)}
 var annual by remember {mutableStateOf<String?>(null)}
 var status by remember {mutableStateOf("")}
 DisposableEffect(Unit) {
  var catalog:PlayCatalog?=null
  if(BuildConfig.PLAY_MONTHLY_PRODUCT.isNotBlank() && BuildConfig.PLAY_ANNUAL_PRODUCT.isNotBlank()) {
   catalog=PlayCatalog(context,BillingConfiguration(BuildConfig.PLAY_MONTHLY_PRODUCT,BuildConfig.PLAY_ANNUAL_PRODUCT))
   catalog.connect {result->result.onSuccess {products->
    products.forEach {product->
     val price=product.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice
     if(product.productId==BuildConfig.PLAY_MONTHLY_PRODUCT) monthly=price
     if(product.productId==BuildConfig.PLAY_ANNUAL_PRODUCT) annual=price
    }
   }.onFailure {status=tr(language,"Google Play pricing is unavailable on this device.","በዚህ መሣሪያ የGoogle Play ዋጋ አይገኝም።")} }
  }
  onDispose {catalog?.close()}
 }
 LazyColumn(modifier,contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
  item {TextButton(onClick=onBack){Text(tr(language,"← Back","← ተመለስ"))};Text("HELLO PHOTOGRAPHY · PREMIUM",color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.labelSmall);HpHeading(tr(language,"More room\nto create.","ለመፍጠር\nየበለጠ ዕድል።"))}
  item {HpNotice(tr(language,"Create without limits","ያለ ገደብ ይፍጠሩ"),tr(language,"Premium is designed for advanced posing collections, professional courses, unlimited favorites, exclusive lessons, and an ad-free experience.","ፕሪሚየም ለከፍተኛ የአቋም ስብስቦች፣ ለሙያ ኮርሶች፣ ላልተገደቡ ተወዳጆች፣ ለልዩ ትምህርቶች እና ከማስታወቂያ ነፃ ተሞክሮ የተዘጋጀ ነው።"))}
  listOf("monthly","annual").forEach {plan->item {
   Card(onClick={selected=plan},shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=if(selected==plan)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)){
    Row(Modifier.fillMaxWidth().padding(18.dp)){RadioButton(selected==plan,{selected=plan});Column(Modifier.padding(start=8.dp)){Text(if(plan=="monthly")tr(language,"Monthly subscription","ወርሃዊ ምዝገባ") else tr(language,"Yearly subscription","ዓመታዊ ምዝገባ"),style=MaterialTheme.typography.titleMedium);Text((if(plan=="monthly")monthly else annual) ?: tr(language,"Price available after Play Console setup","ከPlay Console ዝግጅት በኋላ ዋጋው ይገኛል"))}}
   }
  }}
  if(status.isNotBlank())item{Text(status)}
  item {HpNotice(tr(language,"Purchasing is not enabled","ግዢ አልተከፈተም"),tr(language,"No purchase or premium unlock happens in this build. A published Play subscription, authenticated purchase flow, and a deployed verification backend are required. Selecting a plan only compares the options.","በዚህ ስሪት ግዢ ወይም ፕሪሚየም መክፈት አይከናወንም። በPlay የታተመ ምዝገባ፣ የተረጋገጠ የግዢ ሂደት እና የማረጋገጫ አገልጋይ ያስፈልጋሉ። እቅድ መምረጥ አማራጮቹን ለማነጻጸር ብቻ ነው።"))}
  item {HpTip(tr(language,"Your free plan","ነፃ እቅድዎ"),tr(language,"Bundled posing guides, beginner courses, daily inspiration, offline rules, and 20 favorites. Previous saved collections remain intact.","የተካተቱ የአቋም መመሪያዎች፣ ጀማሪ ኮርሶች፣ የዕለቱ ሃሳብ፣ የኦፍላይን ምክሮች እና 20 ተወዳጆች። የነበሩ ስብስቦች ይጠበቃሉ።"));TextButton(onClick=onPolicies){Text(tr(language,"Privacy, terms & settings →","ግላዊነት፣ ውሎች እና ቅንብሮች →"))}}
 }
}
