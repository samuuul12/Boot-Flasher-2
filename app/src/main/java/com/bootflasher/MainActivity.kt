package com.bootflasher

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.bootflasher.data.ImageInfo
import com.bootflasher.root.RootManager
import com.bootflasher.root.PartitionManager
import com.bootflasher.root.PartitionInfo
import com.bootflasher.flashing.ImageFlasher
import com.bootflasher.ui.home.HomeScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var rootAvailable by remember { mutableStateOf<Boolean?>(null) }
            var partition by remember { mutableStateOf<PartitionInfo?>(null) }
            var image by remember { mutableStateOf<ImageInfo?>(null) }

            var checking by remember { mutableStateOf(false) }
            var flashing by remember { mutableStateOf(false) }
            var flashMessage by remember { mutableStateOf<String?>(null) }

            val imagePicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri: Uri? ->

                uri ?: return@rememberLauncherForActivityResult

                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }

                val selected = ImageInfo.fromUri(this, uri)

                if (selected == null) {
                    Toast.makeText(
                        this,
                        "Selecione um arquivo com extensão .img",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    image = selected
                    flashMessage = null
                }
            }

            MaterialTheme {
                Surface {
                    HomeScreen(
                        rootAvailable = rootAvailable,
                        partition = partition,
                        image = image,
                        checking = checking,
                        flashing = flashing,
                        flashMessage = flashMessage,

                        onCheck = {
                            lifecycleScope.launch {
                                checking = true

                                try {
                                    rootAvailable = RootManager.isRootAvailable()

                                    partition = if (rootAvailable == true) {
                                        PartitionManager.inspectBoot()
                                    } else {
                                        null
                                    }
                                } finally {
                                    checking = false
                                }
                            }
                        },

                        onSelectImage = {
                            imagePicker.launch(arrayOf("*/*"))
                        },

                        onFlash = {
                            val selectedImage = image ?: return@HomeScreen

                            lifecycleScope.launch {
                                flashing = true
                                flashMessage = "Preparando gravação..."

                                try {
                                    val result = ImageFlasher.flash(
                                        this@MainActivity,
                                        selectedImage.uri
                                    )

                                    flashMessage = buildString {
                                        append(result.message)

                                        if (result.output.isNotBlank()) {
                                            append("\n\n")
                                            append(result.output)
                                        }
                                    }

                                    if (result.success) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Gravação concluída!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } finally {
                                    flashing = false
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
