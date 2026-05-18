package com.example.viewlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.viewlist.ui.screens.AddEditEntryScreen
import com.example.viewlist.ui.screens.HomeScreen
import com.example.viewlist.ui.theme.Background
import com.example.viewlist.ui.theme.ViewListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ViewListTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Background) {
                    ViewListNavGraph()
                }
            }
        }
    }
}

@Composable
private fun ViewListNavGraph() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onAddEntry = { status -> nav.navigate("entry/-1/$status") },
                onEditEntry = { id -> nav.navigate("entry/$id/viewed") },
            )
        }
        composable(
            route = "entry/{entryId}/{status}",
            arguments = listOf(
                navArgument("entryId") { type = NavType.LongType },
                navArgument("status")  { type = NavType.StringType },
            ),
        ) { back ->
            val entryId = back.arguments!!.getLong("entryId").takeIf { it != -1L }
            val status  = back.arguments!!.getString("status") ?: "viewed"
            AddEditEntryScreen(
                entryId = entryId,
                initialStatus = status,
                onBack = { nav.popBackStack() },
            )
        }
    }
}
