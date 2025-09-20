package com.example.core_ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Search",
    placeholder: String = "Type to search...",
    showLeadingIcon: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        leadingIcon = if (showLeadingIcon) {
            { Icon(Icons.Default.Search, contentDescription = "Search") }
        } else null,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier,
        singleLine = true  // ✅ 强制单行
    )
}