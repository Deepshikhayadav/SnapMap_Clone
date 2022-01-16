package com.example.myapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class HomeAdapter(private val list: List<HashMap<String, String>>,private val myOnClickListener: MyOnClickListener) :
    RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_resource, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.name.text= list[position]["name"].toString()
        holder.desc.text= list[position]["place"].toString()
        Log.i("---->",list[position]["lot"].toString())
        Log.i("---->",list[position]["lat"].toString())

        val name=list[position]["name"].toString()
        val place=list[position]["place"].toString()
        val lot=list[position]["lot"]!!.toDouble()
        val lat=list[position]["lat"]!!.toDouble()
        val img=list[position]["img"]!!.toString()
        Glide.with(holder.pic.context)
            .load(list[position]["img"])
            .into(holder.pic)
    holder.itemView.setOnClickListener {
        myOnClickListener.onClick(name,place,lat,lot,img)
    }
        
    }

    override fun getItemCount(): Int = list.size
}
class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val name: TextView =itemView.findViewById<TextView>(R.id.name)
    val desc: TextView =itemView.findViewById<TextView>(R.id.desc)
    val pic: CircleImageView =itemView.findViewById(R.id.pic)


}

interface MyOnClickListener{
    fun onClick(n:String,p:String,a:Double,b:Double,i:String)
}