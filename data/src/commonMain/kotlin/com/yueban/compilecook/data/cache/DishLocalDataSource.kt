package com.yueban.compilecook.data.cache

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.yueban.compilecook.data.db.entity.DishLocalEntity
import com.yueban.compilecook.data.db.entity.DishQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface DishLocalDataSource {
  fun getAllDishes(): Flow<List<DishLocalEntity>>
  fun getDishByName(name: String): Flow<DishLocalEntity?>
  suspend fun insertDish(dish: DishLocalEntity)
  suspend fun upsertDishes(dishes: List<DishLocalEntity>)
  suspend fun updateDish(dish: DishLocalEntity)
  suspend fun deleteDishByName(name: String)
  suspend fun deleteAllDishes()
}

class DishLocalDataSourceImpl(
  private val dishQueries: DishQueries,
  private val dispatcher: CoroutineDispatcher,
) : DishLocalDataSource {
  override fun getAllDishes(): Flow<List<DishLocalEntity>> =
    dishQueries.getAll().asFlow().mapToList(dispatcher)

  override fun getDishByName(name: String): Flow<DishLocalEntity?> =
    dishQueries.getByName(name).asFlow().mapToOneOrNull(dispatcher)

  override suspend fun insertDish(dish: DishLocalEntity): Unit = withContext(dispatcher) {
    dishQueries.upsertDish(
      name = dish.name,
      description = dish.description,
      category = dish.category,
      difficulty = dish.difficulty,
      image = dish.image,
      ingredient = dish.ingredient,
      calculation = dish.calculation,
      operation = dish.operation,
      addition = dish.addition
    )
  }

  override suspend fun upsertDishes(dishes: List<DishLocalEntity>) = withContext(dispatcher) {
    dishQueries.transaction {
      dishes.forEach { dish ->
        dishQueries.upsertDish(
          name = dish.name,
          description = dish.description,
          category = dish.category,
          difficulty = dish.difficulty,
          image = dish.image,
          ingredient = dish.ingredient,
          calculation = dish.calculation,
          operation = dish.operation,
          addition = dish.addition
        )
      }
    }
  }

  override suspend fun updateDish(dish: DishLocalEntity): Unit = withContext(dispatcher) {
    dishQueries.upsertDish(
      name = dish.name,
      description = dish.description,
      category = dish.category,
      difficulty = dish.difficulty,
      image = dish.image,
      ingredient = dish.ingredient,
      calculation = dish.calculation,
      operation = dish.operation,
      addition = dish.addition
    )
  }

  override suspend fun deleteDishByName(name: String): Unit = withContext(dispatcher) {
    dishQueries.deleteByName(name)
  }

  override suspend fun deleteAllDishes(): Unit = withContext(dispatcher) {
    dishQueries.deleteAll()
  }
}
