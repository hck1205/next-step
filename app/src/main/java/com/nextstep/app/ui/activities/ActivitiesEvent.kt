package com.nextstep.app.ui.activities

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.model.ActivityType

/** Activities 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface ActivitiesEvent {
    data class Save(val activity: ActivityEntity) : ActivitiesEvent
    data class Delete(val id: String) : ActivitiesEvent
    data class SetFilter(val type: ActivityType?) : ActivitiesEvent
}
