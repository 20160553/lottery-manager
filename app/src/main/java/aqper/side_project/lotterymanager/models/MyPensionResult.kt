package aqper.side_project.lotterymanager.models

import androidx.room.ColumnInfo

data class MyPensionResult(
    @ColumnInfo(name="win_result") val winResult: String,
    @ColumnInfo(name="bonus_result") val bonusResult: String,
    @ColumnInfo(name="background_list") val backgroundList: List<Boolean>,
    @ColumnInfo(name="number_list") val numberList: List<Int>,
    @ColumnInfo(name="bonus_background_list") val bonusBackgroundList: List<Boolean>
)