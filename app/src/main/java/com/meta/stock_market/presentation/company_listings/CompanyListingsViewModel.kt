package com.meta.stock_market.presentation.company_listings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meta.stock_market.data.repository.StockRepository
import com.meta.stock_market.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyListingsViewModel @Inject constructor(
    private val repository: StockRepository
): ViewModel() {

    var state by mutableStateOf(CompanyListingState())

    private var searchJob: Job? = null
    fun onEvent(event: CompanyListingEvent) {
        when(event){
            is CompanyListingEvent.Refresh -> {
                getCompanyListings(fetchfromRemote = true)
            }
            is CompanyListingEvent.onSearchQueryChange -> {
                state = state.copy(searchQuery = event.Query)
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getCompanyListings()
                }
            }
        }
    }
    private fun getCompanyListings(
        query: String = state.searchQuery.lowercase(),
        fetchfromRemote: Boolean = false
    ){
        viewModelScope.launch {
            repository
                .getCompanyListings(fetchfromRemote,query)
                .collect{ result ->
                    when(result) {
                        is Resource.Success -> {
                            result.data?.let { listings ->
                                state = state.copy(
                                    companies = listings
                                )
                            }
                        }
                        is Resource.Error -> Unit
                        is Resource.Loading -> {
                            state = state.copy(isLoading = result.isLoading)
                        }
                    }
                }
        }
    }
}