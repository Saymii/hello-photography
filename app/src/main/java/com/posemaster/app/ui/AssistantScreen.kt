package com.posemaster.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.posemaster.app.domain.*

@Composable fun AssistantScreen(poses:List<Pose>,language:String,localize:(Pose)->Pose,onOpen:(Int)->Unit,onBack:()->Unit,modifier:Modifier=Modifier) {
 var request by rememberSaveable {mutableStateOf("")}
 var subject by rememberSaveable {mutableStateOf("Any")}
 var location by rememberSaveable {mutableStateOf("Any")}
 var occasion by rememberSaveable {mutableStateOf("Any")}
 var clothing by rememberSaveable {mutableStateOf("Any")}
 var position by rememberSaveable {mutableStateOf("Any")}
 var setting by rememberSaveable {mutableStateOf("Any")}
 var applied by rememberSaveable {mutableStateOf("")}
 val suggestions=remember(applied,poses){PoseAssistant.recommend(poses,applied)}
 LazyColumn(modifier.testTag("assistant_list"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
  item {TextButton(onClick=onBack){Text(tr(language,"← Back","← ተመለስ"))};HpHeading(tr(language,"AI Pose Assistant","የAI አቋም ረዳት"));Text(tr(language,"Offline mode · rule-based recommendations","ከመስመር ውጭ፤ በመመሪያ ላይ የተመሠረተ ምክር"),color=MaterialTheme.colorScheme.primary,modifier=Modifier.padding(top=8.dp))}
  item {HpNotice(tr(language,"A little context helps","ትንሽ መረጃ ይረዳል"),tr(language,"Describe your style, occasion, clothing, location, and preferred position. This uses transparent local matching rules, not a generative AI model. No request leaves your device.","ዘይቤዎን፣ ዝግጅቱን፣ ልብሱን፣ ቦታውን እና አቋሙን ይግለጹ። ይህ በመሣሪያዎ ውስጥ ያሉ ግልጽ መመሪያዎችን ይጠቀማል፤ የAI ሞዴል አይደለም። ጥያቄው ከመሣሪያዎ አይወጣም።"))}
  item {OutlinedTextField(request,{request=it},label={Text(tr(language,"Tell us about your photo","ስለ ፎቶዎ ይንገሩን"))},placeholder={Text(tr(language,"A couple standing in a park, casual clothing","ጥንዶች በፓርክ ውስጥ ቆመው በተራ ልብስ"))},minLines=3,modifier=Modifier.fillMaxWidth().testTag("assistant_request"))}
  item {
   HpChoice(tr(language,"Subject / preferred style","ተነሺ / ዘይቤ"),subject,listOf("Any","Individual","Men","Women","Unisex","Couple","Family","Group"),{subject=it})
   HpChoice(tr(language,"Location","ቦታ"),location,listOf("Any","Studio","Street","Home","Park","Café"),{location=it})
   HpChoice(tr(language,"Occasion","ዝግጅት"),occasion,listOf("Any","Wedding","Graduation","Birthday","Fashion","Portrait","Selfie"),{occasion=it})
   HpChoice(tr(language,"Clothing","ልብስ"),clothing,listOf("Any","Casual","Business","Formal","Jacket","Dress"),{clothing=it})
   HpChoice(tr(language,"Position","አቋም"),position,listOf("Any","Standing","Sitting","Walking","Close-up"),{position=it})
   HpChoice(tr(language,"Setting","አካባቢ"),setting,listOf("Any","Indoor","Outdoor"),{setting=it})
   Button(onClick={applied=(listOf(request)+listOf(subject,location,occasion,clothing,position,setting).filter{it!="Any"}).joinToString(" ")},modifier=Modifier.fillMaxWidth()){Text(tr(language,"Recommend poses","አቋሞችን ምከር"))}
   TextButton(onClick={request="";subject="Any";location="Any";occasion="Any";clothing="Any";position="Any";setting="Any";applied=""}){Text(tr(language,"Reset recommendations","ምክሮችን አጽዳ"))}
  }
  item {HpSection(tr(language,"${suggestions.size} ideas for your frame","ለፎቶዎ ${suggestions.size} ሃሳቦች"));if(applied.isNotBlank()) Text(applied,style=MaterialTheme.typography.bodySmall)}
  if(suggestions.isEmpty()) item {HpNotice(tr(language,"No suitable sample yet","የሚስማማ ናሙና ገና የለም"),tr(language,"Try fewer requirements. The assistant will not substitute a different subject or position when the sample catalog cannot match your request.","ጥቂት መስፈርቶችን ይሞክሩ። ስብስቡ ጥያቄዎን ማሟላት ካልቻለ ረዳቱ ሌላ ተነሺ ወይም አቋም አይተካም።"))}
  items(suggestions,key={it.pose.id}) {suggestion->HpPoseCard(localize(suggestion.pose),language,onOpen=onOpen);Text(suggestion.explanation,Modifier.padding(top=8.dp),style=MaterialTheme.typography.bodySmall)}
 }
}
