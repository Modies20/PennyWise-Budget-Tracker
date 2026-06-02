package com.pennywise.budgettracker

import com.pennywise.budgettracker.utils.PasswordHasher
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun passwordHasher_returnsSameHashForSameInput() {
        val hash1 = PasswordHasher.hashPassword("test123")
        val hash2 = PasswordHasher.hashPassword("test123")
        assertEquals(hash1, hash2)
    }
}
