package com.pluto.bookapp.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pluto.bookapp.model.ModelCategory
import com.pluto.bookapp.viewmodel.AdminViewModel
import com.pluto.bookapp.viewmodel.OperationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfAddScreen(
    viewModel: AdminViewModel,
    onBackClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ModelCategory?>(null) }
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()
    
    // Filter out static categories "All", "Most Viewed", etc. if present in repository? 
    // Wait, getCategories in Repo ADDS static categories. For Upload, we only want REAL categories.
    // We should filter them out here or fix repo. 
    // Repo adds ID "01", "02", "03". Real categories have timestamp IDs.
    val realCategories = categories.filter { it.id != "01" && it.id != "02" && it.id != "03" }

    LaunchedEffect(operationState) {
        if (operationState is OperationState.Success) {
            onBackClick()
            viewModel.resetState()
        }
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        pdfUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add PDF") },
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
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Category Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory?.category ?: "Pick Category",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                         Icon(Icons.Default.ArrowDropDown, "contentDescription", 
                             Modifier.clickable { expanded = !expanded }) 
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                     modifier = Modifier.fillMaxWidth()
                ) {
                    realCategories.forEach { category ->
                         DropdownMenuItem(
                            text = { Text(category.category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { launcher.launch("application/pdf") }) {
                Text(text = if (pdfUri == null) "Pick PDF" else "PDF Selected")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    if (pdfUri != null && title.isNotEmpty() && selectedCategory != null) {
                        viewModel.uploadPdf(pdfUri!!, title, description, selectedCategory!!.id) 
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = pdfUri != null && title.isNotEmpty() && selectedCategory != null && operationState !is OperationState.Loading
            ) {
                 if (operationState is OperationState.Loading) {
                     CircularProgressIndicator(modifier = Modifier.size(24.dp))
                 } else {
                     Text("Upload Book")
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
