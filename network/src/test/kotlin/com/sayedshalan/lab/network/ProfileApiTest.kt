package com.sayedshalan.lab.network

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileApiTest {
    @Test
    fun readsAProfileFromMockWebServer() =
        runTest {
            val server = MockWebServer()
            server.enqueue(
                MockResponse
                    .Builder()
                    .body("""{"name":"Ada"}""")
                    .addHeader("Content-Type", "application/json")
                    .build(),
            )
            server.start()
            try {
                val api = profileApi(server.url("/").toString())
                val profile = api.profile()
                assertEquals("Ada", profile.name)
                assertEquals("/profile", server.takeRequest().target)
            } finally {
                server.close()
            }
        }
}
