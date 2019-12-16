package com.bookyrself.bookyrself.presenters

import android.util.Log
import com.bookyrself.bookyrself.data.serverModels.SearchRequest.*
import com.bookyrself.bookyrself.data.serverModels.SearchRequest.Date
import com.bookyrself.bookyrself.data.serverModels.SearchResponseEvents.SearchResponse2
import com.bookyrself.bookyrself.data.serverModels.SearchResponseUsers.SearchResponseUsers
import com.bookyrself.bookyrself.services.SearchService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * Created by benmedcalf on 9/16/17.
 */

class SearchPresenter
/**
 * Constructor
 */
(private val listener: SearchPresenterListener) {
    private val service: SearchService = SearchService()

    /**
     * Methods
     */
    fun executeSearch(searchType: Int, what: String, where: String, fromWhen: String?, toWhen: String?) {
        listener.showProgressbar(true)
        val query = createQuery(what, where, fromWhen, toWhen)
        val body = RequestBody()
        body.query = query
        body.setSize(100)
        if (searchType == EVENT_SEARCH_FLAG) {
            service.api.executeEventsSearch(body)
                    .enqueue(object : Callback<SearchResponse2> {
                        override fun onResponse(call: Call<SearchResponse2>, response: Response<SearchResponse2>) {
                            if (response.body() != null) {
                                val hits = response.body()!!.hits.hits
                                listener.searchEventsResponseReady(hits)
                            }
                        }

                        override fun onFailure(call: Call<SearchResponse2>, t: Throwable) {
                            Log.e(javaClass.toString(), call.request().body!!.toString())
                            Log.e(javaClass.toString(), t.message)
                            listener.showError()
                        }
                    })
        } else {
            service.api.executeUsersSearch(body)
                    .enqueue(object : Callback<SearchResponseUsers> {
                        override fun onResponse(call: Call<SearchResponseUsers>, response: Response<SearchResponseUsers>) {
                            if (response.body() != null) {
                                val hits = response.body()!!.hits.hits
                                listener.searchUsersResponseReady(hits)
                            } else if (response.errorBody() != null) {
                                listener.showError()
                            }
                        }

                        override fun onFailure(call: Call<SearchResponseUsers>, t: Throwable) {
                            Log.e(javaClass.toString(), call.request().body!!.toString())
                            Log.e(javaClass.toString(), t.message)
                            listener.showError()
                        }
                    })
        }
    }

    private fun createQuery(what: String, where: String, fromWhen: String?, toWhen: String?): Query {
        val fields = listOf("username", "tags", "eventname")
        val query = Query()
        val bool = Bool()
        val musts = ArrayList<Must>()

        // Set the "Where"
        if (where != "") {
            val must1 = Must()
            val match1 = Match()
            match1.citystate = where
            must1.match = match1
            musts.add(must1)
        }

        // Set the "what"
        if (what != "") {
            val must2 = Must()
            val multiMatch = MultiMatch()
            multiMatch.fields = fields
            multiMatch.query = what
            must2.multiMatch = multiMatch
            musts.add(must2)
        }

        if (musts.isNotEmpty()) {
            bool.must = musts
        }
        query.bool = bool

        if (toWhen != null && fromWhen != null) {
            val filter = Filter()
            val bool_ = Bool_()
            val must_ = Must_()
            val range = Range()
            val date = Date()
            date.lte = toWhen
            date.gte = fromWhen
            range.date = date
            must_.range = range
            bool_.must = must_
            filter.bool = bool_
            bool.filter = filter
        }
        return query
    }

    fun setStartDate(date: String) {
        listener.startDateChanged(date)
    }

    fun setEndDate(date: String) {
        listener.endDateChanged(date)
    }

    fun clearStartDate() {
        listener.startDateChanged("From")
    }

    fun clearEndDate() {
        listener.endDateChanged("To")
    }

    /**
     * Contract / Listener
     */
    interface SearchPresenterListener {
        fun searchEventsResponseReady(hits: MutableList<com.bookyrself.bookyrself.data.serverModels.SearchResponseEvents.Hit>)

        fun searchUsersResponseReady(hits: MutableList<com.bookyrself.bookyrself.data.serverModels.SearchResponseUsers.Hit>)

        fun startDateChanged(date: String)

        fun endDateChanged(date: String)

        fun showProgressbar(bool: Boolean?)

        fun itemSelected(id: String, flag: Int)

        fun showError()
    }

    companion object {
        private val EVENT_SEARCH_FLAG = 1
    }
}
