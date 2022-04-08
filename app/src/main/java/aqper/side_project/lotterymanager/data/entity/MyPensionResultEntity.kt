package aqper.side_project.lotterymanager.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import aqper.side_project.lotterymanager.models.MyPensionResult

@Entity
data class MyPensionResultEntity(
    @PrimaryKey val pid: Int?,
    val round: String = "",
    @Embedded val result: MyPensionResult
)
