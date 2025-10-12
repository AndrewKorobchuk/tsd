package com.sh.an.tsd.data.repository

import com.sh.an.tsd.data.api.UnitsApiService
import com.sh.an.tsd.data.database.UnitOfMeasureDao
import com.sh.an.tsd.data.model.UnitOfMeasure
import kotlinx.coroutines.flow.Flow

class UnitsRepository(
    private val unitsApiService: UnitsApiService,
    private val unitOfMeasureDao: UnitOfMeasureDao
) {

    // Получение всех активных единиц измерения из локальной БД
    fun getAllActiveUnits(): Flow<List<UnitOfMeasure>> {
        return unitOfMeasureDao.getAllActiveUnits()
    }

    // Поиск единиц измерения в локальной БД
    fun searchActiveUnits(search: String): Flow<List<UnitOfMeasure>> {
        return unitOfMeasureDao.searchActiveUnits(search)
    }

    // Получение единицы измерения по ID из локальной БД
    suspend fun getUnitById(id: Int): UnitOfMeasure? {
        return unitOfMeasureDao.getUnitById(id)
    }

    // Получение единицы измерения по коду из локальной БД
    suspend fun getUnitByCode(code: String): UnitOfMeasure? {
        return unitOfMeasureDao.getUnitByCode(code)
    }

    // Загрузка единиц измерения с сервера и сохранение в локальную БД
    suspend fun loadUnitsFromServer(authorization: String): Result<Int> {
        return try {
            val response = unitsApiService.getUnitsOfMeasure(
                authorization = authorization,
                skip = 0,
                limit = 1000,
                activeOnly = true
            )

            if (response.isSuccessful) {
                val units = response.body()
                if (units != null) {
                    // Сохраняем единицы измерения в локальную БД
                    unitOfMeasureDao.insertUnits(units)
                    Result.success(units.size)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Server error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Синхронизация единиц измерения с сервера
    suspend fun syncUnitsFromServer(authorization: String): Result<Int> {
        return try {
            val response = unitsApiService.getUnitsOfMeasure(
                authorization = authorization,
                skip = 0,
                limit = 1000,
                activeOnly = true
            )

            if (response.isSuccessful) {
                val units = response.body()
                if (units != null) {
                    // Полностью заменяем данные в локальной БД
                    unitOfMeasureDao.deleteAllUnits()
                    unitOfMeasureDao.insertUnits(units)
                    Result.success(units.size)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Server error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Получение количества единиц измерения в локальной БД
    suspend fun getLocalUnitsCount(): Int {
        return unitOfMeasureDao.getUnitsCount()
    }

    // Получение времени последнего обновления
    suspend fun getLastUpdateTime(): String? {
        return unitOfMeasureDao.getLastUpdateTime()
    }
}
