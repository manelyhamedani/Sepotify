package com.example.sepotify.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState

class SupabasePagingSource<T: Any>(
    private val loader: suspend(
        from: Long,
        to: Long
    ) -> List<T>
): PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            val from = (page * pageSize).toLong()
            val to = from + pageSize - 1
            Log.d("SupabasePaging", "Fetching from $from to $to")
            val items = loader(from, to)
            Log.d("SupabasePaging", "Fetched ${items.size} items")
            LoadResult.Page(
                data = items,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (items.size < pageSize) null else page + 1
            )
        } catch (e: Exception) {
            Log.e("SupabasePaging", "Load failed", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? = state.anchorPosition?.let { anchor ->
        state.closestPageToPosition(anchor)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
    }
}