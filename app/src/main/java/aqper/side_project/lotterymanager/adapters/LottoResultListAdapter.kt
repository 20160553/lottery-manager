package aqper.side_project.lotterymanager.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import aqper.side_project.lotterymanager.R
import aqper.side_project.lotterymanager.data.entity.MyLottoResultEntity
import aqper.side_project.lotterymanager.databinding.LottoResultRowItemBinding
import aqper.side_project.lotterymanager.models.MyLottoResult

class LottoResultListAdapter(): ListAdapter<MyLottoResultEntity, LottoResultListAdapter.ViewHolder>(diffUtil) {
    lateinit var context: Context
    lateinit var inflater: LayoutInflater
    inner class ViewHolder internal constructor(val binding: LottoResultRowItemBinding): RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.lotto_result_row_item, parent, false)
        return ViewHolder(LottoResultRowItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder.binding) {
        val cLottoResultEntity = currentList[position]
        roundTextView.text = "로또 6/45 " + cLottoResultEntity.round
        roundTextView.setOnClickListener {
            it.isSelected = !it.isSelected
            lottoResultLinearLayout.isGone = !lottoResultLinearLayout.isGone
        }
        cLottoResultEntity.results.forEach { lottoResult ->
            addResultView(lottoResult, this)
        }
    }

    private fun addResultView(lottoResult: MyLottoResult, binding: LottoResultRowItemBinding) {
        val lottoResultView = inflater.inflate(R.layout.result_lotto, null)
        var resultTextView = lottoResultView.findViewById<TextView>(R.id.result)
        resultTextView.text = lottoResult.winLank
        for (j in (1..6)) {
            val resource = context.resources.getIdentifier(
                "lottery_number_shape_${lottoResult.backgroundList}",
                "drawable",
                context.packageName
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
            textView?.text = lottoResult.numberList[j-1].toString()
            if (lottoResult.backgroundList[j-1] != -1)
                textView?.setBackgroundResource(resource)
            textView?.isVisible = true
            Log.d("textView", textView.toString())
        }
        binding.lottoResultLinearLayout.addView(lottoResultView)
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<MyLottoResultEntity>() {
            override fun areItemsTheSame(
                oldItem: MyLottoResultEntity,
                newItem: MyLottoResultEntity
            ): Boolean {
                return oldItem.lid == newItem.lid
            }

            override fun areContentsTheSame(
                oldItem: MyLottoResultEntity,
                newItem: MyLottoResultEntity
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}