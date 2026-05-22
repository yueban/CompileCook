package com.yueban.compilecook.data.cache

import app.cash.sqldelight.async.coroutines.awaitCreate
import com.yueban.compilecook.data.cache.db.AppDatabase
import com.yueban.compilecook.data.cache.db.provideInMemoryDbDriver
import com.yueban.compilecook.data.db.entity.AiChatConversationLocalEntity
import com.yueban.compilecook.data.db.entity.AiChatMessageLocalEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiChatLocalDataSourceTest {
  private suspend fun createDataSource(): Pair<AiChatLocalDataSource, () -> Unit> {
    val driver = provideInMemoryDbDriver(AppDatabase.Schema)
    AppDatabase.Schema.awaitCreate(driver)
    val db = AppDatabase(driver)
    val dataSource = AiChatLocalDataSourceImpl(
      aiChatQueries = db.aiChatQueries,
      defaultDispatcher = UnconfinedTestDispatcher(),
    )
    return dataSource to { driver.close() }
  }

  @Test
  fun getConversations_empty() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()

    assertTrue(dataSource.getConversations().first().isEmpty())

    cleanup()
  }

  @Test
  fun getConversations_orderedByUpdatedAt() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "First", "general", "", 1000L, 1000L))
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-2", "Second", "general", "", 2000L, 3000L))
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-3", "Third", "general", "", 3000L, 2000L))

    val conversations = dataSource.getConversations().first()
    assertEquals(3, conversations.size)
    assertEquals("conv-2", conversations[0].id) // updatedAt=3000
    assertEquals("conv-3", conversations[1].id) // updatedAt=2000
    assertEquals("conv-1", conversations[2].id) // updatedAt=1000

    cleanup()
  }

  @Test
  fun getConversationById_found() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "Chat", "dish", "Pizza", 1000L, 2000L))

    val conversation = dataSource.getConversationById("conv-1")
    assertNotNull(conversation)
    assertEquals("conv-1", conversation.id)
    assertEquals("Chat", conversation.title)
    assertEquals("dish", conversation.contextType)
    assertEquals("Pizza", conversation.contextName)

    cleanup()
  }

  @Test
  fun getConversationById_notFound() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()

    val conversation = dataSource.getConversationById("non-existent")
    assertNull(conversation)

    cleanup()
  }

  @Test
  fun upsertConversation() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "Test", "general", "", 1000L, 1000L))

    val conversations = dataSource.getConversations().first()
    assertEquals(1, conversations.size)
    assertEquals("Test", conversations.first().title)

    cleanup()
  }

  @Test
  fun updateConversationTitle() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "Original", "general", "", 1000L, 1000L))

    dataSource.updateConversationTitle("conv-1", "Updated", 2000L)

    val conversation = dataSource.getConversationById("conv-1")
    assertNotNull(conversation)
    assertEquals("Updated", conversation.title)
    assertEquals(2000L, conversation.updatedAt)

    cleanup()
  }

  @Test
  fun updateConversationTimestamp() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "Title", "general", "", 1000L, 1000L))

    dataSource.updateConversationTimestamp("conv-1", 5000L)

    val conversation = dataSource.getConversationById("conv-1")
    assertNotNull(conversation)
    assertEquals(5000L, conversation.updatedAt)
    assertEquals("Title", conversation.title) // unchanged

    cleanup()
  }

  @Test
  fun deleteConversationById_cascadesToMessages() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "Chat", "general", "", 1000L, 1000L))
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-2", "Other", "general", "", 2000L, 2000L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-1", "conv-1", "user", "Hello", 1001L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-2", "conv-2", "user", "Hi", 2001L))

    dataSource.deleteConversationById("conv-1")

    assertNull(dataSource.getConversationById("conv-1"))
    assertNotNull(dataSource.getConversationById("conv-2"))
    assertTrue(dataSource.getMessagesByConversationId("conv-1").first().isEmpty())
    assertEquals(1, dataSource.getMessagesByConversationId("conv-2").first().size)

    cleanup()
  }

  @Test
  fun deleteAllConversations() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "First", "general", "", 1000L, 1000L))
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-2", "Second", "general", "", 2000L, 2000L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-1", "conv-1", "user", "Hello", 1001L))

    dataSource.deleteAllConversations()

    assertTrue(dataSource.getConversations().first().isEmpty())
    assertTrue(dataSource.getMessagesByConversationId("conv-1").first().isEmpty())

    cleanup()
  }

  @Test
  fun getMessagesByConversationId_empty() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()

    assertTrue(dataSource.getMessagesByConversationId("non-existent").first().isEmpty())

    cleanup()
  }

  @Test
  fun getMessagesByConversationId_orderedByTimestamp() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "Chat", "general", "", 1000L, 1000L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-3", "conv-1", "user", "Third", 3000L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-1", "conv-1", "user", "First", 1000L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-2", "conv-1", "assistant", "Second", 2000L))

    val messages = dataSource.getMessagesByConversationId("conv-1").first()
    assertEquals(3, messages.size)
    assertEquals("msg-1", messages[0].id)
    assertEquals("msg-2", messages[1].id)
    assertEquals("msg-3", messages[2].id)

    cleanup()
  }

  @Test
  fun getMessageCount() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "Chat", "general", "", 1000L, 1000L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-1", "conv-1", "user", "Hello", 1001L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-2", "conv-1", "assistant", "Hi", 1002L))

    val count = dataSource.getMessageCount("conv-1")
    assertEquals(2L, count)

    cleanup()
  }

  @Test
  fun upsertMessage() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "Chat", "general", "", 1000L, 1000L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-1", "conv-1", "user", "Hello!", 1001L))

    val messages = dataSource.getMessagesByConversationId("conv-1").first()
    assertEquals(1, messages.size)
    assertEquals("msg-1", messages.first().id)
    assertEquals("user", messages.first().role)
    assertEquals("Hello!", messages.first().content)

    cleanup()
  }

  @Test
  fun upsertMessages() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "Chat", "general", "", 1000L, 1000L))
    dataSource.upsertMessages(
      listOf(
        AiChatMessageLocalEntity("msg-1", "conv-1", "user", "Hello", 1001L),
        AiChatMessageLocalEntity("msg-2", "conv-1", "assistant", "Hi!", 1002L),
        AiChatMessageLocalEntity("msg-3", "conv-1", "user", "Thanks", 1003L),
      )
    )

    val messages = dataSource.getMessagesByConversationId("conv-1").first()
    assertEquals(3, messages.size)

    cleanup()
  }

  @Test
  fun deleteMessagesByConversationId() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "Chat 1", "general", "", 1000L, 1000L))
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-2", "Chat 2", "general", "", 2000L, 2000L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-1", "conv-1", "user", "Hello", 1001L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-2", "conv-2", "user", "Hi", 2001L))

    dataSource.deleteMessagesByConversationId("conv-1")

    assertTrue(dataSource.getMessagesByConversationId("conv-1").first().isEmpty())
    assertEquals(1, dataSource.getMessagesByConversationId("conv-2").first().size)

    cleanup()
  }

  @Test
  fun deleteAllMessages() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "Chat", "general", "", 1000L, 1000L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-1", "conv-1", "user", "Hello", 1001L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-2", "conv-1", "assistant", "Hi", 1002L))

    dataSource.deleteAllMessages()

    assertTrue(dataSource.getMessagesByConversationId("conv-1").first().isEmpty())

    cleanup()
  }

  @Test
  fun multipleConversations_independent() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertConversation(AiChatConversationLocalEntity("conv-1", "Chat 1", "dish", "Pizza", 1000L, 1000L))
    dataSource.upsertConversation(
      AiChatConversationLocalEntity("conv-2", "Chat 2", "tip", "Knife Skills", 2000L, 2000L)
    )
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-1", "conv-1", "user", "About pizza", 1001L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-2", "conv-1", "assistant", "Pizza info", 1002L))
    dataSource.upsertMessage(AiChatMessageLocalEntity("msg-3", "conv-2", "user", "About knives", 2001L))

    val conv1Messages = dataSource.getMessagesByConversationId("conv-1").first()
    val conv2Messages = dataSource.getMessagesByConversationId("conv-2").first()
    assertEquals(2, conv1Messages.size)
    assertEquals(1, conv2Messages.size)
    assertTrue(conv1Messages.all { it.conversationId == "conv-1" })
    assertTrue(conv2Messages.all { it.conversationId == "conv-2" })

    cleanup()
  }
}
