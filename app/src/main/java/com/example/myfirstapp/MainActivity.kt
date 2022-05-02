package com.example.myfirstapp

import android.content.ClipDescription
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.kotlin.where
import java.lang.RuntimeException
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList



class MainActivity : AppCompatActivity() {
    lateinit var vText:TextView
    lateinit var vList:LinearLayout
    lateinit var vListView: ListView
    lateinit var vRecView: RecyclerView
    var request:Disposable?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vRecView = findViewById<RecyclerView>(R.id.act1_recView)

            val o = createRequest("https://api.rss2json.com/v1/api.json?rss_url=https%3A%2F%2Fria.ru%2Fexport%2Frss2%2Findex.xml%3Fpage_type%3Dgoogle_newsstand")
                    .map { Gson().fromJson(it,FeedAPI::class.java)}
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

            request = o.subscribe({

                val feed = Feed(it.items.mapTo(RealmList<FeedItem>(), {feed -> FeedItem(feed.title,feed.link,feed.thumbnail,feed.description) }))
                Realm.getDefaultInstance().executeTransaction{ realm->

                    val oldList =realm.where(Feed::class.java).findAll()
                    if(oldList.size > 0)
                        for(item in oldList)
                            item.deleteFromRealm()

                    realm.copyToRealm(feed)
                }
              //      showLinearLayout(it.items)
                showRecView()
              //    for(item in it.items)
              //     Log.w("test", "title: ${item.title}")
            },{
                Log.e("test", "", it)
                showRecView()
            })
        }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }
        fun showLinearLayout(feedList:ArrayList<FeedItemAPI>){
            val inflater = layoutInflater
            for(f in feedList) {
                val view = inflater.inflate(R.layout.list_item, vList, false)
                val vTitle = view.findViewById<TextView>(R.id.item_title)
                vTitle.text=f.title
                vList.addView(view)
            }
        }
    fun showListView(feedList: ArrayList<FeedItemAPI>){
        vListView.adapter = Adapter(feedList)
    }
    fun showRecView(){
        Realm.getDefaultInstance().executeTransaction { realm ->
            val feed =realm.where(Feed::class.java).findAll()
            if(feed.size>0)
            {
                vRecView.adapter=RecAdapter(feed[0]!!.items)
                vRecView.layoutManager=LinearLayoutManager(this)
            }
        }

    }
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if(data != null){
                val str = data.getStringExtra("tag2")
                vText.text=str
            }
        }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        request?.dispose()
        super.onDestroy()
    }


}
class FeedAPI(
    val items:ArrayList<FeedItemAPI>
)
class FeedItemAPI(
    val title:String,
    val link:String,
    val thumbnail:String,
    val description:String
)

open class Feed(
    var items:RealmList<FeedItem> = RealmList<FeedItem>()
): RealmObject()
open class FeedItem(
    var title:String="",
    var link:String="",
    var thumbnail:String="",
    var description:String=""

): RealmObject()
class Adapter(val items:ArrayList<FeedItemAPI>):BaseAdapter(){
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val inflater = LayoutInflater.from(parent!!.context)

        val view = convertView ?: inflater.inflate(R.layout.list_item, parent, false)
        val vTitle = view.findViewById<TextView>(R.id.item_title)
        val item = getItem(position) as FeedItemAPI
        vTitle.text=item.title
        return view
    }

}
class RecAdapter(val items:RealmList<FeedItem>):RecyclerView.Adapter<RecHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecHolder {
        val inflater = LayoutInflater.from(parent!!.context)
        val view = inflater.inflate(R.layout.list_item, parent, false)
        return RecHolder(view)
    }

    override fun onBindViewHolder(holder: RecHolder, position: Int) {
        val item = items[position]!!
        holder.bind(item)
    }

    override fun getItemCount(): Int {
       return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

}
class RecHolder(view:View):RecyclerView.ViewHolder(view){

    fun bind(item:FeedItem){
        val vTitle = itemView.findViewById<TextView>(R.id.item_title)
        val vDecs = itemView.findViewById<TextView>(R.id.item_desc)
        val vThumb = itemView.findViewById<ImageView>(R.id.item_thumb)
        vTitle.text=item.title
        vDecs.text = item.description
        val thumbURI: String = item.thumbnail
        try {
            Picasso.with(vThumb.context).load(thumbURI).into(vThumb)
        } catch (e: Exception) {
            Log.e("ImgLoadingError", e.message ?: "null")
            val defaultThumbURI: String = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTQrWJC4a3Y2j2s1jvuKww_0RVQfrkQDmM-0n0Sczt_2JA6Rwsftk0CSMJRzwFKgMXoEtQ&usqp=CAU"
            Picasso.with(vThumb.context).load(defaultThumbURI).into(vThumb)
        }
        itemView.setOnClickListener{
            val i =Intent(Intent.ACTION_VIEW)
            i.data= Uri.parse(item.link)
            vThumb.context.startActivity(i)
        }
    }
}


