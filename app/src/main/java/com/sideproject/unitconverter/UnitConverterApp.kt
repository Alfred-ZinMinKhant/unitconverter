package com.sideproject.unitconverter

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.sideproject.unitconverter.BuildConfig
import com.sideproject.unitconverter.ui.screens.*
import com.sideproject.unitconverter.ui.theme.LocalInstrumentColors

@Composable
fun UnitConverterApp() {
    val colors = LocalInstrumentColors.current
    var route by remember { mutableStateOf("home") }
    var injectedValue by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgPage)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = route,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { if (targetState == "home") -it / 4 else it / 4 } togetherWith
                            fadeOut() + slideOutHorizontally { if (targetState == "home") it / 4 else -it / 4 }
                },
                label = "nav",
            ) { currentRoute ->
                when (currentRoute) {
                    "home" -> HomeScreen(onOpen = { route = it })
                    "unit" -> UnitConverterScreen(
                        onBack = { route = "home" },
                        injectedValue = injectedValue,
                        onInjectConsumed = { injectedValue = null },
                    )
                    "currency" -> CurrencyScreen(onBack = { route = "home" })
                    "calc" -> CalculatorScreen(
                        onBack = { route = "home" },
                        onSendToConverter = { v ->
                            injectedValue = v
                            route = "unit"
                        },
                    )
                }
            }
        }

        // AdMob banner
        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = BuildConfig.BANNER_AD_UNIT_ID
                    loadAd(AdRequest.Builder().build())
                }
            },
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        )
    }
}
