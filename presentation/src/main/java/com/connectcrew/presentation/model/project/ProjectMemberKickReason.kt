package com.connectcrew.presentation.model.project

data class ProjectMemberKickReason(
    val type: String,
    val reason: String,
    var isChecked: Boolean = false
)
