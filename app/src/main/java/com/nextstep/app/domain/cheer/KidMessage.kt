package com.nextstep.app.domain.cheer

/** 아이가 가족에게 한 번 눌러 보내는 말. 글을 쓰지 않아도 마음을 전할 수 있게. */
enum class KidMessage(val text: String) {
    THANKS("고마워요"),
    LOVE("사랑해요"),
    DONE("오늘 할 일 다 했어요!"),
    HELP("도와주세요"),
    MISS("보고 싶어요"),
    PROUD("나 잘했죠?"),
}
