package com.connectcrew.data.model.project

import com.connectcrew.domain.usecase.project.entity.KickReasonEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KickReason(
    @Json(name = "type")
    val type: String,
    @Json(name = "reason")
    val reason: String
)

fun KickReason.asEntity(): KickReasonEntity {
    return KickReasonEntity(
        type = type,
        reason = reason
    )
}

fun KickReasonEntity.asExternalModel(): KickReason {
    return KickReason(
        type = type,
        reason = reason
    )
}