package aqper.side_project.lotterymanager.data.database

import android.content.Context
import androidx.room.Room
import aqper.side_project.lotterymanager.MyTypeConverters

object DatabaseProvider {
    private const val DB_NAME = "lottery_manager.db"
    private val converters = MyTypeConverters()

    fun provideDB(applicationContext: Context) = Room.databaseBuilder(
        applicationContext,
        MyLotteryDatabase::class.java, DB_NAME
    )
        .addTypeConverter(converters)
        .build()

}