package com.pogotcghelper.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pogotcghelper.app.PogoTcgApplication
import com.pogotcghelper.app.R
import com.pogotcghelper.app.ui.collection.CollectionDetailScreen
import com.pogotcghelper.app.ui.collection.CollectionDetailViewModel
import com.pogotcghelper.app.ui.collection.CollectionsListScreen
import com.pogotcghelper.app.ui.collection.CollectionsListViewModel
import com.pogotcghelper.app.ui.detail.CardDetailScreen
import com.pogotcghelper.app.ui.detail.CardDetailViewModel
import com.pogotcghelper.app.ui.scan.ScanScreen
import com.pogotcghelper.app.ui.scan.ScanViewModel
import com.pogotcghelper.app.ui.search.SearchScreen
import com.pogotcghelper.app.ui.search.SearchViewModel

private object Routes {
    const val SEARCH = "search"
    const val SCAN = "scan"
    const val COLLECTIONS = "collections"
    const val COLLECTION_DETAIL = "collections/{collectionId}"
    const val CARD_DETAIL = "card/{cardId}"
    fun cardDetail(cardId: String) = "card/$cardId"
    fun collectionDetail(collectionId: Long) = "collections/$collectionId"
}

private data class TopLevelDestination(val route: String, val labelRes: Int, val icon: ImageVector)

private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.SEARCH, R.string.nav_search, Icons.Filled.Search),
    TopLevelDestination(Routes.SCAN, R.string.nav_scan, Icons.Filled.CameraAlt),
    TopLevelDestination(Routes.COLLECTIONS, R.string.nav_collection, Icons.Filled.Collections),
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
                        initializer { SearchViewModel(container.cardRepository, container.collectionRepository) }
                    }
                )
                SearchScreen(
                    viewModel = viewModel,
                    onCardClick = { cardId -> navController.navigate(Routes.cardDetail(cardId)) },
                )
            }
            composable(Routes.SCAN) {
                val viewModel: ScanViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { ScanViewModel(container.cardRepository) }
                    }
                )
                ScanScreen(
                    viewModel = viewModel,
                    onCardClick = { cardId -> navController.navigate(Routes.cardDetail(cardId)) },
                )
            }
            composable(Routes.COLLECTIONS) {
                val viewModel: CollectionsListViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            CollectionsListViewModel(container.collectionRepository, container.cardRepository)
                        }
                    }
                )
                CollectionsListScreen(
                    viewModel = viewModel,
                    onCollectionClick = { id -> navController.navigate(Routes.collectionDetail(id)) },
                )
            }
            composable(Routes.COLLECTION_DETAIL) { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getString("collectionId")?.toLongOrNull() ?: return@composable
                val viewModel: CollectionDetailViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { CollectionDetailViewModel(collectionId, container.collectionRepository) }
                    }
                )
                CollectionDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
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

    val isTopLevelRoute = topLevelDestinations.any { it.route == currentRoute?.route }
    if (!isTopLevelRoute) return

    NavigationBar {
        topLevelDestinations.forEach { destination ->
            val selected = currentRoute?.route == destination.route
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
