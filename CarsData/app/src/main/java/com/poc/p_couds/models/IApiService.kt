package com.poc.p_couds.models

import com.poc.p_couds.pojo.Authenticate
import com.poc.p_couds.pojo.ChangeStatus
import com.poc.p_couds.pojo.ChangeStatusSession
import com.poc.p_couds.pojo.ECU
import com.poc.p_couds.pojo.FileNode
import com.poc.p_couds.pojo.GetIdentifiers
import com.poc.p_couds.pojo.ReadAccesTiming
import com.poc.p_couds.pojo.ReadAccessTimingPost
import com.poc.p_couds.pojo.ReadDtc
import com.poc.p_couds.pojo.ResetEcu
import com.poc.p_couds.pojo.ResetEcuPost
import com.poc.p_couds.pojo.TesterPresent
import com.poc.p_couds.pojo.UpdateHistory
import com.poc.p_couds.pojo.UpdateV
import com.poc.p_couds.pojo.UpdateVResponse
import com.poc.p_couds.pojo.VINResponse
import com.poc.p_couds.pojo.WriteTiming
import com.poc.p_couds.pojo.WriteTimingPost
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface IApiService {

    //UDS
    @GET("api/read_info_battery")
    fun getInfoBattery(
        @Query("is_manual_flow") isManualFlow: Boolean,
        @Query("item") item: String
    ): Call<Any>

    @GET("api/read_info_engine?is_manual_flow=false")
    fun getInfoEngine(
        @Query("is_manual_flow") isManualFlow: Boolean,
        @Query("item") item: String
    ): Call<Any>

    @GET("api/read_info_doors?is_manual_flow=false")
    fun getInfoDoors(
        @Query("is_manual_flow") isManualFlow: Boolean,
        @Query("item") item: String
    ): Call<Any>

    @GET("api/read_info_hvac?is_manual_flow=false")
    fun getInfoHVAC(
        @Query("is_manual_flow") isManualFlow: Boolean,
        @Query("item") item: String
    ): Call<Any>

    @POST("api/write_info_battery")
    suspend fun writeInfoBattery(
        @Body request: Any
    ): Response<Any>

    @POST("api/write_info_engine")
    suspend fun writeInfoEngine(
        @Body request: Any
    ): Response<Any>

    @POST("api/write_info_doors")
    suspend fun writeInfoDoors(
        @Body request: Any
    ): Response<Any>

    @POST("api/write_info_hvac")
    suspend fun writeInfoHVAC(
        @Body request: Any
    ): Response<Any>

    @POST("api/write_info_battery")
    fun writeInfoBatteryForJavaCompatibility(
        @Body request: Any
    ): Call<Any>

    @POST("api/write_info_engine")
    fun writeInfoEngineForJavaCompatibility(
        @Body request: Any
    ): Call<Any>

    @POST("api/write_info_doors")
    fun writeInfoDoorsForJavaCompatibility(
        @Body request: Any
    ): Call<Any>

    @POST("api/write_info_hvac")
    fun writeInfoHVACForJavaCompatibility(
        @Body request: Any
    ): Call<Any>

    @GET("api/read_dtc_info")
    fun getBatteryDTC(): Call<Any>

    //MAIN

    @GET("DecodeVinValues/{vin}?format=json")
    fun getVinDetails(@Path("vin") vin:String): Call<VINResponse>

    // OTA
    @GET("/api/request_ids")
    fun requestListOfEcus(): Call<ECU>

    @POST("/api/update_to_version")
    fun updateVersion(@Body updateV: UpdateV): Call<UpdateVResponse>

    @GET("/api/drive_update_data")
    fun requestListOfVersions(): Call<FileNode>

    @GET("/api/history_updates")
    fun requestListOfUpdatesHistory(): Call<List<UpdateHistory>>

    @POST("/api/change_session")
    fun requestChangeStatus(@Body changeStatusSession: ChangeStatusSession): Call<ChangeStatus>

    @GET("/api/read_dtc_info")
    fun requestReadDtcInfo(): Call<ReadDtc>

    @GET("/api/authenticate")
    fun requestAuthenticate(): Call<Authenticate>

    @POST("/api/read_access_timing")
    fun requestReadAccessTiming(@Body readAccessTimingPost: ReadAccessTimingPost): Call<ReadAccesTiming>

    @GET("/api/tester_present")
    fun requestTesterPresent(): Call<TesterPresent>

    @POST("/api/reset_ecu")
    fun requestResetEcu(@Body resetEcuPost: ResetEcuPost): Call<ResetEcu>

    @GET("/api/get_identifiers")
    fun requestGetIdentifiers(): Call<GetIdentifiers>

    @POST("/api/write_timing")
    fun requestWriteTiming(@Body writeTimingPost: WriteTimingPost): Call<WriteTiming>
}