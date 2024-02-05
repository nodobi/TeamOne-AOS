package com.connectcrew.presentation.screen.feature.project.projectmember

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.connectcrew.presentation.R
import com.connectcrew.presentation.adapter.project.member.ProjectMemberAdapter
import com.connectcrew.presentation.databinding.FragmentProjectDetailMemberBinding
import com.connectcrew.presentation.model.project.ProjectFeedDetailCategory
import com.connectcrew.presentation.screen.base.BaseFragment
import com.connectcrew.presentation.screen.feature.project.ProjectDetailContainerFragmentDirections
import com.connectcrew.presentation.screen.feature.project.ProjectDetailContainerViewModel
import com.connectcrew.presentation.util.launchAndRepeatWithViewLifecycle
import com.connectcrew.presentation.util.safeNavigate
import com.connectcrew.presentation.util.view.createAlert
import com.connectcrew.presentation.util.view.dialogViewBuilder
import com.connectcrew.presentation.util.widget.RecyclerviewItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch


// TODO:: 동작을 하는 도중 삭제, 변경 등의 상태를 반영하여 동작해야함!!!
// TODO:: 다이얼로그 불러오는 동작중에 로딩 바 보여주어야 함
@AndroidEntryPoint
class ProjectDetailMemberFragment : BaseFragment<FragmentProjectDetailMemberBinding>(R.layout.fragment_project_detail_member) {

    private val projectDetailContainerViewModel: ProjectDetailContainerViewModel by hiltNavGraphViewModels(R.id.nav_project_detail)

    private val projectDetailMemberViewModel: ProjectDetailMemberViewModel by hiltNavGraphViewModels(R.id.nav_project_detail)

    private val projectMemberAdapter: ProjectMemberAdapter by lazy {
        ProjectMemberAdapter(
            onClickMemberProfile = projectDetailMemberViewModel::navigateToMemberProfile,
            onKickMember = projectDetailMemberViewModel::navigateToKickMemberDialog,
            onClickRepresentProject = projectDetailMemberViewModel::navigateToProjectFeedDetail
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(dataBinding) {
            viewModel = projectDetailMemberViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        initView()
        initListener()
        initObserver()
    }


    private fun initView() {
        with(dataBinding.rvMembers) {
            adapter = projectMemberAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(RecyclerviewItemDecoration(0, 8, 0, 0, R.layout.item_project_member))
            hasFixedSize()
        }

    }

    private fun initListener() {
//        TODO("Not yet implemented")
    }

    private fun initObserver() {
        launchAndRepeatWithViewLifecycle {
            launch {
                projectDetailContainerViewModel.projectId.filterNotNull().collect {
                    projectDetailMemberViewModel.setProjectId(it)
                }
            }
            launch {
                projectDetailContainerViewModel.isProjectLeader.filterNotNull().collect {
                    projectDetailMemberViewModel.setIsProjectLeader(it)
                    projectMemberAdapter.setIsLeader(it)
                }
            }
            launch {
                projectDetailMemberViewModel.projectMembers.filterNotNull().collectLatest {
                    projectMemberAdapter.submitData(it)
                }
            }
            launch {
                projectDetailMemberViewModel.navigateToProjectFeedDetail.collect {
                    findNavController().safeNavigate(ProjectDetailContainerFragmentDirections.actionProjectDetailContainerFragmentToNavProjectDetail(it))
                    projectDetailContainerViewModel.setSelectedProjectDetailCategory(ProjectFeedDetailCategory.INTRODUCTION)
                }
            }
            launch {
                // TODO:: 이후 다이얼로그 작성 로직 필요
                projectDetailMemberViewModel.navigateToKickMemberDialog.collect { member ->
                    createAlert(requireContext())
                        .dialogViewBuilder(
                            titleRes = R.string.project_detail_member_kick_title,
                            titleResArg = member.profile.nickname,
                            descriptionRes = R.string.project_detail_member_kick_description,
                            positiveButtonTextRes = R.string.project_detail_member_kick,
                            iconTint = R.color.color_d62246,
                            iconDrawableRes = R.drawable.ic_warning,
                            onClickPositiveButton = { projectDetailMemberViewModel.navigateToKickReasonDialog(member) }
                        ).show()
                }
            }
            launch {
                projectDetailMemberViewModel.navigateToKickReasonDialog.collect { member ->
                    findNavController().safeNavigate(ProjectDetailContainerFragmentDirections.actionProjectDetailContainerFragmentToProjectKickReasonAlertDialog())
                }
            }
        }

    }

    companion object {
        fun getInstance(): ProjectDetailMemberFragment {
            return ProjectDetailMemberFragment()
        }
    }
}