package com.blundell.tut.gemma3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun MainScreen(viewModel: MainViewModel) {
    val mainState by viewModel.mainState.collectAsStateWithLifecycle()
    when (val state = mainState) {
        is MainState.Error -> {
            Text("Something went wrong, check LogCat.")
        }
        is MainState.Idle -> {
            Text("Hello World")
        }
        is MainState.LoadedModel -> {
            val scrollableState = rememberScrollState()
            Column(
                modifier = Modifier
                    .verticalScroll(scrollableState)
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                val latestResponse = state.latestResponse
                if (latestResponse.isNotEmpty()) {
                    Text("Latest response: ")
                    Text(latestResponse)
                }

                var text by remember { mutableStateOf("") }
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )
                Spacer(
                    modifier = Modifier
                        .padding(8.dp)
                )
                Text("Enter a query")
                TextField(
                    value = text,
                    onValueChange = { newText -> text = newText },
                    label = { Text("Enter text") },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(
                    modifier = Modifier
                        .padding(4.dp)
                )
                Button(
                    onClick = { viewModel.sendQuery(text) },
                    enabled = !state.responding,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text("Send")
                }
            }
        }
        is MainState.LoadingModel -> {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                Text("Loading!")
                CircularProgressIndicator()
            }
        }
    }
}
