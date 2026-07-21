package com.example.sepotify.di

import androidx.room.Room
import com.example.sepotify.data.local.database.AppDatabase
import com.example.sepotify.data.local.preferences.SettingsDataStoreManager
import com.example.sepotify.data.player.PlayerManager
import com.example.sepotify.data.remote.repository.SupabaseChatRepository
import com.example.sepotify.data.remote.repository.SupabaseFollowRepository
import com.example.sepotify.data.repository.MusicRepository
import com.example.sepotify.data.repository.ProfileRepository
import com.example.sepotify.data.remote.repository.SupabaseMusicRepository
import com.example.sepotify.data.remote.repository.SupabaseProfileRepository
import com.example.sepotify.data.repository.ChatRepository
import com.example.sepotify.data.repository.FollowRepository
import com.example.sepotify.ui.artist.ArtistDetailViewModel
import com.example.sepotify.ui.auth.AuthViewModel
import com.example.sepotify.ui.chat.ChatListViewModel
import com.example.sepotify.ui.chat.ChatViewModel
import com.example.sepotify.ui.download.DownloadsViewModel
import com.example.sepotify.ui.home.HomeViewModel
import com.example.sepotify.ui.main.MainViewModel
import com.example.sepotify.ui.player.PlayerViewModel
import com.example.sepotify.ui.playlist.PlaylistDetailViewModel
import com.example.sepotify.ui.profile.ProfileViewModel
import com.example.sepotify.ui.profile.UserProfileViewModel
import com.example.sepotify.ui.search.SearchViewModel
import com.example.sepotify.ui.splash.SplashViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.example.sepotify.ui.follow.FollowListViewModel
import com.example.sepotify.ui.home.LikedSongsViewModel
import com.example.sepotify.ui.home.RecentlyPlayedViewModel
import com.example.sepotify.ui.playlist.PlaylistsViewModel
import com.example.sepotify.ui.settings.SettingsViewModel


val appModule = module {
    single<ProfileRepository> {
        SupabaseProfileRepository()
    }

    single<MusicRepository> {
        SupabaseMusicRepository(downloadedSongDao = get(), context = androidApplication(), profileRepo = get())
    }

    single<FollowRepository> {
        SupabaseFollowRepository(followDao = get())
    }

    single<ChatRepository> {
        SupabaseChatRepository(messageDao = get())
    }

    single {
        PlayerManager(get(), androidApplication())
    }

    viewModel {
        AuthViewModel(get(), androidApplication())
    }

    viewModel {
        ProfileViewModel(get(), androidApplication())
    }

    viewModel {
        SplashViewModel(get())
    }

    viewModel {
        HomeViewModel(get())
    }

    viewModel { (userId: String) ->
        PlayerViewModel(get(), get(), userId)
    }

    viewModel {
        MainViewModel(get())
    }

    viewModel { (userId: String) ->
        SearchViewModel(get(), get(), get(), userId)
    }

    viewModel {
        (artistId: String) -> ArtistDetailViewModel(get(), artistId)
    }

    viewModel {
        (userId: String, currentUserId: String) -> UserProfileViewModel(get(), get(), get(), userId, currentUserId)
    }

    viewModel {
        (playlistId: String) -> PlaylistDetailViewModel(get(), playlistId)
    }

    viewModel { (userId: String, currentUserId: String, mode: FollowListViewModel.FollowListMode) ->
        FollowListViewModel(get(), userId = userId, currentUserId = currentUserId, mode = mode)
    }
    viewModel { (currentUserId: String, otherUserId: String) ->
        ChatViewModel(get(), get(), currentUserId, otherUserId)
    }
    viewModel { (userId: String) ->
        ChatListViewModel(get(), get(), get(), userId)
    }

    viewModel { (userId: String) ->
        PlaylistsViewModel(get(), userId, androidApplication())
    }

    viewModel { (userId: String) ->
        LikedSongsViewModel(get(), userId)
    }

    viewModel { (userId: String) ->
        RecentlyPlayedViewModel(get(), userId)
    }

    viewModel { (userId: String) ->
        DownloadsViewModel(get(), userId)
    }

    viewModel {
        SettingsViewModel(get())
    }

    single { SettingsDataStoreManager(get()) }

    single {
        Room.databaseBuilder(
                androidApplication(),
                AppDatabase::class.java,
                "app_database"
            ).fallbackToDestructiveMigration(false).build()
    }

    single { get<AppDatabase>().searchHistoryDao() }
    single { get<AppDatabase>().downloadedSongDao() }
    single { get<AppDatabase>().messageDao() }
    single { get<AppDatabase>().followDao() }
    single { get<AppDatabase>().searchHistoryDao() }
}