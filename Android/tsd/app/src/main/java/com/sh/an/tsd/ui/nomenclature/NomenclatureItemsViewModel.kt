package com.sh.an.tsd.ui.nomenclature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sh.an.tsd.data.repository.NomenclatureRepository
import com.sh.an.tsd.data.model.Nomenclature
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NomenclatureItemsViewModel(
    private val nomenclatureRepository: NomenclatureRepository
) : ViewModel() {

    private val _nomenclature = MutableStateFlow<List<Nomenclature>>(emptyList())
    val nomenclature: StateFlow<List<Nomenclature>> = _nomenclature.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _categoryId = MutableStateFlow<Int?>(null)

    fun setCategoryId(categoryId: Int) {
        _categoryId.value = categoryId
    }

    init {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300),
                _categoryId
            ) { query, categoryId ->
                Pair(query, categoryId)
            }
                .flatMapLatest { (query, categoryId) ->
                    when {
                        categoryId != null && query.isNotBlank() -> {
                            nomenclatureRepository.searchActiveNomenclatureByCategory(categoryId, query)
                        }
                        categoryId != null -> {
                            nomenclatureRepository.getActiveNomenclatureByCategory(categoryId)
                        }
                        query.isNotBlank() -> {
                            nomenclatureRepository.searchActiveNomenclature(query)
                        }
                        else -> {
                            nomenclatureRepository.allActiveNomenclature
                        }
                    }
                }
                .collect {
                    _nomenclature.value = it
                }
        }
    }

    fun searchNomenclature(query: String) {
        _searchQuery.value = query
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
