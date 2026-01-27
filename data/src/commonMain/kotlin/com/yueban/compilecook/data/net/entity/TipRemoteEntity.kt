package com.yueban.compilecook.data.net.entity

import com.yueban.compilecook.data.db.entity.TipLocalEntity
import kotlinx.serialization.Serializable

@Serializable
data class TipRemoteEntity(
  val name: String,
  val pinyin: String,
  val type: String,
  val content: String,
)

fun TipRemoteEntity.toLocalEntity(): TipLocalEntity =
  TipLocalEntity(
    name = this.name,
    pinyin = this.pinyin,
    type = this.type,
    content = this.content,
  )
