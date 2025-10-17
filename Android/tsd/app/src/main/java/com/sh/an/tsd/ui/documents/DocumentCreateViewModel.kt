package com.sh.an.tsd.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sh.an.tsd.data.model.*
import com.sh.an.tsd.data.repository.DocumentsRepository
import com.sh.an.tsd.data.repository.WarehousesRepository
import com.sh.an.tsd.data.repository.NomenclatureRepository
import com.sh.an.tsd.data.repository.UnitsRepository
import com.sh.an.tsd.data.repository.TsdDeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DocumentCreateViewModel(
    private val documentsRepository: DocumentsRepository,
    private val warehousesRepository: WarehousesRepository,
    private val nomenclatureRepository: NomenclatureRepository,
    private val unitsRepository: UnitsRepository,
    private val tsdDeviceRepository: TsdDeviceRepository,
    private val devicePrefix: String = ""
) : ViewModel() {

    // Состояние формы документа
    private val _documentNumber = MutableStateFlow("")
    val documentNumber: StateFlow<String> = _documentNumber.asStateFlow()

    private val _selectedWarehouse = MutableStateFlow<Warehouse?>(null)
    val selectedWarehouse: StateFlow<Warehouse?> = _selectedWarehouse.asStateFlow()

    private val _documentDate = MutableStateFlow(Date())
    val documentDate: StateFlow<Date> = _documentDate.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    // Строки документа
    private val _documentItems = MutableStateFlow<List<DocumentItem>>(emptyList())
    val documentItems: StateFlow<List<DocumentItem>> = _documentItems.asStateFlow()

    // Данные для выпадающих списков
    val warehouses: StateFlow<List<Warehouse>> = warehousesRepository.allActiveWarehouses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val nomenclature: StateFlow<List<Nomenclature>> = nomenclatureRepository.allActiveNomenclature.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val units: StateFlow<List<UnitOfMeasure>> = unitsRepository.getAllActiveUnits().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Состояние загрузки и ошибок
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // Генерация номера документа с инкрементной нумерацией
    fun generateDocumentNumber() {
        viewModelScope.launch {
            try {
                val result = tsdDeviceRepository.getNextDocumentNumber("input_balance")
                if (result.isSuccess) {
                    _documentNumber.value = result.getOrNull()?.documentNumber ?: ""
                } else {
                    // Fallback к старому методу, если не удалось получить номер с сервера
                    val dateFormatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                    val timeFormatter = SimpleDateFormat("HHmmss", Locale.getDefault())
                    val currentDate = Date()
                    val dateStr = dateFormatter.format(currentDate)
                    val timeStr = timeFormatter.format(currentDate)
                    
                    val prefix = if (devicePrefix.isNotEmpty()) devicePrefix else "ВО"
                    _documentNumber.value = "$prefix-$dateStr-$timeStr"
                }
            } catch (e: Exception) {
                // Fallback к старому методу в случае ошибки
                val dateFormatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val timeFormatter = SimpleDateFormat("HHmmss", Locale.getDefault())
                val currentDate = Date()
                val dateStr = dateFormatter.format(currentDate)
                val timeStr = timeFormatter.format(currentDate)
                
                val prefix = if (devicePrefix.isNotEmpty()) devicePrefix else "ВО"
                _documentNumber.value = "$prefix-$dateStr-$timeStr"
            }
        }
    }

    // Обновление полей формы
    fun updateDocumentNumber(number: String) {
        _documentNumber.value = number
    }

    fun updateSelectedWarehouse(warehouse: Warehouse?) {
        _selectedWarehouse.value = warehouse
    }

    fun updateDocumentDate(date: Date) {
        _documentDate.value = date
    }

    fun updateDescription(desc: String) {
        _description.value = desc
    }

    // Управление строками документа
    fun addDocumentItem(item: DocumentItem) {
        val currentItems = _documentItems.value.toMutableList()
        currentItems.add(item)
        _documentItems.value = currentItems
    }

    fun updateDocumentItem(updatedItem: DocumentItem) {
        val currentItems = _documentItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            currentItems[index] = updatedItem
            _documentItems.value = currentItems
        }
    }

    fun removeDocumentItem(item: DocumentItem) {
        val currentItems = _documentItems.value.toMutableList()
        currentItems.remove(item)
        _documentItems.value = currentItems
    }

    fun clearDocumentItems() {
        _documentItems.value = emptyList()
    }

    // Сохранение документа
    fun saveDocument(documentType: DocumentType, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null

            try {
                val warehouse = _selectedWarehouse.value
                if (warehouse == null) {
                    _errorMessage.value = "Выберите склад"
                    return@launch
                }

                if (_documentItems.value.isEmpty()) {
                    _errorMessage.value = "Добавьте хотя бы одну строку документа"
                    return@launch
                }

                val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                
                val documentRequest = com.sh.an.tsd.data.api.DocumentCreateRequest(
                    document_type = documentType.value,
                    document_number = _documentNumber.value.ifBlank { 
                        val dateFormatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                        val timeFormatter = SimpleDateFormat("HHmmss", Locale.getDefault())
                        val currentDate = Date()
                        val dateStr = dateFormatter.format(currentDate)
                        val timeStr = timeFormatter.format(currentDate)
                        val prefix = if (devicePrefix.isNotEmpty()) devicePrefix else "ВО"
                        "$prefix-$dateStr-$timeStr"
                    },
                    warehouse_id = warehouse.id,
                    date = dateFormatter.format(_documentDate.value),
                    status = "draft",
                    description = _description.value.takeIf { it.isNotBlank() }
                )

                val result = documentsRepository.createDocument(documentRequest)
                if (result.isSuccess) {
                    val document = result.getOrNull()!!
                    
                    // Добавляем строки документа
                    for (item in _documentItems.value) {
                        val itemRequest = com.sh.an.tsd.data.api.DocumentItemCreateRequest(
                            nomenclature_id = item.nomenclatureId,
                            quantity = item.quantity,
                            unit_id = item.unitId,
                            price = item.price,
                            total = item.total,
                            description = item.description
                        )
                        
                        documentsRepository.createDocumentItem(document.id, itemRequest)
                    }
                    
                    onSuccess()
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Ошибка сохранения документа"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка сохранения документа"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetForm() {
        _documentNumber.value = ""
        _selectedWarehouse.value = null
        _documentDate.value = Date()
        _description.value = ""
        _documentItems.value = emptyList()
        _errorMessage.value = null
    }
}