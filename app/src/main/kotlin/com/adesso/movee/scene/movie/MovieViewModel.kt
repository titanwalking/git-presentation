package com.adesso.movee.scene.movie

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.adesso.movee.R
import com.adesso.movee.base.BaseAndroidViewModel
import com.adesso.movee.domain.FetchNowPlayingMoviesUseCase
import com.adesso.movee.domain.FetchPopularMoviesUseCase
import com.adesso.movee.internal.util.AppBarStateChangeListener
import com.adesso.movee.internal.util.AppBarStateChangeListener.State.COLLAPSED
import com.adesso.movee.internal.util.AppBarStateChangeListener.State.EXPANDED
import com.adesso.movee.internal.util.AppBarStateChangeListener.State.IDLE
import com.adesso.movee.internal.util.UseCase
import com.adesso.movee.uimodel.MovieUiModel
import com.adesso.movee.uimodel.ShowHeaderUiModel
import com.adesso.movee.uimodel.ShowUiModel
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class MovieViewModel @Inject constructor(
    private val fetchPopularMoviesUseCase: FetchPopularMoviesUseCase,
    private val fetchNowPlayingMoviesUseCase: FetchNowPlayingMoviesUseCase,
    application: Application
) : BaseAndroidViewModel(application) {

    private val _popularMovies = MutableStateFlow<List<MovieUiModel>?>(null)
    private val _toolbarTitle = MutableStateFlow<String?>(null)
    private val _toolbarSubtitle = MutableStateFlow(getString(R.string.movie_message_popular))
    private val _nowPlayingMovies = MutableStateFlow<List<MovieUiModel>?>(null)
    val popularMovies: StateFlow<List<MovieUiModel>?> get() = _popularMovies

    val showHeader = combine(
        _toolbarTitle,
        _toolbarSubtitle,
        _nowPlayingMovies
    ) { title, subtitle, nowPlayingShows ->
        ShowHeaderUiModel(
            title,
            subtitle,
            nowPlayingShows
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        fetchPopularMovies()
        fetchNowPlayingMovies()
    }

    private fun fetchNowPlayingMovies() {
        bgScope.launch {
            val nowPlayingMoviesResult = fetchNowPlayingMoviesUseCase.run(UseCase.None)

            onUIThread {
                nowPlayingMoviesResult
                    .onSuccess(::postNowPlayingMovieList)
                    .onFailure(::handleFailure)
            }
        }
    }

    private fun postNowPlayingMovieList(movies: List<MovieUiModel>) {
        _nowPlayingMovies.value = movies
    }

    private fun fetchPopularMovies() {
        bgScope.launch {
            val popularMoviesResult = fetchPopularMoviesUseCase.run(UseCase.None)

            onUIThread {
                popularMoviesResult
                    .onSuccess(::postPopularMovieList)
                    .onFailure(::handleFailure)
            }
        }
    }

    private fun postPopularMovieList(movies: List<MovieUiModel>) {
        _popularMovies.value = movies
    }

    private fun postToolbarTitle(@StringRes titleRes: Int) {
        _toolbarTitle.value = getString(titleRes)
    }

    fun appbarStateChanged(state: AppBarStateChangeListener.State) {
        when (state) {
            COLLAPSED -> postToolbarTitle(R.string.movie_message_popular_movies)
            EXPANDED, IDLE -> postToolbarTitle(R.string.movie_message_movies)
        }
    }

    fun onPopularMovieClick(movie: MovieUiModel) {
        navigateMovieDetailFragment(movie)
    }

    fun onNowPlayingShowClick(show: ShowUiModel) {
        navigateMovieDetailFragment(show)
    }

    private fun navigateMovieDetailFragment(show: ShowUiModel) {
        navigate(MovieFragmentDirections.toMovieDetail(show.id))
    }
}
