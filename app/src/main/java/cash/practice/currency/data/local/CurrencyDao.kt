package cash.practice.currency.data.local

import androidx.room.*
import cash.practice.currency.model.Rate

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM RateTable ORDER BY currency ASC")
    suspend fun getAll(): List<Rate>

    @Query("UPDATE RateTable SET rate = :rate WHERE currency = :currency")
    fun updateRateByCurrency(currency: String, rate: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRate(data: Rate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<Rate>)


}