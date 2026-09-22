package com.nextstep.app.fake

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSubjectRepository : SubjectRepository {
    override val subjects = MutableStateFlow<List<SubjectEntity>>(emptyList())
    val saved = mutableListOf<SubjectEntity>()
    val deleted = mutableListOf<String>()

    override fun observe(id: String): Flow<SubjectEntity?> = subjects.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun save(subject: SubjectEntity) {
        saved += subject
        subjects.value = subjects.value.filter { it.id != subject.id } + subject
    }

    override suspend fun delete(id: String) {
        deleted += id
        subjects.value = subjects.value.filter { it.id != id }
    }
}
