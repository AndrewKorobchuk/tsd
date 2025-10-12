package com.sh.an.tsd.ui.units

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sh.an.tsd.data.model.UnitOfMeasure
import com.sh.an.tsd.data.repository.UnitsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UnitsViewModel(
    private val unitsRepository: UnitsRepository
) : ViewModel() {

    private val _units = MutableStateFlow<List<UnitOfMeasure>>(emptyList())
    val units: StateFlow<List<UnitOfMeasure>> = _units.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()

    private val _localUnitsCount = MutableStateFlow(0)
    val localUnitsCount: StateFlow<Int> = _localUnitsCount.asStateFlow()

    init {
        loadLocalUnits()
        loadLocalUnitsCount()
        loadLastSyncTime()
    }

    private fun loadLocalUnits() {
        viewModelScope.launch {
            unitsRepository.getAllActiveUnits().collect { units ->
                _units.value = units
            }
        }
    }

    private fun loadLocalUnitsCount() {
        viewModelScope.launch {
            _localUnitsCount.value = unitsRepository.getLocalUnitsCount()
        }
    }

    private fun loadLastSyncTime() {
        viewModelScope.launch {
            _lastSyncTime.value = unitsRepository.getLastUpdateTime()
        }
    }

    fun searchUnits(searchQuery: String) {
        viewModelScope.launch {
            if (searchQuery.isBlank()) {
                loadLocalUnits()
            } else {
                unitsRepository.searchActiveUnits(searchQuery).collect { units ->
                    _units.value = units
                }
            }
        }
    }

    fun syncUnitsFromServer(authorization: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = unitsRepository.syncUnitsFromServer(authorization)
            result.fold(
                onSuccess = { count ->
                    _localUnitsCount.value = count
                    loadLastSyncTime()
                    println("Successfully synced $count units from server")
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Ошибка синхронизации"
                    println("Failed to sync units: ${error.message}")
                }
            )

            _isLoading.value = false
        }
    }

    fun loadUnitsFromServer(authorization: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = unitsRepository.loadUnitsFromServer(authorization)
            result.fold(
                onSuccess = { count ->
                    _localUnitsCount.value = count
                    loadLastSyncTime()
                    println("Successfully loaded $count units from server")
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "Ошибка загрузки"
                    println("Failed to load units: ${error.message}")
                }
            )

            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
