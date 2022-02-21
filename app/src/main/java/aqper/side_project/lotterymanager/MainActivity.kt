package aqper.side_project.lotterymanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import aqper.side_project.lotterymanager.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.*
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityMainBinding
    private lateinit var qrcodeLauncher: ActivityResultLauncher<ScanOptions>

    private val job = Job()
    override val coroutineContext get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initQRCodeScanner()
        initViews()
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

    //연금복권 QR 코드 스캔 결과 크롤링 함수
    private fun getQrCodeScanResultPension() {
        launch {
            withContext(Dispatchers.IO) {
                try {
                    val jsoup =
                        Jsoup.connect("https://m.dhlottery.co.kr/qr.do?method=winQr&v=pd1200935s865901")
                    val doc: Document = jsoup.get()
                    val myNumber: ArrayList<Int> = arrayListOf()
                    val winNumber: ArrayList<ArrayList<Int>> = arrayListOf()
                    val elements: Elements = doc
                        .select("div.win720_num")

                    Log.d("jsoup1", elements.toString())

                    for (i in 0..1) {
                        val tempList = arrayListOf<Int>()
                        if (i == 1) {
                            tempList.add(0)
                        } else {
                            tempList.add(
                                elements[i]
                                    .select("span.group span")
                                    .text().toInt()
                            )
                        }
                        for (j in 1..6) {
                            val temp = elements[i]
                                .select("span.num.al720_color$j span")
                            tempList.add(temp.text().toInt())
                        }
                        winNumber.add(tempList)
                    }

                    Log.d("jsoup2", winNumber.toString())

                    //내 번호 추출하기
                    myNumber.add(
                        elements[3]
                            .select("span.group_list span")
                            .text().toInt()
                    )
                    for (i in 1..6) {
                        val temp = elements[3]
                            .select("span.num.al720_color$i span")
                        myNumber.add(temp.text().toInt())
                    }
                    Log.d("jsoup3", myNumber.toString())
                } catch (httpStatusException: HttpStatusException) {

                }
            }
        }
    }

    //연금복권 QR
    private fun getWinningHistoryPension() {
        launch {

        }
    }


    companion object {
        val QR_INTENT_KEY = "QR_INTENT_KEY"
    }
}