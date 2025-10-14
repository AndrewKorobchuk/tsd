package com.sh.an.tsd.data.database

import androidx.room.*
import com.sh.an.tsd.data.model.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents ORDER BY created_at DESC")
    fun getAllDocuments(): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE document_type = :documentType ORDER BY created_at DESC")
    fun getDocumentsByType(documentType: String): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE warehouse_id = :warehouseId ORDER BY created_at DESC")
    fun getDocumentsByWarehouse(warehouseId: Int): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE status = :status ORDER BY created_at DESC")
    fun getDocumentsByStatus(status: String): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE document_type = :documentType AND warehouse_id = :warehouseId ORDER BY created_at DESC")
    fun getDocumentsByTypeAndWarehouse(documentType: String, warehouseId: Int): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Int): Document?

    @Query("SELECT * FROM documents WHERE document_number = :documentNumber")
    suspend fun getDocumentByNumber(documentNumber: String): Document?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<Document>)

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("DELETE FROM documents")
    suspend fun deleteAllDocuments()

    @Query("SELECT COUNT(*) FROM documents")
    suspend fun getDocumentsCount(): Int

    @Query("SELECT COUNT(*) FROM documents WHERE document_type = :documentType")
    suspend fun getDocumentsCountByType(documentType: String): Int
}
