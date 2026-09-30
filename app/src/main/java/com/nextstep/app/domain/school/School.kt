package com.nextstep.app.domain.school

/**
 * 학교 한 곳(NEIS 학교 기본정보). [officeCode] 는 시도교육청 코드(예: B10), [code] 는 표준 학교 코드.
 * 구성원에는 [key]("B10:7010057")와 이름만 저장합니다.
 */
data class School(val officeCode: String, val code: String, val name: String, val kind: String = "", val address: String = "") {
    val key: String get() = "$officeCode:$code"

    companion object {
        /** 저장된 [key] 와 이름에서 되살립니다. 비었거나 모양이 틀리면 null. */
        fun fromKey(key: String, name: String): School? {
            val parts = key.split(':')
            if (parts.size != 2 || parts.any { it.isBlank() }) return null
            return School(parts[0], parts[1], name)
        }
    }
}
