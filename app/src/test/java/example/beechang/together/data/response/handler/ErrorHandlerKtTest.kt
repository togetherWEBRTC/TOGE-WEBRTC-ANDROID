package example.beechang.together.data.response.handler

import example.beechang.together.data.response.BaseResponse
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.data.TogeResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import retrofit2.Response


class ErrorHandlerKtTest {

    /**
     * @see [serverErrorCodeToTogeError]
     */
    @DisplayName("serverErrorCodeToTogeError 함수 테스트")
    class ServerErrorCodeToTogeErrorTest {

        @Nested
        @DisplayName("Given: 서버 오류 코드 1")
        inner class GivenServerErrorCode1 {
            private val errorCode = 1
            private val testMsg = "SERVER_ERROR"

            @Nested
            @DisplayName("When: 오류 코드를 TogeError로 변환")
            inner class WhenConvertingToTogeError {
                private lateinit var result: TogeError

                @BeforeEach
                fun setup() {
                    result = serverErrorCodeToTogeError(errorCode, testMsg)
                }

                @Test
                @DisplayName("Then: ServerError 타입을 반환")
                fun thenShouldReturnServerErrorType() {
                    assertTrue(result is TogeError.ServerError)
                }
            }
        }

        @Nested
        @DisplayName("Given: 알 수 없는 서버 오류 코드 9999")
        inner class GivenUnknownServerErrorCode {
            private val errorCode = 9999
            private val testMsg = "UNKNOWN_SERVER_ERROR"

            @Nested
            @DisplayName("When: 오류 코드를 TogeError로 변환")
            inner class WhenConvertingToTogeError {
                private lateinit var result: TogeError

                @BeforeEach
                fun setup() {
                    result = serverErrorCodeToTogeError(errorCode, testMsg)
                }

                @Test
                @DisplayName("Then: UnknownError 타입을 반환")
                fun thenShouldReturnUnknownErrorType() {
                    assertTrue(result is TogeError.UnknownError)
                }

                @Test
                @DisplayName("Then: 올바른 에러 메시지를 포함")
                fun thenShouldContainCorrectMessage() {
                    assertEquals(testMsg, (result as TogeError.UnknownError).msg)
                }
            }
        }
    }


    /**
     * @see [apiToResult]
     */
    @DisplayName("apiToResult 함수 테스트")
    class ApiToResultTest {

        @Nested
        @DisplayName("Given: API 호출 성공 & 서버 응답 코드 0 (정상)")
        inner class GivenApiCallSuccessWithCodeZero {
            private val successResponse = BaseResponse(code = 0, message = "success")

            @Nested
            @DisplayName("When: API 응답을 TogeResult로 변환")
            inner class WhenConvertApiResponseToResult {

                @Test
                @DisplayName("Then: TogeResult.Success 반환 & 올바른 데이터 포함")
                fun thenReturnSuccessWithCorrectData() = runBlocking {
                    val mockResponse = Response.success(successResponse)
                    val apiCall: suspend () -> Response<BaseResponse> = { mockResponse }

                    val result = apiToResult(apiCall)

                    assertTrue(result is TogeResult.Success)
                    val successResult = result as TogeResult.Success
                    assertEquals(successResponse, successResult.data)
                    assertTrue(successResult.data.toSuccessBoolean())
                }
            }
        }

        @Nested
        @DisplayName("Given: API 호출 성공 & 서버 응답 코드 1 (서버 오류)")
        inner class GivenApiCallSuccessWithServerError {
            private val errorResponse = BaseResponse(code = 1, message = "SERVER_ERROR")

            @Nested
            @DisplayName("When: API 응답을 TogeResult로 변환")
            inner class WhenConvertApiResponseToResult {

                @Test
                @DisplayName("Then: TogeResult.Error 반환 & ServerError 타입")
                fun thenReturnErrorWithServerErrorType() = runBlocking {
                    val mockResponse = Response.success(errorResponse)
                    val apiCall: suspend () -> Response<BaseResponse> = { mockResponse }

                    val result = apiToResult(apiCall)

                    assertTrue(result is TogeResult.Error)
                    val errorResult = result as TogeResult.Error
                    assertTrue(errorResult.togeError is TogeError.ServerError)
                    assertEquals(errorResponse.message, errorResult.msg)
                    assertFalse(errorResponse.toSuccessBoolean())
                }
            }
        }

        @Nested
        @DisplayName("Given: API 호출 성공 & 응답 body가 null")
        inner class GivenApiCallSuccessWithNullBody {

            @Nested
            @DisplayName("When: API 응답을 TogeResult로 변환")
            inner class WhenConvertApiResponseToResult {

                @Test
                @DisplayName("Then: TogeResult.Error 반환 & UnknownError 타입")
                fun thenReturnErrorWithUnknownErrorType() = runTest {
                    val mockResponse: Response<BaseResponse> = Response.success(null)
                    val apiCall: suspend () -> Response<BaseResponse> = { mockResponse }

                    val result = apiToResult(apiCall)

                    assertTrue(result is TogeResult.Error)
                    val errorResult = result as TogeResult.Error
                    assertTrue(errorResult.togeError is TogeError.UnknownError)
                }
            }
        }

        @Nested
        @DisplayName("Given: API 호출 실패 (HTTP 오류)")
        inner class GivenApiCallFailure {

            @Nested
            @DisplayName("When: API 응답을 TogeResult로 변환")
            inner class WhenConvertApiResponseToResult {

                @Test
                @DisplayName("Then: TogeResult.Error 반환 & NetworkError 타입")
                fun thenReturnErrorWithNetworkErrorType() = runTest {
                    val errorResponseBody = "Network error".toResponseBody("text/plain".toMediaTypeOrNull())
                    val mockResponse = Response.error<BaseResponse>(404, errorResponseBody)
                    val apiCall: suspend () -> Response<BaseResponse> = { mockResponse }

                    val result = apiToResult(apiCall)

                    assertTrue(result is TogeResult.Error)
                    val errorResult = result as TogeResult.Error
                    assertTrue(errorResult.togeError is TogeError.NetworkError)
                }
            }
        }

        @Nested
        @DisplayName("Given: API 호출 중 예외 발생")
        inner class GivenApiCallException {
            private val testException = Exception("Test exception")

            @Nested
            @DisplayName("When: API 응답을 TogeResult로 변환")
            inner class WhenConvertApiResponseToResult {

                @Test
                @DisplayName("Then: TogeResult.Error 반환 & NetworkError 타입 & 예외 객체 포함")
                fun thenReturnErrorWithNetworkErrorAndException() = runTest {
                    val apiCall: suspend () -> Response<BaseResponse> = { throw testException }
                    val result = apiToResult(apiCall)

                    assertTrue(result is TogeResult.Error)
                    val errorResult = result as TogeResult.Error
                    assertTrue(errorResult.togeError is TogeError.NetworkError)
                    assertEquals(testException, errorResult.exception)
                }
            }
        }
    }
}