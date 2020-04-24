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

    inner class MilestoneViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var nameTextView:TextView = itemView.findViewById(R.id.tv_name)
        var iconImageView:ImageView = itemView.findViewById(R.id.iv_alarm)

        fun bindItem(milestone: Milestone) {
            nameTextView.text = milestone.name
            when (milestone.passed) {
                true -> {
                    iconImageView.background = context.getDrawable(R.drawable.ic_alarm_done)
                }
                false -> {
                    iconImageView.background = context.getDrawable(R.drawable.ic_alarm_going)
                }
            }
        }
    }
}