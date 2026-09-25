package com.nextstep.app.ui.yearplan

import com.nextstep.app.domain.year.YearTask

/** 올해 할 일 한 줄과 완료 여부. */
data class YearTaskView(val task: YearTask, val done: Boolean)
