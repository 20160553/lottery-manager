package aqper.side_project.lotterymanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isGone
import aqper.side_project.lotterymanager.MainActivity.Companion.PENSION_RESULT_KEY
import aqper.side_project.lotterymanager.databinding.ActivityPensionResultBinding
import aqper.side_project.lotterymanager.models.MyPensionNumber
import aqper.side_project.lotterymanager.models.MyPensionResult
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class PensionResultActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityPensionResultBinding

    //코루틴 설정
    private var myPensionResult: MyPensionResult? = null
    private var roundText = ""
    private val job = Job()
    override val coroutineContext get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPensionResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getQrCodeScanResultPension()
    }

    //연금복권 QR 코드 스캔 결과 크롤링 함수
    private fun getQrCodeScanResultPension() {
        if (!MainActivity.isNetworkAvailable(this)) {
            Toast.makeText(this, "당첨 결과를 얻어오는데 실패했습니다.\n인터넷 연결 상태를 확인해주세요.", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val url = intent.getStringExtra(PENSION_RESULT_KEY)
        launch {
            withContext(Dispatchers.IO) {
                try {
                    //크롤링할 주소 지정
                    val jsoup =
                        Jsoup.connect(url)
                    //사이트 크롤링
                    val doc = jsoup.get().select("div.contents")

                    //이전코드
                    //val myNumber: ArrayList<Int> = arrayListOf()
                    val myNumberList: ArrayList<Int> = arrayListOf()
                    val backgroundList = arrayListOf<Boolean>()
                    val bonusBackgroundList = arrayListOf<Boolean>()

                    //회차 추출
                    roundText = doc.select("span.key_clr1").text()

                    //당첨 복권번호 (1등, 보너스)
                    val winNumber: ArrayList<ArrayList<Int>> = arrayListOf()

                    //div 태그의 class="win720_num"인 요소들 파싱
                    val elements: Elements = doc
                        .select("div.win720_num")
                    //당첨 번호 추출
                    for (i in 0..1) {
                        val tempList = arrayListOf<Int>()
                        //보너스 번호는 조 상관 x
                        if (i == 1) {
                            tempList.add(0)
                        } else {
                            //당첨번호의 조 파싱
                            val e = elements[i]
                                //span 태그 class="group"의 하위 span 태그 파싱
                                .select("span.group span")
                                .text().toIntOrNull()
                            //toInt 불가능 할 경우 오류처리.
                            if (e == null)
                                break
                            tempList.add(e)
                        }
                        //당첨 번호 파싱
                        for (j in 1..6) {
                            val temp = elements[i]
                                .select("span.num.al720_color$j span")
                            temp.text().toIntOrNull()?.let { tempList.add(it) }
                        }
                        winNumber.add(tempList)
                    }

                    Log.d("jsoup2", winNumber.toString())

                    val groupNum =
                        elements[2]
                            .select("span.group_list span")
                            .text().toInt()

                    if (winNumber[0][0] == groupNum)
                        backgroundList.add(true)
                    else
                        backgroundList.add(false)

                    //내 번호 추출하기
                    myNumberList.add(
                        groupNum
                    )

                    val result = doc.select("strong.result").text().toString().replace(" 당첨", "").split(" ")
                    Log.d("pensionNumberBonus", winNumber[1].toString())
                    for (i in 1..6) {
                        val temp = elements[2]
                            .select("span.num.al720_color$i span")
                        val num = temp.text().toInt()
                        myNumberList.add(num)
                        if (winNumber[0][i] == myNumberList[i])
                            backgroundList.add(true)
                        else
                            backgroundList.add(false)
                        if (winNumber[1][i] == myNumberList[i])
                            bonusBackgroundList.add(true)
                        else
                            bonusBackgroundList.add(false)
                    }
                    Log.d("jsoup3", myNumberList.toString())
                    myPensionResult = MyPensionResult(
                        result[0],
                        result[1],
                        MyPensionNumber(backgroundList, myNumberList, bonusBackgroundList)
                    )
                    Log.d("pensionNumber", myPensionResult.toString())
                } catch (e: Exception) {
                    Log.e("err", e.toString())
                }
            }
            //after
            displayPensionResult()
        }
    }

    private fun displayPensionResult() {
        myPensionResult ?: return
        binding.roundTextView.text = "연금복권720+ $roundText"
        myPensionResult?.let { result ->
            binding.pensionResultLayout.pensionResultTextView.text = result.winResult
            binding.pensionResultLayout.pensionBonusResultTextView.text = result.bonusResult

            //결과 텍스트 설정
            if (result.winResult == "낙첨" && result.bonusResult == "낙첨") {
                binding.resultTextView2.isGone = true
                binding.resultTextView1.text = "아쉽게도,"
                binding.resultTextView3.text = "낙첨되셨습니다."
            } else {
                binding.resultTextView1.text = "축하합니다!"
                binding.resultTextView3.text = "당첨되셨습니다."
                if (result.winResult != "낙첨") {
                    binding.resultTextView2.text = result.winResult
                } else {
                    binding.resultTextView2.text = result.bonusResult
                }
            }

            //본 번호 레이아웃
            binding.pensionResultLayout.pensionGroupNumber.text =
                result.myPensionNumbers.numberList[0].toString()
            binding.pensionResultLayout.pensionFirstNumber.text =
                result.myPensionNumbers.numberList[1].toString()
            binding.pensionResultLayout.pensionSecondNumber.text =
                result.myPensionNumbers.numberList[2].toString()
            binding.pensionResultLayout.pensionThirdNumber.text =
                result.myPensionNumbers.numberList[3].toString()
            binding.pensionResultLayout.pensionFourthNumber.text =
                result.myPensionNumbers.numberList[4].toString()
            binding.pensionResultLayout.pensionFifthNumber.text =
                result.myPensionNumbers.numberList[5].toString()
            binding.pensionResultLayout.pensionSixthNumber.text =
                result.myPensionNumbers.numberList[6].toString()

            //보너스 레이아웃
            binding.pensionResultLayout.pensionBonusFirstNumber.text =
                result.myPensionNumbers.numberList[1].toString()
            binding.pensionResultLayout.pensionBonusSecondNumber.text =
                result.myPensionNumbers.numberList[2].toString()
            binding.pensionResultLayout.pensionBonusThirdNumber.text =
                result.myPensionNumbers.numberList[3].toString()
            binding.pensionResultLayout.pensionBonusFourthNumber.text =
                result.myPensionNumbers.numberList[4].toString()
            binding.pensionResultLayout.pensionBonusFifthNumber.text =
                result.myPensionNumbers.numberList[5].toString()
            binding.pensionResultLayout.pensionBonusSixthNumber.text =
                result.myPensionNumbers.numberList[6].toString()

            //일치하는 번호 배경색 지정
            for (i in 0..6) {
                val resource = applicationContext.resources.getIdentifier(
                    "lottery_number_shape_$i",
                    "drawable",
                    applicationContext.packageName
                )
                when (i) {
                    0 -> {
                        if (result.myPensionNumbers.backgroundList[0])
                            binding.pensionResultLayout.pensionGroupNumber.setBackgroundResource(
                                resource
                            )
                    }
                    1 -> {
                        if (result.myPensionNumbers.backgroundList[1])
                            binding.pensionResultLayout.pensionFirstNumber.setBackgroundResource(
                                resource
                            )
                        if (result.myPensionNumbers.bonusBackgroundList[0])
                            binding.pensionResultLayout.pensionBonusFirstNumber.setBackgroundResource(
                                resource
                            )
                    }
                    2 -> {
                        if (result.myPensionNumbers.backgroundList[2])
                            binding.pensionResultLayout.pensionSecondNumber.setBackgroundResource(
                                resource
                            )
                        if (result.myPensionNumbers.bonusBackgroundList[1])
                            binding.pensionResultLayout.pensionBonusSecondNumber.setBackgroundResource(
                                resource
                            )
                    }
                    3 -> {
                        if (result.myPensionNumbers.backgroundList[3])
                            binding.pensionResultLayout.pensionThirdNumber.setBackgroundResource(
                                resource
                            )
                        if (result.myPensionNumbers.bonusBackgroundList[2])
                            binding.pensionResultLayout.pensionBonusThirdNumber.setBackgroundResource(
                                resource
                            )
                    }
                    4 -> {
                        if (result.myPensionNumbers.backgroundList[4])
                            binding.pensionResultLayout.pensionFourthNumber.setBackgroundResource(
                                resource
                            )
                        if (result.myPensionNumbers.bonusBackgroundList[3])
                            binding.pensionResultLayout.pensionBonusFourthNumber.setBackgroundResource(
                                resource
                            )
                    }
                    5 -> {
                        if (result.myPensionNumbers.backgroundList[5])
                            binding.pensionResultLayout.pensionFifthNumber.setBackgroundResource(
                                resource
                            )
                        if (result.myPensionNumbers.bonusBackgroundList[4])
                            binding.pensionResultLayout.pensionBonusFifthNumber.setBackgroundResource(
                                resource
                            )
                    }
                    6 -> {
                        if (result.myPensionNumbers.backgroundList[6])
                            binding.pensionResultLayout.pensionSixthNumber.setBackgroundResource(
                                resource
                            )
                        if (result.myPensionNumbers.bonusBackgroundList[5])
                            binding.pensionResultLayout.pensionBonusSixthNumber.setBackgroundResource(
                                resource
                            )
                    }
                }
            }
        }
    }
}