package com.example.home_project.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.home_project.R
import com.example.home_project.parcel.busParcel


class MyAdapter(private val items: List<busParcel>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    // ViewHolder: 아이템 뷰를 재활용하기 위한 클래스
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val busName: TextView = itemView.findViewById(R.id.busName)
        val busStopName: TextView = itemView.findViewById(R.id.busStopName)
        val busArrivalTime: TextView = itemView.findViewById(R.id.busArrivalTime)
        val direction: TextView = itemView.findViewById(R.id.direction)
    }

    // item_view.xml을 inflate하여 ViewHolder를 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    // 데이터와 뷰를 바인딩
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.busName.text = item.busNm
        holder.busStopName.text = item.stationNm
        holder.busArrivalTime.text = item.arrivalTime
        val direction = if (item.stationNm == "경성고교입구") "집" else "학원"
        holder.direction.text = direction
        val imageVal =
            if (item.stationNm == "경성고교입구") R.mipmap.ic_noorong_round else R.mipmap.ic_noorong2_round
        holder.direction.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            imageVal,
            0
        )

    }

    override fun getItemCount(): Int = items.size
}
