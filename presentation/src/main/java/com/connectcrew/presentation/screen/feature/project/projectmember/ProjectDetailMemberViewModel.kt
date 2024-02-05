package com.connectcrew.presentation.screen.feature.project.projectmember

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.connectcrew.domain.usecase.project.GetProjectMembersUseCase
import com.connectcrew.domain.usecase.project.KickProjectMemberUseCase
import com.connectcrew.domain.usecase.project.entity.ProjectMemberEntity
import com.connectcrew.domain.util.ApiResult
import com.connectcrew.domain.util.asResult
import com.connectcrew.presentation.model.project.KickReason
import com.connectcrew.presentation.model.project.KickReasonItem
import com.connectcrew.presentation.model.project.KickType
import com.connectcrew.presentation.model.project.ProjectMember
import com.connectcrew.presentation.model.project.asItem
import com.connectcrew.presentation.screen.base.BaseViewModel
import com.connectcrew.presentation.util.delegate.SignViewModelDelegate
import com.connectcrew.presentation.util.event.EventFlow
import com.connectcrew.presentation.util.event.MutableEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProjectDetailMemberViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getProjectMembersUseCase: GetProjectMembersUseCase,
    private val kickProjectMemberUseCase: KickProjectMemberUseCase,
    private val signViewModelDelegate: SignViewModelDelegate
) : BaseViewModel(), SignViewModelDelegate by signViewModelDelegate {

    private val projectId = savedStateHandle.getStateFlow<Long?>(KEY_PROJECT_ID, null)

    private val isProjectLeader = savedStateHandle.getStateFlow<Int?>(KEY_IS_PROJECT_LEADER, null)

    private val projectMembersResult =
        combine(loadDataSignal, projectId, ::Pair)
            .filter { it.second != null }
            .flatMapLatest { (_, id) -> getProjectMembersUseCase(GetProjectMembersUseCase.Param(id!!)) }
            .map { it.map(ProjectMemberEntity::asItem) }
            .cachedIn(viewModelScope)
            .onEach { projectMembers ->
                _projectMembers.value = projectMembers
                projectMembers.map { if (it.isLeader) setIsProjectLeader(it.profile.id == signViewModelDelegate.userId.value) }
            }
            .asResult()

    val _projectDetailMemberUiState = MutableStateFlow(InitializerUiState.Loading)
    val projectDetailMemberUiState: StateFlow<InitializerUiState> get() = _projectDetailMemberUiState

    private val _memberKickReasonItem: MutableStateFlow<List<KickReasonItem>> = MutableStateFlow(createMemberKickReasonItems())
    val memberKickReasonItem: StateFlow<List<KickReasonItem>> = _memberKickReasonItem

    val isReasonWrittenCorrect: StateFlow<Boolean> =
        _memberKickReasonItem.map {
            it.forEach {
                if (it.isChecked) {
                    if (it.kickReason.kickType == KickType.TYPE_ETC && it.kickReason.reason.isEmpty()) {
                        return@forEach
                    }
                    return@map true
                }
            }
            return@map false
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

//    val isReasonWrittenCorrect = flow {
//        _memberKickReasonItem.map {
//            it.forEach {
//                if(it.isChecked)
//                    if (it.kickReason.kickType == KickType.TYPE_ETC && it.kickReason.reason.isEmpty()) return@forEach
//                    emit(true)
//            }
//            emit(false)
//        }
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val kickTargetMember = savedStateHandle.getStateFlow<ProjectMember?>(KEY_PROJECT_KICK_TARGET, null)

    private val _projectMembers: MutableStateFlow<PagingData<ProjectMember>?> = MutableStateFlow(null)
    val projectMembers: StateFlow<PagingData<ProjectMember>?> = _projectMembers

    private val _navigateToProjectFeedDetail: MutableEventFlow<Long> = MutableEventFlow()
    val navigateToProjectFeedDetail: EventFlow<Long> = _navigateToProjectFeedDetail

    private val _navigateToKickMemberDialog: MutableEventFlow<ProjectMember> = MutableEventFlow()
    val navigateToKickMemberDialog: EventFlow<ProjectMember> = _navigateToKickMemberDialog

    private val _navigateToKickReasonDialog: MutableEventFlow<ProjectMember> = MutableEventFlow()
    val navigateToKickReasonDialog: EventFlow<ProjectMember> = _navigateToKickReasonDialog

    init {
        viewModelScope.launch {
            projectMembersResult.collect {
                Timber.d("${it is ApiResult.Loading}")
                when (it) {
                    is ApiResult.Success -> _projectDetailMemberUiState.value = InitializerUiState.Success
                    is ApiResult.Loading -> _projectDetailMemberUiState.value = InitializerUiState.Loading
                    is ApiResult.Error -> _projectDetailMemberUiState.value = InitializerUiState.Error
                }
            }
            kickTargetMember.filterNotNull().collect {
                _memberKickReasonItem.value = createMemberKickReasonItems()
            }
        }
    }

    fun setProjectId(projectId: Long) {
        savedStateHandle.set(KEY_PROJECT_ID, projectId)
    }

    fun setIsProjectLeader(isProjectLeader: Boolean) {
        savedStateHandle.set(KEY_IS_PROJECT_LEADER, isProjectLeader)
    }

    fun setKickTargetMember(member: ProjectMember) {
        savedStateHandle.set(KEY_PROJECT_KICK_TARGET, member)
    }

    fun setProjectMemberKickReason(reason: KickReason, isAdd: Boolean) {
        val reasons = mutableListOf<KickReasonItem>()

        reasons.addAll(_memberKickReasonItem.value)
        reasons.find { it.kickReason.kickType == reason.kickType }?.let {
            if (isAdd && it.kickReason.kickType == KickType.TYPE_ETC) it.kickReason.reason = reason.reason
            it.isChecked = isAdd
        }

        _memberKickReasonItem.value = reasons

        Timber.d("isReasonWrittenCorrect| ${isReasonWrittenCorrect.value}")
    }

    fun navigateToKickMemberDialog(member: ProjectMember) {
        setKickTargetMember(member)
        viewModelScope.launch {
            _navigateToKickMemberDialog.emit(member)
        }
    }

    // TODO::멤버 프로필로 이동 필요
    fun navigateToMemberProfile(projectMember: ProjectMember) {
        Timber.d("Navigate to member who named ${projectMember.profile.nickname}")
    }

    // TODO:: 네비게이션 관련 문제점 없는지 점검 필요
    fun navigateToProjectFeedDetail(projectId: Long) {
        Timber.d("Navigate to project id $projectId")
        viewModelScope.launch {
            _navigateToProjectFeedDetail.emit(projectId)
        }
    }

    fun navigateToKickReasonDialog(member: ProjectMember) {
        viewModelScope.launch {
            _navigateToKickReasonDialog.emit(member)
        }
    }

    fun kickMember() {
        // TODO:: KickUserUseCase 작성 후 동작, 수정 필요
        Timber.d("Kick Member who named ${kickTargetMember.value?.profile?.nickname}")
        Timber.d("SelectedKickReasons: ${memberKickReasonItem.value.filter { it.isChecked }}")
//        val tempKickReason = listOf<KickReason>()
////        TODO:: 임시 데이터로 호출
//        viewModelScope.launch {
//            kickProjectMemberUseCase(
//                KickProjectMemberUseCase.Params(
//                    projectId = projectId.value!!,
//                    memberId = kickTargetMember.value!!.profile.id,
//                    kickReasons = tempKickReason.map { it.asEntity() }
//                )
//            )
//        }

    }

    private fun createMemberKickReasonItems(): List<KickReasonItem> {
        return mutableListOf<KickReasonItem>().apply {
            KickType.entries.forEach { type ->
                add(
                    when (type) {
                        KickType.TYPE_ABUSE -> KickReasonItem(KickReason.KickReasonForAbuse(type))
                        KickType.TYPE_BAD_PARTICIPATION -> KickReasonItem(KickReason.KickReasonForBadParticipation(type))
                        KickType.TYPE_DISSENSION -> KickReasonItem(KickReason.KickReasonForDissension(type))
                        KickType.TYPE_GIVEN_UP -> KickReasonItem(KickReason.KickReasonForGivenUp(type))
                        KickType.TYPE_OBSCENITY -> KickReasonItem(KickReason.KickReasonForObscenity(type))
                        KickType.TYPE_ETC -> KickReasonItem(KickReason.KickReasonForEtc(type, ""))
                    }
                )
            }
        }
    }

    fun setEtcKickReason(reason: String) {
        _memberKickReasonItem.value.find { it.kickReason.kickType == KickType.TYPE_ETC }?.kickReason?.reason = reason
    }

    companion object {
        private const val KEY_PROJECT_ID = "project_id"
        private const val KEY_IS_PROJECT_LEADER = "is_project_leader"

        private const val KEY_PROJECT_KICK_TARGET = "project_member_kicked"
    }
}