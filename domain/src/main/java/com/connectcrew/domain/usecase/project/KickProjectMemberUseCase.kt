package com.connectcrew.domain.usecase.project

import com.connectcrew.domain.di.IoDispatcher
import com.connectcrew.domain.usecase.FlowUseCase
import com.connectcrew.domain.usecase.project.entity.KickReasonEntity
import com.connectcrew.domain.usecase.sign.entity.UserEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class KickProjectMemberUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : FlowUseCase<KickProjectMemberUseCase.Params, UserEntity>(ioDispatcher) {

    override fun execute(params: Params): Flow<UserEntity> = flow {
        emit(
            projectRepository.kickProjectMember(
                params.projectId,
                params.memberId,
                params.kickReasons
            )
        )
    }

    data class Params(
        val projectId: Long,
        val memberId: Int,
        val kickReasons: List<KickReasonEntity>
    )
}