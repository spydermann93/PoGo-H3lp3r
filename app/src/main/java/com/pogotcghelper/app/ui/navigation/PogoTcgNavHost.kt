package com.pogotcghelper.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pogotcghelper.app.PogoTcgApplication
import com.pogotcghelper.app.R
import com.pogotcghelper.app.ui.collection.CollectionScreen
import com.pogotcghelper.app.ui.collection.CollectionViewModel
import com.pogotcghelper.app.ui.detail.CardDetailScreen
import com.pogotcghelper.app.ui.detail.CardDetailViewModel
import com.pogotcghelper.app.ui.search.SearchScreen
import com.pogotcghelper.app.ui.search.SearchViewModel

private object Routes {
    const val SEARCH = "search"
    const val COLLECTION = "collection"
    const val CARD_DETAIL = "card/{cardId}"
    fun cardDetail(cardId: String) = "card/$cardId"
}

private data class TopLevelDestination(val route: String, val labelRes: Int, val icon: ImageVector)

private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.SEARCH, R.string.nav_search, Icons.Filled.Search),
    TopLevelDestination(Routes.COLLECTION, R.string.nav_collection, Icons.Filled.Collections),
)

@Composable
fun PogoTcgNavHost() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as PogoTcgApplication
    val container = application.container

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SEARCH,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.SEARCH) {
                val viewModel: SearchViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { SearchViewModel(container.cardRepository) }
                    }
                )
                SearchScreen(
                    viewModel = viewModel,
                    onCardClick = { cardId -> navController.navigate(Routes.cardDetail(cardId)) },
                )
            }
            composable(Routes.COLLECTION) {
                val viewModel: CollectionViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { CollectionViewModel(container.collectionRepository) }
                    }
                )
                CollectionScreen(viewModel = viewModel)
            }
            composable(Routes.CARD_DETAIL) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString("cardId").orEmpty()
                val viewModel: CardDetailViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            CardDetailViewModel(cardId, container.cardRepository, container.collectionRepository)
                        }
                    }
                )
                CardDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val isTopLevelRoute = topLevelDestinations.any { dest ->
        currentRoute?.hierarchy?.any { it.route == dest.route } == true
    }
    if (!isTopLevelRoute) return

    NavigationBar {
        topLevelDestinations.forEach { destination ->
            val selected = currentRoute?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(stringResource(destination.labelRes)) },
            )
        }
    }
}
