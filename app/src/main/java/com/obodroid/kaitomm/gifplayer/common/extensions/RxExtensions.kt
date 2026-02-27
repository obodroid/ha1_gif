package com.obodroid.kaitomm.gifplayer.common.extensions

import android.util.Log
import com.obodroid.kaitomm.gifplayer.common.LoggerFactory
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

val logger = LoggerFactory.getLogger("subscribeEx:onError")

fun <T> Observable<T>.applySchedulers(delayError: Boolean = false): Observable<T> {
    return subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread(), delayError)
}

fun <T> Flowable<T>.applySchedulers(delayError: Boolean = false): Flowable<T> {
    return subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread(), delayError)
}

fun <T> Single<T>.applySchedulers(): Single<T> {
    return subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun Completable.applySchedulers(): Completable {
    return subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T> Maybe<T>.applySchedulers(): Maybe<T> {
    return subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T : Any> Single<T>.subscribeEx(
        onSuccess: ((T) -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
): Disposable {
    return subscribeBy(
            onSuccess = {
                onSuccess?.invoke(it)
            },
            onError = {
                onError?.invoke(it)
                logger.error(it.message!!)
            }
    )
}

fun <T : Any> Flowable<T>.subscribeEx(
        onNext: ((T) -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
        onComplete: (() -> Unit)? = null
): Disposable {
    return subscribeBy(
            onNext = {
                onNext?.invoke(it)
            },
            onError = {
                onError?.invoke(it)
                logger.error(it.message!!)
            },
            onComplete = {
                onComplete?.invoke()
            }
    )
}

fun Completable.subscribeEx(onComplete: (() -> Unit)? = null, onError: ((Throwable) -> Unit)? = null): Disposable {
    return subscribeBy(
            onComplete = {
                onComplete?.invoke()
            },
            onError = {
                onError?.invoke(it)
                logger.error(it.message!!)
            }
    )
}