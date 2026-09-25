# NextStep 저장소 안내

학생·학부모·멘토가 함께 쓰는 학업 관리 Android 앱. Kotlin + Jetpack Compose + Room + (선택) Firestore.

## 빌드·검증
- `./gradlew :app:assembleDebug :app:testDebugUnitTest` 가 CI(`.github/workflows/android.yml`)와 동일한 명령.
- Claude Code 웹 환경에서는 dl.google.com 이 차단되어 Android SDK/AGP 를 받을 수 없다. 로컬 컴파일 대신 **푸시 후 GitHub Actions 결과로 검증**한다.
- `google-services.json` 이 없으면 Firebase 플러그인이 적용되지 않고 로컬 전용 모드로 빌드된다. 절대 커밋하지 않는다.

## 코드 규칙
- **`docs/ENGINEERING_GUIDE.md` 가 코드 규칙의 기준이다.** 새 코드는 그 패턴(파일당 타입 하나, 인터페이스 경계, Screen/Content/Actions, UiState+StateFlow, 순수 domain, Fake 기반 테스트)을 예외 없이 따른다.
- 레이어: `data`(Room/DataStore/동기화) → `domain`(순수 Kotlin) → `ui`. `ui` 는 저장소 **인터페이스**만 의존한다.
- 새 기능은 `ui/<feature>/` 에 `XxxScreen.kt`, `XxxUiState.kt`, `XxxViewModel.kt`, `XxxActions.kt`, `components/` 로 추가한다. `ui` 끼리는 `ui/components` 와 명시적으로 공개한 카드만 import 한다.
- 역할별 권한은 `domain/access/Capabilities.kt` 한 곳에서만 판단한다. 화면에서 `role == ...` 로 분기하지 말고 `caps.canXxx` 를 쓴다.
- 학생 화면의 학년별 차이는 domain 에서만 정한다: 단계(보이는 카드·말투·탭)는 `domain/growth/StudentUiLevel.kt`, 해마다 달라지는 공부 종류·양·글씨·카드 순서는 `domain/growth/YearProfiles.kt`, 둘을 합친 결과는 `StudentScreen.of(student)`. 아이가 직접 쓸 때의 도움 장치(스티커판·아이용 가족·그림 기록·읽어 주기·보이는 타이머·어른 확인)는 `StudentUiLevel.kid`(`KidMode`) 로만 켜고 끈다. 화면에서 학년으로 분기하지 말고 `level.shows(StudentHomeSection.X)`·`level.words` 를 쓴다. 기록 탭에서 섹션이 보이는 학년은 `domain/hub/ConcernSection.minLevel`, 보이는 역할은 `ConcernSection.audiences`, 역할별 관심사 순서는 `HubAudience.order` 한 곳에서 정한다(보는 사람 = `HubViewer.of(caps, level)`).
- 학생의 해마다 할 일은 `domain/year/YearPlans` 한 곳에 분류(`YearArea`)·때(`YearTerm`)로 적고, 학생 "올해" 탭(`ui/yearplan`)이 그 분류를 탭으로 보여 준다. 학년·생년월일·화면 단계는 1년을 가는 값이라 가족 탭의 요약 한 줄 + "고치기" 창에서만 바꾼다(칩을 화면에 늘어놓지 않는다).
- 새 동기화 엔티티 = `entity` + `dao`(`SyncDao<T>` 구현) + `sync/mapper` + `SyncRegistry` 의 `SyncedCollection.of(mapper, dao)` 한 줄 + Room version + 테스트 Fake. 삭제는 항상 소프트 삭제.
- Room 스키마 변경 시 `AppDatabase.version` 을 올린다. 출시 전까지는 destructive migration 허용.
- **테스트는 기능과 같은 커밋에.** domain 함수·매퍼·Room 저장소·ViewModel 마다 단위 테스트를 쓴다(가이드 5장 표). Fake 는 `test/.../fake/`, 빌더는 `testing/Fixtures.kt`. 테스트 없는 기능은 미완성으로 본다.

## 제품 방향
- `docs/PRODUCT_STRATEGY.md` 가 제품·비즈니스 기준 문서. 기능을 추가하기 전에 어떤 역할의 어떤 핵심 흐름을 강화하는지 여기에 맞춰 판단한다.
- `docs/UX_GUIDE.md` 가 화면 구조의 기준. 하단 탭은 3개 + 기록 버튼 + 가족이며(학생은 여정 자리에 "올해") 새 기능은 오늘의 카드·여정의 항목·기록 탭의 **관심사 › 섹션**(`domain/hub/ConcernSection` 에 한 줄) 중 하나로만 들어간다. 섹션은 한 관심사에만 속하고, 기능 화면 하나 + 자기 ViewModel 을 가진다. 쓰기는 + 시트로, 목록은 지금 기준 3개까지.
