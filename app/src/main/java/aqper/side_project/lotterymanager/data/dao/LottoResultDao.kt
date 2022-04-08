package aqper.side_project.lotterymanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import aqper.side_project.lotterymanager.data.entity.MyLottoResultEntity
import aqper.side_project.lotterymanager.models.MyLottoResult

@Dao
interface LottoResultDao {
    @Query("SELECT * FROM mylottoresultentity")
    fun getAll(): List<MyLottoResultEntity>

    @Query("SELECT SUM(winning_ammount) FROM mylottoresultentity")
    fun getTotalWinningAmmount(): Int

    @Insert
    fun insert(lottoResult: MyLottoResultEntity)

    @Delete
    fun delete(lottoResult: MyLottoResultEntity)
}