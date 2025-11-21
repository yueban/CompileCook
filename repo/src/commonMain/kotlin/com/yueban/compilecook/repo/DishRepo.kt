package com.yueban.compilecook.repo

import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.net.entity.toLocalEntity
import com.yueban.compilecook.data.net.service.DishRemoteDataSource
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.repo.entity.Dish
import com.yueban.compilecook.repo.entity.toDish
import com.yueban.compilecook.repo.entity.toLocalEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface DishRepo {
  fun getAllDishes(): Flow<List<Dish>>
  fun getDishByName(name: String): Flow<Dish?>
  suspend fun addOrUpdateDishes(dishes: List<Dish>)
  suspend fun deleteDishByName(name: String)
  suspend fun clearAllDishes()
}

internal class DishRepoImpl(
  coroutineScope: CoroutineScope,
  private val dishLocalDataSource: DishLocalDataSource,
  private val dishRemoteDataSource: DishRemoteDataSource,
) : DishRepo {
  init {
    coroutineScope.launch { updateDishes() }
  }

  private suspend fun updateDishes() {
    val dishes = dishRemoteDataSource.getDishes().map { it.toLocalEntity() }
    dishLocalDataSource.upsertDishes(dishes)
    Logger.d("updated dishes: ${dishes.size}")
  }

  override fun getAllDishes(): Flow<List<Dish>> =
    dishLocalDataSource.getAllDishes().map { entities -> entities.map { it.toDish() } }

  override fun getDishByName(name: String): Flow<Dish?> =
    dishLocalDataSource.getDishByName(name).map { it?.toDish() }

  override suspend fun addOrUpdateDishes(dishes: List<Dish>) =
    dishLocalDataSource.upsertDishes(dishes.map { it.toLocalEntity() })

  override suspend fun deleteDishByName(name: String) = dishLocalDataSource.deleteDishByName(name)

  override suspend fun clearAllDishes() = dishLocalDataSource.deleteAllDishes()
}
