package com.example.sepotify.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.sepotify.ui.navigation.Routes
import com.example.sepotify.ui.theme.Dimens
import com.example.sepotify.ui.theme.selectedBackground
import com.example.sepotify.ui.theme.unselectedIcon

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    val items = listOf(
        Routes.Home to Icons.Outlined.Home,
        Routes.Search to Icons.Outlined.Search,
        Routes.Library to Icons.Outlined.LibraryMusic,
        Routes.Downloads to Icons.Outlined.Download,
        Routes.ChatList to Icons.Outlined.ChatBubbleOutline,
        Routes.Profile to Icons.Outlined.Person
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.elevationNone
    ) {
        items.forEach { (route, icon) ->
            val selected = currentDestination?.hasRoute(route::class) == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    IconButton(
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier
                            .size(Dimens.bottomNavItemSize)
                            .clip(CircleShape)
                            .background(
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary.copy(selectedBackground)
                                } else Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(unselectedIcon)
                            },
                            modifier = Modifier.size(Dimens.bottomNavIconSize)
                        )
                    }
                },
                label = null,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(unselectedIcon),
                    selectedTextColor = Color.Transparent,
                    unselectedTextColor = Color.Transparent,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}