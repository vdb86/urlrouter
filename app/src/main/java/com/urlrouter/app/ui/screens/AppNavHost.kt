package com.urlrouter.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.urlrouter.app.ui.MainViewModel
import com.urlrouter.app.ui.NavRoutes

@Composable
fun AppNavHost(viewModel: MainViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavRoutes.SETTINGS) {
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(viewModel = viewModel, onNavigate = { navController.navigate(it) })
        }
        composable(NavRoutes.BROWSER_MANAGEMENT) {
            BrowserManagementScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.ROUTING_RULES) {
            RoutingRulesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddRule = { navController.navigate(NavRoutes.RULE_EDITOR_NEW) },
                onEditRule = { navController.navigate(NavRoutes.ruleEditor(it)) }
            )
        }
        composable(NavRoutes.RULE_EDITOR) { backStackEntry ->
            val ruleId = backStackEntry.arguments?.getString("ruleId")?.toLongOrNull() ?: -1L
            RuleEditorScreen(ruleId = ruleId, viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.APPEARANCE) {
            AppearanceScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.IMPORT_EXPORT) {
            ImportExportScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.DIAGNOSTICS) {
            DiagnosticsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
