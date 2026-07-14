package com.example.sepotify.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.sepotify.ui.auth.AuthState
import com.example.sepotify.ui.auth.AuthViewModel
import com.example.sepotify.ui.auth.SignInScreen
import com.example.sepotify.ui.profile.ProfileViewModel
import org.koin.androidx.compose.koinViewModel
import com.example.sepotify.ui.splash.SplashScreen
import com.example.sepotify.ui.auth.SignUpStep1Screen
import com.example.sepotify.ui.auth.SignUpStep2Screen
import com.example.sepotify.ui.main.MainScreen
import com.example.sepotify.ui.player.PlayerScreen
import com.example.sepotify.ui.profile.ProfileScreen

@Composable
fun NavGraph(
    authViewModel: AuthViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel()
) {

    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        val currentDest = navController.currentDestination
        when (authState) {
            is AuthState.Authenticated -> {
                val userId = (authState as AuthState.Authenticated).profile.id
                val isAtAuthFlow = currentDest?.hasRoute<Routes.Splash>() == true ||
                        currentDest?.hasRoute<Routes.SignIn>() == true ||
                        currentDest?.hasRoute<Routes.SignUpStep1>() == true ||
                        currentDest?.hasRoute<Routes.SignUpStep2>() == true

                if (isAtAuthFlow) {
                    navController.navigate(Routes.Main(userId)) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            is AuthState.SignUpStep1 -> {
                val isAlreadyAtStep2 = currentDest?.hasRoute<Routes.SignUpStep2>() == true
                if (!isAlreadyAtStep2) {
                    navController.navigate(Routes.SignUpStep2)
                }
            }

            else -> { }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Splash
    ) {
        composable<Routes.Splash> {
            SplashScreen(
                onNavigateToSignIn = {
                    navController.navigate(Routes.SignIn) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onNavigateToMain = { userId ->
                    navController.navigate(Routes.Main(userId)) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.SignIn> {
            SignInScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = {
                    navController.navigate(Routes.SignUpStep1)
                }
            )
        }

        composable<Routes.SignUpStep1> {
            SignUpStep1Screen(
                viewModel = authViewModel,
                onNavigateToSignIn = {
                    navController.navigate(Routes.SignIn) {
                        popUpTo(Routes.SignUpStep1) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.SignUpStep2> {
            SignUpStep2Screen(
                viewModel = authViewModel
            )
        }

        composable<Routes.Profile> { backStackEntry ->
            val profileArgs = backStackEntry.toRoute<Routes.Profile>()

            ProfileScreen(
                viewModel = profileViewModel,
                onLogout = {
                    authViewModel.logOut()
                    navController.navigate(Routes.SignIn) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onDeleted = {},
                onFollowListClick = { userId, mode ->
                    navController.navigate(Routes.FollowList(userId = userId, currentUserId = userId, mode = mode))
                }
            )
        }

        composable<Routes.Main> { backStackEntry ->
            val args = backStackEntry.toRoute<Routes.Main>()
            MainScreen(
                userId = args.userId,
                onPlayerClick = {
                    navController.navigate(Routes.Player(userId = args.userId))
                },
                onLogout = {
                    authViewModel.logOut()
                    navController.navigate(Routes.SignIn) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Player> {
            PlayerScreen(
                userId = (authState as AuthState.Authenticated).profile.id,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

    }
}