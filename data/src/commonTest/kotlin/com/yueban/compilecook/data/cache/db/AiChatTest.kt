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
  fun upsertAndSelectConversation() = testingDb {
    aiChatQueries.upsertConversation(
      id = "conv-1",
      title = "Cooking tips",
      contextType = "general",
      contextName = "",
      createdAt = 1000L,
      updatedAt = 1000L,
    )

    val conversations = aiChatQueries.getConversations().awaitAsList()
    assertEquals(1, conversations.size)
    assertEquals("conv-1", conversations.first().id)
    assertEquals("Cooking tips", conversations.first().title)
    assertEquals("general", conversations.first().contextType)

    val detail = aiChatQueries.getConversationById("conv-1").awaitAsOneOrNull()
    assertNotNull(detail)
    assertEquals("conv-1", detail.id)
    assertEquals("Cooking tips", detail.title)
    assertEquals("general", detail.contextType)
    assertEquals("", detail.contextName)
    assertEquals(1000L, detail.createdAt)
    assertEquals(1000L, detail.updatedAt)
  }

  @Test
  fun getConversationById_nonExistent_returnsNull() = testingDb {
    val conversation = aiChatQueries.getConversationById("non-existent").awaitAsOneOrNull()
    assertNull(conversation)
  }

  @Test
  fun upsertConversation_modifiesExisting() = testingDb {
    aiChatQueries.upsertConversation("conv-1", "Original", "general", "", 1000L, 1000L)
    aiChatQueries.upsertConversation("conv-1", "Updated", "dish", "Pizza", 1000L, 2000L)

    val updated = aiChatQueries.getConversationById("conv-1").awaitAsOne()
    assertEquals("Updated", updated.title)
    assertEquals("dish", updated.contextType)
    assertEquals("Pizza", updated.contextName)
    assertEquals(2000L, updated.updatedAt)
  }

  @Test
  fun updateConversationTitle() = testingDb {
    aiChatQueries.upsertConversation("conv-1", "Original", "general", "", 1000L, 1000L)
    aiChatQueries.updateConversationTitle(title = "New Title", updatedAt = 2000L, id = "conv-1")

    val updated = aiChatQueries.getConversationById("conv-1").awaitAsOne()
    assertEquals("New Title", updated.title)
    assertEquals(2000L, updated.updatedAt)
    assertEquals("general", updated.contextType) // unchanged
  }

  @Test
  fun updateConversationTimestamp() = testingDb {
    aiChatQueries.upsertConversation("conv-1", "Title", "general", "", 1000L, 1000L)
    aiChatQueries.updateConversationTimestamp(updatedAt = 5000L, id = "conv-1")

    val updated = aiChatQueries.getConversationById("conv-1").awaitAsOne()
    assertEquals(5000L, updated.updatedAt)
    assertEquals("Title", updated.title) // unchanged
  }

  @Test
  fun deleteConversationById_removesCorrectEntry() = testingDb {
    aiChatQueries.upsertConversation("conv-1", "First", "general", "", 1000L, 1000L)
    aiChatQueries.upsertConversation("conv-2", "Second", "general", "", 2000L, 2000L)

    aiChatQueries.deleteConversationById("conv-1")

    val remaining = aiChatQueries.getConversations().awaitAsList()
    assertEquals(1, remaining.size)
    assertEquals("conv-2", remaining.first().id)

    val deleted = aiChatQueries.getConversationById("conv-1").awaitAsOneOrNull()
    assertNull(deleted)
  }

  @Test
  fun deleteAllConversations_clearsTable() = testingDb {
    aiChatQueries.upsertConversation("conv-1", "First", "general", "", 1000L, 1000L)
    aiChatQueries.upsertConversation("conv-2", "Second", "general", "", 2000L, 2000L)

    aiChatQueries.deleteAllConversations()

    assertTrue(aiChatQueries.getConversations().awaitAsList().isEmpty())
  }

  @Test
  fun upsertAndSelectMessage() = testingDb {
    aiChatQueries.upsertConversation("conv-1", "Chat", "general", "", 1000L, 1000L)
    aiChatQueries.upsertMessage(
      id = "msg-1",
      conversationId = "conv-1",
      role = "user",
      content = "Hello!",
      timestamp = 1001L,
    )

    val messages = aiChatQueries.getMessagesByConversationId("conv-1").awaitAsList()
    assertEquals(1, messages.size)
    assertEquals("msg-1", messages.first().id)
    assertEquals("conv-1", messages.first().conversationId)
    assertEquals("user", messages.first().role)
    assertEquals("Hello!", messages.first().content)
    assertEquals(1001L, messages.first().timestamp)
  }

  @Test
  fun getMessagesByConversationId_ordered() = testingDb {
    aiChatQueries.upsertConversation("conv-1", "Chat", "general", "", 1000L, 1000L)
    aiChatQueries.upsertMessage("msg-2", "conv-1", "assistant", "Hi!", 1003L)
    aiChatQueries.upsertMessage("msg-1", "conv-1", "user", "Hello!", 1001L)
    aiChatQueries.upsertMessage("msg-3", "conv-1", "user", "Thanks", 1005L)

    val messages = aiChatQueries.getMessagesByConversationId("conv-1").awaitAsList()
    assertEquals(3, messages.size)
    assertEquals("msg-1", messages[0].id)
    assertEquals("msg-2", messages[1].id)
    assertEquals("msg-3", messages[2].id)
  }

  @Test
  fun getMessagesByConversationId_empty() = testingDb {
    val messages = aiChatQueries.getMessagesByConversationId("non-existent").awaitAsList()
    assertTrue(messages.isEmpty())
  }

  @Test
  fun getMessageCount() = testingDb {
    aiChatQueries.upsertConversation("conv-1", "Chat", "general", "", 1000L, 1000L)
    aiChatQueries.upsertMessage("msg-1", "conv-1", "user", "Hello!", 1001L)
    aiChatQueries.upsertMessage("msg-2", "conv-1", "assistant", "Hi!", 1002L)

    val count = aiChatQueries.getMessageCount("conv-1").awaitAsOne()
    assertEquals(2L, count)
  }

  @Test
  fun deleteMessagesByConversationId() = testingDb {
    aiChatQueries.upsertConversation("conv-1", "Chat 1", "general", "", 1000L, 1000L)
    aiChatQueries.upsertConversation("conv-2", "Chat 2", "general", "", 2000L, 2000L)
    aiChatQueries.upsertMessage("msg-1", "conv-1", "user", "Hello!", 1001L)
    aiChatQueries.upsertMessage("msg-2", "conv-2", "user", "Hi!", 2001L)

    aiChatQueries.deleteMessagesByConversationId("conv-1")

    assertTrue(aiChatQueries.getMessagesByConversationId("conv-1").awaitAsList().isEmpty())
    assertEquals(1, aiChatQueries.getMessagesByConversationId("conv-2").awaitAsList().size)
  }

  @Test
  fun deleteAllMessages() = testingDb {
    aiChatQueries.upsertConversation("conv-1", "Chat", "general", "", 1000L, 1000L)
    aiChatQueries.upsertMessage("msg-1", "conv-1", "user", "Hello!", 1001L)
    aiChatQueries.upsertMessage("msg-2", "conv-1", "assistant", "Hi!", 1002L)

    aiChatQueries.deleteAllMessages()

    assertTrue(aiChatQueries.getMessagesByConversationId("conv-1").awaitAsList().isEmpty())
  }
}
