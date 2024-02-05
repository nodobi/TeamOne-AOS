package com.connectcrew.presentation.adapter.project.member

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.connectcrew.presentation.R
import com.connectcrew.presentation.databinding.ItemCheckedTextBinding
import com.connectcrew.presentation.databinding.ItemCheckedTextWithEditBinding
import com.connectcrew.presentation.model.project.KickReason
import com.connectcrew.presentation.model.project.KickReasonItem
import com.connectcrew.presentation.util.executeAfter
import com.connectcrew.presentation.util.listener.DebounceEditTextListener
import kotlinx.coroutines.CoroutineScope


class ProjectMemberKickReasonAdapter(
    private val coroutineScope: CoroutineScope,
    private val onCheckKickReason: (KickReason, Boolean) -> Unit,
    private val etcReasonChangeListener: DebounceEditTextListener
) : ListAdapter<KickReasonItem, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<KickReasonItem>() {
        override fun areItemsTheSame(oldItem: KickReasonItem, newItem: KickReasonItem): Boolean {
            return oldItem.kickReason.reason.compareTo(newItem.kickReason.reason) == 0
        }

        override fun areContentsTheSame(oldItem: KickReasonItem, newItem: KickReasonItem): Boolean {
            return oldItem.kickReason.reason.compareTo(newItem.kickReason.reason) == 0
        }
    }
) {

    private lateinit var etcReasonChangeListenerForWarning: DebounceEditTextListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            ViewType.TEXT.ordinal -> {
                ProjectMemberKickReasonViewHolder(ItemCheckedTextBinding.inflate(inflater, parent, false))
            }

            ViewType.EDITTEXT.ordinal -> {
                ProjectMemberKickReasonEtcViewHolder(ItemCheckedTextWithEditBinding.inflate(inflater, parent, false))
            }

            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = getItem(position) ?: return

        // TODO:: 기타 밑에 입력하지 않았을 떄 오류 띄우는 텍스트 Visibility 설정 필요
        when (holder) {
            is ProjectMemberKickReasonViewHolder -> {
                holder.binding.executeAfter {
                    reason = data

                    // 클릭 리스너 말고 데이터 바인딩 통해서 할 수 있도록 변경 필요
                    cbKickReason.setOnCheckedChangeListener { _, isChecked ->
                        val recruitmentColor = if (isChecked) R.color.color_00aee4 else R.color.color_616161
                        cbKickReason.setTextColor(cbKickReason.context.getColor(recruitmentColor))
//                        getItem(position).isChecked = isChecked
//                        reason = data
                        onCheckKickReason(data.kickReason, isChecked)
                    }
                }
            }

            is ProjectMemberKickReasonEtcViewHolder -> {
                holder.binding.executeAfter {
                    reason = data

                    etcReasonChangeListenerForWarning = DebounceEditTextListener(
                        scope = coroutineScope,
                        debouncePeriod = 0L,
                        onDebounceEditTextChange = { groupWarning.isGone = (it.length != 0) }
                    )

                    tietKickReason.addTextChangedListener(etcReasonChangeListenerForWarning)
                    tietKickReason.addTextChangedListener(etcReasonChangeListener)

                    // 클릭 리스너 말고 데이터 바인딩 통해서 할 수 있도록 변경 필요
                    cbKickReasonTitle.setOnCheckedChangeListener { _, isChecked ->
                        val recruitmentColor = if (isChecked) R.color.color_00aee4 else R.color.color_616161
                        cbKickReasonTitle.setTextColor(cbKickReasonTitle.context.getColor(recruitmentColor))

                        if(isChecked && tietKickReason.length() == 0) groupWarning.isGone = false
                        if(!isChecked) groupWarning.isGone = true

                        with(tietKickReason) {
                            isFocusable = isChecked
                            isFocusableInTouchMode = isChecked
                        }
                        onCheckKickReason(data.kickReason, isChecked)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            itemCount - 1 -> ViewType.EDITTEXT.ordinal
            else -> ViewType.TEXT.ordinal
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is ProjectMemberKickReasonEtcViewHolder) {
            holder.binding.tietKickReason.removeTextChangedListener(etcReasonChangeListener)
            holder.binding.tietKickReason.removeTextChangedListener(etcReasonChangeListenerForWarning)
        }
        super.onViewAttachedToWindow(holder)
    }

    private enum class ViewType {
        TEXT, EDITTEXT
    }
}