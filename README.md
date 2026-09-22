# NextStep — 학생·학부모 학업 관리 앱 (Android)

학생, 학부모, 멘토(선생님·과외·튜터)가 **같은 데이터를 서로 다른 화면으로** 보는 학업 스케줄·진도·성적 관리 앱입니다.
한 학생에 학부모와 멘토가 여러 명 연결될 수 있습니다.
Kotlin + Jetpack Compose 로 작성했고, Room(오프라인 우선) + Firebase Firestore(실시간 동기화)를 사용합니다.

## 역할별 화면과 기능

세 역할은 같은 데이터를 보지만 **하단 탭, 기본 화면, 편집 권한이 다릅니다.** 권한은 `domain/Capabilities.kt` 한 곳에서 정의합니다.

| | 학생 | 학부모 | 멘토 (선생님·과외·튜터) |
|---|---|---|---|
| 초점 | 지금 배우는 과목의 학습 내용, 커리큘럼 스케줄링 | 격려, 모니터링, 트래킹, 분석, 제안, 재능 발견 | 큐레이팅, 로드맵 제안, 학습 지도 |
| 하단 탭 | 홈 · 커리큘럼 · 캘린더 · 성적 · 분석 | 대시보드 · 격려 · 분석 · 캘린더 · 성적 | 지도 · 로드맵 · 진도 · 캘린더 · 성적 |
| 홈 | 오늘 일정·할 일, 타이머, **지금 배우는 과목**, **학습 계획 만들기**, 멘토 로드맵 진행, 복습·예습 추천 | 연속 학습·오늘·주간 시간, 7일 추이, 과목별 시간 vs 목표, 시험 D-day, **재능 발견**, 분석 요약, 진도·성적 요약, 메모 | 로드맵 현황, 담당 과목, 담당 과목 기준 학습 시간·진도·성적, 분석, 과제 배정, 피드백 |
| 전용 화면 | 학습 타이머 | **격려**: 오늘 기록에 맞춘 칭찬 문구 제안, 응원 메시지, 연속 학습 | **로드맵**: 무엇을·어떻게·어떤 자료로·언제까지 큐레이팅, 진도 기반 추천 |
| 과목·단원 편집 | ○ | ✕ (열람) | ○ |
| 학급 진도 설정 | ○ | ✕ | ○ |
| 단원 상태·이해도 기록 | ○ (본인만) | ✕ | ✕ |
| 일정 등록 | ○ | ○ (학원 등) | ○ |
| 성적 입력 | ○ | ○ | ○ |
| 할 일 / 과제 | 내 할 일 생성·완료 | ✕ (격려·메모로 대신) | 과제 배정 |
| 로드맵 | 진행 상태 갱신 | 열람 | 작성·편집 |
| 인사이트 → 할 일 | ○ | ✕ | ○ (과제로) |

### 여러 명 연결
- 한 학생에 **학부모 여러 명(엄마, 아빠 등)** 과 **멘토 여러 명** 이 같은 연결 코드로 참여합니다. 각자 구성원(members) 행을 가지며 설정 화면에서 목록을 볼 수 있습니다.
- 멘토는 **담당 과목** 을 지정하면 대시보드가 그 과목 기준으로 좁혀집니다.

### 학부모 겸 멘토 (parent as mentor)
- 학부모가 직접 가르치는 경우 설정에서 **"멘토 역할 겸하기"** 를 켭니다.
- 켜면 학부모 기능은 그대로 두고 멘토 기능(로드맵 큐레이팅, 과제 배정, 단원·학급 진도 관리, 담당 과목)이 열립니다. 대시보드에 "멘토 모드" 진입 카드가 생기고, 배정한 과제와 로드맵은 멘토 명의로 기록됩니다.

