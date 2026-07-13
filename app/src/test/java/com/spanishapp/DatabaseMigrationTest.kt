package com.spanishapp

import android.content.Context
import androidx.room.Room
import com.spanishapp.data.db.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * v1.26.1: тесты вокруг Room-миграций. Полноценный migrateAll v1→v32 невозможен
 * (исторические схемы v1..v31 никогда не экспортировались — schema-export
 * включён только с этой версии, для БУДУЩИХ миграций). Поэтому здесь:
 *  1) чистая проверка целостности ALL_MIGRATIONS (непрерывная цепочка 1→32),
 *     которая ловит «объявил MIGRATION_x_y, но забыл дописать в список» —
 *     ровно то, от чего защищает единая константа;
 *  2) smoke-тест: БД со всеми миграциями реально СТРОИТСЯ (Room валидирует
 *     @Entity↔схему) и открывается.
 */
class MigrationChainTest {

    /** Чистый JUnit — без Android. Самый дешёвый и надёжный барьер. */
    @Test
    fun `ALL_MIGRATIONS is a contiguous 1 to 32 chain, no gaps or duplicates`() {
        val migrations = AppDatabase.ALL_MIGRATIONS
        assertEquals("ожидается 31 миграция (v1→v32)", 31, migrations.size)

        val sorted = migrations.sortedBy { it.startVersion }
        var expected = 1
        for (m in sorted) {
            assertEquals("разрыв цепочки миграций перед v$expected", expected, m.startVersion)
            assertEquals("миграция должна поднимать ровно на +1", expected + 1, m.endVersion)
            expected++
        }
        assertEquals("цепочка должна доходить до версии БД = 32", 32, expected)
    }

    /** Версия @Database должна совпадать с концом цепочки миграций. */
    @Test
    fun `migration chain end matches ALL_MIGRATIONS max endVersion`() {
        val maxEnd = AppDatabase.ALL_MIGRATIONS.maxOf { it.endVersion }
        assertEquals(32, maxEnd)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class DatabaseBuildSmokeTest {

    @Test
    fun `database builds with all migrations and opens cleanly`() {
        val ctx: Context = RuntimeEnvironment.getApplication()
        val db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .addMigrations(*AppDatabase.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()
        try {
            // Форсируем открытие → Room валидирует, что @Entity-сущности
            // консистентны со схемой. Рассогласование = исключение здесь.
            val sqlDb = db.openHelper.writableDatabase
            sqlDb.query("SELECT count(*) FROM sqlite_master WHERE type='table'").use { c ->
                assertTrue(c.moveToFirst())
                // 27+ пользовательских таблиц + служебные Room — заведомо > 20.
                assertTrue("ожидается много таблиц, а не пустая БД", c.getInt(0) > 20)
            }
        } finally {
            db.close()
        }
    }
}
