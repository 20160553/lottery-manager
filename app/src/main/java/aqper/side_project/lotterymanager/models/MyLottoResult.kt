package aqper.side_project.lotterymanager.models

import androidx.room.ColumnInfo

data class MyLottoResult(
    @ColumnInfo(name="win_lank") val winLank: String,
    @ColumnInfo(name="background_list") val backgroundList: List<Int>,
    @ColumnInfo(name="number_list") val numberList: List<Int>
)