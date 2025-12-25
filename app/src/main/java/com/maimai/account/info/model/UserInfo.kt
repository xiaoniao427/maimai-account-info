package com.maimai.account.info.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val userName: String,
    val iconId: Int,
    val rating: Int,
    val isLogin: Boolean,
    val info: Info? = null,
    val b50: B50Data? = null,
    val characters: List<Character>? = null,
    val divingFishData: List<SongRecord>? = null
)

@Serializable
data class Info(
    val banState: Int,
    val dispRate: Int,
    val lastDataVersion: String,
    val lastRomVersion: String,
    val totalAwake: Int
)

@Serializable
data class B50Data(
    val dx: List<SongRecord>? = null,
    val sd: List<SongRecord>? = null
)

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

@Serializable
data class Character(
    val awakening: Int,
    val characterId: Int,
    val level: Int,
    val nextAwake: Int,
    val nextAwakePercent: Int,
    val point: Int,
    val useCount: Int
)