package com.connectcrew.presentation.util.delegate

import com.connectcrew.domain.di.ApplicationScope
import com.connectcrew.domain.usecase.token.GetUpdatedTokenUseCase
import com.connectcrew.domain.usecase.user.ObserveUserInfoUseCase
import com.connectcrew.domain.util.asResult
import com.connectcrew.domain.util.data
import com.connectcrew.presentation.model.user.User
import com.connectcrew.presentation.model.user.asItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

interface SignViewModelDelegate {

    val user: StateFlow<User?>

    val userToken: StateFlow<String?>

    val userNickname: StateFlow<String?>

    val userEmail: StateFlow<String?>

    val userProfileUrl: StateFlow<String?>

    val userTemperaTure: StateFlow<Float>

    val userResponseRate: StateFlow<Float>

    val userIntroduction: StateFlow<String?>

    val userParts: StateFlow<List<String>>

    suspend fun refreshUserToken()
}

class SignViewModelDelegateImpl @Inject constructor(
    @ApplicationScope applicationScope: CoroutineScope,
    observeUserInfoUseCase: ObserveUserInfoUseCase,
    private val getUpdatedTokenUseCase: GetUpdatedTokenUseCase,
) : SignViewModelDelegate {

    override val user: StateFlow<User?> = observeUserInfoUseCase(Unit)
        .asResult()
        .mapLatest { it.data?.asItem() }
        .stateIn(applicationScope, SharingStarted.Eagerly, null)

    override val userToken: StateFlow<String?> = user
        .mapLatest { it?.accessToken }
        .stateIn(applicationScope, SharingStarted.Eagerly, null)

    override val userNickname: StateFlow<String?> = user
        .mapLatest { it?.nickname }
        .stateIn(applicationScope, SharingStarted.Eagerly, null)

    override val userEmail: StateFlow<String?> = user
        .mapLatest { it?.email }
        .stateIn(applicationScope, SharingStarted.Eagerly, null)

    override val userProfileUrl: StateFlow<String?> = user
        .mapLatest { it?.profile }
        .stateIn(applicationScope, SharingStarted.Eagerly, null)

    override val userTemperaTure: StateFlow<Float> = user
        .mapLatest { it?.temperature ?: 0f }
        .stateIn(applicationScope, SharingStarted.Eagerly, 0f)

    override val userResponseRate: StateFlow<Float> = user
        .mapLatest { it?.responseRate ?: 0f }
        .stateIn(applicationScope, SharingStarted.Eagerly, 0f)

    override val userIntroduction: StateFlow<String?> = user
        .mapLatest { it?.introduction }
        .stateIn(applicationScope, SharingStarted.Eagerly, null)

    override val userParts: StateFlow<List<String>> = user
        .mapLatest { it?.parts ?: emptyList() }
        .stateIn(applicationScope, SharingStarted.Eagerly, emptyList())

    init {
        user.launchIn(applicationScope)
    }

    override suspend fun refreshUserToken() {
        getUpdatedTokenUseCase(Unit).asResult().collect()
    }
}