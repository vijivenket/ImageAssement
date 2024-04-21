package com.example.assessment

import android.content.Context
import com.example.assessment.model.LoadImageItem
import com.example.assessment.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class MainRepository constructor(private val retrofitService: ApiService) {

    companion object {

        /*  var loginDatabase: LocalDatabase? = null

        var loginTableModel: LiveData<LoadImageItemRoom>? = null

        private fun initializeDB(context: Context): LocalDatabase? {
            return LocalDatabase.getDatabaseClient(context)
        }

    }*/

        fun insertData(context: Context, loadImageList: LoadImageItem) {

            //   loginDatabase = initializeDB(context)

            CoroutineScope(IO).launch {
                val loginDetails = arrayListOf(loadImageList)
                // loginDatabase?.loginDao()?.InsertData(loginDetails)
            }

        }

    }
        suspend fun getImageList() = retrofitService.getImageList()
    //handle local storage condition

}