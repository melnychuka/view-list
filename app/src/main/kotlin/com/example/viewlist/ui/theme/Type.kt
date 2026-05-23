package com.example.viewlist.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge  = TextStyle(fontFamily = PhilosopherFamily, fontWeight = FontWeight.Bold,     fontSize = 26.sp, letterSpacing = 0.sp),
    headlineMedium = TextStyle(fontFamily = PhilosopherFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, letterSpacing = (-0.3).sp),
    titleLarge     = TextStyle(fontFamily = PhilosopherFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, letterSpacing = (-0.2).sp),
    titleMedium    = TextStyle(fontFamily = PhilosopherFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = (-0.1).sp),
    bodyLarge      = TextStyle(fontFamily = PhilosopherFamily, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = PhilosopherFamily, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = PhilosopherFamily, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = PhilosopherFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.4.sp),
)

