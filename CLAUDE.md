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
- 학생 화면의 학년별 차이(보이는 카드·글자 크기·말투·탭)는 `domain/growth/StudentUiLevel.kt` 한 곳에서만 정한다. 화면에서 학년으로 분기하지 말고 `level.shows(StudentHomeSection.X)`·`level.words` 를 쓴다.
- 새 동기화 엔티티 = `entity` + `dao`(`SyncDao<T>` 구현) + `sync/mapper` + `SyncRegistry` 의 `SyncedCollection.of(mapper, dao)` 한 줄 + Room version + 테스트 Fake. 삭제는 항상 소프트 삭제.
- Room 스키마 변경 시 `AppDatabase.version` 을 올린다. 출시 전까지는 destructive migration 허용.
- **테스트는 기능과 같은 커밋에.** domain 함수·매퍼·Room 저장소·ViewModel 마다 단위 테스트를 쓴다(가이드 5장 표). Fake 는 `test/.../fake/`, 빌더는 `testing/Fixtures.kt`. 테스트 없는 기능은 미완성으로 본다.

## 제품 방향
- `docs/PRODUCT_STRATEGY.md` 가 제품·비즈니스 기준 문서. 기능을 추가하기 전에 어떤 역할의 어떤 핵심 흐름을 강화하는지 여기에 맞춰 판단한다.
- `docs/UX_GUIDE.md` 가 화면 구조의 기준. 탭은 3개 + 기록 버튼 + 가족이며 새 기능은 오늘의 카드·여정의 항목·기록의 세그먼트 중 하나로만 들어간다. 쓰기는 + 시트로, 목록은 지금 기준 3개까지.
