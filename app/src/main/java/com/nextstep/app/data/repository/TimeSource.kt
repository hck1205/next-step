package com.nextstep.app.data.repository

/** 수정 시각 발급. 테스트에서 고정 시각을 주입하기 위한 경계. */
fun interface TimeSource {
    fun now(): Long
}

object SystemTimeSource : TimeSource {
    override fun now(): Long = System.currentTimeMillis()
}
