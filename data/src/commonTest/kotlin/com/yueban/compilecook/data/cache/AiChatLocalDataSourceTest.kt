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
    dataSource.insertConversation(AiChatConversationLocalEntity(0L, "First", "general", "", 1000L, 1000L))
    dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Second", "general", "", 2000L, 3000L))
    dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Third", "general", "", 3000L, 2000L))

    val conversations = dataSource.getConversations().first()
    assertEquals(3, conversations.size)
    assertEquals("Second", conversations[0].title) // updatedAt=3000
    assertEquals("Third", conversations[1].title) // updatedAt=2000
    assertEquals("First", conversations[2].title) // updatedAt=1000

    cleanup()
  }

  @Test
  fun getConversationById_found() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val convId = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Chat", "dish", "Pizza", 1000L, 2000L))

    val conversation = dataSource.getConversationById(convId)
    assertNotNull(conversation)
    assertEquals(convId, conversation.id)
    assertEquals("Chat", conversation.title)
    assertEquals("dish", conversation.contextType)
    assertEquals("Pizza", conversation.contextName)

    cleanup()
  }

  @Test
  fun getConversationById_notFound() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()

    val conversation = dataSource.getConversationById(999L)
    assertNull(conversation)

    cleanup()
  }

  @Test
  fun insertConversation() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val convId = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Test", "general", "", 1000L, 1000L))

    val conversations = dataSource.getConversations().first()
    assertEquals(1, conversations.size)
    assertEquals(convId, conversations.first().id)
    assertEquals("Test", conversations.first().title)

    cleanup()
  }

  @Test
  fun updateConversationTitle() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val convId = dataSource.insertConversation(
      AiChatConversationLocalEntity(0L, "Original", "general", "", 1000L, 1000L)
    )

    dataSource.updateConversationTitle(convId, "Updated", 2000L)

    val conversation = dataSource.getConversationById(convId)
    assertNotNull(conversation)
    assertEquals("Updated", conversation.title)
    assertEquals(2000L, conversation.updatedAt)

    cleanup()
  }

  @Test
  fun updateConversationTimestamp() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val convId = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Title", "general", "", 1000L, 1000L))

    dataSource.updateConversationTimestamp(convId, 5000L)

    val conversation = dataSource.getConversationById(convId)
    assertNotNull(conversation)
    assertEquals(5000L, conversation.updatedAt)
    assertEquals("Title", conversation.title) // unchanged

    cleanup()
  }

  @Test
  fun deleteConversationById_cascadesToMessages() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val conv1 = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Chat", "general", "", 1000L, 1000L))
    val conv2 = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Other", "general", "", 2000L, 2000L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, conv1, "user", "Hello", 1001L, 0L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, conv2, "user", "Hi", 2001L, 0L))

    dataSource.deleteConversationById(conv1)

    assertNull(dataSource.getConversationById(conv1))
    assertNotNull(dataSource.getConversationById(conv2))
    assertTrue(dataSource.getMessagesByConversationId(conv1).first().isEmpty())
    assertEquals(1, dataSource.getMessagesByConversationId(conv2).first().size)

    cleanup()
  }

  @Test
  fun deleteAllConversations() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val conv1 = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "First", "general", "", 1000L, 1000L))
    dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Second", "general", "", 2000L, 2000L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, conv1, "user", "Hello", 1001L, 0L))

    dataSource.deleteAllConversations()

    assertTrue(dataSource.getConversations().first().isEmpty())
    assertTrue(dataSource.getMessagesByConversationId(conv1).first().isEmpty())

    cleanup()
  }

  @Test
  fun getMessagesByConversationId_empty() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()

    assertTrue(dataSource.getMessagesByConversationId(999L).first().isEmpty())

    cleanup()
  }

  @Test
  fun getMessagesByConversationId_orderedByTimestamp() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val convId = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Chat", "general", "", 1000L, 1000L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, convId, "user", "Third", 3000L, 0L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, convId, "user", "First", 1000L, 0L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, convId, "assistant", "Second", 2000L, 0L))

    val messages = dataSource.getMessagesByConversationId(convId).first()
    assertEquals(3, messages.size)
    assertEquals("First", messages[0].content)
    assertEquals("Second", messages[1].content)
    assertEquals("Third", messages[2].content)

    cleanup()
  }

  @Test
  fun getMessageCount() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val convId = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Chat", "general", "", 1000L, 1000L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, convId, "user", "Hello", 1001L, 0L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, convId, "assistant", "Hi", 1002L, 0L))

    val count = dataSource.getMessageCount(convId)
    assertEquals(2L, count)

    cleanup()
  }

  @Test
  fun insertMessage() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val convId = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Chat", "general", "", 1000L, 1000L))
    val msgId = dataSource.insertMessage(AiChatMessageLocalEntity(0L, convId, "user", "Hello!", 1001L, 0L))

    val messages = dataSource.getMessagesByConversationId(convId).first()
    assertEquals(1, messages.size)
    assertEquals(msgId, messages.first().id)
    assertEquals("user", messages.first().role)
    assertEquals("Hello!", messages.first().content)

    cleanup()
  }

  @Test
  fun insertMessages() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val convId = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Chat", "general", "", 1000L, 1000L))
    val ids = dataSource.insertMessages(
      listOf(
        AiChatMessageLocalEntity(0L, convId, "user", "Hello", 1001L, 0L),
        AiChatMessageLocalEntity(0L, convId, "assistant", "Hi!", 1002L, 0L),
        AiChatMessageLocalEntity(0L, convId, "user", "Thanks", 1003L, 0L),
      )
    )

    assertEquals(3, ids.size)
    val messages = dataSource.getMessagesByConversationId(convId).first()
    assertEquals(3, messages.size)

    cleanup()
  }

  @Test
  fun updateMessageContent() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val convId = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Chat", "general", "", 1000L, 1000L))
    val msgId = dataSource.insertMessage(AiChatMessageLocalEntity(0L, convId, "assistant", "", 1001L, 0L))

    dataSource.updateMessageContent(msgId, "Updated content")

    val messages = dataSource.getMessagesByConversationId(convId).first()
    assertEquals(1, messages.size)
    assertEquals("Updated content", messages.first().content)

    cleanup()
  }

  @Test
  fun deleteMessagesByConversationId() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val conv1 = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Chat 1", "general", "", 1000L, 1000L))
    val conv2 = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Chat 2", "general", "", 2000L, 2000L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, conv1, "user", "Hello", 1001L, 0L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, conv2, "user", "Hi", 2001L, 0L))

    dataSource.deleteMessagesByConversationId(conv1)

    assertTrue(dataSource.getMessagesByConversationId(conv1).first().isEmpty())
    assertEquals(1, dataSource.getMessagesByConversationId(conv2).first().size)

    cleanup()
  }

  @Test
  fun deleteAllMessages() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val convId = dataSource.insertConversation(AiChatConversationLocalEntity(0L, "Chat", "general", "", 1000L, 1000L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, convId, "user", "Hello", 1001L, 0L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, convId, "assistant", "Hi", 1002L, 0L))

    dataSource.deleteAllMessages()

    assertTrue(dataSource.getMessagesByConversationId(convId).first().isEmpty())

    cleanup()
  }

  @Test
  fun multipleConversations_independent() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val conv1 = dataSource.insertConversation(
      AiChatConversationLocalEntity(0L, "Chat 1", "dish", "Pizza", 1000L, 1000L)
    )
    val conv2 = dataSource.insertConversation(
      AiChatConversationLocalEntity(0L, "Chat 2", "tip", "Knife Skills", 2000L, 2000L)
    )
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, conv1, "user", "About pizza", 1001L, 0L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, conv1, "assistant", "Pizza info", 1002L, 0L))
    dataSource.insertMessage(AiChatMessageLocalEntity(0L, conv2, "user", "About knives", 2001L, 0L))

    val conv1Messages = dataSource.getMessagesByConversationId(conv1).first()
    val conv2Messages = dataSource.getMessagesByConversationId(conv2).first()
    assertEquals(2, conv1Messages.size)
    assertEquals(1, conv2Messages.size)
    assertTrue(conv1Messages.all { it.conversationId == conv1 })
    assertTrue(conv2Messages.all { it.conversationId == conv2 })

    cleanup()
  }
}
