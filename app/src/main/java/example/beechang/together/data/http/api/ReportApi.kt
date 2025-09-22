package example.beechang.together.data.http.api

import example.beechang.together.data.request.CreateInquiryRequest
import example.beechang.together.data.request.ReportUserRequest
import example.beechang.together.data.response.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportApi {

    @POST("api/report/user")
    suspend fun reportUser(
        @Body request: ReportUserRequest,
    ): Response<BaseResponse>

    @POST("api/report/inquiry")
    suspend fun createInquiry(
        @Body request: CreateInquiryRequest,
    ): Response<BaseResponse>
}