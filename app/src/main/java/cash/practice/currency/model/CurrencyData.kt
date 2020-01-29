package cash.practice.currency.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class CurrencyList(
    @SerializedName("success") val success: Boolean,
    @SerializedName("terms") val terms: String,
    @SerializedName("privacy") val privacy: String,
    @SerializedName("currencies") val currencies: HashMap<String, String>,
    @SerializedName("error") val error: CurrencyLayerError
)

data class LiveRateList(
    @SerializedName("success") val success: Boolean,
    @SerializedName("terms") val terms: String,
    @SerializedName("privacy") val privacy: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("source") val source: String,
    @SerializedName("quotes") val quotes: HashMap<String, Double>?,
    @SerializedName("error") val error: CurrencyLayerError
) {
    override fun toString(): String {
        return "success: $success, timestamp: $timestamp, quotes size: ${quotes?.size}"
    }
}

data class CurrencyLayerError(
    @SerializedName("code") val code: Int,
    @SerializedName("info") val info: String
)

@Entity(tableName = "RateTable")
data class Rate(
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "currency")
    val currency: String,

    @ColumnInfo(name = "currencyName")
    val currencyName: String?,

    @ColumnInfo(name = "rate")
    val rate: Double? = 0.0
) {
    override fun toString(): String {
        return "$currency - $currencyName - $rate"
    }
}