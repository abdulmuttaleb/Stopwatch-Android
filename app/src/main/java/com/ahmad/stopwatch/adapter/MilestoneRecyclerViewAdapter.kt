package com.ahmad.stopwatch.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ahmad.stopwatch.R
import com.ahmad.stopwatch.model.Milestone
import com.ahmad.stopwatch.utils.Constants

class MilestoneRecyclerViewAdapter: RecyclerView.Adapter<MilestoneRecyclerViewAdapter.MilestoneViewHolder>(){

    lateinit var context:Context

    var milestones:ArrayList<Milestone> = arrayListOf()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MilestoneViewHolder {
        this.context = parent.context
        return MilestoneViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_milestone,parent, false))
    }

    override fun getItemCount(): Int {
        return milestones.size
    }

    override fun onBindViewHolder(holder: MilestoneViewHolder, position: Int) {
        holder.bindItem(milestones[position])
    }

    fun addToMilestones(milestone: Milestone){
        milestones.add(milestone)
        notifyDataSetChanged()
    }

    fun clearMilestones(){
        milestones.clear()
        notifyDataSetChanged()
    }

    inner class MilestoneViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var nameTextView:TextView = itemView.findViewById(R.id.tv_name)
        var timeTextView:TextView = itemView.findViewById(R.id.tv_time)
        var iconImageView:ImageView = itemView.findViewById(R.id.iv_alarm)

        fun bindItem(milestone: Milestone) {
            nameTextView.text = milestone.name
            timeTextView.text = Constants.periodFormatter.print(milestone.period)
            when (milestone.passed) {
                true -> {
                    iconImageView.background = context.getDrawable(R.drawable.ic_alarm_done)
                }
                false -> {
                    iconImageView.background = context.getDrawable(R.drawable.ic_alarm_going)
                }
            }

            if(milestone.name.isEmpty()){
                nameTextView.visibility = View.GONE
            }
        }
    }
}