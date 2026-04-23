package com.allergytrack.app.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.allergytrack.app.ui.components.*
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FoodLogScreen(
    navController: NavController,
    viewModel: FoodLogViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Food", fontFamily = SerifFontFamily) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            
            SectionHeading(
                title = "Identify Your Food",
                subtitle = "Choose the most convenient way to log"
            )

            // Camera View Area
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                borderColor = AccentGreen.copy(alpha = 0.3f),
                onClick = { navController.navigate(Screen.Camera.route) }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Camera Corners
                    Box(Modifier.fillMaxSize().padding(8.dp)) {
                        val cornerSize = 24.dp
                        val thickness = 3.dp
                        
                        // Top Left
                        Box(Modifier.size(cornerSize).border(thickness, AccentGreen, RoundedCornerShape(topStart = 8.dp)))
                        // Top Right
                        Box(Modifier.align(Alignment.TopEnd).size(cornerSize).border(thickness, AccentGreen, RoundedCornerShape(topEnd = 8.dp)))
                        // Bottom Left
                        Box(Modifier.align(Alignment.BottomStart).size(cornerSize).border(thickness, AccentGreen, RoundedCornerShape(bottomStart = 8.dp)))
                        // Bottom Right
                        Box(Modifier.align(Alignment.BottomEnd).size(cornerSize).border(thickness, AccentGreen, RoundedCornerShape(bottomEnd = 8.dp)))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "TAP TO SCAN PHOTO",
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentGreen,
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Fast AI identification",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim
                        )
                    }
                }
            }

            // Quick Actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = "BARCODE", 
                    onClick = { /* TODO */ }, 
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = "VOICE", 
                    onClick = { /* TODO */ }, 
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar
            AppCard(
                borderColor = Border,
                onClick = { /* TODO: Manual Search */ }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("🔍", fontSize = 18.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Search food manually...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDim
                    )
                }
            }

            // Recent
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CardTitle("RECENTLY LOGGED")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Pill(text = "Oatmeal")
                    Pill(text = "Greek Yogurt")
                    Pill(text = "Banana")
                    Pill(text = "MILK ⚠️", color = DotRed.copy(alpha = 0.1f), textColor = DotRed)
                    Pill(text = "Chicken Breast")
                    Pill(text = "Almonds ⚠️", color = DotRed.copy(alpha = 0.1f), textColor = DotRed)
                }
            }
        }
    }
}
