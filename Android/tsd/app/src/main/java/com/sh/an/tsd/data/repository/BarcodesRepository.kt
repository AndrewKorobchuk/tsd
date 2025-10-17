package com.sh.an.tsd.data.repository

import com.sh.an.tsd.data.api.BarcodesApiService
import com.sh.an.tsd.data.model.Barcode
import com.sh.an.tsd.data.model.BarcodeCreateRequest
import com.sh.an.tsd.data.model.BarcodeUpdateRequest
import com.sh.an.tsd.data.model.BarcodeSearchRequest

class BarcodesRepository(
    private val barcodesApiService: BarcodesApiService
) {
    
    /**
     * Получение списка штрих-кодов
     */
    suspend fun getBarcodes(
        skip: Int = 0,
        limit: Int = 100,
        activeOnly: Boolean = true,
        authorization: String
    ): Result<List<Barcode>> {
        return try {
            val response = barcodesApiService.getBarcodes(
                authorization = authorization,
                skip = skip,
                limit = limit,
                activeOnly = activeOnly
            )
            
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get barcodes: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Поиск штрих-кодов по запросу
     */
    suspend fun searchBarcodes(
        query: String,
        limit: Int = 10,
        authorization: String
    ): Result<List<Barcode>> {
        return try {
            val response = barcodesApiService.searchBarcodes(
                authorization = authorization,
                query = query,
                limit = limit
            )
            
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to search barcodes: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Получение штрих-кода по ID
     */
    suspend fun getBarcode(
        barcodeId: Int,
        authorization: String
    ): Result<Barcode> {
        return try {
            val response = barcodesApiService.getBarcode(
                authorization = authorization,
                barcodeId = barcodeId
            )
            
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get barcode: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Создание нового штрих-кода
     */
    suspend fun createBarcode(
        request: BarcodeCreateRequest,
        authorization: String
    ): Result<Barcode> {
        return try {
            val response = barcodesApiService.createBarcode(
                authorization = authorization,
                request = request
            )
            
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create barcode: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Обновление штрих-кода
     */
    suspend fun updateBarcode(
        barcodeId: Int,
        request: BarcodeUpdateRequest,
        authorization: String
    ): Result<Barcode> {
        return try {
            val response = barcodesApiService.updateBarcode(
                authorization = authorization,
                barcodeId = barcodeId,
                request = request
            )
            
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update barcode: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Удаление штрих-кода
     */
    suspend fun deleteBarcode(
        barcodeId: Int,
        authorization: String
    ): Result<Unit> {
        return try {
            val response = barcodesApiService.deleteBarcode(
                authorization = authorization,
                barcodeId = barcodeId
            )
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete barcode: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
