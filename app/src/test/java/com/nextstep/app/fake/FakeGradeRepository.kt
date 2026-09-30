package com.nextstep.app.fake

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.repository.GradeRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** 저장·삭제 호출을 기록하고 스트림에 반영하는 Fake. */
class FakeGradeRepository : GradeRepository {
    override val grades = MutableStateFlow<List<GradeEntity>>(emptyList())
    val saved = mutableListOf<GradeEntity>()
    val deleted = mutableListOf<String>()

    override suspend fun save(grade: GradeEntity) {
        saved += grade
        grades.value = grades.value.filter { it.id != grade.id } + grade
    }

    override suspend fun delete(id: String) {
        deleted += id
        grades.value = grades.value.filter { it.id != id }
    }
}
