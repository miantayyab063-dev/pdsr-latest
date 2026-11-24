package com.pdsrlatest.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.*
import android.view.View
import org.json.JSONObject
import org.json.JSONArray
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection
import java.util.concurrent.Executors

class MainActivity : Activity() {
    // Replace with your configured HTTPS endpoint before building.
    private val endpoint = "https://YOUR-DOMAIN.example/search.php"
    private val navy = Color.rgb(15,34,61)
    private val teal = Color.rgb(0,135,126)
    private val gold = Color.rgb(228,184,85)
    private val worker = Executors.newSingleThreadExecutor()
    private val prefs by lazy { getSharedPreferences("private_history", MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private lateinit var number: EditText
    private lateinit var result: TextView
    private lateinit var search: Button
    private lateinit var history: LinearLayout
    private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
    private fun bg(c:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(16).toFloat()}
    private fun text(s:String,size:Float,c:Int=navy)=TextView(this).apply{text=s;textSize=size;setTextColor(c);setPadding(0,dp(6),0,dp(6))}
    private fun add(p:LinearLayout,v:View){p.addView(v,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(10)})}
    private fun panel()=LinearLayout(this).apply{orientation=1;setPadding(dp(18),dp(18),dp(18),dp(18));background=bg(Color.WHITE);add(root,this)}
    private fun action(s:String, click:()->Unit)=Button(this).apply{text=s;isAllCaps=false;setTextColor(Color.WHITE);background=bg(teal);minHeight=dp(48);setOnClickListener{click()}}
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
        window.statusBarColor=navy
        val scroll=ScrollView(this).apply{setBackgroundColor(Color.rgb(244,248,251));fitsSystemWindows=true}
        root=LinearLayout(this).apply{orientation=1;setPadding(dp(18),dp(20),dp(18),dp(24));fitsSystemWindows=true}
        scroll.addView(root);setContentView(scroll)
        val hero=panel();hero.background=bg(navy)
        add(hero,text("PDSR LATEST",14f,gold).apply{setTypeface(null,Typeface.BOLD)})
        add(hero,text("Find a business.\nIn seconds.",28f,Color.WHITE).apply{setTypeface(null,Typeface.BOLD)})
        add(hero,text("Online public business directory",14f,Color.WHITE))
        val form=panel();add(form,text("Search by mobile number",19f))
        number=EditText(this).apply{hint="03XX XXXXXXX";inputType=android.text.InputType.TYPE_CLASS_PHONE;setSingleLine(true)};add(form,number)
        add(form,text("Only verified public business listings. Your query is sent securely to the server.",13f))
        search=action("Search online"){lookup()};add(form,search)
        val card=panel();add(card,text("Search result",19f));result=text("Enter a complete mobile number to begin.",15f);add(card,result)
        val hp=panel();add(hp,text("Private search history",19f));history=LinearLayout(this).apply{orientation=1};add(hp,history)
        add(hp,action("Clear history"){prefs.edit().remove("items").apply();drawHistory()})
        add(root,text("Independent directory. Not affiliated with PTA, NADRA or telecom operators.",12f));drawHistory()
    }
    private fun normalized(s:String):String?{
        if(!Regex("^[+0-9\\s()-]{7,25}$").matches(s))return null
        var n=s.filter{it in '0'..'9'}
        if(n.startsWith("0092"))n="0"+n.drop(4) else if(n.startsWith("92"))n="0"+n.drop(2)
        return n.takeIf{Regex("^03[0-9]{9}$").matches(it)}
    }
    private fun items():List<String>{val a=JSONArray(prefs.getString("items","[]")?:"[]");return (0 until a.length()).map{a.getString(it)}}
    private fun drawHistory(){history.removeAllViews();if(items().isEmpty())add(history,text("No searches yet",13f));items().forEach{n->add(history,action(n){number.setText(n);lookup()})}}
    private fun lookup(){val n=normalized(number.text.toString())?:run{result.text="Enter a valid complete Pakistani mobile number.";return}
        if(!search.isEnabled)return
        search.isEnabled=false;result.text="Searching…"
        worker.execute{
            var completed=false
            val message=try{
                val connection=URL(endpoint).openConnection() as HttpsURLConnection
                try{connection.instanceFollowRedirects=false;connection.requestMethod="POST";connection.connectTimeout=10000;connection.readTimeout=10000;connection.doOutput=true;connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");connection.outputStream.use{it.write(("number="+URLEncoder.encode(n,"UTF-8")).toByteArray())}
                    when(connection.responseCode){200->{val data=JSONObject(connection.inputStream.bufferedReader().use{it.readText()});val r=data.optJSONObject("record");completed=true;if(r==null)"No verified public business listing found." else listOf("Business: "+r.getString("business_name"),"Category: "+r.getString("category"),"Number: "+r.getString("phone"),"Website: "+r.getString("website")).joinToString("\n\n")};429->"Too many requests. Try again later.";else->"Service unavailable. Please try again."}
                }finally{connection.disconnect()}
            }catch(e:Exception){"Connection failed. Check your internet or server settings."}
            runOnUiThread{if(!isFinishing&&!isDestroyed){result.text=message;search.isEnabled=true;if(completed){prefs.edit().putString("items",JSONArray((listOf(n)+items()).distinct().take(30)).toString()).apply();drawHistory()}}}
        }
    }
    override fun onDestroy(){worker.shutdownNow();super.onDestroy()}
}
