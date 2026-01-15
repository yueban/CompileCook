package com.yueban.compilecook.ui.base

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
sealed interface Async<out T> {
  val value: T?
  operator fun invoke(): T? = value
}

@Serializable
data object Uninitialized : Async<Nothing> {
  override val value: Nothing? = null
}

@Serializable
data class Loading<out T>(override val value: T? = null) : Async<T>

@Serializable
data class Success<out T>(override val value: T) : Async<T>

@Serializable(with = FailSerializer::class)
data class Fail<out T>(val error: Throwable, override val value: T? = null) : Async<T> {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as Fail<*>
    if (error.message != other.error.message) return false
    if (value != other.value) return false
    return true
  }

  override fun hashCode(): Int {
    var result = error.message?.hashCode() ?: 0
    result = 31 * result + (value?.hashCode() ?: 0)
    return result
  }
}

class FailSerializer<T>(
  private val dataSerializer: KSerializer<T>,
) : KSerializer<Fail<T>> {

  @Serializable
  private data class FailSurrogate<T>(
    val message: String,
    val value: T? = null,
  )

  override val descriptor: SerialDescriptor = FailSurrogate.serializer(dataSerializer).descriptor

  override fun serialize(encoder: Encoder, value: Fail<T>) {
    val surrogate = FailSurrogate(
      message = value.error.message ?: "Unknown Error",
      value = value.value
    )
    FailSurrogate.serializer(dataSerializer).serialize(encoder, surrogate)
  }

  override fun deserialize(decoder: Decoder): Fail<T> {
    val surrogate = FailSurrogate.serializer(dataSerializer).deserialize(decoder)
    return Fail(
      error = Throwable(surrogate.message),
      value = surrogate.value
    )
  }
}
