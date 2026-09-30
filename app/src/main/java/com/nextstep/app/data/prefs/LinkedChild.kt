package com.nextstep.app.data.prefs

/**
 * 이 기기에 연결된 자녀 한 명. 자녀마다 자기 가족 공간(familyId)과 연결 코드가 있고,
 * 이 기기 사용자는 그 공간마다 구성원 행(memberId)을 하나씩 가집니다. 다자녀 가정은 이 목록을 오갑니다.
 */
data class LinkedChild(val familyId: String, val studentName: String, val pairingCode: String, val memberId: String)
