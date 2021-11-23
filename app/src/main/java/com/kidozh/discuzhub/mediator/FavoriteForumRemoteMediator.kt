package com.kidozh.discuzhub.mediator

import androidx.paging.*
import com.kidozh.discuzhub.entities.FavoriteForum
import com.kidozh.discuzhub.services.DiscuzApiService

@OptIn(ExperimentalPagingApi::class)
class FavoriteForumRemoteMediator(private val page: Int, val service: DiscuzApiService, val database: DataSource<Int, FavoriteForum>): RemoteMediator<Int, FavoriteForum>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, FavoriteForum>): MediatorResult {
        // return to true when all db runs out
        return MediatorResult.Success(endOfPaginationReached = true)
//        try {
//            // Get the closest item from Paging State that we want to load data around
//            var loadKey =  when(loadType){
//                LoadType.REFRESH -> null
//                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
//                LoadType.APPEND ->{
//
//                }
//            }
//        }

    }


}