package com.posemaster.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posemaster.app.domain.Pose
import com.posemaster.app.data.categoryLabel

fun tr(language:String,en:String,am:String)=if(language=="am")am else en
val HelloGold=Color(0xFFD9BD86)
@Composable fun HpHeading(text:String,modifier:Modifier=Modifier) {Text(text,modifier,fontFamily=FontFamily.Serif,fontSize=30.sp,lineHeight=37.sp)}
@Composable fun HpSection(title:String,subtitle:String="") {Column(verticalArrangement=Arrangement.spacedBy(4.dp)){Text(title,fontFamily=FontFamily.Serif,fontSize=24.sp);if(subtitle.isNotBlank()) Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
@Composable fun HpNotice(title:String,body:String) {Card(shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(title,fontWeight=FontWeight.SemiBold);Text(body,style=MaterialTheme.typography.bodyMedium)}}}
@Composable fun HpChoice(label:String,value:String,options:List<String>,onChange:(String)->Unit,display:(String)->String={it}) {
 var open by remember {mutableStateOf(false)}
 Box(Modifier.fillMaxWidth().padding(vertical=3.dp)){OutlinedButton(onClick={open=true},modifier=Modifier.fillMaxWidth()){Text("$label: ${display(value)}",Modifier.weight(1f));Icon(Icons.Outlined.ExpandMore,null)};DropdownMenu(expanded=open,onDismissRequest={open=false}){options.forEach{option->DropdownMenuItem(text={Text(display(option))},onClick={onChange(option);open=false})}}}
}
@Composable fun HpPoseCard(pose:Pose,language:String,featured:Boolean=false,onOpen:(Int)->Unit) {
 Card(onClick={onOpen(pose.id)},shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth()) {
  if(featured) PoseReferenceImage(pose,Modifier.fillMaxWidth().height(260.dp),language=language)
  Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
   if(!featured) PoseReferenceImage(pose,Modifier.size(88.dp),showLabel=false,language=language)
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
    Text(categoryLabel(pose.category,language),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
    Text(pose.name,fontFamily=FontFamily.Serif,fontSize=21.sp)
    Text(if(pose.premium)tr(language,"Premium collection preview","የፕሪሚየም ስብስብ ቅድመ እይታ") else tr(language,"Free original guide","ነፃ የተዘጋጀ መመሪያ"),style=MaterialTheme.typography.labelSmall)
   }
   Icon(Icons.AutoMirrored.Outlined.ArrowForward,contentDescription=tr(language,"Open ${pose.name}","${pose.name} ክፈት"))
  }
 }
}
@Composable fun PhotographyBackdrop(modifier:Modifier=Modifier) {
 Canvas(modifier.clip(RoundedCornerShape(26.dp)).background(Brush.linearGradient(listOf(Color(0xFF314B42),Color(0xFF151D1C))))) {
  val center=Offset(size.width*.74f,size.height*.51f)
  drawCircle(Color(0xFF52675B),size.height*.7f,center)
  drawCircle(Color(0xFF213B32),size.height*.50f,center)
  drawCircle(HelloGold.copy(alpha=.8f),size.height*.37f,center)
  drawCircle(Color(0xFF142B25),size.height*.34f,center)
  drawCircle(Color(0xFF4B665E),size.height*.24f,center)
  drawCircle(Color(0xFF0D201D),size.height*.16f,center)
  drawCircle(Color.White.copy(alpha=.6f),size.height*.045f,Offset(center.x-size.height*.07f,center.y-size.height*.09f))
  drawLine(HelloGold.copy(alpha=.5f),Offset(0f,size.height*.78f),Offset(size.width*.43f,0f),size.height*.08f)
 }
}
@Composable fun HpTip(title:String,body:String){Column(Modifier.fillMaxWidth().padding(vertical=10.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(title,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.primary);Text(body,lineHeight=25.sp);HorizontalDivider(Modifier.padding(top=8.dp))}}
