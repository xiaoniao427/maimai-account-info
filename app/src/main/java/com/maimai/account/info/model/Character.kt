package com.maimai.account.info.model

import kotlinx.serialization.Serializable

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