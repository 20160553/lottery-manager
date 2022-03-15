package aqper.side_project.lotterymanager

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var currentLottoRound: String = ""
    var currentPensionRound: String = ""
    var currentLottoWinArray: ArrayList<Int> = arrayListOf()
    var currentPensionWinArray: ArrayList<Int> = arrayListOf()
    var currentPensionBonusArray: ArrayList<Int> = arrayListOf()
}