@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("TooManyFunctions")

package com.yueban.compilecook.util

import kotlinx.coroutines.await
import kotlin.js.Promise

actual object ImageCompressor {
  private var cacheImageBytes: ByteArray? = null
  private var cacheMaxWidth: Int = -1
  private var cacheImageBitmap: JsAny? = null

  actual suspend fun compress(imageBytes: ByteArray, maxWidth: Int, quality: Int): ByteArray {
    val imageBitmap = getOrDecodeImageBitmap(imageBytes, maxWidth)
    val compressedJs = compressImageJs(imageBitmap, maxWidth, quality).await()
    return uint8ArrayToByteArray(compressedJs)
  }

  private suspend fun getOrDecodeImageBitmap(imageBytes: ByteArray, maxWidth: Int): JsAny {
    if (cacheImageBytes === imageBytes && cacheMaxWidth == maxWidth && cacheImageBitmap != null) {
      return cacheImageBitmap!!
    }

    cacheImageBytes = imageBytes
    cacheMaxWidth = maxWidth

    closeImageBitmap(cacheImageBitmap)
    val uint8Array = byteArrayToUint8Array(imageBytes)
    val newBitmap = decodeToImageBitmapJs(uint8Array).await()
    cacheImageBitmap = newBitmap
    return newBitmap
  }

  actual suspend fun getDimensions(imageBytes: ByteArray): ImageDimensions? {
    val uint8Array = byteArrayToUint8Array(imageBytes)
    val bitmap = decodeToImageBitmapJs(uint8Array).await()
    val width = getImageBitmapWidth(bitmap)
    val height = getImageBitmapHeight(bitmap)
    closeImageBitmap(bitmap)
    return if (width > 0 && height > 0) {
      ImageDimensions(width, height)
    } else {
      null
    }
  }
}

private fun byteArrayToUint8Array(bytes: ByteArray): JsAny {
  val jsArray = createUint8Array(bytes.size)
  for (i in bytes.indices) {
    setUint8ArrayElement(jsArray, i, bytes[i])
  }
  return jsArray
}

private fun uint8ArrayToByteArray(jsArray: JsAny): ByteArray {
  val length = getUint8ArrayLength(jsArray)
  val bytes = ByteArray(length)
  for (i in 0 until length) {
    bytes[i] = getUint8ArrayElement(jsArray, i)
  }
  return bytes
}

@JsFun("(size) => new Uint8Array(size)")
private external fun createUint8Array(size: Int): JsAny

@JsFun("(array, index, value) => array[index] = value")
private external fun setUint8ArrayElement(array: JsAny, index: Int, value: Byte)

@JsFun("(array) => array.length")
private external fun getUint8ArrayLength(array: JsAny): Int

@JsFun("(array, index) => array[index]")
private external fun getUint8ArrayElement(array: JsAny, index: Int): Byte

@JsFun("(bitmap) => bitmap.width")
private external fun getImageBitmapWidth(bitmap: JsAny): Int

@JsFun("(bitmap) => bitmap.height")
private external fun getImageBitmapHeight(bitmap: JsAny): Int

@JsFun("(bitmap) => { if (bitmap && typeof bitmap.close === 'function') bitmap.close(); }")
private external fun closeImageBitmap(bitmap: JsAny?)

@JsFun(
  """(uint8Array) => {
    const blob = new Blob([uint8Array]);
    return createImageBitmap(blob);
}"""
)
private external fun decodeToImageBitmapJs(uint8Array: JsAny): Promise<JsAny>

@JsFun(
  """(bitmap, maxWidth, quality) => {
    let width = bitmap.width;
    let height = bitmap.height;

    if (width > maxWidth) {
        height = Math.round(height * (maxWidth / width));
        width = maxWidth;
    }

    const canvas = new OffscreenCanvas(width, height);
    const ctx = canvas.getContext('2d');
    ctx.drawImage(bitmap, 0, 0, width, height);

    return canvas.convertToBlob({
        type: 'image/jpeg',
        quality: quality / 100
    }).then(blob => blob.arrayBuffer())
      .then(buffer => new Uint8Array(buffer));
}"""
)
private external fun compressImageJs(bitmap: JsAny, maxWidth: Int, quality: Int): Promise<JsAny>
