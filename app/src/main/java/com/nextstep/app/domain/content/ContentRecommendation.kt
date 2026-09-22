package com.nextstep.app.domain.content

import com.nextstep.app.data.local.entity.ContentEntity

data class ContentRecommendation(val content: ContentEntity, val score: Float, val reason: String)
