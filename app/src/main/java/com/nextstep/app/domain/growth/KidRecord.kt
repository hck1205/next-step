package com.nextstep.app.domain.growth

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.data.model.Role
import java.time.LocalDate

/**
 * 아이용 기록하기의 그림 타일. 한 번 누르면 [title] 의 오늘 활동이 저장됩니다(입력 양식 없음).
 * 제목은 소질 신호(AptitudeEngine)가 영역을 알아볼 수 있는 말로 짓습니다.
 */
enum class KidRecord(val label: String, val title: String, val type: ActivityType) {
    READ("책 읽었어요", "책 읽기", ActivityType.HOBBY),
    EXERCISE("운동했어요", "운동·줄넘기", ActivityType.HOBBY),
    DRAW("그림 그렸어요", "그림 그리기", ActivityType.HOBBY),
    MUSIC("악기 연습했어요", "피아노·악기 연습", ActivityType.HOBBY),
    HELP("집안일 도왔어요", "집안일 돕기", ActivityType.VOLUNTEER),
    OUTING("나들이 갔어요", "나들이·체험", ActivityType.EXPERIENCE);

    /** [date] 에 아이가 스스로 남긴 활동 하나. */
    fun activityOn(date: LocalDate): ActivityEntity =
        ActivityEntity(familyId = "", type = type, title = title, date = date.toEpochDay(), createdByRole = Role.STUDENT.name)
}
