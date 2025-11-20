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
  fun getDishById(id: Long): Flow<Dish?>
  suspend fun addDish(dish: Dish)
  suspend fun addDishes(dishes: List<Dish>)
  suspend fun updateDish(dish: Dish)
  suspend fun deleteDishById(id: Long)
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
    dishLocalDataSource.insertDishes(dishes)
    Logger.d("updated dishes: ${dishes.size}")
  }

  override fun getAllDishes(): Flow<List<Dish>> =
    dishLocalDataSource.getAllDishes().map { entities -> entities.map { it.toDish() } }

  override fun getDishById(id: Long): Flow<Dish?> =
    dishLocalDataSource.getDishById(id).map { it?.toDish() }

  override suspend fun addDish(dish: Dish) = dishLocalDataSource.insertDish(dish.toLocalEntity())

  override suspend fun addDishes(dishes: List<Dish>) =
    dishLocalDataSource.insertDishes(dishes.map { it.toLocalEntity() })

  override suspend fun updateDish(dish: Dish) = dishLocalDataSource.updateDish(dish.toLocalEntity())

  override suspend fun deleteDishById(id: Long) = dishLocalDataSource.deleteDishById(id)

  override suspend fun clearAllDishes() = dishLocalDataSource.deleteAllDishes()
}
