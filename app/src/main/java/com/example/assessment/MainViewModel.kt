package com.example.assessment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.assessment.model.LoadImageItem
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MainViewModel constructor(private val mainRepository: MainRepository) : ViewModel() {

    val usersSuccessLiveData = MutableLiveData<String>()
    val usersFailureLiveData = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val imageList = MutableLiveData<List<LoadImageItem>?>()
    var job: Job? = null
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }

    fun getImageList() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = mainRepository.getImageList()
            withContext(Dispatchers.Main) {

                try {
                    Log.d("TAG", "$response")

                    if (response.isSuccessful) {
                        Log.d("TAG", "SUCCESS")
                        Log.d("TAG", "${response.body()}")
                        imageList.postValue(response.body())

                    } else {
                        Log.d("TAG", "FAILURE")
                        Log.d("TAG", "${response.body()}")
                        usersFailureLiveData.postValue(true)
                    }

                } catch (e: UnknownHostException) {
                    Log.e("TAG", e.message ?: "")
                    //this exception occurs when there is no internet connection or host is not available
                    //so inform user that something went wrong
                    usersFailureLiveData.postValue(true)
                } catch (e: SocketTimeoutException) {
                    Log.e("TAG", e.message ?: "")
                    //this exception occurs when time out will happen
                    //so inform user that something went wrong
                    usersFailureLiveData.postValue(true)
                } catch (e: Exception) {
                    Log.e("TAG", e.message ?: "")
                    //this is generic exception handling
                    //so inform user that something went wrong
                    usersFailureLiveData.postValue(true)
                }

/*
                if (response.isSuccessful) {
                    imageList.postValue(response.body())
                } else {
                    onError("Error : ${response.message()} ")
                }
*/
            }
        }

    }

    private fun onError(message: String) {
        errorMessage.postValue(message)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }

}