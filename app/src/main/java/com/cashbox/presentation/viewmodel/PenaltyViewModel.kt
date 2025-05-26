package com.cashbox.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashbox.domain.model.Penalty
import com.cashbox.domain.usecase.penalty.CreatePenaltyUseCase
import com.cashbox.domain.usecase.penalty.GetPenaltiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for penalties screen.
 */
data class PenaltyUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for penalties screen.
 */
@HiltViewModel
class PenaltyViewModel @Inject constructor(
    private val getPenaltiesUseCase: GetPenaltiesUseCase,
    private val createPenaltyUseCase: CreatePenaltyUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PenaltyUiState())
    val uiState: StateFlow<PenaltyUiState> = _uiState.asStateFlow()
    
    private val _penalties = MutableLiveData<List<Penalty>>()
    val penalties: LiveData<List<Penalty>> = _penalties
    
    init {
        loadPenalties()
    }
    
    private fun loadPenalties() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getPenaltiesUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
                .collect { penaltiesList ->
                    _penalties.value = penaltiesList
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }
    
    fun createPenalty(penalty: Penalty) {
        viewModelScope.launch {
            createPenaltyUseCase(penalty)
                .onSuccess {
                    // Refresh penalties list
                    loadPenalties()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message
                    )
                }
        }
    }
    
    fun refresh() {
        loadPenalties()
    }
}