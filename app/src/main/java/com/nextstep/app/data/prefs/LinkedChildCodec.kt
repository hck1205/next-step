package com.nextstep.app.data.prefs

/** 연결된 자녀 목록을 DataStore 문자열 하나로 저장합니다. 한 줄에 한 명, 칸은 탭으로 나눕니다. */
object LinkedChildCodec {
    private const val FIELD = '\t'
    private const val ROW = '\n'

    fun encode(children: List<LinkedChild>): String =
        children.joinToString(ROW.toString()) { c -> listOf(c.familyId, c.pairingCode, c.memberId, c.studentName).joinToString(FIELD.toString()) { clean(it) } }

    fun decode(raw: String?): List<LinkedChild> =
        raw.orEmpty().split(ROW).mapNotNull { line ->
            val parts = line.split(FIELD)
            if (parts.size < FIELDS || parts[0].isBlank()) null else LinkedChild(familyId = parts[0], pairingCode = parts[1], memberId = parts[2], studentName = parts[3])
        }

    /** 같은 가족이면 자리를 지키며 바꾸고, 새 가족이면 끝에 붙입니다. */
    fun upsert(children: List<LinkedChild>, child: LinkedChild): List<LinkedChild> =
        if (children.any { it.familyId == child.familyId }) children.map { if (it.familyId == child.familyId) child else it } else children + child

    private fun clean(value: String): String = value.replace(FIELD, ' ').replace(ROW, ' ')

    private const val FIELDS = 4
}
