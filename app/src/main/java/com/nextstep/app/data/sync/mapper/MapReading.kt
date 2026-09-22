package com.nextstep.app.data.sync.mapper

/** Firestore 문서 Map 에서 타입 안전하게 값을 읽는 헬퍼. 없거나 타입이 다르면 기본값. */
internal fun Map<String, Any?>.str(key: String, default: String = ""): String = this[key] as? String ?: default
internal fun Map<String, Any?>.strOrNull(key: String): String? = this[key] as? String
internal fun Map<String, Any?>.long(key: String, default: Long = 0L): Long = (this[key] as? Number)?.toLong() ?: default
internal fun Map<String, Any?>.longOrNull(key: String): Long? = (this[key] as? Number)?.toLong()
internal fun Map<String, Any?>.int(key: String, default: Int = 0): Int = (this[key] as? Number)?.toInt() ?: default
internal fun Map<String, Any?>.dbl(key: String, default: Double = 0.0): Double = (this[key] as? Number)?.toDouble() ?: default
internal fun Map<String, Any?>.dblOrNull(key: String): Double? = (this[key] as? Number)?.toDouble()
internal fun Map<String, Any?>.bool(key: String, default: Boolean = false): Boolean = this[key] as? Boolean ?: default
