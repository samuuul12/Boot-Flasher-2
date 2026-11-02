package com.bootflasher.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bootflasher.data.ImageInfo
import com.bootflasher.root.PartitionInfo

private val BootBackground = Color(0xFF0B1220)
private val BootSurface = Color(0xFF121D2E)
private val BootPrimary = Color(0xFF00D4FF)
private val BootSecondary = Color(0xFF8B9BB4)

private val BootDarkColors = darkColorScheme(
    primary = BootPrimary,
    onPrimary = Color(0xFF001820),
    secondary = Color(0xFF64B5F6),
    onSecondary = Color(0xFF001B2E),
    background = BootBackground,
    onBackground = Color(0xFFE8F0FA),
    surface = BootSurface,
    onSurface = Color(0xFFE8F0FA),
    surfaceVariant = Color(0xFF1D2A3D),
    onSurfaceVariant = BootSecondary,
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF310000)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    rootAvailable: Boolean?,
    partition: PartitionInfo?,
    image: ImageInfo?,
    checking: Boolean,
    flashing: Boolean,
    flashMessage: String?,
    onCheck: () -> Unit,
    onSelectImage: () -> Unit,
    onFlash: () -> Unit
) {
    var showConfirmation by remember { mutableStateOf(false) }

    val canFlash = rootAvailable == true &&
        partition?.isBlockDevice == true &&
        image != null &&
        !flashing

    MaterialTheme(colorScheme = BootDarkColors) {

        if (showConfirmation) {
            AlertDialog(
                onDismissRequest = { showConfirmation = false },
                title = { Text("Confirmar gravação") },
                text = {
                    Text(
                        "A imagem ${image?.name} será gravada diretamente " +
                            "na partição /dev/block/by-name/boot.\n\n" +
                            "Deseja continuar?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmation = false
                            onFlash()
                        }
                    ) {
                        Text("Gravar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showConfirmation = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Scaffold(
            containerColor = BootBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Boot Flasher",
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BootBackground
                    )
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                Text(
                    "Gravador de imagens",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Text(
                    "Gerencie imagens de inicialização do dispositivo.",
                    color = BootSecondary
                )

                // Root
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BootSurface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Acesso root",
                            style = MaterialTheme.typography.titleMedium
                        )

                        when (rootAvailable) {
                            true -> Text(
                                "Root disponível",
                                color = BootPrimary
                            )

                            false -> Text(
                                "Root não disponível",
                                color = MaterialTheme.colorScheme.error
                            )

                            null -> Text(
                                "Não verificado",
                                color = BootSecondary
                            )
                        }
                    }
                }

                // Partition
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BootSurface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Partição de destino",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            "/dev/block/by-name/boot",
                            color = BootSecondary
                        )

                        when {
                            partition == null -> Text(
                                "Partição ainda não verificada",
                                color = BootSecondary
                            )

                            partition.isBlockDevice -> {
                                Text(
                                    "Partição encontrada",
                                    color = BootPrimary
                                )

                                Text("Dispositivo: ${partition.realPath}")
                                Text("Tamanho: ${formatSize(partition.size)}")
                            }

                            else -> Text(
                                "Partição não encontrada ou inválida",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Button(
                            onClick = onCheck,
                            enabled = !checking && !flashing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (checking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Verificar partição")
                            }
                        }
                    }
                }

                // Image
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BootSurface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Imagem de boot",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (image != null) {
                            Text(image.name)

                            Text(
                                formatSize(image.size),
                                color = BootSecondary
                            )
                        } else {
                            Text(
                                "Nenhuma imagem selecionada",
                                color = BootSecondary
                            )
                        }

                        OutlinedButton(
                            onClick = onSelectImage,
                            enabled = !flashing,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Selecionar arquivo .img")
                        }
                    }
                }

                // Flash result
                if (flashMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BootSurface
                        )
                    ) {
                        Text(
                            text = flashMessage,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Flash button
                Button(
                    onClick = { showConfirmation = true },
                    enabled = canFlash,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BootPrimary,
                        contentColor = Color(0xFF001820),
                        disabledContainerColor = Color(0xFF263548),
                        disabledContentColor = Color(0xFF8493A8)
                    )
                ) {
                    if (flashing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text("Gravando imagem...")
                    } else {
                        Text("Gravar imagem")
                    }
                }

                Text(
                    "Boot Flasher • Versão 0.2.0",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = BootSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "Desconhecido"

    val mb = bytes / 1024.0 / 1024.0

    return if (mb >= 1024) {
        "%.2f GB".format(mb / 1024)
    } else {
        "%.2f MB".format(mb)
    }
}
