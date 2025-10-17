package com.sh.an.tsd.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sh.an.tsd.data.model.DocumentType
import com.sh.an.tsd.data.repository.DocumentsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DocumentsMainViewModel(
    private val documentsRepository: DocumentsRepository
) : ViewModel() {

    private val _stockInputCount = MutableStateFlow(0)
    val stockInputCount: StateFlow<Int> = _stockInputCount.asStateFlow()

    private val _receiptCount = MutableStateFlow(0)
    val receiptCount: StateFlow<Int> = _receiptCount.asStateFlow()

    private val _expenseCount = MutableStateFlow(0)
    val expenseCount: StateFlow<Int> = _expenseCount.asStateFlow()

    private val _transferCount = MutableStateFlow(0)
    val transferCount: StateFlow<Int> = _transferCount.asStateFlow()

    private val _inventoryCount = MutableStateFlow(0)
    val inventoryCount: StateFlow<Int> = _inventoryCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadDocumentCounts()
    }

    private fun loadDocumentCounts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Загружаем количество документов по типам
                _stockInputCount.value = documentsRepository.getLocalDocumentsCountByType(DocumentType.STOCK_INPUT)
                _receiptCount.value = documentsRepository.getLocalDocumentsCountByType(DocumentType.RECEIPT)
                _expenseCount.value = documentsRepository.getLocalDocumentsCountByType(DocumentType.EXPENSE)
                _transferCount.value = documentsRepository.getLocalDocumentsCountByType(DocumentType.TRANSFER)
                _inventoryCount.value = documentsRepository.getLocalDocumentsCountByType(DocumentType.INVENTORY)

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка загрузки количества документов"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshCounts() {
        loadDocumentCounts()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}


