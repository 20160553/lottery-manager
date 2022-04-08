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
import aqper.side_project.lotterymanager.data.entity.MyPensionResultEntity
import aqper.side_project.lotterymanager.databinding.LottoResultRowItemBinding
import aqper.side_project.lotterymanager.databinding.PensionResultRowItemBinding
import aqper.side_project.lotterymanager.models.MyLottoResult
import aqper.side_project.lotterymanager.models.MyPensionResult

class PensionResultListAdapter(): ListAdapter<MyPensionResultEntity, PensionResultListAdapter.ViewHolder>(diffUtil) {
    lateinit var context: Context
    lateinit var inflater: LayoutInflater
    inner class ViewHolder internal constructor(val binding: PensionResultRowItemBinding): RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.lotto_result_row_item, parent, false)
        return ViewHolder(PensionResultRowItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder.binding) {
        val cPensionResultEntity = currentList[position]
        roundTextView.text = "연금복권 720+ " + cPensionResultEntity.round
        roundTextView.setOnClickListener {
            it.isSelected = !it.isSelected
            pensionResultLinearLayout.isGone = !pensionResultLinearLayout.isGone
        }
        //todo display
        displayMyResult(cPensionResultEntity.result, holder.binding)
    }

    private fun displayMyResult(result: MyPensionResult, binding: PensionResultRowItemBinding) {
        //본 번호 레이아웃
        binding.pensionResultLayout.pensionGroupNumber.text =
            result.numberList[0].toString()
        binding.pensionResultLayout.pensionFirstNumber.text =
            result.numberList[1].toString()
        binding.pensionResultLayout.pensionSecondNumber.text =
            result.numberList[2].toString()
        binding.pensionResultLayout.pensionThirdNumber.text =
            result.numberList[3].toString()
        binding.pensionResultLayout.pensionFourthNumber.text =
            result.numberList[4].toString()
        binding.pensionResultLayout.pensionFifthNumber.text =
            result.numberList[5].toString()
        binding.pensionResultLayout.pensionSixthNumber.text =
            result.numberList[6].toString()

        //보너스 레이아웃
        binding.pensionResultLayout.pensionBonusFirstNumber.text =
            result.numberList[1].toString()
        binding.pensionResultLayout.pensionBonusSecondNumber.text =
            result.numberList[2].toString()
        binding.pensionResultLayout.pensionBonusThirdNumber.text =
            result.numberList[3].toString()
        binding.pensionResultLayout.pensionBonusFourthNumber.text =
            result.numberList[4].toString()
        binding.pensionResultLayout.pensionBonusFifthNumber.text =
            result.numberList[5].toString()
        binding.pensionResultLayout.pensionBonusSixthNumber.text =
            result.numberList[6].toString()

        //일치하는 번호 배경색 지정
        for (i in 0..6) {
            val resource = context.resources.getIdentifier(
                "lottery_number_shape_$i",
                "drawable",
                context.packageName
            )
            when (i) {
                0 -> {
                    if (result.backgroundList[0])
                        binding.pensionResultLayout.pensionGroupNumber.setBackgroundResource(resource)
                }
                1 -> {
                    if (result.backgroundList[1])
                        binding.pensionResultLayout.pensionFirstNumber.setBackgroundResource(resource)
                    if (result.bonusBackgroundList[0])
                        binding.pensionResultLayout.pensionBonusFirstNumber.setBackgroundResource(resource)
                }
                2 -> {
                    if (result.backgroundList[2])
                        binding.pensionResultLayout.pensionSecondNumber.setBackgroundResource(resource)
                    if (result.bonusBackgroundList[1])
                        binding.pensionResultLayout.pensionBonusSecondNumber.setBackgroundResource(resource)
                }
                3 -> {
                    if (result.backgroundList[3])
                        binding.pensionResultLayout.pensionThirdNumber.setBackgroundResource(resource)
                    if (result.bonusBackgroundList[2])
                        binding.pensionResultLayout.pensionBonusThirdNumber.setBackgroundResource(resource)
                }
                4 -> {
                    if (result.backgroundList[4])
                        binding.pensionResultLayout.pensionFourthNumber.setBackgroundResource(resource)
                    if (result.bonusBackgroundList[3])
                        binding.pensionResultLayout.pensionBonusFourthNumber.setBackgroundResource(resource)
                }
                5 -> {
                    if (result.backgroundList[5])
                        binding.pensionResultLayout.pensionFifthNumber.setBackgroundResource(resource)
                    if (result.bonusBackgroundList[4])
                        binding.pensionResultLayout.pensionBonusFifthNumber.setBackgroundResource(resource)
                }
                6 -> {
                    if (result.backgroundList[6])
                        binding.pensionResultLayout.pensionSixthNumber.setBackgroundResource(resource)
                    if (result.bonusBackgroundList[5])
                        binding.pensionResultLayout.pensionBonusSixthNumber.setBackgroundResource(resource)
                }
            }
        }
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<MyPensionResultEntity>() {
            override fun areItemsTheSame(
                oldItem: MyPensionResultEntity,
                newItem: MyPensionResultEntity
            ): Boolean {
                return oldItem.pid == newItem.pid
            }
            override fun areContentsTheSame(
                oldItem: MyPensionResultEntity,
                newItem: MyPensionResultEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}