package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.SubjectEntity

data class SubjectMinutes(val subject: SubjectEntity?, val minutes: Int, val goalMinutes: Int)
