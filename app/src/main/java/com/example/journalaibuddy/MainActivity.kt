package com.example.journalaibuddy

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.journalaibuddy.database.isFirstLaunch
import com.example.journalaibuddy.database.setFirstLaunch
import com.example.journalaibuddy.ui.screens.MainScreenWithBottomNav
import com.example.journalaibuddy.ui.screens.WelcomeScreen
import com.example.journalaibuddy.ui.screens.WelcomeViewModel
import com.example.journalaibuddy.viewmodel.JournalViewModel
import com.example.journalaibuddy.viewmodel.JournalViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("journal_channel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var isFirstLaunch by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isFirstLaunch = isFirstLaunch(context).first()
    }

    NavHost(navController, startDestination = if (isFirstLaunch) "welcome" else "main") {
        composable("welcome") {
            val viewModel: WelcomeViewModel = viewModel()
            WelcomeScreen(viewModel = viewModel, onLoginSuccess = {
                navController.navigate("main") {
                    popUpTo("welcome") { inclusive = true }
                }
                coroutineScope.launch {
                    setFirstLaunch(context, false)
                }
            })

        }
        composable("main") {
            val viewModel: JournalViewModel = viewModel(factory = JournalViewModelFactory(context.applicationContext as Application))
            MainScreenWithBottomNav(viewModel)
        }

    }
}