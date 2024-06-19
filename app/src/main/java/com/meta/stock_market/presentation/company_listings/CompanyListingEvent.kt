package com.meta.stock_market.presentation.company_listings

sealed class CompanyListingEvent {
    object Refresh: CompanyListingEvent()
    data class onSearchQueryChange(val Query:String): CompanyListingEvent()
}