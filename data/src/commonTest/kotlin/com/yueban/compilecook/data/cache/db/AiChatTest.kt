package com.yueban.compilecook.data.cache.db

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiChatTest {
  @Test
  fun initialState_conversationsEmpty() = testingDb {
    val conversations = aiChatQueries.getConversations().awaitAsList()
    assertTrue(conversations.isEmpty(), "Initially, the conversation table should be empty.")
  }

  @Test
  fun insertAndSelectConversation() = testingDb {
    aiChatQueries.insertConversation(
      title = "Cooking tips",
      contextType = "general",
      contextName = "",
      createdAt = 1000L,
      updatedAt = 1000L,
    )
    val convId = aiChatQueries.selectLastInsertRowId().awaitAsOne()

    val conversations = aiChatQueries.getConversations().awaitAsList()
    assertEquals(1, conversations.size)
    assertEquals(convId, conversations.first().id)
    assertEquals("Cooking tips", conversations.first().title)
    assertEquals("general", conversations.first().contextType)

    val detail = aiChatQueries.getConversationById(convId).awaitAsOneOrNull()
    assertNotNull(detail)
    assertEquals(convId, detail.id)
    assertEquals("Cooking tips", detail.title)
    assertEquals("general", detail.contextType)
    assertEquals("", detail.contextName)
    assertEquals(1000L, detail.createdAt)
    assertEquals(1000L, detail.updatedAt)
  }

  @Test
  fun getConversationById_nonExistent_returnsNull() = testingDb {
    val conversation = aiChatQueries.getConversationById(999L).awaitAsOneOrNull()
    assertNull(conversation)
  }

  @Test
  fun updateConversationTitle() = testingDb {
    aiChatQueries.insertConversation("Original", "general", "", 1000L, 1000L)
    val convId = aiChatQueries.selectLastInsertRowId().awaitAsOne()
    aiChatQueries.updateConversationTitle(title = "New Title", updatedAt = 2000L, id = convId)

    val updated = aiChatQueries.getConversationById(convId).awaitAsOne()
    assertEquals("New Title", updated.title)
    assertEquals(2000L, updated.updatedAt)
    assertEquals("general", updated.contextType) // unchanged
  }

  @Test
  fun updateConversationTimestamp() = testingDb {
    aiChatQueries.insertConversation("Title", "general", "", 1000L, 1000L)
    val convId = aiChatQueries.selectLastInsertRowId().awaitAsOne()
    aiChatQueries.updateConversationTimestamp(updatedAt = 5000L, id = convId)

    val updated = aiChatQueries.getConversationById(convId).awaitAsOne()
    assertEquals(5000L, updated.updatedAt)
    assertEquals("Title", updated.title) // unchanged
  }

  @Test
  fun deleteConversationById_removesCorrectEntry() = testingDb {
    aiChatQueries.insertConversation("First", "general", "", 1000L, 1000L)
    val conv1 = aiChatQueries.selectLastInsertRowId().awaitAsOne()
    aiChatQueries.insertConversation("Second", "general", "", 2000L, 2000L)
    val conv2 = aiChatQueries.selectLastInsertRowId().awaitAsOne()

    aiChatQueries.deleteConversationById(conv1)

    val remaining = aiChatQueries.getConversations().awaitAsList()
    assertEquals(1, remaining.size)
    assertEquals(conv2, remaining.first().id)

    val deleted = aiChatQueries.getConversationById(conv1).awaitAsOneOrNull()
    assertNull(deleted)
  }

  @Test
  fun deleteAllConversations_clearsTable() = testingDb {
    aiChatQueries.insertConversation("First", "general", "", 1000L, 1000L)
    aiChatQueries.insertConversation("Second", "general", "", 2000L, 2000L)

    aiChatQueries.deleteAllConversations()

    assertTrue(aiChatQueries.getConversations().awaitAsList().isEmpty())
  }

  @Test
  fun insertAndSelectMessage() = testingDb {
    aiChatQueries.insertConversation("Chat", "general", "", 1000L, 1000L)
    val convId = aiChatQueries.selectLastInsertRowId().awaitAsOne()

    aiChatQueries.insertMessage(
      conversationId = convId,
      role = "user",
      content = "Hello!",
      timestamp = 1001L,
      status = 0L,
    )
    val msgId = aiChatQueries.selectLastInsertRowId().awaitAsOne()

    val messages = aiChatQueries.getMessagesByConversationId(convId).awaitAsList()
    assertEquals(1, messages.size)
    assertEquals(msgId, messages.first().id)
    assertEquals(convId, messages.first().conversationId)
    assertEquals("user", messages.first().role)
    assertEquals("Hello!", messages.first().content)
    assertEquals(1001L, messages.first().timestamp)
  }

  @Test
  fun getMessagesByConversationId_ordered() = testingDb {
    aiChatQueries.insertConversation("Chat", "general", "", 1000L, 1000L)
    val convId = aiChatQueries.selectLastInsertRowId().awaitAsOne()

    aiChatQueries.insertMessage(convId, "assistant", "Hi!", 1003L, 0L)
    val msg2 = aiChatQueries.selectLastInsertRowId().awaitAsOne()
    aiChatQueries.insertMessage(convId, "user", "Hello!", 1001L, 0L)
    val msg1 = aiChatQueries.selectLastInsertRowId().awaitAsOne()
    aiChatQueries.insertMessage(convId, "user", "Thanks", 1005L, 0L)
    val msg3 = aiChatQueries.selectLastInsertRowId().awaitAsOne()

    val messages = aiChatQueries.getMessagesByConversationId(convId).awaitAsList()
    assertEquals(3, messages.size)
    assertEquals(msg1, messages[0].id)
    assertEquals(msg2, messages[1].id)
    assertEquals(msg3, messages[2].id)
  }

  @Test
  fun getMessagesByConversationId_empty() = testingDb {
    val messages = aiChatQueries.getMessagesByConversationId(999L).awaitAsList()
    assertTrue(messages.isEmpty())
  }

  @Test
  fun getMessageCount() = testingDb {
    aiChatQueries.insertConversation("Chat", "general", "", 1000L, 1000L)
    val convId = aiChatQueries.selectLastInsertRowId().awaitAsOne()
    aiChatQueries.insertMessage(convId, "user", "Hello!", 1001L, 0L)
    aiChatQueries.insertMessage(convId, "assistant", "Hi!", 1002L, 0L)

    val count = aiChatQueries.getMessageCount(convId).awaitAsOne()
    assertEquals(2L, count)
  }

  @Test
  fun updateMessageContent() = testingDb {
    aiChatQueries.insertConversation("Chat", "general", "", 1000L, 1000L)
    val convId = aiChatQueries.selectLastInsertRowId().awaitAsOne()
    aiChatQueries.insertMessage(convId, "assistant", "", 1001L, 0L)
    val msgId = aiChatQueries.selectLastInsertRowId().awaitAsOne()

    aiChatQueries.updateMessageContent(content = "Updated content", id = msgId)

    val messages = aiChatQueries.getMessagesByConversationId(convId).awaitAsList()
    assertEquals(1, messages.size)
    assertEquals("Updated content", messages.first().content)
  }

  @Test
  fun deleteMessagesByConversationId() = testingDb {
    aiChatQueries.insertConversation("Chat 1", "general", "", 1000L, 1000L)
    val conv1 = aiChatQueries.selectLastInsertRowId().awaitAsOne()
    aiChatQueries.insertConversation("Chat 2", "general", "", 2000L, 2000L)
    val conv2 = aiChatQueries.selectLastInsertRowId().awaitAsOne()
    aiChatQueries.insertMessage(conv1, "user", "Hello!", 1001L, 0L)
    aiChatQueries.insertMessage(conv2, "user", "Hi!", 2001L, 0L)

    aiChatQueries.deleteMessagesByConversationId(conv1)

    assertTrue(aiChatQueries.getMessagesByConversationId(conv1).awaitAsList().isEmpty())
    assertEquals(1, aiChatQueries.getMessagesByConversationId(conv2).awaitAsList().size)
  }

  @Test
  fun deleteAllMessages() = testingDb {
    aiChatQueries.insertConversation("Chat", "general", "", 1000L, 1000L)
    val convId = aiChatQueries.selectLastInsertRowId().awaitAsOne()
    aiChatQueries.insertMessage(convId, "user", "Hello!", 1001L, 0L)
    aiChatQueries.insertMessage(convId, "assistant", "Hi!", 1002L, 0L)

    aiChatQueries.deleteAllMessages()

    assertTrue(aiChatQueries.getMessagesByConversationId(convId).awaitAsList().isEmpty())
  }
}
