package com.allergytrack.app.ui.screens.food

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.allergytrack.app.R
import com.allergytrack.app.data.remote.food.FoodAllergenHelper
import com.allergytrack.shared.data.remote.food.RecognizedFood
import com.allergytrack.app.ui.navigation.Screen
import com.allergytrack.app.ui.theme.MonoFontFamily

@Composable
fun FoodManualSearchScreen(
    navController: NavController,
    initialQuery: String = "",
    viewModel: FoodLogViewModel = koinViewModel()
) {
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (initialQuery.isNotEmpty()) {
            viewModel.onSearchQueryChange(initialQuery)
        }
        focusRequester.requestFocus()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Info banner — vysvětluje zdroj dat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.search_info_banner),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = searchState.query,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text(stringResource(R.string.search_food_query_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, stringResource(R.string.cd_clear))
                        }
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
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
                                Icons.Default.Search, null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                stringResource(R.string.search_min_chars),
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
                            val encodedName = Uri.encode(food.name)
                            val encodedAllergens = Uri.encode(food.allergens.joinToString("|"))
                            navController.navigate(
                                Screen.FoodConfirm.createRoute(encodedName, encodedAllergens)
                            )
                        }
                    )
                }

                if (searchState.query.length >= 2 && !searchState.isSearching && searchResults.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (searchState.isServiceUnavailable) {
                                Text(
                                    stringResource(R.string.search_service_unavailable_title),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    stringResource(R.string.search_service_unavailable_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    stringResource(R.string.search_no_results_title),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    stringResource(R.string.search_no_results_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Tlačítko "Zadat ručně" — vždy viditelné; Primary když nejsou výsledky, Secondary když jsou
            val hasResults = searchResults.isNotEmpty()
            val buttonLabel = if (searchState.query.length >= 2)
                stringResource(R.string.search_add_manually, searchState.query)
            else
                stringResource(R.string.btn_add_food_manually)
            val buttonModifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)

            if (hasResults) {
                OutlinedButton(
                    onClick = {
                        navController.navigate(
                            Screen.FoodManualEntry.createRoute(Uri.encode(searchState.query))
                        )
                    },
                    modifier = buttonModifier,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(buttonLabel)
                }
            } else {
                Button(
                    onClick = {
                        navController.navigate(
                            Screen.FoodManualEntry.createRoute(Uri.encode(searchState.query))
                        )
                    },
                    modifier = buttonModifier,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(buttonLabel, fontFamily = MonoFontFamily)
                }
            }
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
            Icon(Icons.Default.Restaurant, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f)) {
                Text(
                    food.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                if (!food.nameIsLocalized) {
                    Text(
                        text = stringResource(R.string.food_name_original_lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                if (food.allergens.isNotEmpty()) {
                    val localizedAllergens = food.allergens.map { key ->
                        FoodAllergenHelper.keyToDisplayRes[key]
                            ?.let { stringResource(it) }
                            ?: key
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            localizedAllergens.joinToString(", "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
