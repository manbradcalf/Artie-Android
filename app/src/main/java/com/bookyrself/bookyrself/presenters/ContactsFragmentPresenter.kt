package com.bookyrself.bookyrself.presenters

import com.bookyrself.bookyrself.data.Contacts.ContactsRepository
import com.bookyrself.bookyrself.data.ServerModels.User.User
import com.bookyrself.bookyrself.views.MainActivity
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class ContactsFragmentPresenter(private val presenterListener: ContactsPresenterListener) : BasePresenter {

    private val contactsRepository: ContactsRepository = MainActivity.getContactsRepo()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var userId: String? = null

    /**
     * Methods
     */
    private fun loadContacts() {
        compositeDisposable
                .add(contactsRepository.getContactsForUser(userId)
                        .subscribe(
                                { stringUserEntry -> presenterListener.contactReturned(stringUserEntry.key, stringUserEntry.value) },
                                { throwable ->
                                    if (throwable is NoSuchElementException) {
                                        presenterListener.noContactsReturned()
                                    } else {
                                        throwable.message?.let { presenterListener.presentError(it) }
                                    }
                                }))
    }


    override fun subscribe() {
        if (FirebaseAuth.getInstance().uid != null) {
            userId = FirebaseAuth.getInstance().uid
            loadContacts()
        } else {
            presenterListener.showSignedOutEmptyState()
        }

    }

    override fun unsubscribe() {
        compositeDisposable.clear()
    }


    /**
     * PresenterListener Definition
     */
    interface ContactsPresenterListener : BasePresenterListener {

        fun noContactsReturned()

        fun contactReturned(id: String, user: User)

        fun presentError(error: String)

    }
}
