package com.sh.an.tsd.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sh.an.tsd.data.model.Document
import com.sh.an.tsd.data.model.DocumentType
import com.sh.an.tsd.data.model.DocumentStatus
import com.sh.an.tsd.data.repository.DocumentsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DocumentsViewModel(
    private val documentsRepository: DocumentsRepository
) : ViewModel() {

    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedDocumentType = MutableStateFlow<DocumentType?>(null)
    val selectedDocumentType: StateFlow<DocumentType?> = _selectedDocumentType.asStateFlow()

    private val _selectedStatus = MutableStateFlow<DocumentStatus?>(null)
    val selectedStatus: StateFlow<DocumentStatus?> = _selectedStatus.asStateFlow()

    init {
        loadDocuments()
        // Проверяем количество документов в БД
        viewModelScope.launch {
            try {
                val count = documentsRepository.getLocalDocumentsCount()
                println("DocumentsViewModel: Local documents count: $count")
            } catch (e: Exception) {
                println("DocumentsViewModel: Error getting documents count: ${e.message}")
            }
        }
    }

    fun loadDocuments() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // Сначала проверяем, есть ли документы в БД
                val count = documentsRepository.getLocalDocumentsCount()
                println("DocumentsViewModel: Local documents count: $count")
                
                if (count == 0) {
                    // Если документов нет, сразу показываем пустой список
                    _documents.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }
                
                documentsRepository.allDocuments.collect { documentsList ->
                    println("DocumentsViewModel: Received ${documentsList.size} documents")
                    _documents.value = documentsList
                    _isLoading.value = false // Устанавливаем false после получения данных
                }
            } catch (e: Exception) {
                println("DocumentsViewModel: Error loading documents: ${e.message}")
                _errorMessage.value = e.message ?: "Ошибка загрузки документов"
                _isLoading.value = false
            }
        }
    }

    fun filterByDocumentType(documentType: DocumentType?) {
        _selectedDocumentType.value = documentType
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                if (documentType != null) {
                    documentsRepository.getDocumentsByType(documentType).collect { documentsList ->
                        _documents.value = documentsList
                        _isLoading.value = false
                    }
                } else {
                    documentsRepository.allDocuments.collect { documentsList ->
                        _documents.value = documentsList
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка фильтрации документов"
                _isLoading.value = false
            }
        }
    }

    fun filterByStatus(status: DocumentStatus?) {
        _selectedStatus.value = status
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                if (status != null) {
                    documentsRepository.getDocumentsByStatus(status).collect { documentsList ->
                        _documents.value = documentsList
                        _isLoading.value = false
                    }
                } else {
                    documentsRepository.allDocuments.collect { documentsList ->
                        _documents.value = documentsList
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка фильтрации документов"
                _isLoading.value = false
            }
        }
    }

    fun syncDocumentsFromServer(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                val result = documentsRepository.syncDocumentsFromServer(token)
                result.fold(
                    onSuccess = {
                        // Документы уже обновлены в репозитории
                    },
                    onFailure = { error ->
                        _errorMessage.value = error.message ?: "Ошибка синхронизации документов"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка синхронизации документов"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearFilters() {
        _selectedDocumentType.value = null
        _selectedStatus.value = null
        loadDocuments()
    }
}
