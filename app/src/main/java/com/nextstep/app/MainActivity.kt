package com.nextstep.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nextstep.app.ui.navigation.NextStepRoot
import com.nextstep.app.ui.theme.NextStepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NextStepTheme {
                NextStepRoot()
            }
        }
    }
}
