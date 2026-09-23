package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.nextstep.app.BuildConfig

/**
 * 소수의 배너 광고. 정책:
 * - 학생 화면에는 절대 넣지 않는다 (미성년자 대상 광고 정책과 학습 마찰 회피).
 * - 학부모·멘토 화면에서도 목록 맨 아래 한 자리만.
 * - 광고 단위 ID 는 BuildConfig 로 주입. 기본값은 구글 테스트 ID 라 실수로 실제 광고가 나가지 않는다.
 */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current || BuildConfig.ADMOB_BANNER_ID.isBlank()) return
    AndroidView(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BuildConfig.ADMOB_BANNER_ID
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}
