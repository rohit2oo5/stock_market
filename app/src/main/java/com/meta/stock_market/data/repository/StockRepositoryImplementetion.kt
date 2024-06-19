package com.meta.stock_market.data.repository

import android.net.http.HttpException
import com.meta.stock_market.data.csv.CsvParser
import com.meta.stock_market.data.local.StockDatabase
import com.meta.stock_market.data.mappers.toCompanyListing
import com.meta.stock_market.data.mappers.toCompanyListingEntity
import com.meta.stock_market.data.remote.StockApi
import com.meta.stock_market.domain.model.CompanyListing
import com.meta.stock_market.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImplementetion @Inject constructor(
    val api: StockApi,
    val db: StockDatabase,
    val companyListingParser: CsvParser<CompanyListing>
): StockRepository{

    private val dao = db.dao
    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading(true))
            val localListings = dao.searchCompanyListing(query)
            emit(Resource.Success(
                data = localListings.map { it.toCompanyListing() }
            ))

            val isDbEmpty = localListings.isEmpty() && query.isBlank()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
            if (shouldJustLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }
            val remoteListings = try {
                val response = api.getListings()
                companyListingParser.parse(response.byteStream())
            }catch (e: IOException){
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            }catch (e: HttpException){
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            }

            remoteListings?.let { listings ->

                dao.clearCompanyListings()
                dao.insertCompanyListings(
                    listings.map { it.toCompanyListingEntity() }
                )
                emit(Resource.Success(
                    data = dao
                        .searchCompanyListing("")
                        .map {it.toCompanyListing()}
                ))
                emit(Resource.Loading(false))
            }
        }
    }
}