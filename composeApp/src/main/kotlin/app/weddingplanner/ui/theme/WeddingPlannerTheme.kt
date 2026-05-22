package app.weddingplanner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WeddingColors = lightColorScheme(
    primary = Color(0xFF123D31),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8F1E8),
    onPrimaryContainer = Color(0xFF123D31),
    secondary = Color(0xFF7A4F6D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF5D8E4),
    onSecondaryContainer = Color(0xFF31101F),
    tertiary = Color(0xFF8D5D2D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF8D7C6),
    onTertiaryContainer = Color(0xFF2F1500),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFBF4E8),
    onBackground = Color(0xFF17211D),
    surface = Color(0xFFFFFDF9),
    onSurface = Color(0xFF17211D),
    surfaceVariant = Color(0xFFF1E8DC),
    onSurfaceVariant = Color(0xFF534349),
    outline = Color(0xFF847078),
    outlineVariant = Color(0xFFD8C1CA),
    inverseSurface = Color(0xFF332F32),
    inverseOnSurface = Color(0xFFF7EFF4),
)

private val WeddingTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 38.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)

private val WeddingShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
)

@Composable
fun WeddingPlannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WeddingColors,
        typography = WeddingTypography,
        shapes = WeddingShapes,
        content = content,
    )
}
