package com.example.ui

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.local.RadioDatabase
import com.example.data.model.RadioStation
import com.example.data.repository.RadioRepository
import com.example.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RadioViewModel(context: Context) : ViewModel() {

    private val database = RadioDatabase.getDatabase(context)
    private val repository = RadioRepository(database.radioDao())

    // Base flow of all radio stations from repository (synced with room favorites)
    val stations: StateFlow<List<RadioStation>> = repository.stationsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Filtered Stations calculated on the fly reactively
    val filteredStations: StateFlow<List<RadioStation>> = combine(
        stations,
        _searchQuery,
        _selectedCategory
    ) { stationList, query, cat ->
        stationList.filter { station ->
            val matchesQuery = station.name.contains(query, ignoreCase = true) ||
                    station.country.contains(query, ignoreCase = true)
            val matchesCategory = if (cat == "الكل") true else station.category == cat
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Player state flows
    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?> = _currentStation.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Sleep timer state
    private val _sleepTimeRemaining = MutableStateFlow<Long?>(null) // Milliseconds remaining
    val sleepTimeRemaining: StateFlow<Long?> = _sleepTimeRemaining.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    // Player listener instance to update UI states in real time
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (!isPlaying) {
                _isLoading.value = false
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _isLoading.value = playbackState == Player.STATE_BUFFERING
            if (playbackState == Player.STATE_READY) {
                _isPlaying.value = controller?.isPlaying == true
            }
        }
    }

    init {
        val sessionToken = SessionToken(
            context.applicationContext,
            ComponentName(context.applicationContext, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context.applicationContext, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                controller = controllerFuture?.get()
                controller?.addListener(playerListener)
                // If there's already a currently playing item on connection, restore it
                controller?.currentMediaItem?.let { item ->
                    val station = stations.value.firstOrNull { it.id == item.mediaId }
                    if (station != null) {
                        _currentStation.value = station
                        _isPlaying.value = controller?.isPlaying == true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun playStation(station: RadioStation) {
        _currentStation.value = station
        val controller = controller ?: return
        _isLoading.value = true

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(station.name)
            .setSubtitle(station.category)
            .setArtist(station.country)
            .setArtworkUri(android.net.Uri.parse(station.imageUrl))
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(station.id)
            .setUri(station.streamUrl)
            .setMediaMetadata(mediaMetadata)
            .build()

        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }

    fun togglePlayPause() {
        val controller = controller ?: return
        if (controller.isPlaying) {
            controller.pause()
            _isPlaying.value = false
        } else {
            if (controller.mediaItemCount > 0) {
                controller.play()
                _isPlaying.value = true
            } else {
                _currentStation.value?.let { playStation(it) }
            }
        }
    }

    fun toggleFavorite(station: RadioStation) {
        viewModelScope.launch {
            repository.toggleFavorite(station.id, station.isFavorite)
            // Synchronize local current playing reference if it was the target
            if (_currentStation.value?.id == station.id) {
                _currentStation.value = _currentStation.value?.copy(isFavorite = !station.isFavorite)
            }
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val durationMs = minutes * 60 * 1000L
        val endTime = System.currentTimeMillis() + durationMs

        sleepTimerJob = viewModelScope.launch {
            while (System.currentTimeMillis() < endTime) {
                val remaining = endTime - System.currentTimeMillis()
                _sleepTimeRemaining.value = remaining
                delay(1000L)
            }
            _sleepTimeRemaining.value = null
            controller?.pause()
            _isPlaying.value = false
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimeRemaining.value = null
    }

    override fun onCleared() {
        super.onCleared()
        controller?.removeListener(playerListener)
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}

class RadioViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RadioViewModel::class.java)) {
            return RadioViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
