package com.example.monitoringbatuk.model


import com.google.gson.annotations.SerializedName

data class Sample(
    @SerializedName("originalIntervalMs")
    val originalIntervalMs: Double = 0.0,
    @SerializedName("added")
    val added: String = "",
    @SerializedName("isProcessing")
    val isProcessing: Boolean = false,
    @SerializedName("deviceName")
    val deviceName: String = "",
    @SerializedName("frequency")
    val frequency: Int = 0,
    @SerializedName("intervalMs")
    val intervalMs: Double = 0.0,
    @SerializedName("signatureValidate")
    val signatureValidate: Boolean = false,
    @SerializedName("signatureKey")
    val signatureKey: String = "",
    @SerializedName("originalFrequency")
    val originalFrequency: Int = 0,
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("isDisabled")
    val isDisabled: Boolean = false,
    @SerializedName("deviceType")
    val deviceType: String = "",
    @SerializedName("created")
    val created: String = "",
    @SerializedName("signatureMethod")
    val signatureMethod: String = "",
    @SerializedName("label")
    val label: String = "",
    @SerializedName("totalLengthMs")
    val totalLengthMs: Double = 0.0,
    @SerializedName("valuesCount")
    val valuesCount: Int = 0,
    @SerializedName("boundingBoxesType")
    val boundingBoxesType: String = "",
    @SerializedName("filename")
    val filename: String = "",
    @SerializedName("sensors")
    val sensors: List<SensorsItem>?,
    @SerializedName("chartType")
    val chartType: String = "",
    @SerializedName("coldstorageFilename")
    val coldstorageFilename: String = "",
    @SerializedName("category")
    val category: String = "",
    @SerializedName("processingError")
    val processingError: Boolean = false,
)


data class Payload(
    @SerializedName("crop_start")
    val cropStart: Int = 0,
    @SerializedName("device_name")
    val deviceName: String = "",
    @SerializedName("sensors")
    val sensors: List<SensorsItem>?,
    @SerializedName("values")
    val values: ArrayList<List<Int>>?,
    @SerializedName("crop_end")
    val cropEnd: Int = 0,
    @SerializedName("device_type")
    val deviceType: String = "",
)


data class RawResultResponse(
    @SerializedName("totalPayloadLength")
    val totalPayloadLength: Int = 0,
    @SerializedName("payload")
    val payload: Payload,
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("sample")
    val sample: Sample,
)


data class SensorsItem(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("units")
    val units: String = "",
)


