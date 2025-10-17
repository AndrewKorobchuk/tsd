package com.sh.an.tsd.data.api

import com.sh.an.tsd.data.model.Document
import com.sh.an.tsd.data.model.DocumentItem
import retrofit2.Response
import retrofit2.http.*

interface DocumentsApiService {
    
    @GET("api/v1/documents/")
    suspend fun getDocuments(
        @Header("Authorization") authorization: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100,
        @Query("document_type") documentType: String? = null,
        @Query("warehouse_id") warehouseId: Int? = null,
        @Query("status") status: String? = null
    ): Response<List<Document>>
    
    @GET("api/v1/documents/{document_id}")
    suspend fun getDocument(
        @Header("Authorization") authorization: String,
        @Path("document_id") documentId: Int
    ): Response<Document>
    
    @POST("api/v1/documents/")
    suspend fun createDocument(
        @Header("Authorization") authorization: String,
        @Body document: DocumentCreateRequest
    ): Response<Document>
    
    @PUT("api/v1/documents/{document_id}")
    suspend fun updateDocument(
        @Header("Authorization") authorization: String,
        @Path("document_id") documentId: Int,
        @Body document: DocumentUpdateRequest
    ): Response<Document>
    
    @DELETE("api/v1/documents/{document_id}")
    suspend fun deleteDocument(
        @Header("Authorization") authorization: String,
        @Path("document_id") documentId: Int
    ): Response<Unit>
    
    @PATCH("api/v1/documents/{document_id}/post")
    suspend fun postDocument(
        @Header("Authorization") authorization: String,
        @Path("document_id") documentId: Int
    ): Response<Map<String, String>>
    
    @PATCH("api/v1/documents/{document_id}/cancel")
    suspend fun cancelDocument(
        @Header("Authorization") authorization: String,
        @Path("document_id") documentId: Int
    ): Response<Map<String, String>>
    
    // Эндпоинты для работы со строками документов
    @POST("api/v1/documents/{document_id}/items")
    suspend fun createDocumentItem(
        @Header("Authorization") authorization: String,
        @Path("document_id") documentId: Int,
        @Body item: DocumentItemCreateRequest
    ): Response<DocumentItem>
    
    @PUT("api/v1/documents/items/{item_id}")
    suspend fun updateDocumentItem(
        @Header("Authorization") authorization: String,
        @Path("item_id") itemId: Int,
        @Body item: DocumentItemUpdateRequest
    ): Response<DocumentItem>
    
    @DELETE("api/v1/documents/items/{item_id}")
    suspend fun deleteDocumentItem(
        @Header("Authorization") authorization: String,
        @Path("item_id") itemId: Int
    ): Response<Unit>
}

// DTO для создания документа
data class DocumentCreateRequest(
    val document_type: String,
    val document_number: String,
    val warehouse_id: Int,
    val date: String,
    val status: String = "draft",
    val description: String? = null
)

// DTO для обновления документа
data class DocumentUpdateRequest(
    val document_number: String? = null,
    val warehouse_id: Int? = null,
    val date: String? = null,
    val status: String? = null,
    val description: String? = null
)

// DTO для создания строки документа
data class DocumentItemCreateRequest(
    val nomenclature_id: Int,
    val quantity: Double,
    val unit_id: Int,
    val price: Double? = null,
    val total: Double? = null,
    val description: String? = null
)

// DTO для обновления строки документа
data class DocumentItemUpdateRequest(
    val nomenclature_id: Int? = null,
    val quantity: Double? = null,
    val unit_id: Int? = null,
    val price: Double? = null,
    val total: Double? = null,
    val description: String? = null
)


