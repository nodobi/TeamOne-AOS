package com.connectcrew.presentation.screen.feature.project.projectmember

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.connectcrew.presentation.R
import com.connectcrew.presentation.adapter.project.member.ProjectMemberKickReasonAdapter
import com.connectcrew.presentation.databinding.DialogProjectKickReasonBinding
import com.connectcrew.presentation.screen.base.BaseAlertDialogFragment
import com.connectcrew.presentation.util.hideKeyboard
import com.connectcrew.presentation.util.launchAndRepeatWithViewLifecycle
import com.connectcrew.presentation.util.listener.DebounceEditTextListener
import com.connectcrew.presentation.util.listener.setOnSingleClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

class ProjectKickReasonAlertDialog : BaseAlertDialogFragment<DialogProjectKickReasonBinding>() {
    override val layoutResId: Int = R.layout.dialog_project_kick_reason

    private val projectDetailMemberViewModel: ProjectDetailMemberViewModel by hiltNavGraphViewModels(R.id.nav_project_detail)

    private val etcReasonTextChangeListener by lazy {
        DebounceEditTextListener(
            debouncePeriod = 0L,
            scope = projectDetailMemberViewModel.viewModelScope,
            onDebounceEditTextChange = projectDetailMemberViewModel::setEtcKickReason
        )
    }

    private val kickReasonAdapter: ProjectMemberKickReasonAdapter by lazy {
        ProjectMemberKickReasonAdapter(
            coroutineScope = projectDetailMemberViewModel.viewModelScope,
            onCheckKickReason = projectDetailMemberViewModel::setProjectMemberKickReason,
            etcReasonChangeListener = etcReasonTextChangeListener
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
        with(dataBinding.rvKickReason) {
            adapter = kickReasonAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun initListener() {
        with(dataBinding) {
            btnKick.setOnSingleClickListener {
                projectDetailMemberViewModel.kickMember()
                lifecycleScope.launch {
                    dataBinding.rvKickReason.hideKeyboard().also {
                        delay(100)
                        dismiss()
                    }
                }
            }

            btnCancel.setOnSingleClickListener {
                lifecycleScope.launch {
                    dataBinding.rvKickReason.hideKeyboard().also {
                        delay(100)
                        dismiss()
                    }
                }
            }
        }
    }

    private fun initObserver() {
        launchAndRepeatWithViewLifecycle {
            launch {
                projectDetailMemberViewModel.memberKickReasonItem.filterNotNull().collectLatest {
                    Timber.d("Collect memberKickReasonItem")
                    kickReasonAdapter.submitList(it)
                }
            }
        }
    }

}
