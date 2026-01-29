package com.yueban.compilecook.repo

import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.net.entity.toLocalEntity
import com.yueban.compilecook.data.net.service.DishRemoteDataSource
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.repo.entity.Dish
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.repo.entity.Tip
import com.yueban.compilecook.repo.entity.TipType
import com.yueban.compilecook.repo.entity.toDish
import com.yueban.compilecook.repo.entity.toTip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface DishRepo {
  fun getAllDishes(): Flow<List<Dish>>
  fun getDishCategories(): Flow<List<DishCategory>>
  fun getDishByName(name: String): Flow<Dish?>
  fun getGroupedTipsSortedByPinyin(): Flow<List<Pair<TipType, List<Tip>>>>
  suspend fun updateDishes()
  suspend fun updateTips()
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
    coroutineScope.launch { updateTips() }
  }

  override fun getAllDishes(): Flow<List<Dish>> =
    dishLocalDataSource.getAllDishes().map { entities -> entities.map { it.toDish() } }

  override fun getDishCategories(): Flow<List<DishCategory>> =
    dishLocalDataSource.getDishCategories().map { categories ->
      categories.map { DishCategory.fromValue(it) }.sortedBy { it.ordinal }
    }

  override fun getDishByName(name: String): Flow<Dish?> =
    dishLocalDataSource.getDishByName(name).map { it?.toDish() }

  override fun getGroupedTipsSortedByPinyin(): Flow<List<Pair<TipType, List<Tip>>>> =
    dishLocalDataSource.getAllTips().map { tipLocalEntities ->
      tipLocalEntities.map { it.toTip() }
        .filter { it.type != TipType.UNKNOWN }
        .groupBy { it.type }
        .toList()
        .sortedBy { it.first.ordinal }
    }

  override suspend fun updateDishes() {
    val dishes = dishRemoteDataSource.getDishes().map { it.toLocalEntity() }
    dishLocalDataSource.upsertDishes(dishes)
    Logger.d("updated dishes: ${dishes.size}")
  }

  override suspend fun updateTips() {
    val tips = dishRemoteDataSource.getTips().map { it.toLocalEntity() }
    dishLocalDataSource.upsertTips(tips)
    Logger.d("updated tips: ${tips.size}")
  }

  override suspend fun deleteDishByName(name: String) = dishLocalDataSource.deleteDishByName(name)

  override suspend fun clearAllDishes() = dishLocalDataSource.deleteAllDishes()
}
