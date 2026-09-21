package com.pogotcghelper.app.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pogotcghelper.app.R
import com.pogotcghelper.app.domain.model.Card
import com.pogotcghelper.app.ocr.recognizeCardText
import com.pogotcghelper.app.ui.common.CardArtwork
import java.util.concurrent.Executors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(viewModel: ScanViewModel, onCardClick: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_scan)) }) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                !hasCameraPermission -> CameraPermissionRequest(onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) })
                uiState.phase == ScanPhase.CAMERA || uiState.phase == ScanPhase.RECOGNIZING -> CameraCaptureView(
                    isRecognizing = uiState.phase == ScanPhase.RECOGNIZING,
                    onCapturing = viewModel::onCapturing,
                    onCaptured = viewModel::onTextRecognized,
                )
                uiState.phase == ScanPhase.NO_TEXT_FOUND -> NoTextFoundState(onRetake = viewModel::retake)
                else -> ReviewAndResults(uiState = uiState, viewModel = viewModel, onCardClick = onCardClick)
            }
        }
    }
}

@Composable
private fun CameraPermissionRequest(onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.scan_permission_rationale), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequest) { Text(stringResource(R.string.scan_grant_permission)) }
    }
}

/**
 * Live camera preview with a shutter button. CameraX binds [ImageCapture] alongside the
 * preview so tapping the shutter grabs one full-resolution frame -- fed to on-device OCR
 * rather than continuously analyzing every preview frame, which keeps this simple and
 * avoids fighting over a live-updating guess while the user is still framing the card.
 */
@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraCaptureView(
    isRecognizing: Boolean,
    onCapturing: () -> Unit,
    onCaptured: (List<String>) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val scope = rememberCoroutineScope()
    val executor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(executor) { onDispose { executor.shutdown() } }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build()
                            .also { it.setSurfaceProvider(previewView.surfaceProvider) }
                        runCatching {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture,
                            )
                        }
                    },
                    ContextCompat.getMainExecutor(ctx),
                )
                previewView
            },
        )

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.scan_instructions),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            if (isRecognizing) {
                CircularProgressIndicator(color = Color.White)
            } else {
                FloatingActionButton(
                    onClick = {
                        onCapturing()
                        imageCapture.takePicture(
                            executor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                @OptIn(ExperimentalGetImage::class)
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    scope.launch { onCaptured(recognizeCardText(image)) }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    onCaptured(emptyList())
                                }
                            },
                        )
                    }
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = stringResource(R.string.scan_capture))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewAndResults(uiState: ScanUiState, viewModel: ScanViewModel, onCardClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            singleLine = true,
            label = { Text(stringResource(R.string.scan_review_title)) },
            modifier = Modifier.fillMaxWidth(),
        )

        if (uiState.recognizedLines.size > 1) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                items(uiState.recognizedLines) { line ->
                    AssistChip(onClick = { viewModel.onQueryChange(line) }, label = { Text(line) })
                }
            }
        }

        Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::search, enabled = uiState.query.isNotBlank()) {
                Text(stringResource(R.string.scan_search))
            }
            TextButton(onClick = viewModel::retake) { Text(stringResource(R.string.scan_retake)) }
        }

        uiState.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        when (uiState.phase) {
            ScanPhase.SEARCHING -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            ScanPhase.RESULTS -> if (uiState.results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.search_no_results))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.results, key = { it.id }) { card ->
                        ScanResultItem(card = card, onClick = { onCardClick(card.id) })
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ScanResultItem(card: Card, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        CardArtwork(
            imageUrl = card.smallImageUrl,
            contentDescription = card.name,
            modifier = Modifier.fillMaxWidth().aspectRatio(0.72f),
        )
        Text(text = card.name, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
        card.setName?.let { Text(text = it, style = MaterialTheme.typography.labelMedium) }
    }
}

@Composable
private fun NoTextFoundState(onRetake: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.scan_no_text_found), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetake) { Text(stringResource(R.string.scan_retake)) }
    }
}
