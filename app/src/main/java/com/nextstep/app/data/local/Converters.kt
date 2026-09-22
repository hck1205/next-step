package com.nextstep.app.data.local

import androidx.room.TypeConverter
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.ExamType
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus

class Converters {
    @TypeConverter fun topicStatusToString(v: TopicStatus): String = v.name
    @TypeConverter fun stringToTopicStatus(v: String): TopicStatus = TopicStatus.from(v)

    @TypeConverter fun taskTypeToString(v: TaskType): String = v.name
    @TypeConverter fun stringToTaskType(v: String): TaskType = TaskType.from(v)

    @TypeConverter fun eventTypeToString(v: EventType): String = v.name
    @TypeConverter fun stringToEventType(v: String): EventType = EventType.from(v)

    @TypeConverter fun examTypeToString(v: ExamType): String = v.name
    @TypeConverter fun stringToExamType(v: String): ExamType = ExamType.from(v)
}
