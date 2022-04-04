package aqper.side_project.lotterymanager

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import aqper.side_project.lotterymanager.MainActivity.Companion.LOTTO_RESULT_KEY
import aqper.side_project.lotterymanager.MainActivity.Companion.isNetworkAvailable
import aqper.side_project.lotterymanager.databinding.ActivityLottoResultBinding
import aqper.side_project.lotterymanager.models.MyLottoNumber
import aqper.side_project.lotterymanager.models.MyLottoResult
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class LottoResultActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityLottoResultBinding
    private var myLottoResults: ArrayList<MyLottoResult>? = null
    private var winningAmmount: String = ""
    private var roundText = ""

    //코루틴 설정
    private val job = Job()
    override val coroutineContext get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLottoResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getQrCodeScanResultLotto()
    }

    //로또 6/45 QR 코드 스캔 결과 크롤링 함수
    private fun getQrCodeScanResultLotto() {
        val url = intent.getStringExtra(LOTTO_RESULT_KEY)
        if (!isNetworkAvailable(this)) {
            Toast.makeText(this, "당첨 결과를 얻어오는데 실패했습니다.\n인터넷 연결 상태를 확인해주세요.", Toast.LENGTH_SHORT)
                .show()
            return
        }
        launch {
            withContext(Dispatchers.IO) {
                try {
                    //크롤링할 주소 지정
                    val jsoup =
                        Jsoup.connect(url)

                    //사이트 크롤링
                    val doc = jsoup.get().select("div.contents")

                    //당첨 복권번호 1등
                    val winNumber: ArrayList<Int> = arrayListOf()
                    //내 번호
                    myLottoResults = arrayListOf()

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

                    //당첨 액수 추출
                    winningAmmount = doc.select("div.bx_notice.winner span.key_clr1").text()
                    //회차 추출
                    roundText = doc.select("span.key_clr1").text()

                    //내 번호 추출
                    val myNumberElements = doc.select("div.list_my_number")
                    Log.d("myLotto", myNumberElements.toString())

                    //내 번호 차례대로 추출
                    myNumberElements.select("tr")
                        .forEach {
                            val tempList = arrayListOf<Int>()
                            val tempBackgroundList = arrayListOf<Int>()
                            val tempResult = it.select("td.result").text().replace("당첨", "")
                            it.select("span.clr").text().split(" ").forEach { n ->
                                val num = n.toInt()
                                if (winNumber.contains(num)) {
                                    tempBackgroundList.add(num / 10)
                                } else {
                                    tempBackgroundList.add(-1)
                                }
                                tempList.add(num)
                            }
                            myLottoResults!!.add(
                                MyLottoResult(
                                    tempResult,
                                    MyLottoNumber(tempBackgroundList, tempList)
                                )
                            )
                        }
                    Log.d("myLottos", myLottoResults.toString())
                } catch (e: Exception) {

                }
            }
            //after
            displayLottoResult()
        }
    }

    private fun displayLottoResult() {
        myLottoResults ?: return
        //회차 텍스트 지정
        binding.roundTextView.text = "로또 6/45 $roundText"
        //결과 텍스트 설정
        if (winningAmmount == "") {
            binding.resultTextView1.text = "아쉽게도,"
            binding.resultTextView3.text = "낙첨되셨습니다."
            binding.resultTextView2.isGone = true
        } else {
            binding.resultTextView1.text = "축하합니다!"
            binding.resultTextView2.text = "총 ${winningAmmount}에"
            binding.resultTextView3.text = "당첨되셨습니다."
        }

        //동적으로 커스텀 레이아웃 추가
        val layoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        for (lottoResult in myLottoResults!!) {
            val lottoResultView = layoutInflater.inflate(R.layout.result_lotto_row_item, null)
            var resultTextView = lottoResultView.findViewById<TextView>(R.id.result)
            resultTextView.text = lottoResult.winLank
            for (j in (1..6)) {
                val resource = applicationContext.resources.getIdentifier(
                    "lottery_number_shape_${lottoResult.myLottoNumbers.backgroundList}",
                    "drawable",
                    applicationContext.packageName
                )
                var textView: TextView? = null
                when(j) {
                    1 -> textView = lottoResultView.findViewById<TextView>(R.id.lottoNumber1)
                    2 -> textView = lottoResultView.findViewById<TextView>(R.id.lottoNumber2)
                    3 -> textView = lottoResultView.findViewById<TextView>(R.id.lottoNumber3)
                    4 -> textView = lottoResultView.findViewById<TextView>(R.id.lottoNumber4)
                    5 -> textView = lottoResultView.findViewById<TextView>(R.id.lottoNumber5)
                    6 -> textView = lottoResultView.findViewById<TextView>(R.id.lottoNumber6)
                }
                textView?.text = lottoResult.myLottoNumbers.numberList[j-1].toString()
                if (lottoResult.myLottoNumbers.backgroundList[j-1] != -1)
                    textView?.setBackgroundResource(resource)
                textView?.isVisible = true
                Log.d("textView", textView.toString())
            }
            binding.winningResultLayout.addView(lottoResultView)
        }
    }
}