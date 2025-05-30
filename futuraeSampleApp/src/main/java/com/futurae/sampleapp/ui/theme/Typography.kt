package com.futurae.sampleapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class ExtendedMaterialTypography(
    val regularMaterialTypography: Typography,
    val titleH2: TextStyle,
    val titleH4: TextStyle,
    val titleH5: TextStyle,
    val bodyLarge: TextStyle,
    val bodySmallRegular: TextStyle,
    val menu: TextStyle,
    val button: TextStyle,
)

val FuturaeTypography = ExtendedMaterialTypography(
    regularMaterialTypography = Typography(),
    titleH2 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleH4 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 30.sp
    ),
    titleH5 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    bodySmallRegular = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    menu = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    button = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp
    )
)


val SubtitleStyle = TextStyle(
    fontSize = 14.sp,
    color = TextGreyColor,
    fontWeight = FontWeight(400)
)

val ItemTitleStyle = TextStyle(
    fontSize = 16.sp,
    color = TextDarkColor,
    fontWeight = FontWeight.Bold,
)

val H2TestStyle = TextStyle(
    fontSize = 24.sp,
    color = Color.White,
    fontWeight = FontWeight.Bold,
)