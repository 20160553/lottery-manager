package aqper.side_project.lotterymanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isGone
import aqper.side_project.lotterymanager.databinding.ActivityMainBinding
import aqper.side_project.lotterymanager.models.MyLottoNumber
import aqper.side_project.lotterymanager.models.MyLottoResult
import aqper.side_project.lotterymanager.models.MyPensionNumber
import aqper.side_project.lotterymanager.models.MyPensionResult
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.*
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityMainBinding
    private lateinit var qrcodeLauncher: ActivityResultLauncher<ScanOptions>

    private val mainViewModel = MainViewModel()

    //코루틴 설정
    private val job = Job()
    override val coroutineContext get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initQRCodeScanner()
        initViews()
        getRecentResult()
        getQrCodeScanResultLotto()
        getQrCodeScanResultPension()
    }

    private fun initViews() = with(binding) {
        val options = ScanOptions().apply {
            //QR 코드만 인식
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            //인식시 소리 설정 x
            setBeepEnabled(false)
            //안내 메세지 설정
            setPrompt("QR코드를 인증해주세요.")
            //핸드폰 방향에 맞게 변경
            setOrientationLocked(false)
        }
        refreshButton.setOnClickListener {
            getRecentResult()
        }
        qrcodeLaunchButton.setOnClickListener {
            qrcodeLauncher.launch(options)
        }
        lotteryRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (i == R.id.pensionRadioButton) {
                //연금복권
                binding.roundTextView.text = mainViewModel.currentPensionRound
                pensionLayout.root.isGone = false
                lottoLayout.root.isGone = true
            } else {
                //로또복권
                binding.roundTextView.text = mainViewModel.currentLottoRound
                pensionLayout.root.isGone = true
                lottoLayout.root.isGone = false
            }
        }
    }

    private fun initQRCodeScanner() {
        qrcodeLauncher = registerForActivityResult(ScanContract()) { result ->
            if (result.contents == null) {
                //QR Scanning Failed
                Toast.makeText(this, "QR코드 인증이 취소되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                //QR Scanning Successed
                Toast.makeText(this, "${result.contents}", Toast.LENGTH_SHORT).show()
                getQrCodeScanResultPension()
            }
        }
    }

    private fun getRecentResult() {
        launch {
            val pensionArray = arrayListOf<Int>()
            val pensionBonusArray = arrayListOf<Int>()
            val lottoArray = arrayListOf<Int>()
            withContext(Dispatchers.IO) {
                try {
                    //크롤링할 주소 지정
                    val jsoup =
                        Jsoup.connect("https://m.dhlottery.co.kr/common.do?method=main")
                    //사이트 크롤링
                    val doc: Document = jsoup.get()
                    val elements: Elements = doc
                        .select("div.slide_item")
                    Log.d("recentResult", elements.toString())

                    //로또 최근 당첨회차 크롤링
                    mainViewModel.currentLottoRound =
                        elements[0].select("strong.round.key_clr1").text()
                    for (i in 1..6) {
                        if (i != 6) {
                            //십의 자리가 i인 번호 얻어오기
                            elements[0].select("div.num.clr$i span").text().split(" ")
                                .forEach {
                                    it.toIntOrNull()?.let {
                                        lottoArray.add(it)
                                    }
                                }
                        } else {
                            val bonus = elements[0].select("div.num.bonus span").text().toInt()
                            lottoArray.remove(bonus)
                            lottoArray.add(bonus)
                        }
                    }
                    //연금 최근 당첨번호 크롤링
                    mainViewModel.currentPensionRound =
                        elements[5].select("strong.round.key_clr1").text()
                    //1등 당첨번호
                    //조 파싱
                    pensionArray.add(
                        elements[5].select("h4 strong").text().toInt()
                    )
                    //번호 파싱
                    for (i in 1..6) {
                        val temp = elements[5].select("li.clr$i").text().split(" ")
                        //1등 당첨번호
                        pensionArray.add(temp[0].toInt())
                        //보너스 당첨번호
                        pensionBonusArray.add(temp[1].toInt())
                    }
                    mainViewModel.currentLottoWinArray = lottoArray
                    mainViewModel.currentPensionWinArray = pensionArray
                    mainViewModel.currentPensionBonusArray = pensionBonusArray
                    Log.d("currentLotto", mainViewModel.currentLottoWinArray.toString())
                    Log.d("currentPension", mainViewModel.currentPensionWinArray.toString())
                    Log.d("currentPensionBonus", mainViewModel.currentPensionBonusArray.toString())
                } catch (httpStatusException: HttpStatusException) {

                }
            }
            displayRecentResult()
        }
    }

    private fun displayRecentResult() {
        if (binding.lottoRadioButton.isChecked)
            binding.roundTextView.text = mainViewModel.currentLottoRound
        else
            binding.roundTextView.text = mainViewModel.currentPensionRound

        Log.d("it5", mainViewModel.currentPensionBonusArray.toString())
        binding.pensionLayout.let {
            it.pensionBonusGroupNumber.text = "모든"
            it.pensionBonusFirstNumber.text = "${mainViewModel.currentPensionBonusArray[0]}"
            it.pensionBonusSecondNumber.text = "${mainViewModel.currentPensionBonusArray[1]}"
            it.pensionBonusThirdNumber.text = "${mainViewModel.currentPensionBonusArray[2]}"
            it.pensionBonusFourthNumber.text = "${mainViewModel.currentPensionBonusArray[3]}"
            it.pensionBonusFifthNumber.text = "${mainViewModel.currentPensionBonusArray[4]}"
            it.pensionBonusSixthNumber.text = "${mainViewModel.currentPensionBonusArray[5]}"

            it.pensionGroupNumber.text = "${mainViewModel.currentPensionWinArray[0]}"
            it.pensionFirstNumber.text = "${mainViewModel.currentPensionWinArray[1]}"
            it.pensionSecondNumber.text = "${mainViewModel.currentPensionWinArray[2]}"
            it.pensionThirdNumber.text = "${mainViewModel.currentPensionWinArray[3]}"
            it.pensionFourthNumber.text = "${mainViewModel.currentPensionWinArray[4]}"
            it.pensionFifthNumber.text = "${mainViewModel.currentPensionWinArray[5]}"
            it.pensionSixthNumber.text = "${mainViewModel.currentPensionWinArray[6]}"
        }

        binding.lottoLayout.let {
            it.lottoFirstNumber.text = "${mainViewModel.currentLottoWinArray[0]}"
            it.lottoSecondNumber.text = "${mainViewModel.currentLottoWinArray[1]}"
            it.lottoThirdNumber.text = "${mainViewModel.currentLottoWinArray[2]}"
            it.lottoFourthNumber.text = "${mainViewModel.currentLottoWinArray[3]}"
            it.lottoFifthNumber.text = "${mainViewModel.currentLottoWinArray[4]}"
            it.lottoSixthNumber.text = "${mainViewModel.currentLottoWinArray[5]}"
            it.bonusNumber.text = "${mainViewModel.currentLottoWinArray[6]}"
            for (i in 0..6) {
                val resource = applicationContext.resources.getIdentifier(
                    "lottery_number_shape_${mainViewModel.currentLottoWinArray[i] / 10}",
                    "drawable",
                    applicationContext.packageName
                )
                it.bonusNumber.setBackgroundResource(R.drawable.lottery_number_shape_0)
                when (i) {
                    0 -> it.lottoFirstNumber.setBackgroundResource(resource)
                    1 -> it.lottoSecondNumber.setBackgroundResource(resource)
                    2 -> it.lottoThirdNumber.setBackgroundResource(resource)
                    3 -> it.lottoFourthNumber.setBackgroundResource(resource)
                    4 -> it.lottoFifthNumber.setBackgroundResource(resource)
                    5 -> it.lottoSixthNumber.setBackgroundResource(resource)
                    6 -> it.bonusNumber.setBackgroundResource(resource)
                }
            }
        }
    }

    //연금복권 QR 코드 스캔 결과 크롤링 함수
    private fun getQrCodeScanResultPension() {
        launch {
            withContext(Dispatchers.IO) {
                try {
                    //크롤링할 주소 지정
                    val jsoup =
                        Jsoup.connect("https://m.dhlottery.co.kr/qr.do?method=winQr&v=pd1200975s865901")
                    //사이트 크롤링
                    val doc: Document = jsoup.get()

                    //이전코드
                    //val myNumber: ArrayList<Int> = arrayListOf()
                    val myNumberList: ArrayList<Int> = arrayListOf()
                    val backgroundList = arrayListOf<Boolean>()
                    val bonusBackgroundList = arrayListOf<Boolean>()
                    var myPensionResult: MyPensionResult

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

                    val result = doc.select("strong.result").text().toString().split(" ")

                    for (i in 1..6) {
                        val temp = elements[2]
                            .select("span.num.al720_color$i span")
                        val num = temp.text().toInt()
                        myNumberList.add(num)
                        if (winNumber[0][i] == groupNum)
                            backgroundList.add(true)
                        else
                            backgroundList.add(false)
                        if (i < 6 && winNumber[1][i] == groupNum)
                            bonusBackgroundList.add(true)
                        else
                            bonusBackgroundList.add(false)
                    }
                    Log.d("jsoup3", myNumberList.toString())
                    myPensionResult = MyPensionResult(result[0], result[1], MyPensionNumber(backgroundList, myNumberList, bonusBackgroundList))
                } catch (e: Exception) {

                }
            }
            //after
        }
    }

    //로또 6/45 QR 코드 스캔 결과 크롤링 함수
    private fun getQrCodeScanResultLotto() {
        launch {
            withContext(Dispatchers.IO) {
                try {
                    //크롤링할 주소 지정
                    val jsoup =
                        Jsoup.connect("https://m.dhlottery.co.kr/qr.do?method=winQr&v=1000q012023374344q040620213345q121520233739q232830333437q0218202132441739553448")
                    //사이트 크롤링
                    val doc: Document = jsoup.get()
                    Log.d("lottoDoc", doc.toString())

                    //당첨 복권번호 1등
                    val winNumber: ArrayList<Int> = arrayListOf()
                    //내 번호
                    val myLottoResults: ArrayList<MyLottoResult> = arrayListOf()

                    //1등 번호 추출
                    val winElements = doc.select("div.bx_winner")
                    Log.d("winLotto", winElements.toString())

                    val temp = winElements
                        .select("div.clr.clr span").text()
                    Log.d("winLottoNum", temp)

                    //1등 당첨번호 숫자 배열로 변환
                    temp.split(" ").forEach {
                        winNumber.add(it.toInt())
                    }

                    //내 번호 추출
                    val myNumberElements = doc.select("div.list_my_number")
                    Log.d("myLotto", myNumberElements.toString())

                    //내 번호 차례대로 추출
                    myNumberElements.select("tr")
                        .forEach {
                            val tempList = arrayListOf<Int>()
                            val tempBackgroundList = arrayListOf<Int>()
                            val tempResult = it.select("td.result").text()
                            it.select("span.clr").text().split(" ").forEach { n ->
                                val num = n.toInt()
                                if (winNumber.contains(num)) {
                                    tempBackgroundList.add(num/10)
                                } else {
                                    tempBackgroundList.add(-1)
                                }
                                tempList.add(num)
                            }
                            myLottoResults.add(MyLottoResult(tempResult,MyLottoNumber(tempBackgroundList, tempList)))
                        }
                    Log.d("myLottos", myLottoResults.toString())
                } catch (e: Exception) {

                }
            }
            //after
        }
    }

    companion object {
        val QR_INTENT_KEY = "QR_INTENT_KEY"
    }
}