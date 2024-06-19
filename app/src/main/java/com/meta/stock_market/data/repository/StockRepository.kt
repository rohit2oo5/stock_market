package com.meta.stock_market.data.repository

import com.meta.stock_market.domain.model.CompanyListing
import com.meta.stock_market.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>>
}