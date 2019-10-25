package com.bookyrself.bookyrself.services.clients

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.services.FirebaseServiceCoroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

//TODO: Make a service and flesh out this client so its actually a client ala UsersClient
//TODO: This shouldn't even be a client since I'm manipulating the main thread blegh
object EventsClient {
    suspend fun getEvents(eventIds: Set<String>?, events: MutableLiveData<HashMap<EventDetail, String>>) {
        val eventsHashMap = HashMap<EventDetail, String>()

        CoroutineScope(Dispatchers.IO).launch {
            eventIds?.forEach { eventId ->
                val eventDetailResponse = FirebaseServiceCoroutines.instance.getEventData(eventId)
                withContext(Dispatchers.Main) {
                    if (eventDetailResponse.isSuccessful) {
                        val eventDetail = eventDetailResponse.body()
                        if (eventDetail != null) {
                            eventsHashMap[eventDetail] = eventId
                            events.value = eventsHashMap
                        } else {
                            Log.e("UsersClient", "Event data empty")
                        }
                    } else {
                        Log.e("UsersClient", "Trouble finding event with eventId $eventId")
                    }
                }
            }
        }
    }
}
