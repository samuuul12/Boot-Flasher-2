package com.bootflasher.flashing

import android.content.Context
import android.net.Uri
import com.bootflasher.root.RootManager
import com.bootflasher.root.PartitionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class FlashResult(
    val success: Boolean,
    val message: String,
    val output: String = ""
)

object ImageFlasher {

    suspend fun flash(
        context: Context,
        uri: Uri
    ): FlashResult = withContext(Dispatchers.IO) {

        var tempFile: File? = null

        try {
            if (!RootManager.isRootAvailable()) {
                return@withContext FlashResult(
                    false,
                    "Acesso root não disponível."
                )
            }

            val partition = PartitionManager.inspectBoot()

            if (!partition.exists || !partition.isBlockDevice) {
                return@withContext FlashResult(
                    false,
                    "Partição boot inválida."
                )
            }

            tempFile = File(context.cacheDir, "boot_flashing.img")

            val input = context.contentResolver.openInputStream(uri)
                ?: return@withContext FlashResult(
                    false,
                    "Não foi possível abrir a imagem."
                )

            var imageSize = 0L

            input.use { source ->
                FileOutputStream(tempFile).use { destination ->
                    val buffer = ByteArray(1024 * 1024)

                    while (true) {
                        val count = source.read(buffer)
                        if (count == -1) break

                        destination.write(buffer, 0, count)
                        imageSize += count
                    }

                    destination.fd.sync()
                }
            }

            if (imageSize <= 0) {
                return@withContext FlashResult(
                    false,
                    "Imagem vazia."
                )
            }

            if (imageSize > partition.size) {
                return@withContext FlashResult(
                    false,
                    "Imagem maior que a partição boot."
                )
            }

            val imagePath = tempFile.absolutePath
                .replace("'", "'\\''")

            val command = """
                dd if='$imagePath' of='${PartitionManager.BOOT_PARTITION}' bs=4M &&
                sync
            """.trimIndent()

            val result = RootManager.execute(command)

            if (!result.success) {
                return@withContext FlashResult(
                    false,
                    "Falha durante a gravação.",
                    result.error.ifBlank { result.output }
                )
            }

            FlashResult(
                true,
                "Imagem gravada com sucesso!",
                result.output
            )

        } catch (e: Exception) {
            FlashResult(
                false,
                "Erro: ${e.message ?: "Falha desconhecida"}"
            )
        } finally {
            tempFile?.delete()
        }
    }
}
