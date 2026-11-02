package com.bootflasher.root

data class PartitionInfo(
    val exists: Boolean,
    val isBlockDevice: Boolean,
    val path: String,
    val realPath: String = "",
    val size: Long = 0L
)

object PartitionManager {

    const val BOOT_PARTITION = "/dev/block/by-name/boot"

    suspend fun inspectBoot(): PartitionInfo {
        val result = RootManager.execute(
            """
            if [ ! -e '$BOOT_PARTITION' ]; then
                echo MISSING
                exit 1
            fi

            if [ ! -b '$BOOT_PARTITION' ]; then
                echo NOT_BLOCK
                exit 1
            fi

            echo BLOCK
            readlink -f '$BOOT_PARTITION'
            blockdev --getsize64 '$BOOT_PARTITION'
            """.trimIndent()
        )

        if (!result.success) {
            return PartitionInfo(
                exists = result.output.contains("NOT_BLOCK"),
                isBlockDevice = false,
                path = BOOT_PARTITION
            )
        }

        val lines = result.output.lines()

        return PartitionInfo(
            exists = true,
            isBlockDevice = lines.firstOrNull() == "BLOCK",
            path = BOOT_PARTITION,
            realPath = lines.getOrNull(1) ?: "",
            size = lines.getOrNull(2)?.toLongOrNull() ?: 0L
        )
    }
}
