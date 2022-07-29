package com.example.filedownloaddemoapp.viewmodel

import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filedownloaddemoapp.data.api.FileDownloadApi
import com.example.filedownloaddemoapp.data.networking.createRetrofitApi
import com.example.filedownloaddemoapp.presentation.model.FileDownloadScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File

class FileDownloadScreenViewModel : ViewModel() {
    private val api: FileDownloadApi = createRetrofitApi()
    var state by mutableStateOf<FileDownloadScreenState>(FileDownloadScreenState.Idle)
        private set

    fun downloadFile() {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            api.downloadZipFile()
                .saveFile(timestamp.toString())
                .collect { downloadState ->
                    state = when (downloadState) {
                        is DownloadState.Downloading -> {
                            FileDownloadScreenState.Downloading(progress = downloadState.progress)
                        }
                        is DownloadState.Failed -> {
                            FileDownloadScreenState.Failed(error = downloadState.error)
                        }
                        DownloadState.Finished -> {
                            FileDownloadScreenState.Downloaded
                        }
                    }
                }
        }
    }

    fun onIdleRequested() {
        state = FileDownloadScreenState.Idle
    }

    private sealed class DownloadState {
        data class Downloading(val progress: Int) : DownloadState()
        object Finished : DownloadState()
        data class Failed(val error: Throwable? = null) : DownloadState()
    }

    /*private fun ResponseBody.saveFileSimple(filePostfix: String) {
        val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadFolder.absolutePath, "file_${filePostfix}.zip")
        byteStream().use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }*/

    private fun ResponseBody.saveFile(filePostfix: String): Flow<DownloadState> {
        return flow {
            emit(DownloadState.Downloading(0))
            val downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val destinationFile = File(downloadFolder.absolutePath, "file_${filePostfix}.zip")

            try {
                byteStream().use { inputStream ->
                    destinationFile.outputStream().use { outputStream ->
                        val totalBytes = contentLength()
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var progressBytes = 0L

                        var bytes = inputStream.read(buffer)
                        while (bytes >= 0) {
                            outputStream.write(buffer, 0, bytes)
                            progressBytes += bytes
                            bytes = inputStream.read(buffer)
                            emit(DownloadState.Downloading(((progressBytes * 100) / totalBytes).toInt()))
                        }
                    }
                }
                emit(DownloadState.Finished)
            } catch (e: Exception) {
                emit(DownloadState.Failed(e))
            }
        }
            .flowOn(Dispatchers.IO)
            .distinctUntilChanged()
    }
}
