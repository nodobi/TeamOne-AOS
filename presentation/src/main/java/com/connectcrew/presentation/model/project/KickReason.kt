package com.connectcrew.presentation.model.project

import com.connectcrew.domain.usecase.project.entity.KickReasonEntity

// reason은  String.xml 에 넣어서 viewmodel 에서 분기할 때 추가해서 넣어주기
sealed class KickReason(open val kickType: KickType, open var reason: String = "") {
    data class KickReasonForAbuse(override val kickType: KickType, override var reason: String = "욕설/비하발언") : KickReason(kickType, reason)
    data class KickReasonForBadParticipation(override val kickType: KickType, override var reason: String = "참여율 저조") : KickReason(kickType, reason)
    data class KickReasonForDissension(override val kickType: KickType, override var reason: String = "팀원과의 불화") : KickReason(kickType, reason)
    data class KickReasonForGivenUp(override val kickType: KickType, override var reason: String = "자진 중도 포기") : KickReason(kickType, reason)
    data class KickReasonForObscenity(override val kickType: KickType, override var reason: String = "19+ 음란성, 만남 유도") : KickReason(kickType, reason)
    data class KickReasonForEtc(override val kickType: KickType, override var reason: String) : KickReason(kickType, reason)
}

fun KickReasonEntity.asItem(): KickReason {
    val kickType = enumValueOf<KickType>(type)
    return when (kickType) {
        KickType.TYPE_ABUSE -> KickReason.KickReasonForAbuse(kickType, reason)
        KickType.TYPE_BAD_PARTICIPATION -> KickReason.KickReasonForBadParticipation(kickType, reason)
        KickType.TYPE_DISSENSION -> KickReason.KickReasonForDissension(kickType, reason)
        KickType.TYPE_GIVEN_UP -> KickReason.KickReasonForGivenUp(kickType, reason)
        KickType.TYPE_OBSCENITY -> KickReason.KickReasonForObscenity(kickType, reason)
        KickType.TYPE_ETC -> KickReason.KickReasonForEtc(kickType, reason)
    }
}

fun KickReason.asEntity(): KickReasonEntity {
    return KickReasonEntity(kickType.name, reason)
}