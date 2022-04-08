package aqper.side_project.lotterymanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import aqper.side_project.lotterymanager.data.entity.MyPensionResultEntity

@Dao
interface PensionResultDao {
    @Query("SELECT * FROM mypensionresultentity")
    fun getAll(): List<MyPensionResultEntity>

    @Insert
    fun insert(pensionResult: MyPensionResultEntity)

    @Delete
    fun delete(pensionResult: MyPensionResultEntity)
}