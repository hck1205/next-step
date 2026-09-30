package com.nextstep.app.data.school

/** NEIS 가 오류 코드로 답했을 때(인증키 오류 · 호출 한도 등). [code] 는 NEIS 결과 코드(예: ERROR-290). */
class NeisException(val code: String, message: String) : Exception(message)