### 콘텐츠 저장소 (유튜브 링크 큐레이팅)
- 모든 역할이 유튜브 링크를 등록할 수 있습니다. 영상은 저장하지 않고 **링크와 분류만** DB 에 남깁니다.
- 등록 시 제목·채널을 가져와 `domain/ContentClassifier.kt` 가 과목·학년대·유형·키워드를 자동 분류하고 근거를 보여 줍니다. 사용자가 수정해 저장합니다.
- `domain/ContentRecommender.kt` 가 복습·예습 단원, 약한 과목, 다가오는 시험에 맞춰 추천합니다. 학생 홈 "추천 영상"과 저장소 상단에 나타납니다.
- 멘토는 로드맵 항목에 저장소 영상을 연결할 수 있습니다.
- 운영자가 큐레이팅하는 **공용 저장소**는 Firestore 최상위 `catalog` 컬렉션입니다. 문서 필드는 가족 저장소와 같고(`Mappers.contentToMap` 참고) 읽기 전용입니다.

### 광고
- 학부모 분석 탭 하단과 콘텐츠 저장소 하단에 배너 1개씩만 있습니다. 학생 화면에는 없습니다.
- 기본값은 AdMob 테스트 ID 입니다. 실제 ID 는 `gradle.properties` 또는 CI 시크릿에 `ADMOB_APP_ID`, `ADMOB_BANNER_ID` 로 넣습니다.

### 커리큘럼 스케줄링 (학생)
홈의 **학습 계획 만들기** 는 `domain/StudyPlanner.kt` 가 담당합니다. 우선순위 *밀린 복습 → 멘토 로드맵(진행 중 우선) → 다음 예습* 으로 큐를 만들고, 지정한 기간·시작 시간·회당 시간·하루 회수에 맞춰 기존 일정과 겹치지 않는 시간에 자습 일정과 할 일을 넣습니다.

### 재능 발견 (학부모)
`InsightEngine.talents()` 가 성적·학습 시간·진도 패턴에서 강점 신호를 찾습니다: 효율형(적은 시간·높은 점수), 꾸준한 성장세, 안정적 실력, 꾸준함(14일 중 학습일), 몰입력(90분 이상 세션), 자기주도(수업 전 예습 비율), 아침형/저녁 집중형, 높은 이해 자신감.

### 제안 엔진 규칙 (`domain/InsightEngine.kt`)
- 과목 평균 ≥ 80 → 강점, < 70 → 보완 필요(복습 할 일 제안)
- 직전 시험 대비 ±10점 → 상승/하락 알림
- 반 평균보다 10점 이상 낮음 → 기본 개념 복습 제안
- 학급 진도보다 복습이 3단원 이상 밀림 → 주의 + 첫 단원 복습 할 일 제안
- 복습이 밀리지 않았으면 다음 단원 예습 제안
- 주간 목표 대비 40% 미만 과목 → 시간 부족 제안, 한 과목이 60% 초과 → 배분 제안
- 누적 학습 3시간 이상이면 가장 집중한 시간대 안내
- 14일 내 시험인데 준비 할 일이 없으면 주의 + 준비 할 일 제안
- 기한 지난 할 일 알림

## 아키텍처

```
app/src/main/java/com/nextstep/app
├── data
│   ├── local        Room 엔티티/DAO/DB (Subject, Topic, Task, Event, Grade, StudySession, Note, Member, RoadmapItem, Content)
│   ├── remote       YouTube oEmbed 메타데이터 수집
│   ├── prefs        DataStore (역할, 가족 ID, 연결 코드, 실행 중 타이머)
│   ├── repository   StudyRepository — 단일 데이터 진입점, 쓰기 후 동기화 요청
│   └── sync         SyncManager 인터페이스, FirestoreSyncManager, NoOpSyncManager, Mappers
├── domain           DateUtils, StudyStats(집계), InsightEngine(제안·재능 발견), StudyPlanner(커리큘럼 스케줄링), Capabilities(역할별 권한), ContentClassifier/ContentRecommender(콘텐츠 분류·추천)
├── di               AppContainer (수동 DI)
└── ui
    ├── navigation   역할별 하단 탭 + NavHost
    ├── components   공용 카드/피커/다이얼로그, Canvas 차트(막대·꺾은선·레이더·도넛·히트스트립)
    ├── onboarding / home(학생) / parent(대시보드·격려) / mentor(지도) / roadmap / progress / calendar / grades / insights / timer / settings
    └── theme
```

