package com.pluto.bookapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pluto.bookapp.model.ModelComment
import com.pluto.bookapp.repository.BookRepository
import com.pluto.bookapp.repository.CommentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PdfDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite
    
    // We need to fetch book details if we don't pass the full object. 
    // Usually only ID is passed.
    // BookRepository.getAllBooks gives a list. We need getBookDetails(id).
    // For now, let's assume we pass the book object or fetch it.
    // Since Repo doesn't have getBook(id), we might need to add it or just pass details.
    // However, for clean architecture, we should fetch by ID. 
    // I will add getBookDetails to Repo or just use existing "All Books" and filter? No, inefficient.
    // I will check if I can just pass book data via Navigation arguments for simplicity, 
    // but ideally we should fetch fresh data (view count etc).
    // Let's add getBookDetails to Repository later or now.
    // For now, I will implementation checkFavorite and Comments.

    private val _comments = MutableStateFlow<List<ModelComment>>(emptyList())
    val comments: StateFlow<List<ModelComment>> = _comments
    
    fun loadBookData(bookId: String) {
        viewModelScope.launch {
            bookRepository.isFavorite(bookId).collect {
                _isFavorite.value = it
            }
        }
         viewModelScope.launch {
            commentRepository.getComments(bookId).collect {
                _comments.value = it
            }
        }
        
        // Asynchronously increment view count
        viewModelScope.launch {
            bookRepository.incrementViewCount(bookId)
        }
    }

    fun toggleFavorite(bookId: String) {
        viewModelScope.launch {
            if (_isFavorite.value) {
                bookRepository.removeFromFavorites(bookId)
            } else {
                bookRepository.addToFavorites(bookId)
            }
        }
    }

    fun addComment(bookId: String, comment: String) {
        viewModelScope.launch {
            commentRepository.addComment(bookId, comment)
        }
    }

    // View State Management
    sealed class ViewState {
        object Idle : ViewState()
        object Loading : ViewState() // Initial check
        data class Downloading(val progress: Int) : ViewState()
        data class Ready(val file: java.io.File) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Idle)
    val viewState: StateFlow<ViewState> = _viewState

    fun resetViewState() {
        _viewState.value = ViewState.Idle
    }

    fun openBook(bookId: String, bookUrl: String) {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading
            bookRepository.downloadBookToFile(bookUrl, bookId).collect { status ->
                when (status) {
                    is BookRepository.DownloadStatus.Progress -> {
                        _viewState.value = ViewState.Downloading(status.percentage)
                    }
                    is BookRepository.DownloadStatus.Success -> {
                        _viewState.value = ViewState.Ready(status.file)
                    }
                    is BookRepository.DownloadStatus.Error -> {
                        _viewState.value = ViewState.Error(status.message)
                    }
                }
            }
        }
    }

    fun downloadBook(bookUrl: String, bookTitle: String, bookId: String) {
        viewModelScope.launch {
            bookRepository.downloadBook(bookUrl, bookTitle, bookId)
        }
    }
}
