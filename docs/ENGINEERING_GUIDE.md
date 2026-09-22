# NextStep 엔지니어링 가이드

이 문서는 코드 작성의 기준입니다. 새 코드는 이 규칙을 따르고, 기존 코드를 만질 때는 그 파일을 이 규칙에 맞게 정리합니다. 규칙을 바꿀 때는 이 문서를 먼저 고칩니다.

## 1. 원칙 (우선순위 순)

1. **작게 쪼갠다.** 파일 하나에 공개 타입 하나. 파일 200줄, 함수 40줄, 컴포저블 60줄을 넘기면 나눈다.
2. **소비처가 결정한다.** 재사용 코드는 정책을 갖지 않는다. 데이터와 콜백(슬롯)만 받고, "어떻게 보일지·무엇을 할지"는 호출하는 쪽이 정한다.
3. **의존은 한 방향.** `ui → domain`, `ui → data.repository(인터페이스)`, `data → domain(모델만)`. `domain` 은 Android 를 모른다.
4. **인터페이스로 경계를 긋는다.** 저장소·원격·디스패처는 인터페이스. 구현체는 `di` 에서만 조립한다. 테스트는 Fake 로 대체한다.
5. **실패해도 살아 있는다.** 원격 실패는 로컬을 막지 않는다(오프라인 우선). 재시도는 지수 백오프, 예외는 로그 + 상태로 노출, 앱은 절대 크래시하지 않는다.
6. **같은 일은 같은 모양으로.** 아래 패턴을 예외 없이 지킨다. 패턴이 안 맞으면 패턴을 고치고 문서를 갱신한다.

## 2. 패키지 구조

```
com.nextstep.app
├── di/                     AppContainer(구현체 조립), AppViewModelProvider(ViewModel 팩토리)
├── data/
│   ├── model/              enum 하나당 파일 하나 (Role, TaskType, ...)
│   ├── local/
│   │   ├── entity/         Room 엔티티 하나당 파일 하나 + Syncable
│   │   ├── dao/            DAO 하나당 파일 하나
│   │   ├── AppDatabase.kt, Converters.kt
│   ├── prefs/              DataStore (UserPreferences, UserProfile, RunningTimer)
│   ├── remote/             외부 API 클라이언트 (인터페이스 + 구현)
│   ├── repository/         저장소 인터페이스 (XxxRepository) + FamilyDataStreams(읽기 전용 스트림 모음)
│   │   └── room/           Room 구현체 (RoomXxxRepository)
│   └── sync/               SyncManager, SyncedCollection(제네릭), mapper/ (EntityMapper<T> 구현)
├── domain/                 순수 Kotlin. Android import 금지
│   ├── access/             Capabilities
│   ├── time/               DateUtils
│   ├── stats/              StudyStats + 결과 모델
│   ├── insight/            InsightEngine, TalentEngine + 모델
│   ├── planner/            StudyPlanner + 모델
│   └── content/            ContentClassifier, ContentRecommender, YouTubeLinks + 모델
└── ui/
    ├── components/         화면에 독립적인 공용 컴포넌트, 파일 하나당 컴포넌트 하나
    ├── theme/
    ├── navigation/
    └── <feature>/          XxxScreen.kt, XxxUiState.kt, XxxViewModel.kt, XxxActions.kt, components/
```

## 3. 레이어별 패턴

### data.model
- `enum class Xxx(val label: String)` + `companion object { fun from(value: String?): Xxx }`. 라벨은 UI 표시용, `name` 은 저장용.

### data.local.entity
- `data class XxxEntity(...) : Syncable`. 필드 순서: id, familyId, 도메인 필드, updatedAt, deleted, dirty.
- 파생 값은 `val x get() = ...` 로 엔티티 안에 두되 순수 계산만.

### data.local.dao
- 메서드 이름 고정: `observeAll(familyId)`, `getById(id)`, `upsert(item)`, `upsertAll(items)`, `getDirty(familyId)`, `markClean(ids)`. 이 이름은 `SyncedCollection` 이 제네릭으로 묶는 계약이다.

### data.sync
- 엔티티 하나 = `EntityMapper<T>` 구현 하나(`mapper/XxxMapper.kt`) + `SyncRegistry` 의 한 줄. `FirestoreSyncManager` 는 엔티티를 모른다.

### data.repository
- 애그리거트마다 인터페이스 하나. 읽기는 `Flow`, 쓰기는 `suspend fun` 이며 `Unit` 또는 `Result<T>` 를 돌려준다. 예외를 밖으로 던지지 않는다.
- 여러 스트림을 한꺼번에 읽는 화면(대시보드)은 `FamilyDataStreams` 인터페이스만 의존한다.
- 구현체는 `FamilyScope`(현재 가족 ID) 와 `SyncManager.requestPush()` 를 공통으로 쓴다.

### domain
- 상태 없는 `object` 의 순수 함수, 입력은 전부 파라미터. 출력은 `data class`. 시간은 `DateUtils.today()` 로만 얻고 테스트에서 주입 가능하게 파라미터로 열어 둔다.
- 규칙 엔진(Insight, Talent, Recommender)은 결과에 **근거(reason)** 를 포함한다.

