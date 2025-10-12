package com.sh.an.tsd.ui.nomenclature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sh.an.tsd.data.repository.NomenclatureCategoriesRepository
import com.sh.an.tsd.data.model.NomenclatureCategory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NomenclatureCategoriesViewModel(
    private val nomenclatureCategoriesRepository: NomenclatureCategoriesRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<NomenclatureCategory>>(emptyList())
    val categories: StateFlow<List<NomenclatureCategory>> = _categories.asStateFlow()

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
                        nomenclatureCategoriesRepository.allActiveCategories
                    } else {
                        nomenclatureCategoriesRepository.searchActiveCategories(query)
                    }
                }
                .collect {
                    _categories.value = it
                }
        }
    }

    fun searchCategories(query: String) {
        _searchQuery.value = query
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
