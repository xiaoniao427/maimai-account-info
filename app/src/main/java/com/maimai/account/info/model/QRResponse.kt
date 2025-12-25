package com.maimai.account.info.model

import kotlinx.serialization.Serializable

@Serializable
data class QRResponse(
    val errorID: Int,
    val userID: Int
)