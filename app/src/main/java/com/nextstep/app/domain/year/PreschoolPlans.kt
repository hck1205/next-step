package com.nextstep.app.domain.year

import com.nextstep.app.domain.year.YearArea.BODY
import com.nextstep.app.domain.year.YearArea.ENGLISH
import com.nextstep.app.domain.year.YearArea.LIFE
import com.nextstep.app.domain.year.YearArea.MATH
import com.nextstep.app.domain.year.YearArea.PLAY
import com.nextstep.app.domain.year.YearArea.TALK
import com.nextstep.app.domain.year.YearRows.ca
import com.nextstep.app.domain.year.YearRows.pa
import com.nextstep.app.domain.year.YearRows.ta

/** 학령 전(만 0~6세, a0~a6)의 학업·생활 할 일. 누가 하는지([YearDoer])를 줄마다 적습니다. 모으는 곳은 [YearPlans]. */
internal object PreschoolPlans {
    val byYear: Map<String, List<YearTask>> = mapOf(
        // 만 0세(0~12개월): 공부는 없어요. 전부 엄마·아빠가 해 주는 일이고 월령을 적어 둡니다.
        "a0" to listOf(
            pa(TALK, "눈 맞추고 말 걸기", "기저귀 갈 때·수유할 때 지금 하는 일을 말로"), pa(TALK, "옹알이에 대답하기", "4개월 무렵부터 아이 소리를 따라 해 주기"),
            pa(TALK, "초점책·헝겊책 보여 주기", "0~3개월 흑백 초점책, 6개월부터 헝겊책(물고 빨아도 괜찮아요)"), pa(TALK, "노래·자장가 불러 주기", "같은 노래를 하루 여러 번"),
            ta(PLAY, "엎드려 놀기(터미타임)", "깨어 있을 때 하루 몇 번, 목·어깨 힘 기르기"), ta(PLAY, "손 뻗어 잡기", "4~6개월, 장난감을 손 닿는 곳에"), ta(PLAY, "까꿍 놀이", "6~9개월, 사라졌다 나타나는 놀이"),
            pa(LIFE, "이유식 시작", "만 6개월 무렵, 한 숟가락부터"), pa(LIFE, "바로 눕혀 재우기", "딱딱한 매트, 푹신한 이불·베개 없이"), pa(LIFE, "영상 보여 주지 않기", "만 2세 전에는 화면 0분이 권장"),
        ),
        // 만 1세(12~24개월): 걷고 첫 단어가 나와요. 부모가 읽어 주고 말을 늘려 줍니다.
        "a1" to listOf(
            pa(TALK, "그림책 읽어 주기", "하루 10분, 그림을 가리키며 이름 말해 주기"), pa(TALK, "아이 말에 한 단어 붙여 주기", "\"물\" → \"물 줄까?\""), ta(TALK, "몸짓에 말로 대답하기", "가리키기·손 흔들기·고개 젓기"),
            ta(PLAY, "매일 바깥 걷기", "30분, 걷기가 서툴러도 괜찮아요"), ta(PLAY, "쌓기·넣었다 빼기", "컵·블록·통"), ta(PLAY, "끼적이기", "18개월 무렵, 굵은 크레용으로"),
            ta(LIFE, "컵으로 마시기·숟가락 잡기", "흘려도 스스로 해 보게"), pa(LIFE, "젖병·밤중 수유 줄이기", "돌 전후부터 천천히"), pa(LIFE, "영상 없이 지내기", "만 2세 전 화면 0분"),
        ),
        // 만 2세(24~36개월): 말이 트여요. 영어보다 한국어 대화가 먼저인 해.
        "a2" to listOf(
            pa(TALK, "그림책 2~3권 읽어 주기", "아이가 고른 책, 같은 책 반복도 좋아요"), ta(TALK, "두 단어를 세 단어로 늘려 주기", "\"차 가\" → \"빨간 차가 가네\""), ta(TALK, "노래·손유희", "율동 따라 하기"),
            ta(PLAY, "역할 놀이", "소꿉·인형·병원 놀이"), ta(PLAY, "뛰기·점프·공 차기", "놀이터 하루 1시간"), ta(PLAY, "퍼즐·모양 맞추기", "3~4조각부터"), ca(PLAY, "끼적이기·동그라미 그리기", "큰 종이에 자유롭게"),
            ca(LIFE, "혼자 숟가락으로 먹기", "흘려도 괜찮아요"), ta(LIFE, "배변 연습", "아이가 신호를 보일 때 시작"), pa(LIFE, "화면은 하루 1시간 안", "만 2세부터, 부모와 함께 보기"),
        ),
        // 만 3세: 누리과정(놀이 중심) 시작. 읽기는 여전히 읽어 주는 것.
        "a3" to listOf(
            ta(TALK, "함께 읽기 15분", "읽고 나서 \"왜 그랬을까?\" 물어보기"), ca(TALK, "오늘 있었던 일 말하기", "저녁에 한 가지씩"), ta(TALK, "말놀이·수수께끼", "차 안·식탁에서"),
            ta(ENGLISH, "영어 노래·그림책", "하루 10분 안, 놀이로만"),
            ca(PLAY, "블록·퍼즐 끝까지", "스스로 완성해 보기"), ca(PLAY, "가위·풀로 만들기", "안전 가위, 선 따라 자르기"), ta(PLAY, "숫자 세며 놀기", "계단·과일 세기, 10까지"),
            ta(BODY, "바깥놀이 1시간", "놀이터·산책"), 
            ca(LIFE, "스스로 옷 입기", "단추 없는 옷부터"), ca(LIFE, "장난감 정리", "놀고 나서 제자리"), YearTask(LIFE, YearTerm.FIRST, "유치원·어린이집 적응", "3~4월, 짧게 시작해 늘리기", YearDoer.TOGETHER),
        ),
        // 만 4세: 호기심의 해. 글자는 읽기보다 알아보기, 수는 세기와 비교.
        "a4" to listOf(
            ta(TALK, "함께 읽기 15분", "읽은 뒤 이야기 바꿔 말하기"), ca(TALK, "끝말잇기·말놀이", "차 안·식탁에서"), ca(TALK, "이름 글자 알아보기", "자기·가족 이름부터, 쓰기는 아직"),
            ta(MATH, "수 세기·많고 적음", "20까지, 간식 나누기로"), ta(MATH, "모양 찾기", "동그라미·세모·네모를 집 안에서"),
            ta(ENGLISH, "영어 노래·그림책", "주 5회 10분"),
            ca(PLAY, "그리기·만들기", "주 3번"), ta(PLAY, "보드게임", "순서 지키기·지는 연습"),
            ta(BODY, "자전거·킥보드", "주말마다, 헬멧 쓰고"),
            ca(LIFE, "혼자 양치·세수", "마무리는 어른이"), pa(LIFE, "학원·수업은 주 2~3개까지", "시험 보는 곳은 피하기"),
        ),
        // 만 5세: 글자에 관심이 생기는 해. 한글은 놀이로, 받침 없는 글자부터.
        "a5" to listOf(
            ta(TALK, "함께 읽기 20분", "잠들기 전"), ca(TALK, "한글 놀이", "간판·이름 글자, 받침 없는 글자부터"), ca(TALK, "그림일기(그림 + 한 단어)", "주 1~2회"),
            ta(MATH, "가르기·모으기", "10까지, 구슬·손가락으로"), ta(MATH, "시계·달력 보기 놀이", "오늘 요일·몇 시"),
            ta(ENGLISH, "영어 그림책", "주 3회 10분"),
            ca(PLAY, "좋아하는 활동 찾기", "그림·악기·운동 중에서 체험"),
            ca(LIFE, "혼자 씻기·옷 개기", "매일 조금씩"), pa(LIFE, "레벨테스트 보는 학원 피하기", "유아 레벨테스트는 2026년 10월부터 금지"),
        ),
        // 만 6세(입학 전 해): 한글은 초1 국어가 처음부터 가르쳐요. 입학 준비는 생활 습관.
        "a6" to listOf(
            ta(TALK, "매일 함께 읽기 20분", "읽어 주다가 한 줄씩 번갈아"), ca(TALK, "한글 읽기", "받침 없는 글자부터, 흥미만 붙이기"),
            ta(MATH, "10 가르기·모으기", "구슬·손가락으로"), ta(MATH, "20까지 세고 읽기", "달력·엘리베이터 숫자"),
            ca(PLAY, "좋아하는 것 하나 정하기", "그림·악기·운동 중에서"),
            ca(LIFE, "20분 앉아 있기", "그림책·퍼즐로 연습"), ca(LIFE, "혼자 화장실·옷 입기", "학교생활의 기본"),
        ),
    )
}
