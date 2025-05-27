package com.storycanvas.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storycanvas.app.data.model.ArticleFeedItem
import com.storycanvas.app.data.repository.ArticleRepository
import com.storycanvas.app.data.repository.ArticleRepositoryImpl
import com.storycanvas.app.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Feed Screen
 */
class FeedViewModel(
    private val articleRepository: ArticleRepository = ArticleRepositoryImpl()
) : ViewModel() {

    // UI state representing the current state of the feed
    data class FeedUiState(
        val articles: List<ArticleFeedItem> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    // Private mutable state flow
    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    
    // Public immutable state flow exposed to the UI
    val uiState: StateFlow<FeedUiState> = _uiState

    // Current page for pagination
    private var currentPage = 0
    private val pageSize = 10

    init {
        loadInitialArticles()
    }

    /**
     * Load the initial set of articles when the ViewModel is created
     */
    fun loadInitialArticles() {
        currentPage = 0
        loadArticles(refresh = true)
    }

    /**
     * Load more articles (for pagination)
     */
    fun loadMoreArticles() {
        currentPage++
        loadArticles(refresh = false)
    }

    /**
     * Reload the current articles (e.g., on pull-to-refresh)
     */
    fun refreshArticles() {
        currentPage = 0
        loadArticles(refresh = true)
    }

    /**
     * Load articles from the repository
     * @param refresh If true, replace the current list; otherwise append to it
     */
    private fun loadArticles(refresh: Boolean) {
        viewModelScope.launch {
            // Show loading state
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            // Fetch articles from repository
            when (val result = articleRepository.getArticlesForFeed(currentPage, pageSize)) {
                is Result.Success -> {
                    val newArticles = result.data
                    _uiState.value = _uiState.value.copy(
                        articles = if (refresh) newArticles else _uiState.value.articles + newArticles,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> {
                    // This state is handled at the beginning of this block
                }
            }
        }
    }
} 