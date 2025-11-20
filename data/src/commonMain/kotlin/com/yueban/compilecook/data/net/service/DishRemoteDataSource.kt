package com.yueban.compilecook.data.net.service

import com.yueban.compilecook.data.net.di.NetClient
import com.yueban.compilecook.data.net.entity.DishRemoteEntity
import io.ktor.client.call.body

interface DishRemoteDataSource {
  suspend fun getDishes(): List<DishRemoteEntity>
}

class DishRemoteDataSourceImpl(
  private val netClient: NetClient,
) : DishRemoteDataSource {
  override suspend fun getDishes(): List<DishRemoteEntity> = netClient.get("/dishes.json").body()
}
