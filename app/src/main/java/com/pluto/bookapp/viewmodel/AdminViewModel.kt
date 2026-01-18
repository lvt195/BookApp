package com.pluto.bookapp.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pluto.bookapp.model.ModelCategory
import com.pluto.bookapp.model.ModelPdf
import com.pluto.bookapp.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    val categories: StateFlow<List<ModelCategory>> = bookRepository.getCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState

    fun addCategory(category: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = bookRepository.addCategory(category)
            if (result.isSuccess) {
                _operationState.value = OperationState.Success("Category added successfully")
            } else {
                _operationState.value = OperationState.Error(result.exceptionOrNull()?.message ?: "Failed to add category")
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            val result = bookRepository.deleteCategory(categoryId)
            if (result.isFailure) {
                _operationState.value = OperationState.Error(result.exceptionOrNull()?.message ?: "Failed to delete category")
            }
        }
    }

    fun uploadPdf(pdfUri: Uri, title: String, description: String, categoryId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = bookRepository.uploadPdf(pdfUri, title, description, categoryId)
            if (result.isSuccess) {
                _operationState.value = OperationState.Success("PDF uploaded successfully")
            } else {
                _operationState.value = OperationState.Error(result.exceptionOrNull()?.message ?: "Failed to upload PDF")
            }
        }
    }

    fun deleteBook(bookId: String, bookUrl: String) {
        viewModelScope.launch {
             val result = bookRepository.deleteBook(bookId, bookUrl)
             if (result.isFailure) {
                 _operationState.value = OperationState.Error(result.exceptionOrNull()?.message ?: "Failed to delete book")
             }
        }
    }
    
    fun resetState() {
        _operationState.value = OperationState.Idle
    }
}

sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    data class Success(val message: String) : OperationState()
    data class Error(val message: String) : OperationState()
}
