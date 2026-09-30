package com.nextstep.app.ui.components.speech

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * 글이 서툰 아이를 위한 읽어 주기. 돌려받은 함수에 글을 넘기면 한국어로 소리 내어 읽습니다.
 * 화면을 떠나면 음성 엔진을 닫습니다. 엔진이 없거나 한국어가 없으면 조용히 아무것도 하지 않습니다.
 */
@Composable
fun rememberSpeaker(): (String) -> Unit {
    val context = LocalContext.current
    val engine = remember { SpeakerEngine() }
    DisposableEffect(context) {
        engine.tts = TextToSpeech(context) { status -> engine.ready = status == TextToSpeech.SUCCESS && engine.tts?.setLanguage(Locale.KOREAN).let { it != null && it >= TextToSpeech.LANG_AVAILABLE } }
        onDispose { engine.tts?.shutdown(); engine.tts = null; engine.ready = false }
    }
    return { text -> if (engine.ready) engine.tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString()) }
}

/** [rememberSpeaker] 가 쥐고 있는 음성 엔진 상태. */
internal class SpeakerEngine {
    var tts: TextToSpeech? = null
    var ready: Boolean = false
}
