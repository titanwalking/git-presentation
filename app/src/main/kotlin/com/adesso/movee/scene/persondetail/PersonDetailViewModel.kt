package com.adesso.movee.scene.persondetail

import android.app.Application
import com.adesso.movee.base.BaseAndroidViewModel
import com.adesso.movee.domain.FetchPersonDetailsUseCase
import com.adesso.movee.internal.util.AppBarStateChangeListener
import com.adesso.movee.internal.util.AppBarStateChangeListener.State.COLLAPSED
import com.adesso.movee.internal.util.AppBarStateChangeListener.State.EXPANDED
import com.adesso.movee.internal.util.AppBarStateChangeListener.State.IDLE
import com.adesso.movee.uimodel.PersonDetailUiModel
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PersonDetailViewModel @Inject constructor(
    private val fetchPersonDetailsUseCase: FetchPersonDetailsUseCase,
    application: Application
) : BaseAndroidViewModel(application) {

    private val _personDetails = MutableStateFlow<PersonDetailUiModel?>(null)
    private val _profileToolbarTitle = MutableStateFlow<String?>(null)
    val personDetails: StateFlow<PersonDetailUiModel?> get() = _personDetails
    val profileToolbarTitle: StateFlow<String?> get() = _profileToolbarTitle

    fun fetchPersonDetails(personId: Long) {
        if (_personDetails.value == null) {
            bgScope.launch {
                val personDetailResult =
                    fetchPersonDetailsUseCase.run(FetchPersonDetailsUseCase.Params(personId))

                onUIThread {
                    personDetailResult
                        .onSuccess(::postPersonDetails)
                        .onFailure(::handleFailure)
                }
            }
        }
    }

    private fun postPersonDetails(personDetailUiModel: PersonDetailUiModel) {
        _personDetails.value = personDetailUiModel
    }

    fun onProfileAppBarStateChange(state: AppBarStateChangeListener.State) {
        val title = when (state) {
            COLLAPSED -> _personDetails.value?.name ?: ""
            EXPANDED, IDLE -> ""
        }

        _profileToolbarTitle.value = title
    }
}
