package com.sh.an.tsd.data.repository

import com.sh.an.tsd.data.api.DocumentsApiService
import com.sh.an.tsd.data.api.DocumentCreateRequest
import com.sh.an.tsd.data.api.DocumentItemCreateRequest
import com.sh.an.tsd.data.database.DocumentDao
import com.sh.an.tsd.data.database.DocumentItemDao
import com.sh.an.tsd.data.model.Document
import com.sh.an.tsd.data.model.DocumentItem
import com.sh.an.tsd.data.model.DocumentType
import com.sh.an.tsd.data.model.DocumentStatus
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class DocumentsRepository(
    private val documentsApiService: DocumentsApiService,
    private val documentDao: DocumentDao,
    private val documentItemDao: DocumentItemDao
) {

    val allDocuments: Flow<List<Document>> = documentDao.getAllDocuments()

    fun getDocumentsByType(documentType: DocumentType): Flow<List<Document>> {
        return documentDao.getDocumentsByType(documentType.value)
    }

    fun getDocumentsByWarehouse(warehouseId: Int): Flow<List<Document>> {
        return documentDao.getDocumentsByWarehouse(warehouseId)
    }

    fun getDocumentsByStatus(status: DocumentStatus): Flow<List<Document>> {
        return documentDao.getDocumentsByStatus(status.value)
    }

    fun getDocumentsByTypeAndWarehouse(documentType: DocumentType, warehouseId: Int): Flow<List<Document>> {
        return documentDao.getDocumentsByTypeAndWarehouse(documentType.value, warehouseId)
    }

    fun getDocumentItemsByDocumentId(documentId: Int): Flow<List<DocumentItem>> {
        return documentItemDao.getDocumentItemsByDocumentId(documentId)
    }

    suspend fun syncDocumentsFromServer(token: String): Result<Unit> {
        return try {
            val response = documentsApiService.getDocuments(authorization = token)
            if (response.isSuccessful) {
                val documents = response.body()
                if (documents != null) {
                    documentDao.deleteAllDocuments() // Очищаем старые данные
                    documentDao.insertDocuments(documents) // Вставляем новые
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Empty response body from server"))
                }
            } else {
                Result.failure(IOException("Failed to fetch documents: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDocument(token: String, document: Document): Result<Document> {
        return try {
            val createRequest = com.sh.an.tsd.data.api.DocumentCreateRequest(
                document_type = document.documentType,
                document_number = document.documentNumber,
                warehouse_id = document.warehouseId,
                date = document.date,
                status = document.status,
                description = document.description
            )
            
            val response = documentsApiService.createDocument(authorization = token, document = createRequest)
            if (response.isSuccessful) {
                val createdDocument = response.body()
                if (createdDocument != null) {
                    documentDao.insertDocument(createdDocument)
                    Result.success(createdDocument)
                } else {
                    Result.failure(IOException("Empty response body from server"))
                }
            } else {
                Result.failure(IOException("Failed to create document: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDocument(token: String, documentId: Int, document: Document): Result<Document> {
        return try {
            val updateRequest = com.sh.an.tsd.data.api.DocumentUpdateRequest(
                document_number = document.documentNumber,
                warehouse_id = document.warehouseId,
                date = document.date,
                status = document.status,
                description = document.description
            )
            
            val response = documentsApiService.updateDocument(authorization = token, documentId = documentId, document = updateRequest)
            if (response.isSuccessful) {
                val updatedDocument = response.body()
                if (updatedDocument != null) {
                    documentDao.updateDocument(updatedDocument)
                    Result.success(updatedDocument)
                } else {
                    Result.failure(IOException("Empty response body from server"))
                }
            } else {
                Result.failure(IOException("Failed to update document: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun postDocument(token: String, documentId: Int): Result<Unit> {
        return try {
            val response = documentsApiService.postDocument(authorization = token, documentId = documentId)
            if (response.isSuccessful) {
                // Обновляем статус документа в локальной БД
                val document = documentDao.getDocumentById(documentId)
                if (document != null) {
                    val updatedDocument = document.copy(status = DocumentStatus.POSTED.value)
                    documentDao.updateDocument(updatedDocument)
                }
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to post document: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDocumentItem(token: String, documentId: Int, item: DocumentItem): Result<DocumentItem> {
        return try {
            val createRequest = com.sh.an.tsd.data.api.DocumentItemCreateRequest(
                nomenclature_id = item.nomenclatureId,
                quantity = item.quantity,
                unit_id = item.unitId,
                price = item.price,
                total = item.total,
                description = item.description
            )
            
            val response = documentsApiService.createDocumentItem(authorization = token, documentId = documentId, item = createRequest)
            if (response.isSuccessful) {
                val createdItem = response.body()
                if (createdItem != null) {
                    documentItemDao.insertDocumentItem(createdItem)
                    Result.success(createdItem)
                } else {
                    Result.failure(IOException("Empty response body from server"))
                }
            } else {
                Result.failure(IOException("Failed to create document item: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDocumentItem(token: String, itemId: Int, item: DocumentItem): Result<DocumentItem> {
        return try {
            val updateRequest = com.sh.an.tsd.data.api.DocumentItemUpdateRequest(
                nomenclature_id = item.nomenclatureId,
                quantity = item.quantity,
                unit_id = item.unitId,
                price = item.price,
                total = item.total,
                description = item.description
            )
            
            val response = documentsApiService.updateDocumentItem(authorization = token, itemId = itemId, item = updateRequest)
            if (response.isSuccessful) {
                val updatedItem = response.body()
                if (updatedItem != null) {
                    documentItemDao.updateDocumentItem(updatedItem)
                    Result.success(updatedItem)
                } else {
                    Result.failure(IOException("Empty response body from server"))
                }
            } else {
                Result.failure(IOException("Failed to update document item: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDocumentItem(token: String, itemId: Int): Result<Unit> {
        return try {
            val response = documentsApiService.deleteDocumentItem(authorization = token, itemId = itemId)
            if (response.isSuccessful) {
                val item = documentItemDao.getDocumentItemById(itemId)
                if (item != null) {
                    documentItemDao.deleteDocumentItem(item)
                }
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to delete document item: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLocalDocumentsCount(): Int {
        return documentDao.getDocumentsCount()
    }

    suspend fun getLocalDocumentsCountByType(documentType: DocumentType): Int {
        return documentDao.getDocumentsCountByType(documentType.value)
    }

    suspend fun createDocument(documentRequest: DocumentCreateRequest): Result<Document> {
        return try {
            val response = documentsApiService.createDocument(
                authorization = "Bearer dummy_token", // TODO: Получить реальный токен
                document = documentRequest
            )
            if (response.isSuccessful) {
                val document = response.body()
                if (document != null) {
                    documentDao.insertDocument(document)
                    Result.success(document)
                } else {
                    Result.failure(IOException("Empty response body"))
                }
            } else {
                Result.failure(IOException("Failed to create document: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDocumentItem(documentId: Int, itemRequest: DocumentItemCreateRequest): Result<DocumentItem> {
        return try {
            val response = documentsApiService.createDocumentItem(
                authorization = "Bearer dummy_token", // TODO: Получить реальный токен
                documentId = documentId,
                item = itemRequest
            )
            if (response.isSuccessful) {
                val item = response.body()
                if (item != null) {
                    documentItemDao.insertDocumentItem(item)
                    Result.success(item)
                } else {
                    Result.failure(IOException("Empty response body"))
                }
            } else {
                Result.failure(IOException("Failed to create document item: ${response.code()} - ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

