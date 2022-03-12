package aqper.side_project.lotterymanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isGone
import aqper.side_project.lotterymanager.databinding.ActivityMainBinding
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
                    //로또 최근 당첨번호 크롤링
                    mainViewModel.currentLottoRound =
                        elements[0].select("strong.round.key_clr1").text()
                    for (i in 1..6) {
                        if (i != 6) {
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
                } catch (httpStatusException: HttpStatusException) {

                }
            }
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
                    //내 복권번호
                    val myNumber: ArrayList<Int> = arrayListOf()
                    //당첨 복권번호 (1등, 보너스)
                    val winNumber: ArrayList<ArrayList<Int>> = arrayListOf()

                    //div 태그의 class="win720_num"인 요소들 파싱
                    val elements: Elements = doc
                        .select("div.win720_num")

                    Log.d("jsoup1", elements.toString())

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

                    //내 번호 추출하기
                    myNumber.add(
                        elements[2]
                            .select("span.group_list span")
                            .text().toInt()
                    )
                    for (i in 1..6) {
                        val temp = elements[2]
                            .select("span.num.al720_color$i span")
                        myNumber.add(temp.text().toInt())
                    }
                    Log.d("jsoup3", myNumber.toString())
                } catch (httpStatusException: HttpStatusException) {

                }
            }
        }
    }

    companion object {
        val QR_INTENT_KEY = "QR_INTENT_KEY"
    }
}