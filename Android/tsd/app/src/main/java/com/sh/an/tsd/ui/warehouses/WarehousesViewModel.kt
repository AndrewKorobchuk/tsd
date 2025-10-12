package com.sh.an.tsd.ui.warehouses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sh.an.tsd.data.repository.WarehousesRepository
import com.sh.an.tsd.data.model.Warehouse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WarehousesViewModel(
    private val warehousesRepository: WarehousesRepository
) : ViewModel() {

    private val _warehouses = MutableStateFlow<List<Warehouse>>(emptyList())
    val warehouses: StateFlow<List<Warehouse>> = _warehouses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        warehousesRepository.allActiveWarehouses
                    } else {
                        warehousesRepository.searchActiveWarehouses(query)
                    }
                }
                .collect {
                    _warehouses.value = it
                }
        }
    }

    fun searchWarehouses(query: String) {
        _searchQuery.value = query
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