- **오프라인 우선**: Room 이 진실의 원천. 모든 엔티티는 `updatedAt`, `deleted`(소프트 삭제), `dirty`(미전송) 필드를 가집니다.
- **동기화**: 로컬 변경 → `dirty=1` → 디바운스 후 Firestore 배치 쓰기. Firestore 스냅샷 리스너로 원격 변경 수신, `updatedAt` 이 더 최신일 때만 반영(last-write-wins).
- **Firebase 는 선택**: `app/google-services.json` 이 없으면 로컬 전용 모드로 빌드·동작합니다. (학부모 연동만 비활성)

## 빌드

요구 사항: Android Studio Ladybug 이상, JDK 17+, Android SDK 35.

```bash
./gradlew :app:assembleDebug
```

또는 Android Studio 에서 프로젝트를 열고 Run.

## Firebase 동기화 설정 (학생 ↔ 학부모 연동)

1. [Firebase 콘솔](https://console.firebase.google.com)에서 프로젝트 생성 → Android 앱 추가 (패키지명 `com.nextstep.app`).
2. 다운로드한 `google-services.json` 을 `app/` 폴더에 넣습니다. (`.gitignore` 에 포함되어 있습니다)
3. Authentication → 로그인 방법에서 **익명 로그인** 활성화.
4. Firestore Database 생성 후 규칙을 아래처럼 설정합니다.

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 연결 코드로 가족을 찾기 위해 로그인한 사용자는 families 를 조회할 수 있습니다.
    match /families/{familyId} {
      allow read, create: if request.auth != null;
      allow update, delete: if false;
      match /{collection}/{docId} {
        allow read, write: if request.auth != null;
      }
    }
    // 운영자가 큐레이팅하는 공용 콘텐츠 저장소. 앱에서는 읽기만.
    match /catalog/{docId} {
      allow read: if request.auth != null;
      allow write: if false;
    }
  }
}
```

> 위 규칙은 시작용입니다. 실제 배포 시에는 가족 문서에 참여자 UID 목록을 저장하고, 해당 UID 만 하위 컬렉션에 접근하도록 좁히는 것을 권장합니다.

5. 다시 빌드하면 설정 화면의 동기화 상태가 "동기화됨"으로 바뀝니다.

### 연결 절차
1. 학생 기기: 온보딩에서 **학생** 선택 → 이름 입력 → 시작. 설정 화면에 6자리 연결 코드가 표시됩니다.
2. 학부모 기기: 온보딩에서 **학부모** 선택 → 이름과 연결 코드 입력 → 연결.
3. 멘토 기기: 온보딩에서 **멘토** 선택 → 이름, 구분, 연결 코드 입력 → 연결 → 대시보드에서 담당 과목 선택. 멘토는 몇 명이든 추가할 수 있습니다.
4. 이후 모든 기기에서 입력한 데이터(과목, 단원, 일정, 성적, 학습 기록, 메모, 구성원)가 실시간으로 동기화됩니다.

> 참고: 멘토 기기는 현재 한 번에 한 학생에 연결됩니다. 멘토 한 명이 여러 학생을 오가며 보는 기능은 다음 단계입니다.

## 사용 흐름 (학생)
1. 진도 탭에서 과목 확인(기본 5과목 생성) → 과목을 눌러 단원을 줄바꿈으로 한 번에 등록.
2. "학급 진도 설정"으로 수업이 어디까지 진행됐는지 표시 → 홈에 복습/예습 추천이 나타납니다.
3. 캘린더에서 시간표(매주 반복), 학원, 시험 일정을 등록.
4. 공부할 때 홈의 타이머로 시간 기록. 성적이 나오면 성적 탭에 입력.
5. 분석 탭에서 강점/보완점과 제안을 확인하고, 제안을 바로 할 일로 추가.
