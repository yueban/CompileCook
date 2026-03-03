package com.yueban.compilecook.data.cache

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.yueban.compilecook.data.db.entity.DishLocalEntity
import com.yueban.compilecook.data.db.entity.DishQueries
import com.yueban.compilecook.data.db.entity.TipLocalEntity
import com.yueban.compilecook.data.db.entity.TipQueries
import com.yueban.compilecook.logger.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface DishLocalDataSource {
  fun getAllDishes(): Flow<List<DishLocalEntity>>
  fun getDishCategories(): Flow<List<String>>
  fun getDishByName(name: String): Flow<DishLocalEntity?>
  fun getDishesByCategory(category: String): Flow<List<DishLocalEntity>>
  suspend fun getRandomDishName(): String?
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
  private val transactionLock = DbTransactionLock()

  /**
   * For single inserts/deletes. Pushes work to background, NO Mutex required.
   */
  private suspend inline fun <T> write(crossinline block: suspend () -> T): T =
    withContext(defaultDispatcher) { block() }

  /**
   * For bulk operations. Pushes to background AND applies platform-specific transaction lock.
   */
  private suspend inline fun <T> transactionWrite(crossinline block: suspend () -> T): T =
    withContext(defaultDispatcher) { transactionLock.withLock { block() } }

  override fun getAllDishes(): Flow<List<DishLocalEntity>> =
    dishQueries.getAll().asFlow().mapToList(defaultDispatcher)

  override fun getDishCategories(): Flow<List<String>> =
    dishQueries.getDishCategories().asFlow().mapToList(defaultDispatcher)

  override fun getDishByName(name: String): Flow<DishLocalEntity?> =
    dishQueries.getByName(name).asFlow().mapToOneOrNull(defaultDispatcher)

  override fun getDishesByCategory(category: String): Flow<List<DishLocalEntity>> =
    dishQueries.getByCategory(category).asFlow().mapToList(defaultDispatcher)

  override suspend fun getRandomDishName(): String? = withContext(defaultDispatcher) {
    dishQueries.getRandomDishName().awaitAsOneOrNull()
  }

  override fun getAllTips(): Flow<List<TipLocalEntity>> =
    tipQueries.getAll().asFlow().mapToList(defaultDispatcher)

  override fun getTipByName(name: String): Flow<TipLocalEntity?> =
    tipQueries.getByName(name).asFlow().mapToOneOrNull(defaultDispatcher)

  override suspend fun upsertDish(dish: DishLocalEntity) = write {
    dishQueries.upsertDish(dish)
    Logger.d("upsert dish: $dish")
  }

  override suspend fun upsertDishes(dishes: List<DishLocalEntity>) = transactionWrite {
    dishQueries.transaction {
      dishes.forEach { dishQueries.upsertDish(it) }
    }
    Logger.d("upsert dishes: ${dishes.size}")
  }

  override suspend fun deleteDishByName(name: String) = write {
    dishQueries.deleteByName(name)
    Logger.d("delete dish by name: $name")
  }

  override suspend fun deleteAllDishes() = write {
    dishQueries.deleteAll()
    Logger.d("delete all dishes")
  }

  override suspend fun upsertTip(tip: TipLocalEntity) = write {
    tipQueries.upsertTip(tip)
    Logger.d("upsert tip: $tip")
  }

  override suspend fun upsertTips(tips: List<TipLocalEntity>) = transactionWrite {
    tipQueries.transaction {
      tips.forEach { tipQueries.upsertTip(it) }
    }
    Logger.d("upsert tips: ${tips.size}")
  }

  override suspend fun deleteTipByName(name: String) = write {
    tipQueries.deleteByName(name)
    Logger.d("delete tip by name: $name")
  }

  override suspend fun deleteAllTips() = write {
    tipQueries.deleteAll()
    Logger.d("delete all tips")
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

private suspend fun TipQueries.upsertTip(tip: TipLocalEntity) =
  upsertTip(
    name = tip.name,
    pinyin = tip.pinyin,
    type = tip.type,
    content = tip.content,
  )
