package com.pluto.bookapp.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pluto.bookapp.viewmodel.AdminViewModel
import com.pluto.bookapp.viewmodel.OperationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAddScreen(
    viewModel: AdminViewModel,
    onBackClick: () -> Unit
) {
    var category by remember { mutableStateOf("") }
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()

    LaunchedEffect(operationState) {
        if (operationState is OperationState.Success) {
            onBackClick() // Go back after success
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Category") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.addCategory(category) },
                modifier = Modifier.fillMaxWidth(),
                enabled = category.isNotEmpty() && operationState !is OperationState.Loading
            ) {
                 if (operationState is OperationState.Loading) {
                     CircularProgressIndicator(modifier = Modifier.size(24.dp))
                 } else {
                     Text("Submit")
                 }
            }
            
             if (operationState is OperationState.Error) {
                 Text(
                    text = (operationState as OperationState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
