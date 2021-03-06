package uk.easys.easymarketing.woo.cocart.callback

import androidx.lifecycle.LiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.easys.easymarketing.woo.callback.NetworkException
import uk.easys.easymarketing.woo.callback.Resource
import uk.easys.easymarketing.woo.callback.Status

import java.io.IOException

class CallBackLiveData<T> : LiveData<Resource<T>>(), Callback<T> {
    init {
        value = Resource(Status.LOADING)
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful) setValue(Resource(response.body()!!))
        else {
            var error: String? = null
            try {
                error = response.errorBody()!!.string()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (error == null) error = "Something went wrong"
            setValue(Resource(NetworkException(error)))
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        value = Resource(NetworkException(t))
    }
}