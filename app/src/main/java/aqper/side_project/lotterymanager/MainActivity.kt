package aqper.side_project.lotterymanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import aqper.side_project.lotterymanager.databinding.ActivityMainBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var qrcodeLauncher: ActivityResultLauncher<ScanOptions>

    val date = Date(System.currentTimeMillis())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initQRCodeScanner()
        initViews()
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

    companion object {
        val QR_INTENT_KEY = "QR_INTENT_KEY"
    }
}