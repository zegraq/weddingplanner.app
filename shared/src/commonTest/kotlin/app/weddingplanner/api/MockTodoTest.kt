package app.weddingplanner.api

import app.weddingplanner.domain.Clock
import app.weddingplanner.domain.TodoAssignee
import app.weddingplanner.domain.TodoInput
import app.weddingplanner.domain.TodoStatus
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MockTodoTest {

    private val fixedClock = Clock { "2026-05-22T10:00:00+02:00" }

    private fun client() = MockApiClient(clock = fixedClock, random = Random(42))

    @Test
    fun seedsTodos() = runTest {
        val items = client().listTodos().getOrThrow()
        assertTrue(items.size >= 4, "Förväntade minst fyra seedade uppgifter")
        assertTrue(items.any { it.status == TodoStatus.Done }, "Förväntade en avbockad uppgift")
        assertTrue(items.any { it.dueDate == null }, "Förväntade en uppgift utan deadline")
        assertTrue(items.any { it.assignee == TodoAssignee.Both })
    }

    @Test
    fun createTodoAppendsAndDefaultsToOpen() = runTest {
        val api = client()
        val before = api.listTodos().getOrThrow().size
        val created = api.createTodo(
            TodoInput(
                title = "Skicka inbjudningar",
                dueDate = "2026-12-01",
                assignee = TodoAssignee.Me,
                notes = null,
            ),
        ).getOrThrow()
        assertEquals(TodoStatus.Open, created.status)
        assertEquals("Skicka inbjudningar", created.title)
        assertEquals("2026-12-01", created.dueDate)
        assertEquals(TodoAssignee.Me, created.assignee)
        assertNotNull(created.createdAt)

        val after = api.listTodos().getOrThrow()
        assertEquals(before + 1, after.size)
        assertTrue(after.any { it.id == created.id })
    }

    @Test
    fun createRejectsBlankTitle() = runTest {
        val api = client()
        val result = api.createTodo(
            TodoInput(title = "   ", dueDate = null, assignee = TodoAssignee.Me, notes = null),
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun createRejectsMalformedDate() = runTest {
        val api = client()
        val result = api.createTodo(
            TodoInput(title = "X", dueDate = "imorgon", assignee = TodoAssignee.Me, notes = null),
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun createTreatsBlankDateAsNull() = runTest {
        val api = client()
        val created = api.createTodo(
            TodoInput(title = "X", dueDate = "   ", assignee = TodoAssignee.Both, notes = null),
        ).getOrThrow()
        assertNull(created.dueDate)
    }

    @Test
    fun updateChangesFields() = runTest {
        val api = client()
        val target = api.listTodos().getOrThrow().first()
        val updated = api.updateTodo(
            target.id,
            TodoInput(
                title = "Bokad: catering",
                dueDate = "2026-05-15",
                assignee = TodoAssignee.Partner,
                notes = "Påskrivet kontrakt",
            ),
        ).getOrThrow()
        assertEquals("Bokad: catering", updated.title)
        assertEquals("2026-05-15", updated.dueDate)
        assertEquals(TodoAssignee.Partner, updated.assignee)
        assertEquals("Påskrivet kontrakt", updated.notes)
        assertEquals(target.status, updated.status)
        assertEquals(target.createdAt, updated.createdAt)
    }

    @Test
    fun setStatusTogglesOpenAndDone() = runTest {
        val api = client()
        val open = api.listTodos().getOrThrow().first { it.status == TodoStatus.Open }
        val done = api.setTodoStatus(open.id, TodoStatus.Done).getOrThrow()
        assertEquals(TodoStatus.Done, done.status)
        val reopened = api.setTodoStatus(open.id, TodoStatus.Open).getOrThrow()
        assertEquals(TodoStatus.Open, reopened.status)
    }

    @Test
    fun deleteRemovesItem() = runTest {
        val api = client()
        val target = api.listTodos().getOrThrow().first()
        api.deleteTodo(target.id).getOrThrow()
        val remaining = api.listTodos().getOrThrow()
        assertFalse(remaining.any { it.id == target.id })
    }

    @Test
    fun unknownIdFailsCleanly() = runTest {
        val api = client()
        assertTrue(
            api.updateTodo(
                "td-nonexistent",
                TodoInput("X", null, TodoAssignee.Me, null),
            ).isFailure,
        )
        assertTrue(api.setTodoStatus("td-nonexistent", TodoStatus.Done).isFailure)
        assertTrue(api.deleteTodo("td-nonexistent").isFailure)
    }

    @Test
    fun weddingExposesNames() = runTest {
        val w = client().getWedding()
        assertEquals("Daniel", w.myName)
        assertEquals("Sara", w.partnerName)
    }
}
