package com.example.core_ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core_ui.theme.Black
import com.example.core_ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBy(
    selectedFilter: String,
    options: List<String>,
    onFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Filter By:"
) {
    var isExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左边文字（黑色）
        Text(
            text = label,
            color = Black,
            modifier = Modifier
                .padding(start = 4.dp)
                .align(Alignment.CenterVertically),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 右边下拉菜单
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = !isExpanded },
            modifier = Modifier.weight(1f)
        ) {
            TextField(
                value = selectedFilter,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                textStyle = TextStyle(lineHeight = 24.sp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    focusedTextColor = PrimaryColor, // 选中值文字热粉
                    unfocusedTextColor = PrimaryColor
                )
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                options.forEach { text ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = text,
                                color = PrimaryColor // 菜单文字热粉
                            )
                        },
                        onClick = {
                            onFilterChange(text)
                            isExpanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = PrimaryColor,
                            disabledTextColor = PrimaryColor.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }
    }
}