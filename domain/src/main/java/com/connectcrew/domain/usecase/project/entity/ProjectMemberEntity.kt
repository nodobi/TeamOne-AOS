package com.connectcrew.domain.usecase.project.entity

import com.connectcrew.domain.usecase.Entity
import com.connectcrew.domain.usecase.sign.entity.UserEntity

class ProjectMemberEntity(
    val profile: UserEntity,
    val isLeader: Boolean,
    val parts: List<String>
) : Entity