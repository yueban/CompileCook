package com.yueban.compilecook.data.cache

import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.yueban.compilecook.data.cache.db.entity.DishDetailLocalEntity
import com.yueban.compilecook.data.cache.db.entity.DishSummaryLocalEntity
import com.yueban.compilecook.data.cache.db.entity.TipDetailLocalEntity
import com.yueban.compilecook.data.cache.db.entity.TipSummaryLocalEntity
import com.yueban.compilecook.data.db.entity.DishLocalEntity
import com.yueban.compilecook.data.db.entity.DishQueries
import com.yueban.compilecook.data.db.entity.TipLocalEntity
import com.yueban.compilecook.data.db.entity.TipQueries
import com.yueban.compilecook.logger.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface DishLocalDataSource {
  fun getDishSummaries(): Flow<List<DishSummaryLocalEntity>>
  fun getFavoriteDishSummaries(): Flow<List<DishSummaryLocalEntity>>
  fun getDishSummariesByCategory(category: String): Flow<List<DishSummaryLocalEntity>>
  fun getDishSummariesByDifficulty(difficulty: Long): Flow<List<DishSummaryLocalEntity>>
  fun getDishByName(name: String): Flow<DishDetailLocalEntity?>
  fun getDishCategories(): Flow<List<String>>
  suspend fun getRandomDishName(): String?
  fun getTipSummaries(): Flow<List<TipSummaryLocalEntity>>
  fun getTipDetail(name: String): Flow<TipDetailLocalEntity?>
  suspend fun upsertDish(dish: DishLocalEntity)
  suspend fun upsertDishes(dishes: List<DishLocalEntity>)
  suspend fun deleteDishByName(name: String)
  suspend fun deleteAllDishes()
  suspend fun upsertTip(tip: TipLocalEntity)
  suspend fun upsertTips(tips: List<TipLocalEntity>)
  suspend fun deleteTipByName(name: String)
  suspend fun deleteAllTips()
  suspend fun toggleTipFavorite(name: String)
  suspend fun toggleDishFavorite(name: String)
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

  override fun getDishSummaries(): Flow<List<DishSummaryLocalEntity>> =
    dishQueries.getDishSummaries(mapper = ::DishSummaryLocalEntity).asFlow().mapToList(defaultDispatcher)

  override fun getFavoriteDishSummaries(): Flow<List<DishSummaryLocalEntity>> =
    dishQueries.getFavoriteDishSummaries(mapper = ::DishSummaryLocalEntity).asFlow().mapToList(defaultDispatcher)

  override fun getDishSummariesByCategory(category: String): Flow<List<DishSummaryLocalEntity>> =
    dishQueries.getDishSummariesByCategory(category, mapper = ::DishSummaryLocalEntity)
      .asFlow().mapToList(defaultDispatcher)

  override fun getDishSummariesByDifficulty(difficulty: Long): Flow<List<DishSummaryLocalEntity>> =
    dishQueries.getDishSummariesByDifficulty(difficulty, mapper = ::DishSummaryLocalEntity)
      .asFlow().mapToList(defaultDispatcher)

  override fun getDishByName(name: String): Flow<DishDetailLocalEntity?> =
    dishQueries.getDishDetail(name, mapper = ::DishDetailLocalEntity).asFlow().mapToOneOrNull(defaultDispatcher)

  override fun getDishCategories(): Flow<List<String>> =
    dishQueries.getDishCategories().asFlow().mapToList(defaultDispatcher)

  override suspend fun getRandomDishName(): String? = withContext(defaultDispatcher) {
    dishQueries.getRandomDishName().awaitAsOneOrNull()
  }

  override fun getTipSummaries(): Flow<List<TipSummaryLocalEntity>> =
    tipQueries.getTipSummaries(mapper = ::TipSummaryLocalEntity).asFlow().mapToList(defaultDispatcher)

  override fun getTipDetail(name: String): Flow<TipDetailLocalEntity?> =
    tipQueries.getTipDetail(name, mapper = ::TipDetailLocalEntity).asFlow().mapToOneOrNull(defaultDispatcher)

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
    dishQueries.deleteDishByName(name)
    Logger.d("delete dish by name: $name")
  }

  override suspend fun deleteAllDishes() = write {
    dishQueries.deleteAllDishes()
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
    tipQueries.deleteTipByName(name)
    Logger.d("delete tip by name: $name")
  }

  override suspend fun deleteAllTips() = write {
    tipQueries.deleteAllTips()
    Logger.d("delete all tips")
  }

  override suspend fun toggleTipFavorite(name: String) = transactionWrite {
    val exists = tipQueries.isTipFavorite(name).awaitAsOne()
    if (exists) {
      tipQueries.deleteTipFavorite(name)
    } else {
      tipQueries.insertTipFavorite(name)
    }
    Logger.d("toggle tip favorite: $name, updated: ${!exists}")
  }

  override suspend fun toggleDishFavorite(name: String) = transactionWrite {
    val exists = dishQueries.isDishFavorite(name).awaitAsOne()
    if (exists) {
      dishQueries.deleteDishFavorite(name)
    } else {
      dishQueries.insertDishFavorite(name)
    }
    Logger.d("toggle dish favorite: $name, updated: ${!exists}")
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
