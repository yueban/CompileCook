package com.yueban.compilecook.data.cache

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.yueban.compilecook.data.db.entity.DishLocalEntity
import com.yueban.compilecook.data.db.entity.DishQueries
import com.yueban.compilecook.data.db.entity.TipLocalEntity
import com.yueban.compilecook.data.db.entity.TipQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface DishLocalDataSource {
  fun getAllDishes(): Flow<List<DishLocalEntity>>
  fun getDishByName(name: String): Flow<DishLocalEntity?>
  fun getAllTips(): Flow<List<TipLocalEntity>>
  fun getTipByName(name: String): Flow<TipLocalEntity?>
  suspend fun upsertDish(dish: DishLocalEntity)
  suspend fun upsertDishes(dishes: List<DishLocalEntity>)
  suspend fun deleteDishByName(name: String)
  suspend fun deleteAllDishes()
  suspend fun upsertTip(tip: TipLocalEntity)
  suspend fun upsertTips(tips: List<TipLocalEntity>)
  suspend fun deleteTipByName(name: String)
  suspend fun deleteAllTips()
}

class DishLocalDataSourceImpl(
  private val dishQueries: DishQueries,
  private val tipQueries: TipQueries,
  private val dispatcher: CoroutineDispatcher,
) : DishLocalDataSource {
  override fun getAllDishes(): Flow<List<DishLocalEntity>> =
    dishQueries.getAll().asFlow().mapToList(dispatcher)

  override fun getDishByName(name: String): Flow<DishLocalEntity?> =
    dishQueries.getByName(name).asFlow().mapToOneOrNull(dispatcher)

  override fun getAllTips(): Flow<List<TipLocalEntity>> =
    tipQueries.getAll().asFlow().mapToList(dispatcher)

  override fun getTipByName(name: String): Flow<TipLocalEntity?> =
    tipQueries.getByName(name).asFlow().mapToOneOrNull(dispatcher)

  override suspend fun upsertDish(dish: DishLocalEntity): Unit = withContext(dispatcher) {
    dishQueries.upsertDish(
      name = dish.name,
      pinyin = dish.pinyin,
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
          pinyin = dish.pinyin,
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

  override suspend fun deleteDishByName(name: String): Unit = withContext(dispatcher) {
    dishQueries.deleteByName(name)
  }

  override suspend fun deleteAllDishes(): Unit = withContext(dispatcher) {
    dishQueries.deleteAll()
  }

  override suspend fun upsertTip(tip: TipLocalEntity) {
    tipQueries.upsertTip(
      name = tip.name,
      pinyin = tip.pinyin,
      type = tip.type,
      content = tip.content,
    )
  }

  override suspend fun upsertTips(tips: List<TipLocalEntity>) {
    tipQueries.transaction {
      tips.forEach { tip ->
        tipQueries.upsertTip(
          name = tip.name,
          pinyin = tip.pinyin,
          type = tip.type,
          content = tip.content,
        )
      }
    }
  }

  override suspend fun deleteTipByName(name: String) {
    tipQueries.deleteByName(name)
  }

  override suspend fun deleteAllTips() {
    tipQueries.deleteAll()
  }
}
