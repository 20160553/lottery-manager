package aqper.side_project.lotterymanager.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import aqper.side_project.lotterymanager.MyTypeConverters
import aqper.side_project.lotterymanager.data.dao.LottoResultDao
import aqper.side_project.lotterymanager.data.dao.PensionResultDao
import aqper.side_project.lotterymanager.data.entity.MyLottoResultEntity
import aqper.side_project.lotterymanager.data.entity.MyPensionResultEntity

@Database(entities = [MyLottoResultEntity::class, MyPensionResultEntity::class], version = 1)
@TypeConverters(MyTypeConverters::class)
abstract class MyLotteryDatabase: RoomDatabase() {
    abstract fun myLottoResultDao(): LottoResultDao
    abstract fun myPensionResultDao(): PensionResultDao
}