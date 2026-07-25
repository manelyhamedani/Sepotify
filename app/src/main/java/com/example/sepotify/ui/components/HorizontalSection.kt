package com.example.sepotify.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sepotify.ui.theme.Dimens

@Composable
fun <T> HorizontalSection(
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    emptyContent: @Composable (() -> Unit)? = null,
    itemContent: @Composable (Int, T) -> Unit
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
    ) {
        SectionTitle(title = title)

        if (items.isEmpty() && !isLoading) {
            emptyContent?.invoke()
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceLarge),
                contentPadding = PaddingValues(horizontal = Dimens.spaceLarge)
            ) {
                itemsIndexed(items) { index, item ->
                    itemContent(index, item)
                }
            }
        }
    }
}