### ui.<feature>
- `XxxUiState`: 불변 data class, 기본값 필수, 파생 값은 `get()` 프로퍼티.
- `XxxViewModel`: 생성자 주입(인터페이스만). `val state: StateFlow<XxxUiState>` 를 `combine(...).stateIn(viewModelScope, WhileSubscribed(5_000), XxxUiState())` 로 만든다. 사용자 의도는 동사 함수(`toggleTask`, `save`)이며 반환은 `Unit`. `viewModelScope.launch` 는 함수 본문 안에서만.
- `XxxActions`: 화면 밖으로 나가는 콜백 묶음(내비게이션). 화면 시그니처는 `XxxScreen(caps, actions, viewModel)`.
- `XxxScreen` 은 상태를 수집하고 `XxxContent(state, caps, actions, on...)` 을 호출한다. `XxxContent` 는 stateless 라 프리뷰·테스트가 가능하다.
- 60줄이 넘는 섹션은 `components/` 로 뺀다. 컴포넌트는 엔티티 대신 필요한 값과 콜백만 받는 것을 우선한다.
- 역할 분기는 `caps.canXxx` 만 쓴다. `role == Role.X` 를 화면에서 쓰지 않는다.

### ui.components
- 정책 없는 순수 UI. 슬롯(`content: @Composable () -> Unit`)과 콜백으로 열어 둔다. 색·문구·데이터 접근을 안에서 결정하지 않는다.
- 제네릭이 자연스러우면 제네릭으로 (`OptionPicker<T>`, `SelectableRow<T>`).

## 4. 코드 스타일

- Kotlin 공식 스타일, trailing comma, 명명: 클래스 `PascalCase`, 함수·변수 `camelCase`, 상수 `UPPER_SNAKE`.
- 널: `?:` 기본값 우선, `!!` 금지(테스트 제외).
- 조건이 셋 이상이면 `when`.
- 주석은 "왜" 만. KDoc 은 public 타입과 규칙(정책)에 붙인다. 한국어 가능.
- 매직 넘버는 `private const val` 또는 `PlanOptions` 같은 옵션 객체로.
- 로그 태그는 클래스 이름, `Log.w` 이상만 남긴다.

## 5. 테스트 (기능마다 반드시)

**규칙: 기능 하나를 만들면 그 기능의 테스트를 같은 커밋에 넣는다. 테스트 없는 기능 PR 은 미완성이다.**

테스트 계층과 위치 (`app/src/test/java/com/nextstep/app/...`):

| 대상 | 테스트 | 도구 |
|---|---|---|
| `domain/*` | 모든 public 함수. 경계값(빈 입력, 하나, 경계 날짜, 0/음수) 포함 | 순수 JUnit |
| `data/sync/mapper/*` | `toMap` → `fromMap` 왕복이 원본과 같은지(`dirty` 제외) | `MapperRoundTripTest` |
| `data/sync/SyncedCollection` | 병합 규칙, 배치 전송, reconcile | 메모리 저장소 |
| `data/repository/room/*` | 비즈니스 규칙(소프트 삭제 연쇄, 학급 진도, 권한 있는 수정) | `fake/dao/*` 메모리 DAO + `RecordingSyncManager` + `FakeTimeSource` |
| `ui/<feature>/*ViewModel` | 스트림 → UiState 파생값, 이벤트 → 저장소 호출, 필터·다이얼로그 상태 | `MainDispatcherRule` + `FakeFamilyDataStreams` + `fake/Fake*Repository` |

체크리스트:
- 새 도메인 함수 → 같은 이름의 `XxxTest` 에 케이스 추가.
- 새 저장소 메서드 → 인터페이스 Fake 와 Room 구현 테스트 둘 다 갱신.
- 새 화면 → `XxxViewModelTest` 에 (1) 초기 상태 (2) 이벤트별 저장소 호출 (3) 파생값 최소 1개.
- 새 엔티티 → 매퍼 왕복 테스트 한 줄 추가.
- 테스트 이름은 `동작_조건_기대` 대신 문장형 camelCase (`saveEventDelegatesToRepository`).
- 테스트 데이터는 `testing/Fixtures.kt` 의 빌더를 쓴다. 테스트 안에서 엔티티 생성자를 길게 호출하지 않는다.

CI 명령: `./gradlew :app:assembleDebug :app:testDebugUnitTest`. 빨간 상태로 머지하지 않는다.

## 6. 가용성·복원력

- 모든 쓰기는 Room 에 먼저. 원격은 `dirty` 플래그 기반 재전송, 실패 시 지수 백오프 3회 후 `SyncStatus.ERROR` 로 표시.
- 원격 스냅샷 파싱 실패는 그 문서만 건너뛴다.
- 화면은 빈 상태·로딩·오류를 항상 처리한다(`EmptyState`, 에러 텍스트).
- 외부 링크·인텐트는 `runCatching` 으로 감싼다.

## 7. 체크리스트 (PR 전)

- [ ] 파일당 타입 하나, 200줄 이하
- [ ] 새 엔티티: entity + dao + mapper + SyncRegistry + Room version + Fake
- [ ] ViewModel 은 인터페이스만 주입, `state` 하나
- [ ] 화면은 `Screen/Content/Actions` 분리, 역할 분기는 `caps`
- [ ] domain 변경에 테스트 추가
- [ ] CI 그린
