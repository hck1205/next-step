# NextStep 저장소 안내

학생·학부모·멘토가 함께 쓰는 학업 관리 Android 앱. Kotlin + Jetpack Compose + Room + (선택) Firestore.

## 빌드·검증
- `./gradlew :app:assembleDebug :app:testDebugUnitTest` 가 CI(`.github/workflows/android.yml`)와 동일한 명령.
- Claude Code 웹 환경에서는 dl.google.com 이 차단되어 Android SDK/AGP 를 받을 수 없다. 로컬 컴파일 대신 **푸시 후 GitHub Actions 결과로 검증**한다.
- `google-services.json` 이 없으면 Firebase 플러그인이 적용되지 않고 로컬 전용 모드로 빌드된다. 절대 커밋하지 않는다.

## 구조 원칙
- 레이어: `data`(Room/DataStore/동기화) → `domain`(순수 Kotlin 계산: StudyStats, InsightEngine, StudyPlanner, Capabilities) → `ui`(화면 단위 패키지 + ViewModel).
- 새 기능은 `ui/<feature>/` 에 Screen + ViewModel 로 추가하고, 화면 간 공유 로직은 `domain` 으로 올린다. `ui` 끼리 서로 import 하는 것은 공용 컴포넌트(`ui/components`)와 명시적으로 공개한 카드(InsightCard, TalentCard, TaskRow, SessionRow)만 허용.
- 역할별 권한은 `domain/Capabilities.kt` 한 곳에서만 판단한다. 화면에서 `role == ...` 로 분기하지 말고 `caps.canXxx` 를 쓴다.
- 동기화 대상 엔티티는 `Syncable`(updatedAt/deleted/dirty)을 구현하고 `Mappers` + `FirestoreSyncManager` 의 listen/push 목록에 추가한다. 삭제는 항상 소프트 삭제.
- Room 스키마 변경 시 `AppDatabase.version` 을 올린다. 출시 전까지는 destructive migration 허용.

## 제품 방향
- `docs/PRODUCT_STRATEGY.md` 가 제품·비즈니스 기준 문서. 기능을 추가하기 전에 어떤 역할의 어떤 핵심 흐름을 강화하는지 여기에 맞춰 판단한다.
