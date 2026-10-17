package com.posemaster.app.domain
import org.junit.Assert.*
import org.junit.Test
class PoseFilterTest {
 private val a=Pose(1,"Quiet confidence","Portrait photography","Individual","Casual","Studio","Standing","Beginner",false,"Turn toward light","Relax","Support","Look")
 private val b=a.copy(id=2,name="Shared horizon",subject="Couple",difficulty="Intermediate",premium=true)
 @Test fun combinedFiltersMatchOnlySuitableSubjects() {assertEquals(listOf(b),PoseFilter.apply(listOf(a,b),"horizon","All",Finder(subject="Couple",experience="Intermediate")))}
 @Test fun incompatibleFiltersReturnEmpty() {assertTrue(PoseFilter.apply(listOf(a,b),"","All",Finder(subject="Family")).isEmpty())}
 @Test fun searchIgnoresCaseAndIncludesInstructions() {assertEquals(listOf(a,b),PoseFilter.apply(listOf(a,b),"LIGHT","All",Finder()))}
 @Test fun categoryIsRespected() {assertTrue(PoseFilter.apply(listOf(a),"","Wedding photography",Finder()).isEmpty())}
 @Test fun allDefaultsPreserveCatalogOrder() {assertEquals(listOf(a,b),PoseFilter.apply(listOf(a,b),"","All",Finder()))}
}
