package com.rohan.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.work.*
import coil.compose.rememberImagePainter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(
                        NetworkType.CONNECTED
                    )
                    .build()
            )
            .build()
        val workManager = WorkManager.getInstance(applicationContext)

        setContent {
            val workInfos = workManager
                .getWorkInfosForUniqueWorkLiveData("download")
                .observeAsState()
                .value
            val downloadInfo = remember(key1 = workInfos) {
                workInfos?.find { it.id == downloadRequest.id }
            }
            val imageUri by derivedStateOf {
                downloadInfo?.outputData?.getString(WorkerKeys.IMAGE_URI)
                    ?.toUri()
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                imageUri?.let { uri ->
                    Image(
                        painter = rememberImagePainter(
                            data = uri
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Button(
                    onClick = {
                        workManager
                            .beginUniqueWork(
                                "download",
                                ExistingWorkPolicy.KEEP,
                                downloadRequest
                            )
                            .enqueue()
                    },
                    enabled = downloadInfo?.state != WorkInfo.State.RUNNING
                ) {
                    Text(text = "Start download")
                }
                Spacer(modifier = Modifier.height(8.dp))
                when (downloadInfo?.state) {
                    WorkInfo.State.RUNNING -> Text("Downloading...")
                    WorkInfo.State.SUCCEEDED -> Text("Download succeeded")
                    WorkInfo.State.FAILED -> Text("Download failed")
                    WorkInfo.State.CANCELLED -> Text("Download cancelled")
                    WorkInfo.State.ENQUEUED -> Text("Download enqueued")
                    WorkInfo.State.BLOCKED -> Text("Download blocked")
                    else -> null
                }
            }
        }
    }
}