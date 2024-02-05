package com.connectcrew.domain.usecase.project

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.connectcrew.domain.di.IoDispatcher
import com.connectcrew.domain.usecase.FlowUseCase
import com.connectcrew.domain.usecase.project.entity.ProjectMemberEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProjectMembersUseCase @Inject constructor(
    private val projectRepository: ProjectRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : FlowUseCase<GetProjectMembersUseCase.Param, PagingData<ProjectMemberEntity>>(ioDispatcher) {

    override fun execute(params: Param): Flow<PagingData<ProjectMemberEntity>> = Pager(
        config = PagingConfig(20, enablePlaceholders = true)
    ) {
        object : PagingSource<Int, ProjectMemberEntity>() {
            override fun getRefreshKey(state: PagingState<Int, ProjectMemberEntity>): Int? = null

            override suspend fun load(loadParams: LoadParams<Int>): LoadResult<Int, ProjectMemberEntity> {
                val page = loadParams.key

                return projectRepository.getProjectMembers(params.projectId).let {
                    LoadResult.Page(
                        data = it,
                        prevKey = null,
                        nextKey = if (it.size == PROJECT_MEMBER_PAGE_SIZE) it.last().profile.id else null
                    )
                }
            }
        }
    }.flow

    data class Param(
        val projectId: Long
    )

    companion object {
        private const val PROJECT_MEMBER_PAGE_SIZE = 20
    }
}