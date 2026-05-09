package com.example.poultryfarmmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.poultryfarmmanager.ui.navigation.MainNavigation
import com.example.poultryfarmmanager.ui.screens.dashboard.LanguageViewModel
import com.example.poultryfarmmanager.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val languageViewModel: LanguageViewModel = hiltViewModel()
            val language by languageViewModel.language.collectAsState()
            val strings = if (language == "en") EnglishStrings else SwahiliStrings

            CompositionLocalProvider(LocalAppStrings provides strings) {
                PoultryFarmTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        MainNavigation()
                    }
                }
            }
        }
    }
}
