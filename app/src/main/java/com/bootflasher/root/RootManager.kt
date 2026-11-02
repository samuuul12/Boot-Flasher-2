package com.bootflasher.root

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RootResult(
    val success: Boolean,
    val output: String,
    val error: String = ""
)

object RootManager {

    suspend fun execute(command: String): RootResult =
        withContext(Dispatchers.IO) {
            try {
                val process = ProcessBuilder("su", "-c", command).start()
                val output = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                RootResult(exitCode == 0, output.trim(), error.trim())
            } catch (e: Exception) {
                RootResult(false, "", e.message ?: "Erro root")
            }
        }

    suspend fun isRootAvailable(): Boolean {
        val result = execute("id -u")
        return result.success && result.output == "0"
    }
}
