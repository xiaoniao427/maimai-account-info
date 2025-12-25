package com.maimai.account.info.model

import kotlinx.serialization.Serializable

@Serializable
data class SongRecord(
    val achievements: Double,
    val ds: Double,
    val dxScore: Int,
    val fc: String,
    val fs: String,
    val is_new: Boolean,
    val level: String,
    val level_index: Int,
    val play_count: Int,
    val ra: Int,
    val rate: String,
    val song_id: Int,
    val title: String,
    val type: String
)