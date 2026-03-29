package com.yueban.compilecook.repo

import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.net.entity.toLocalEntity
import com.yueban.compilecook.data.net.service.DishRemoteDataSource
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.repo.entity.DishDetail
import com.yueban.compilecook.repo.entity.DishSummary
import com.yueban.compilecook.repo.entity.TipDetail
import com.yueban.compilecook.repo.entity.TipSummary
import com.yueban.compilecook.repo.entity.TipType
import com.yueban.compilecook.repo.entity.toDishDetail
import com.yueban.compilecook.repo.entity.toDishSummary
import com.yueban.compilecook.repo.entity.toTipDetail
import com.yueban.compilecook.repo.entity.toTipSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface DishRepo {
  fun getAllDishes(isFavorite: Boolean): Flow<List<DishSummary>>
  fun getDishesByCategory(category: DishCategory): Flow<List<DishSummary>>
  fun getDishesByDifficulty(difficult: Long): Flow<List<DishSummary>>
  fun getDishByName(name: String): Flow<DishDetail?>
  fun getDishCategories(): Flow<List<DishCategory>>
  suspend fun getRandomDishName(): String?
  fun getGroupedTipsSortedByPinyin(): Flow<List<Pair<TipType, List<TipSummary>>>>
  fun getTipByName(name: String): Flow<TipDetail?>
  suspend fun updateDishes()
  suspend fun updateTips()
  suspend fun deleteDishByName(name: String)
  suspend fun clearAllDishes()
  suspend fun toggleDishFavorite(name: String)
}

internal class DishRepoImpl(
  coroutineScope: CoroutineScope,
  private val dishLocalDataSource: DishLocalDataSource,
  private val dishRemoteDataSource: DishRemoteDataSource,
) : DishRepo {
  init {
    coroutineScope.launch { updateDishes() }
    coroutineScope.launch { updateTips() }
  }

  override fun getAllDishes(isFavorite: Boolean): Flow<List<DishSummary>> =
    if (isFavorite) {
      dishLocalDataSource.getFavoriteDishSummaries()
    } else {
      dishLocalDataSource.getDishSummaries()
    }.map { entities -> entities.map { it.toDishSummary() } }

  override fun getDishesByCategory(category: DishCategory): Flow<List<DishSummary>> =
    dishLocalDataSource.getDishSummariesByCategory(category.name.lowercase())
      .map { entities -> entities.map { it.toDishSummary() } }

  override fun getDishesByDifficulty(difficult: Long): Flow<List<DishSummary>> =
    dishLocalDataSource.getDishSummariesByDifficulty(difficult).map { entities -> entities.map { it.toDishSummary() } }

  override fun getDishByName(name: String): Flow<DishDetail?> =
    dishLocalDataSource.getDishByName(name).map { it?.toDishDetail() }

  override fun getDishCategories(): Flow<List<DishCategory>> =
    dishLocalDataSource.getDishCategories().map { categories ->
      categories.map { DishCategory.fromValue(it) }.sortedBy { it.ordinal }
    }

  override suspend fun getRandomDishName(): String? = dishLocalDataSource.getRandomDishName()

  override fun getGroupedTipsSortedByPinyin(): Flow<List<Pair<TipType, List<TipSummary>>>> =
    dishLocalDataSource.getTipSummaries().map { entities ->
      entities.asSequence().map { it.toTipSummary() }
        .filter { it.type != TipType.UNKNOWN }
        .groupBy { it.type }
        .toList()
        .sortedBy { it.first.ordinal }.toList()
    }

  override fun getTipByName(name: String): Flow<TipDetail?> =
    dishLocalDataSource.getTipDetail(name).map { it?.toTipDetail() }

  override suspend fun updateDishes() {
    val dishes = dishRemoteDataSource.getDishes().map { it.toLocalEntity() }
    Logger.d("remote dishes: ${dishes.size}")
    dishLocalDataSource.upsertDishes(dishes)
  }

  override suspend fun updateTips() {
    val tips = dishRemoteDataSource.getTips().map { it.toLocalEntity() }
    Logger.d("remote tips: ${tips.size}")
    dishLocalDataSource.upsertTips(tips)
  }

  override suspend fun deleteDishByName(name: String) = dishLocalDataSource.deleteDishByName(name)

  override suspend fun clearAllDishes() = dishLocalDataSource.deleteAllDishes()

  override suspend fun toggleDishFavorite(name: String) = dishLocalDataSource.toggleDishFavorite(name)
}
