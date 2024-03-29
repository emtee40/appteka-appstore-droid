package com.tomclaw.appsend.screen.rate

import android.os.Bundle
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.Locale

interface RatePresenter {

    fun attachView(view: RateView)

    fun detachView()

    fun attachRouter(router: RateRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface RateRouter {

        fun leaveScreen(success: Boolean)

    }

}

class RatePresenterImpl(
    private val appId: String,
    private val userBrief: UserBrief,
    startRating: Float,
    startReview: String,
    private val interactor: RateInteractor,
    private val locale: Locale,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : RatePresenter {

    private var view: RateView? = null
    private var router: RatePresenter.RateRouter? = null

    private var rating: Float = state?.getFloat(KEY_RATING) ?: startRating
    private var review: String = state?.getString(KEY_REVIEW) ?: startReview

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: RateView) {
        this.view = view

        bindData()

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.ratingChanged().subscribe {
            rating = it
            bindData()
        }
        subscriptions += view.reviewEditChanged().subscribe { review = it }
        subscriptions += view.submitClicks().subscribe { onSubmitReview() }

        bindMemberInfo(userBrief)
    }

    private fun bindData() {
        with(view ?: return) {
            setRating(rating)
            setReview(review)
            if (rating > 0) {
                enableSubmitButton()
            } else {
                disableSubmitButton()
            }
        }
    }

    private fun onSubmitReview() {
        subscriptions += interactor.submitReview(appId, rating, review)
            .doOnSubscribe { view?.showProgress() }
            .observeOn(schedulers.mainThread())
            .subscribe(
                { onReviewSubmitted() },
                { onReviewSubmitError() }
            )
    }

    private fun onReviewSubmitted() {
        router?.leaveScreen(success = true)
    }

    private fun onReviewSubmitError() {
        view?.showContent()
        view?.showError()
    }

    private fun bindMemberInfo(userBrief: UserBrief) {
        view?.setMemberIcon(userBrief.userIcon)
        val name = userBrief.name.takeIf { !it.isNullOrBlank() }
            ?: userBrief.userIcon.label[locale.language]
            ?: userBrief.userIcon.label[DEFAULT_LOCALE].orEmpty()
        view?.setMemberName(name)
    }

    override fun detachView() {
        this.view = null
    }

    override fun attachRouter(router: RatePresenter.RateRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
        putFloat(KEY_RATING, rating)
        putString(KEY_REVIEW, review)
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false)
    }

}

private const val KEY_RATING = "rating"
private const val KEY_REVIEW = "review"
