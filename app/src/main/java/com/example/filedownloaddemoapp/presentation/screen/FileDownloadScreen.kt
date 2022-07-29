package com.example.filedownloaddemoapp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.filedownloaddemoapp.presentation.model.FileDownloadScreenState
import com.example.filedownloaddemoapp.viewmodel.FileDownloadScreenViewModel

@Composable
fun FileDownloadScreen() {
    val viewModel: FileDownloadScreenViewModel = viewModel()

    DownloadFilesScreenLayout(
        state = viewModel.state,
        onStartDownloadClicked = viewModel::downloadFile,
        onBackToIdleRequested = viewModel::onIdleRequested,
    )
}

@Composable
private fun DownloadFilesScreenLayout(
    state: FileDownloadScreenState,
    onStartDownloadClicked: () -> Unit,
    onBackToIdleRequested: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface)
            .wrapContentSize(Alignment.Center)
            .padding(16.dp)
    ) {
        when (state) {
            FileDownloadScreenState.Idle -> {
                Text(
                    text = "Click to download a 50MB file.",
                    style = MaterialTheme.typography.h6,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onStartDownloadClicked) {
                    Text(
                        text = "Download",
                        style = MaterialTheme.typography.button,
                    )
                }
            }
            is FileDownloadScreenState.Downloading -> {
                DownloadFilesWithProgressLayout(progress = state.progress)
            }
            is FileDownloadScreenState.Failed -> {
                Text(
                    text = "Download failed",
                    style = MaterialTheme.typography.h6,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBackToIdleRequested) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.button,
                    )
                }
            }
            FileDownloadScreenState.Downloaded -> {
                Text(
                    text = "Download succeeded",
                    style = MaterialTheme.typography.h6,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBackToIdleRequested) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.button,
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadFilesWithProgressLayout(progress: Int) {
    Text(
        text = "Downloaded $progress%",
        style = MaterialTheme.typography.h6,
    )
    Spacer(modifier = Modifier.height(8.dp))
    LinearProgressIndicator(
        progress = progress.toFloat() / 100f,
        modifier = Modifier.fillMaxWidth(),
    )
}
