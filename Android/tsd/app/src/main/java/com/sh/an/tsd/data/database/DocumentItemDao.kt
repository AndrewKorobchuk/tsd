package com.sh.an.tsd.data.database

import androidx.room.*
import com.sh.an.tsd.data.model.DocumentItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentItemDao {

    @Query("SELECT * FROM document_items WHERE document_id = :documentId ORDER BY id")
    fun getDocumentItemsByDocumentId(documentId: Int): Flow<List<DocumentItem>>

    @Query("SELECT * FROM document_items WHERE id = :id")
    suspend fun getDocumentItemById(id: Int): DocumentItem?

    @Query("SELECT * FROM document_items WHERE document_id = :documentId AND nomenclature_id = :nomenclatureId")
    suspend fun getDocumentItemByDocumentAndNomenclature(documentId: Int, nomenclatureId: Int): DocumentItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentItem(documentItem: DocumentItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentItems(documentItems: List<DocumentItem>)

    @Update
    suspend fun updateDocumentItem(documentItem: DocumentItem)

    @Delete
    suspend fun deleteDocumentItem(documentItem: DocumentItem)

    @Query("DELETE FROM document_items WHERE document_id = :documentId")
    suspend fun deleteDocumentItemsByDocumentId(documentId: Int)

    @Query("DELETE FROM document_items")
    suspend fun deleteAllDocumentItems()

    @Query("SELECT COUNT(*) FROM document_items WHERE document_id = :documentId")
    suspend fun getDocumentItemsCount(documentId: Int): Int
}


