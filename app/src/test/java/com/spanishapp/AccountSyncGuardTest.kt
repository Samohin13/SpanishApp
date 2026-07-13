package com.spanishapp

import com.spanishapp.service.AccountSyncGuard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * v1.26.1: тесты стейт-машины владельца локального прогресса + шлагбаума
 * upload'ов. Эта логика (introduced v1.25.98, расширена в гостевом батче
 * v1.26.1) напрямую предотвращает: (а) обнуление облака платящего юзера,
 * (б) смешение данных двух аккаунтов на общем устройстве. Раньше — 0 тестов.
 */
@RunWith(RobolectricTestRunner::class)
// Пустой Application вместо @HiltAndroidApp SpanishApp — иначе Robolectric
// зовёт onCreate() с Firebase/Hilt init и падает (FirebaseApp not initialized).
@Config(application = android.app.Application::class)
class AccountSyncGuardTest {

    private lateinit var guard: AccountSyncGuard

    @Before
    fun setUp() {
        guard = AccountSyncGuard(RuntimeEnvironment.getApplication())
        guard.clear() // изоляция между тестами (SharedPreferences переживает)
    }

    @Test
    fun `fresh install — owner null, uploads allowed for anyone`() {
        assertNull(guard.ownerUid())
        assertFalse(guard.isUploadBlocked())
        assertTrue(guard.isUploadAllowed("uidA"))
        assertTrue(guard.isUploadAllowed("uidB"))
    }

    @Test
    fun `setOwner — only owner may upload`() {
        guard.setOwner("uidA")
        assertEquals("uidA", guard.ownerUid())
        assertTrue(guard.isUploadAllowed("uidA"))
        assertFalse(guard.isUploadAllowed("uidB")) // чужой uid — блок
    }

    @Test
    fun `beginAccountSwitch blocks uploads even for the owner`() {
        guard.setOwner("uidA")
        guard.beginAccountSwitch()
        assertTrue(guard.isUploadBlocked())
        assertFalse(guard.isUploadAllowed("uidA")) // до завершения switch — никому
    }

    @Test
    fun `completeAccountSwitch sets new owner and unblocks`() {
        guard.setOwner("uidA")
        guard.beginAccountSwitch()
        guard.completeAccountSwitch("uidB")
        assertEquals("uidB", guard.ownerUid())
        assertFalse(guard.isUploadBlocked())
        assertTrue(guard.isUploadAllowed("uidB"))
        assertFalse(guard.isUploadAllowed("uidA")) // старый владелец больше не может
    }

    @Test
    fun `clear resets owner and block`() {
        guard.setOwner("uidA")
        guard.beginAccountSwitch()
        guard.clear()
        assertNull(guard.ownerUid())
        assertFalse(guard.isUploadBlocked())
    }

    @Test
    fun `abortAccountSwitch unblocks when NOT previously blocked (failed login)`() {
        // Сценарий: гость жмёт вход, барьер ставится ДО await, вход ПАДАЕТ.
        val wasBlockedBefore = guard.isUploadBlocked() // false у гостя
        guard.beginAccountSwitch()
        guard.abortAccountSwitch(wasBlockedBefore)
        assertFalse(guard.isUploadBlocked()) // разблокировали — бэкап гостя жив
    }

    @Test
    fun `abortAccountSwitch keeps block when a switch was ALREADY in progress`() {
        // Незавершённый switch (download прошлого раза не добежал) — блок держим.
        guard.beginAccountSwitch()               // предыдущий незавершённый switch
        val wasBlockedBefore = guard.isUploadBlocked() // true
        guard.beginAccountSwitch()               // новая попытка входа
        guard.abortAccountSwitch(wasBlockedBefore)
        assertTrue(guard.isUploadBlocked())      // НЕ открыли легитимный блок
    }

    @Test
    fun `guest to new account — owner==null path keeps uploads allowed then claims`() {
        // Гость (owner==null) регистрирует НОВЫЙ аккаунт: аплоад разрешён,
        // completeAccountSwitch забирает владение — прогресс уходит в аккаунт.
        assertNull(guard.ownerUid())
        assertTrue(guard.isUploadAllowed("uidNew"))
        guard.completeAccountSwitch("uidNew")
        assertEquals("uidNew", guard.ownerUid())
        assertTrue(guard.isUploadAllowed("uidNew"))
    }

    @Test
    fun `shared device — B logging in while A owns is blocked until switch completes`() {
        guard.setOwner("uidA")                 // юзер A владеет локальными данными
        assertFalse(guard.isUploadAllowed("uidB")) // B не может выгружать данные A
        guard.beginAccountSwitch()             // B входит → барьер
        assertFalse(guard.isUploadAllowed("uidB"))
        guard.completeAccountSwitch("uidB")    // wipe + download прошли
        assertTrue(guard.isUploadAllowed("uidB"))
        assertFalse(guard.isUploadAllowed("uidA"))
    }
}
