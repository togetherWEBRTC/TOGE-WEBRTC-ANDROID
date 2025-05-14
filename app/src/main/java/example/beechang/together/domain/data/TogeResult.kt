package example.beechang.together.domain.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


sealed class TogeResult<out R> {
    data class Success<out T>(val data: T) : TogeResult<T>()
    data class Error(
        val togeError: TogeError? = null,
        val msg: String? = null,
        val exception: Exception? = null
    ) : TogeResult<Nothing>()

    fun onSuccess(block: (R) -> Unit): TogeResult<R> {
        if (this is Success) block(data)
        return this
    }

    fun onError(block: (TogeError?, String?, Exception?) -> Unit): TogeResult<R> {
        if (this is Error) block(togeError, msg, exception)
        return this
    }

    fun onFinally(block: () -> Unit): TogeResult<R> {
        block()
        return this
    }

    fun getOrNull(): R? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun getOrDefault(defaultValue: @UnsafeVariance R): R = when (this) {
        is Success -> data
        is Error -> defaultValue
    }

    fun isSuccess(): Boolean = this is Success

    fun isError(): Boolean = this is Error
}

fun <T, R> TogeResult<T>.map(mapping: (T) -> R): TogeResult<R> {
    return when (this) {
        is TogeResult.Success -> TogeResult.Success(mapping(data))
        is TogeResult.Error -> TogeResult.Error(togeError, msg, exception)
    }
}

/**
 * [사용 전]
 * ```
 * val flow = flow { emit(apiToResult { apiService.getData(id) }) }
 * val transformedFlow = flow.map { result ->
 *     when (result) {
 *         is TogeResult.Success -> TogeResult.Success(transformData(result.data))
 *         is TogeResult.Error -> TogeResult.Error(result.togeError, result.msg, result.exception)
 *     }
 * }
 * ```
 *
 * [사용 후]
 * ```
 * val transformedFlow = flow { emit(apiToResult { apiService.getData(id) }) }
 *     .mapToge { transformData(it) }
 * ```
 */
fun <T, R> Flow<TogeResult<T>>.mapToge(transform: (T) -> R): Flow<TogeResult<R>> {
    return this.map { result ->
        when (result) {
            is TogeResult.Success -> TogeResult.Success(transform(result.data))
            is TogeResult.Error -> TogeResult.Error(result.togeError, result.msg, result.exception)
        }
    }
}