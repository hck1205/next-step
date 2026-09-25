package com.nextstep.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.nextstep.app.R

/**
 * 앱 글꼴: Pretendard(SIL OFL 1.1, 라이선스는 assets/licenses/pretendard_ofl.txt).
 * 한글과 숫자의 폭이 고르고 작은 크기에서도 획이 뭉개지지 않아 기기마다 다른 기본 글꼴보다 읽기 쉽습니다.
 * 자간은 0(한글은 머티리얼 기본값 0.5sp 가 넓어 보임), 줄 간격은 본문 1.5배 안팎으로 넉넉하게 둡니다.
 */
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

private fun style(weight: FontWeight, size: TextUnit, lineHeight: TextUnit) =
    TextStyle(fontFamily = Pretendard, fontWeight = weight, fontSize = size, lineHeight = lineHeight, letterSpacing = 0.sp)

val Typography = Typography(
    displayLarge = style(FontWeight.Bold, 48.sp, 60.sp),
    displayMedium = style(FontWeight.Bold, 40.sp, 50.sp),
    displaySmall = style(FontWeight.Bold, 34.sp, 44.sp),
    headlineLarge = style(FontWeight.Bold, 30.sp, 40.sp),
    headlineMedium = style(FontWeight.Bold, 26.sp, 35.sp),
    headlineSmall = style(FontWeight.Bold, 22.sp, 30.sp),
    titleLarge = style(FontWeight.SemiBold, 19.sp, 27.sp),
    titleMedium = style(FontWeight.SemiBold, 16.sp, 23.sp),
    titleSmall = style(FontWeight.SemiBold, 14.sp, 20.sp),
    bodyLarge = style(FontWeight.Normal, 16.sp, 25.sp),
    bodyMedium = style(FontWeight.Normal, 14.sp, 21.sp),
    bodySmall = style(FontWeight.Normal, 12.sp, 18.sp),
    labelLarge = style(FontWeight.Medium, 14.sp, 20.sp),
    labelMedium = style(FontWeight.Medium, 12.sp, 17.sp),
    labelSmall = style(FontWeight.Medium, 11.sp, 16.sp),
)
