package com.allergytrack.app.ui.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.data.remote.food.RecognizedFood
import com.allergytrack.app.ui.navigation.Screen

@Composable
fun FoodManualSearchScreen(
    navController: NavController,
    viewModel: FoodLogViewModel = hiltViewModel()
) {
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Poznámka: Vyhledávací pole je nyní součástí TopAppBar v MainAppScaffold pro tuto obrazovku?
    // Ne, v MainAppScaffold jsme definovali jen textový titulek. 
    // Pro lepší UX ponecháme vyhledávací logiku zde, ale musíme vyřešit TopAppBar.
    
    // Rozhodnutí: MainAppScaffold TopAppBar je pro tuto obrazovku příliš statický.
    // Abychom zachovali "jeden scaffold", musíme buď přesunout vyhledávací pole do MainAppScaffold,
    // nebo nechat TopAppBar v MainAppScaffold prázdný a vykreslit hledání zde jako první prvek.
    
    // Druhá možnost (vykreslení zde) je čistší pro kód screenu.
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = searchState.query,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = { Text("Hledat potravinu...") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchState.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, "Vymazat")
                    }
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (searchState.isSearching) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                }
            }

            if (searchState.query.length < 2 && !searchState.isSearching) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Zadej alespoň 2 znaky pro vyhledání",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(searchResults) { food ->
                FoodSearchResultItem(
                    food = food,
                    onClick = {
                        val allergens = food.allergens.joinToString("|")
                        navController.navigate(
                            Screen.FoodConfirm.createRoute(food.name, allergens)
                        )
                    }
                )
            }

            if (searchState.query.length >= 2 && !searchState.isSearching && searchResults.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nic nebylo nalezeno",
                            style = MaterialTheme.typography.titleSmall)
                        Text("Zkus jiný název nebo přidej ručně",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = {
                            navController.navigate(
                                Screen.FoodConfirm.createRoute(searchState.query, "")
                            )
                        }) {
                            Text("Přidat \"${searchState.query}\" ručně")
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun FoodSearchResultItem(
    food: RecognizedFood,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Restaurant, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                if (food.allergens.isNotEmpty()) {
                    Text(
                        "⚠️ ${food.allergens.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
