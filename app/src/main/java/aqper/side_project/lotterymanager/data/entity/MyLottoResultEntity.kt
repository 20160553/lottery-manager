package aqper.side_project.lotterymanager.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import aqper.side_project.lotterymanager.models.MyLottoResult

@Entity
data class MyLottoResultEntity(
    @PrimaryKey val lid: Int?,
    val round: String = "",
    @ColumnInfo(name = "winning_ammount") val winningAmount: Int,
    val lottoNum: Int,
    var results: List<MyLottoResult>
)
