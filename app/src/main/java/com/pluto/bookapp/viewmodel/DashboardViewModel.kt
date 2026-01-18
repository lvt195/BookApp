package com.pluto.bookapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pluto.bookapp.model.ModelCategory
import com.pluto.bookapp.model.ModelPdf
import com.pluto.bookapp.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    // Categories are loaded once
    val categories: StateFlow<List<ModelCategory>> = bookRepository.getCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily, // Keep data alive as long as VM is alive
            initialValue = emptyList()
        )


    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun getBooksForCategory(category: ModelCategory): StateFlow<List<ModelPdf>> {
        val flow = when (category.category) {
            "All" -> bookRepository.getAllBooks()
            "Most Viewed" -> bookRepository.getMostViewedBooks()
            "Most Downloaded" -> bookRepository.getMostDownloadedBooks()
            else -> bookRepository.getBooksByCategory(category.id)
        }
        
        return combine(flow, _searchQuery) { books, query ->
            if (query.isBlank()) {
                books
            } else {
                books.filter { 
                    it.title.contains(query, ignoreCase = true) 
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )
    }
}
