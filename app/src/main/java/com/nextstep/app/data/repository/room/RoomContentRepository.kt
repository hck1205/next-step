package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.ContentDao
import com.nextstep.app.data.local.dao.SubjectDao
import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.model.ContentScope
import com.nextstep.app.data.remote.VideoMetadataFetcher
import com.nextstep.app.data.repository.ContentDraft
import com.nextstep.app.data.repository.ContentRepository
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import com.nextstep.app.domain.content.ContentClassifier
import com.nextstep.app.domain.content.YouTubeLinks
import kotlinx.coroutines.flow.Flow

class RoomContentRepository(
    private val dao: ContentDao,
    private val subjectDao: SubjectDao,
    private val metadata: VideoMetadataFetcher,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), ContentRepository {

    override val contents: Flow<List<ContentEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun prepare(url: String): Result<ContentDraft> {
        val videoId = YouTubeLinks.videoId(url)
            ?: return Result.failure(IllegalArgumentException("유튜브 링크가 아니에요. youtube.com 또는 youtu.be 주소를 넣어 주세요."))
        val familyId = scope.requireFamilyId()
        dao.findByVideoId(familyId, videoId)?.let { return Result.failure(IllegalStateException("이미 등록된 영상이에요: ${it.title}")) }
        val canonical = YouTubeLinks.canonicalUrl(videoId)
        val meta = metadata.fetch(canonical)
        val classification = ContentClassifier.classify(
            title = meta?.title ?: "", channel = meta?.channel ?: "", familySubjectNames = subjectDao.getAll(familyId).map { it.name },
        )
        return Result.success(
            ContentDraft(
                url = canonical, videoId = videoId, title = meta?.title ?: "", channel = meta?.channel ?: "",
                thumbnailUrl = meta?.thumbnailUrl?.ifBlank { null } ?: YouTubeLinks.thumbnailUrl(videoId),
                classification = classification, metadataFetched = meta != null,
            ),
        )
    }

    override suspend fun save(content: ContentEntity) {
        val authored = if (content.createdByName.isNotBlank()) content else scope.currentProfile().let { p ->
            content.copy(createdByName = p.displayName, createdByRole = p.role?.name ?: "")
        }
        dao.upsert(authored.copy(familyId = familyIdOr(authored.familyId), scope = ContentScope.FAMILY, updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun rate(id: String, stars: Int) = modifyFamilyContent(id) {
        it.copy(ratingSum = it.ratingSum + stars.coerceIn(MIN_STARS, MAX_STARS), ratingCount = it.ratingCount + 1)
    }

    override suspend fun setWatched(id: String, watched: Boolean) {
        val content = dao.getById(id) ?: return
        // 공용 저장소의 시청 표시는 기기 로컬에만 남깁니다.
        val isFamily = content.scope == ContentScope.FAMILY
        dao.upsert(content.copy(watched = watched, updatedAt = now(), dirty = isFamily))
        if (isFamily) pushLater()
    }

    override suspend fun delete(id: String) = modifyFamilyContent(id) { it.copy(deleted = true) }

    /** 가족 저장소 항목만 바꿉니다. 공용 저장소는 서버에서만 바뀝니다. */
    private suspend fun modifyFamilyContent(id: String, change: (ContentEntity) -> ContentEntity) {
        val content = dao.getById(id) ?: return
        if (content.scope != ContentScope.FAMILY) return
        dao.upsert(change(content).copy(updatedAt = now(), dirty = true))
        pushLater()
    }

    private companion object {
        const val MIN_STARS = 1
        const val MAX_STARS = 5
    }
}
