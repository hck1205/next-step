package com.nextstep.app.domain.journey

/**
 * 기본 목표 트랙. 한 트랙은 장기 목표(예: "초등 졸업 전 영어 챕터북 혼자 읽기")를 학기 하나에 단계 하나씩으로 쪼갠 것입니다.
 * 교과 순서는 한국 초·중·고 교육과정을 따르고, 학령 전은 나이 구간을 씁니다. 사용자는 단계를 할 일로 보내 그 학기 안에서 진행합니다.
 * id 와 periodKey 는 저장된 목표·단계와 연결되므로 바꾸지 않습니다.
 */
object GoalTrackCatalog {
    /** 교과 트랙 먼저, 그다음 교과 밖. 순서가 목록 순서입니다. */
    val tracks: List<GoalTrack> = SubjectTracks.all + LifeTracks.all

    val byId: Map<String, GoalTrack> = tracks.associateBy { it.id }
}
