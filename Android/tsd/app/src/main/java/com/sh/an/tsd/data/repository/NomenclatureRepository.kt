package com.sh.an.tsd.data.repository

import com.sh.an.tsd.data.api.NomenclatureApiService
import com.sh.an.tsd.data.model.Nomenclature
import com.sh.an.tsd.data.model.toNomenclature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NomenclatureRepository(
    private val apiService: NomenclatureApiService
) {
    
    suspend fun getNomenclature(
        page: Int = 1,
        limit: Int = 100,
        search: String? = null,
        category: String? = null
    ): Flow<ApiResult<List<Nomenclature>>> = flow {
        try {
            emit(ApiResult.loading())
            
            val response = apiService.getNomenclature(page, limit, search, category)
            
            if (response.isSuccessful) {
                val nomenclatureResponse = response.body()
                if (nomenclatureResponse?.success == true) {
                    val nomenclature = nomenclatureResponse.data.map { it.toNomenclature() }
                    emit(ApiResult.success(nomenclature))
                } else {
                    emit(ApiResult.error("Ошибка получения данных: ${nomenclatureResponse?.message}"))
                }
            } else {
                emit(ApiResult.error("Ошибка сервера: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(ApiResult.error("Ошибка сети: ${e.message}"))
        }
    }
    
    suspend fun searchNomenclature(query: String): Flow<ApiResult<List<Nomenclature>>> = flow {
        try {
            emit(ApiResult.loading())
            
            val response = apiService.searchNomenclature(query)
            
            if (response.isSuccessful) {
                val nomenclatureResponse = response.body()
                if (nomenclatureResponse?.success == true) {
                    val nomenclature = nomenclatureResponse.data.map { it.toNomenclature() }
                    emit(ApiResult.success(nomenclature))
                } else {
                    emit(ApiResult.error("Ошибка поиска: ${nomenclatureResponse?.message}"))
                }
            } else {
                emit(ApiResult.error("Ошибка сервера: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(ApiResult.error("Ошибка сети: ${e.message}"))
        }
    }
}

// Класс для обработки результатов API
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
    
    companion object {
        fun <T> success(data: T): ApiResult<T> = Success(data)
        fun <T> error(message: String): ApiResult<T> = Error(message)
        fun <T> loading(): ApiResult<T> = Loading
    }
}

// Функции-расширения для удобства
fun <T> ApiResult<T>.isSuccess(): Boolean = this is ApiResult.Success
fun <T> ApiResult<T>.isError(): Boolean = this is ApiResult.Error
fun <T> ApiResult<T>.isLoading(): Boolean = this is ApiResult.Loading

fun <T> ApiResult<T>.getData(): T? = if (this is ApiResult.Success) this.data else null
fun <T> ApiResult<T>.getError(): String? = if (this is ApiResult.Error) this.message else null
