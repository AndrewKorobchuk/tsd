package com.sh.an.tsd.ui.directories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sh.an.tsd.data.repository.UnitsRepository
import com.sh.an.tsd.data.repository.NomenclatureCategoriesRepository
import com.sh.an.tsd.data.repository.NomenclatureRepository
import com.sh.an.tsd.data.repository.WarehousesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DirectoriesViewModel(
    private val unitsRepository: UnitsRepository,
    private val nomenclatureCategoriesRepository: NomenclatureCategoriesRepository,
    private val nomenclatureRepository: NomenclatureRepository,
    private val warehousesRepository: WarehousesRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _syncProgress = MutableStateFlow("")
    val syncProgress: StateFlow<String> = _syncProgress.asStateFlow()

    private val _unitsCount = MutableStateFlow(0)
    val unitsCount: StateFlow<Int> = _unitsCount.asStateFlow()

    private val _categoriesCount = MutableStateFlow(0)
    val categoriesCount: StateFlow<Int> = _categoriesCount.asStateFlow()

    private val _nomenclatureCount = MutableStateFlow(0)
    val nomenclatureCount: StateFlow<Int> = _nomenclatureCount.asStateFlow()

    private val _warehousesCount = MutableStateFlow(0)
    val warehousesCount: StateFlow<Int> = _warehousesCount.asStateFlow()

    init {
        loadLocalCounts()
    }

    fun syncAllDirectories(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _syncProgress.value = "Начинаем синхронизацию..."
            
            try {
                // Синхронизируем единицы измерения
                _syncProgress.value = "Загружаем единицы измерения..."
                val unitsResult = unitsRepository.syncUnitsFromServer(token)
                if (unitsResult.isFailure) {
                    _errorMessage.value = "Ошибка загрузки единиц измерения: ${unitsResult.exceptionOrNull()?.message}"
                    return@launch
                }

                // Синхронизируем категории номенклатуры
                _syncProgress.value = "Загружаем категории номенклатуры..."
                val categoriesResult = nomenclatureCategoriesRepository.syncCategoriesFromServer(token)
                if (categoriesResult.isFailure) {
                    _errorMessage.value = "Ошибка загрузки категорий: ${categoriesResult.exceptionOrNull()?.message}"
                    return@launch
                }

                // Синхронизируем номенклатуру
                _syncProgress.value = "Загружаем номенклатуру..."
                val nomenclatureResult = nomenclatureRepository.syncNomenclatureFromServer(token)
                if (nomenclatureResult.isFailure) {
                    _errorMessage.value = "Ошибка загрузки номенклатуры: ${nomenclatureResult.exceptionOrNull()?.message}"
                    return@launch
                }

                // Синхронизируем склады
                _syncProgress.value = "Загружаем склады..."
                val warehousesResult = warehousesRepository.syncWarehousesFromServer(token)
                if (warehousesResult.isFailure) {
                    _errorMessage.value = "Ошибка загрузки складов: ${warehousesResult.exceptionOrNull()?.message}"
                    return@launch
                }

                _syncProgress.value = "Синхронизация завершена успешно!"
                loadLocalCounts()
                
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка сети: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadLocalCounts() {
        viewModelScope.launch {
            _unitsCount.value = unitsRepository.getLocalUnitsCount()
            _categoriesCount.value = nomenclatureCategoriesRepository.getLocalCategoriesCount()
            _nomenclatureCount.value = nomenclatureRepository.getLocalNomenclatureCount()
            _warehousesCount.value = warehousesRepository.getLocalWarehousesCount()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearProgress() {
        _syncProgress.value = ""
    }
}
