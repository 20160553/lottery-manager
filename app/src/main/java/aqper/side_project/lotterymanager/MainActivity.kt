package aqper.side_project.lotterymanager

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
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
                //연금복권인 경우
                if (result.contents.contains("dhlottery.co.kr/?v=pd120")) {
                    val url = result.contents.replace("http://qr.dhlottery.co.kr/?v=", "https://m.dhlottery.co.kr/qr.do?method=winQr&v=")
                    val intent = Intent(this, PensionResultActivity::class.java)
                    intent.putExtra(PENSION_RESULT_KEY, url)
                    startActivity(intent)
                }
                //로또 복권인 경우
                else if (result.contents.contains("dhlottery.co.kr/?v=")) {
                    val url = result.contents.replace("http://m.dhlottery.co.kr/?v=", "https://m.dhlottery.co.kr/qr.do?method=winQr&v=")
                    val intent = Intent(this, LottoResultActivity::class.java)
                    intent.putExtra(LOTTO_RESULT_KEY, url)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "유효하지 않은 QR 코드입니다.", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
            }
        }
    }

    private fun getRecentResult() {
        launch {
            if (!isNetworkAvailable(this@MainActivity)){
                Toast.makeText(this@MainActivity, "최신 정보를 불러오는 데 실패했습니다.\n인터넷 연결 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
                return@launch
            }
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
                    Toast.makeText(this@MainActivity, "최신 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
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
            it.lottoNumber1.text = "${mainViewModel.currentLottoWinArray[0]}"
            it.lottoNumber2.text = "${mainViewModel.currentLottoWinArray[1]}"
            it.lottoNumber3.text = "${mainViewModel.currentLottoWinArray[2]}"
            it.lottoNumber4.text = "${mainViewModel.currentLottoWinArray[3]}"
            it.lottoNumber5.text = "${mainViewModel.currentLottoWinArray[4]}"
            it.lottoNumber6.text = "${mainViewModel.currentLottoWinArray[5]}"
            it.bonusNumber.text = "${mainViewModel.currentLottoWinArray[6]}"
            for (i in 0..6) {
                val resource = applicationContext.resources.getIdentifier(
                    "lottery_number_shape_${mainViewModel.currentLottoWinArray[i] / 10}",
                    "drawable",
                    applicationContext.packageName
                )
                when (i) {
                    0 -> it.lottoNumber1.setBackgroundResource(resource)
                    1 -> it.lottoNumber2.setBackgroundResource(resource)
                    2 -> it.lottoNumber3.setBackgroundResource(resource)
                    3 -> it.lottoNumber4.setBackgroundResource(resource)
                    4 -> it.lottoNumber5.setBackgroundResource(resource)
                    5 -> it.lottoNumber6.setBackgroundResource(resource)
                    6 -> it.bonusNumber.setBackgroundResource(resource)
                }
            }
        }
    }

    companion object {
        val QR_INTENT_KEY = "QR_INTENT_KEY"
        val LOTTO_RESULT_KEY = "LOTTO_RESULT"
        val PENSION_RESULT_KEY = "PENSION_RESULT"
        //네트워크 상태 확인
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val nw      = connectivityManager.activeNetwork ?: return false
                val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false

                return when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    //for other device how are able to connect with Ethernet
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    //for check internet over Bluetooth
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                    else -> false
                }
            } else {
                return connectivityManager.activeNetworkInfo?.isConnected ?: false
            }
        }
    }
}