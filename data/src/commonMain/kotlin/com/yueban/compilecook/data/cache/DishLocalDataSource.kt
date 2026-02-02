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
  fun getDishCategories(): Flow<List<String>>
  fun getDishByName(name: String): Flow<DishLocalEntity?>
  fun getDishesByCategory(category: String): Flow<List<DishLocalEntity>>
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
  private val defaultDispatcher: CoroutineDispatcher,
) : DishLocalDataSource {
  override fun getAllDishes(): Flow<List<DishLocalEntity>> =
    dishQueries.getAll().asFlow().mapToList(defaultDispatcher)

  override fun getDishCategories(): Flow<List<String>> =
    dishQueries.getDishCategories().asFlow().mapToList(defaultDispatcher)

  override fun getDishByName(name: String): Flow<DishLocalEntity?> =
    dishQueries.getByName(name).asFlow().mapToOneOrNull(defaultDispatcher)

  override fun getDishesByCategory(category: String): Flow<List<DishLocalEntity>> =
    dishQueries.getByCategory(category).asFlow().mapToList(defaultDispatcher)

  override fun getAllTips(): Flow<List<TipLocalEntity>> =
    tipQueries.getAll().asFlow().mapToList(defaultDispatcher)

  override fun getTipByName(name: String): Flow<TipLocalEntity?> =
    tipQueries.getByName(name).asFlow().mapToOneOrNull(defaultDispatcher)

  override suspend fun upsertDish(dish: DishLocalEntity): Unit = withContext(defaultDispatcher) {
    dishQueries.upsertDish(dish)
  }

  override suspend fun upsertDishes(dishes: List<DishLocalEntity>) = withContext(defaultDispatcher) {
    dishQueries.transaction {
      dishes.forEach { dishQueries.upsertDish(it) }
    }
  }

  override suspend fun deleteDishByName(name: String): Unit = withContext(defaultDispatcher) {
    dishQueries.deleteByName(name)
  }

  override suspend fun deleteAllDishes(): Unit = withContext(defaultDispatcher) {
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

private suspend fun DishQueries.upsertDish(dish: DishLocalEntity) =
  upsertDish(
    name = dish.name,
    pinyin = dish.pinyin,
    description = dish.description,
    category = dish.category,
    difficulty = dish.difficulty,
    image = dish.image,
    content = dish.content,
  )
