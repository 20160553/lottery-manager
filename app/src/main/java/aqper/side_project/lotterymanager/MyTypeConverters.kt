package aqper.side_project.lotterymanager

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import aqper.side_project.lotterymanager.models.MyLottoResult
import com.google.gson.Gson

@ProvidedTypeConverter
class MyTypeConverters {
    @TypeConverter
    fun fromMyLottoResultList(value: List<MyLottoResult>): String = Gson().toJson(value)
    @TypeConverter
    fun toMyLottoResultList(value: String) = Gson().fromJson(value, Array<MyLottoResult>::class.java).toList()
    @TypeConverter
    fun fromBooleanList(value: List<Boolean>): String = Gson().toJson(value)
    @TypeConverter
    fun toBooleanList(value: String) = Gson().fromJson(value, Array<Boolean>::class.java).toList()
    @TypeConverter
    fun fromIntList(value: List<Int>): String = Gson().toJson(value)
    @TypeConverter
    fun toIntList(value: String) = Gson().fromJson(value, Array<Int>::class.java).toList()
}