package com.connectcrew.presentation.adapter.project.member

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.connectcrew.presentation.R
import com.connectcrew.presentation.databinding.ItemProjectMemberBinding
import com.connectcrew.presentation.model.project.ProjectMember
import com.connectcrew.presentation.model.project.RepresentProject
import com.connectcrew.presentation.util.executeAfter
import com.connectcrew.presentation.util.listener.setOnSingleClickListener
import com.connectcrew.presentation.util.widget.RecyclerviewItemDecoration
import timber.log.Timber

class ProjectMemberAdapter(
    private val onClickMemberProfile: (ProjectMember) -> Unit,
    private val onKickMember: (ProjectMember) -> Unit,
    private val onClickRepresentProject: (Long) -> Unit
) : PagingDataAdapter<ProjectMember, ProjectMemberViewHolder>(
    object : DiffUtil.ItemCallback<ProjectMember>() {
        override fun areItemsTheSame(oldItem: ProjectMember, newItem: ProjectMember): Boolean {
            return oldItem.profile.id == newItem.profile.id
        }

        override fun areContentsTheSame(oldItem: ProjectMember, newItem: ProjectMember): Boolean {
            return oldItem.profile.id == newItem.profile.id
        }
    }
) {
    private var isProjectLeader = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectMemberViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ProjectMemberViewHolder(ItemProjectMemberBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ProjectMemberViewHolder, position: Int) {
        val data = getItem(position) ?: return

        holder.binding.executeAfter {
            with(rvMemberRepresentProjects) {
                adapter = ProjectMemberRepresentProjectAdapter(onClickRepresentProject).apply {
                    submitList(data.profile.representProjects)

                    // TODO:: 대표 프로젝트 관련 추가 후 임시 데이터 삭제
                    submitList(
                        mutableListOf(
                            RepresentProject(68, "/project/banner/50b5f11f-8d99-4f82-b976-08ecb112aaab.jpg"),
                            RepresentProject(67, "/project/banner/2b216eec-4d46-4394-8f4f-cfd7a66ac235.jpg"),
                            RepresentProject(63, null)
                        )
                    )
                }
                layoutManager = GridLayoutManager(context, 3)
                addItemDecoration(RecyclerviewItemDecoration(0, 0, 8, 8, R.layout.item_project_member))
            }

            isLeader = isProjectLeader
            itemPosition = position

            tvMemberName.text = data.profile.nickname
            tvMemberPart.text = data.parts.joinToString(",")
            tvMemberIntroduction.text = data.profile.introduction

            // TODO:: API가 정상적으로 작성되는 경우 수정 필요
//            if (data.profile.representProjects.isEmpty()) {
            if(false) {
                cvMemberRepresentProjectEmpty.visibility = View.VISIBLE
                rvMemberRepresentProjects.visibility = View.INVISIBLE
            } else {
                cvMemberRepresentProjectEmpty.visibility = View.INVISIBLE
                rvMemberRepresentProjects.visibility = View.VISIBLE
            }
            ivNavigateProfile.setOnSingleClickListener { onClickMemberProfile(data) }

            if (isProjectLeader && position != 0) {
                viewLine.isGone = false
                btnMemberKick.isGone = false
            } else {
                viewLine.isGone = true
                btnMemberKick.isGone = true
            }

            btnMemberKick.setOnSingleClickListener { onKickMember(data) }

        }
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_project_member

    fun setIsLeader(isVisible: Boolean) {
        isProjectLeader = isVisible
    }
}