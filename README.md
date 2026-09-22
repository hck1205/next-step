# NextStep — 학생·학부모 학업 관리 앱 (Android)

학생과 학부모가 **같은 데이터를 서로 다른 화면으로** 보는 학업 스케줄·진도·성적 관리 앱입니다.
Kotlin + Jetpack Compose 로 작성했고, Room(오프라인 우선) + Firebase Firestore(실시간 동기화)를 사용합니다.

## 주요 기능

| 영역 | 학생 화면 | 학부모 화면 |
|---|---|---|
| 홈 / 대시보드 | 오늘 일정·할 일, 학습 타이머, 복습/예습 추천, 과목별 진도 | 자녀 오늘·주간 학습 시간, 7일 추이, 과목별 시간 vs 목표, 다가오는 시험, 분석 요약, 할 일 배정, 응원 메모 |
| 진도 | 과목별 단원 등록, **학급 진도**(수업에서 배운 곳) 체크, 내 상태(예습/수업/복습/완전학습), 이해도 슬라이더 → 예습·복습 목록 자동 계산 | 동일 화면(열람·편집 가능) |
| 캘린더 | 월간 캘린더(일정·시험·할 일·학습기록 마커), 시간표(매주 반복 일정), 시험·학원 일정, 할 일 관리 | 동일 + 일정/할 일 추가 |
| 성적 | 시험 점수 입력(만점·반 평균), 과목별 레이더 차트, 성적 추이 꺾은선, 반 평균 대비 | 동일(열람·입력) |
| 분석 | 강점/보완점/제안/주의 인사이트, 과목 균형, 2주 학습량, 과목별 시간 배분 도넛, 시간대별 집중 분포, 인사이트 → 할 일 생성 | 동일(열람) + 메모 |
| 시간 트래킹 | 학습 타이머(앱 종료 후에도 복원), 직접 기록 | 기록 열람 |
| 동기화 | 6자리 연결 코드 발급 | 코드 입력으로 자녀와 연결, 실시간 동기화 |

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
│   ├── local        Room 엔티티/DAO/DB (Subject, Topic, Task, Event, Grade, StudySession, Note)
│   ├── prefs        DataStore (역할, 가족 ID, 연결 코드, 실행 중 타이머)
│   ├── repository   StudyRepository — 단일 데이터 진입점, 쓰기 후 동기화 요청
│   └── sync         SyncManager 인터페이스, FirestoreSyncManager, NoOpSyncManager, Mappers
├── domain           DateUtils, StudyStats(집계), InsightEngine(규칙 기반 제안)
├── di               AppContainer (수동 DI)
└── ui
    ├── navigation   역할별 하단 탭 + NavHost
    ├── components   공용 카드/피커/다이얼로그, Canvas 차트(막대·꺾은선·레이더·도넛·히트스트립)
    ├── onboarding / home / parent / progress / calendar / grades / insights / timer / settings
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
  }
}
```

> 위 규칙은 시작용입니다. 실제 배포 시에는 가족 문서에 참여자 UID 목록을 저장하고, 해당 UID 만 하위 컬렉션에 접근하도록 좁히는 것을 권장합니다.

5. 다시 빌드하면 설정 화면의 동기화 상태가 "동기화됨"으로 바뀝니다.

### 연결 절차
1. 학생 기기: 온보딩에서 **학생** 선택 → 이름 입력 → 시작. 설정 화면에 6자리 연결 코드가 표시됩니다.
2. 학부모 기기: 온보딩에서 **학부모** 선택 → 이름과 연결 코드 입력 → 연결.
3. 이후 양쪽에서 입력한 모든 데이터(과목, 단원, 일정, 성적, 학습 기록, 메모)가 실시간으로 동기화됩니다.

## 사용 흐름 (학생)
1. 진도 탭에서 과목 확인(기본 5과목 생성) → 과목을 눌러 단원을 줄바꿈으로 한 번에 등록.
2. "학급 진도 설정"으로 수업이 어디까지 진행됐는지 표시 → 홈에 복습/예습 추천이 나타납니다.
3. 캘린더에서 시간표(매주 반복), 학원, 시험 일정을 등록.
4. 공부할 때 홈의 타이머로 시간 기록. 성적이 나오면 성적 탭에 입력.
5. 분석 탭에서 강점/보완점과 제안을 확인하고, 제안을 바로 할 일로 추가.
