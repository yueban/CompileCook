package com.yueban.compilecook.data.cache

import app.cash.sqldelight.async.coroutines.awaitAsList
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
  fun getDishSummaries(category: String?, difficulty: Int?, onlyFavorite: Boolean): Flow<List<DishSummaryLocalEntity>>
  fun getDishByName(name: String): Flow<DishDetailLocalEntity?>
  fun getDishCategories(): Flow<List<String>>
  suspend fun getRandomDishName(): String?
  fun getTipSummaries(): Flow<List<TipSummaryLocalEntity>>
  fun getTipDetail(name: String): Flow<TipDetailLocalEntity?>
  suspend fun updateDishes(dishes: List<DishLocalEntity>)
  suspend fun updateTips(tips: List<TipLocalEntity>)
  suspend fun toggleDishFavorite(name: String)
}

class DishLocalDataSourceImpl(
  private val dishQueries: DishQueries,
  private val tipQueries: TipQueries,
  defaultDispatcher: CoroutineDispatcher,
) : BaseLocalDataSource(defaultDispatcher), DishLocalDataSource {
  override fun getDishSummaries(
    category: String?,
    difficulty: Int?,
    onlyFavorite: Boolean,
  ): Flow<List<DishSummaryLocalEntity>> =
    dishQueries.getDishSummaries(
      category = category,
      difficulty = difficulty?.toLong(),
      onlyFavorite = if (onlyFavorite) 1L else 0L,
      mapper = ::DishSummaryLocalEntity,
    ).asFlow().mapToList(defaultDispatcher)

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

  override suspend fun updateDishes(dishes: List<DishLocalEntity>) = transactionWrite {
    dishQueries.transaction {
      val newNames = dishes.map { it.name }.toSet()
      val favoritesToRemove = dishQueries.getDishFavoriteNames().awaitAsList().filter { it !in newNames }
      if (favoritesToRemove.isNotEmpty()) dishQueries.deleteDishFavoritesByNames(favoritesToRemove)
      dishQueries.deleteAllDishes()
      dishes.forEach { dishQueries.upsertDish(it) }
    }
    Logger.d("update dishes: ${dishes.size}")
  }

  override suspend fun updateTips(tips: List<TipLocalEntity>) = transactionWrite {
    tipQueries.transaction {
      val newNames = tips.map { it.name }.toSet()
      val favoritesToRemove = tipQueries.getTipFavoriteNames().awaitAsList().filter { it !in newNames }
      if (favoritesToRemove.isNotEmpty()) tipQueries.deleteTipFavoritesByNames(favoritesToRemove)
      tipQueries.deleteAllTips()
      tips.forEach { tipQueries.upsertTip(it) }
    }
    Logger.d("update tips: ${tips.size}")
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
