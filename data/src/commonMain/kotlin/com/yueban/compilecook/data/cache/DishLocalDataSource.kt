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
  fun getDishById(id: Long): Flow<DishLocalEntity?>
  suspend fun insertDish(dish: DishLocalEntity)
  suspend fun insertDishes(dishes: List<DishLocalEntity>)
  suspend fun updateDish(dish: DishLocalEntity)
  suspend fun deleteDishById(id: Long)
  suspend fun deleteAllDishes()
}

class DishLocalDataSourceImpl(
  private val dishQueries: DishQueries,
  private val dispatcher: CoroutineDispatcher,
) : DishLocalDataSource {
  override fun getAllDishes(): Flow<List<DishLocalEntity>> =
    dishQueries.getAll().asFlow().mapToList(dispatcher)

  override fun getDishById(id: Long): Flow<DishLocalEntity?> =
    dishQueries.getById(id).asFlow().mapToOneOrNull(dispatcher)

  override suspend fun insertDish(dish: DishLocalEntity): Unit = withContext(dispatcher) {
    dishQueries.insertDish(
      name = dish.name,
      category = dish.category,
      difficulty = dish.difficulty,
      image = dish.image,
      ingredient = dish.ingredient,
      calculation = dish.calculation,
      operation = dish.operation,
      addition = dish.addition
    )
  }

  override suspend fun insertDishes(dishes: List<DishLocalEntity>) = withContext(dispatcher) {
    dishQueries.transaction {
      dishes.forEach { dish ->
        dishQueries.insertDish(
          name = dish.name,
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
    dishQueries.updateDish(
      id = dish.id, // The ID is crucial for the WHERE clause
      name = dish.name,
      category = dish.category,
      difficulty = dish.difficulty,
      image = dish.image,
      ingredient = dish.ingredient,
      calculation = dish.calculation,
      operation = dish.operation,
      addition = dish.addition
    )
  }

  override suspend fun deleteDishById(id: Long): Unit = withContext(dispatcher) {
    dishQueries.deleteById(id)
  }

  override suspend fun deleteAllDishes(): Unit = withContext(dispatcher) {
    dishQueries.deleteAll()
  }
}
