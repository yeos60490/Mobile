package com.example.wlgusdn.mobile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_createpromise.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapReverseGeoCoder
import net.daum.mf.map.api.MapView

@SuppressLint("ValidFragment")
class CreatePromiseFragment(context: Context) : Fragment(), MapView.POIItemEventListener, MapView.MapViewEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {
    var thiscontext : Context = context

    lateinit  var clsPoint : ArrayList<MapPoint>
    lateinit var bu_Date : Button
    lateinit var bu_Time : Button
    lateinit var bu_Invite : Button
    lateinit var tv_Place : TextView
    lateinit var tv_Date : TextView
    lateinit var tv_Time : TextView
    lateinit var tv_Friends : TextView
    lateinit var et_ExtraAddress : EditText
    lateinit var et_Content : EditText
    lateinit var et_Name : EditText
    lateinit var bu_Create : Button
    lateinit var tv_Participant : TextView

    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val myRef : DatabaseReference = database.getReference("PromiseRoom")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_createpromise, container, false) as View

        LobbyActivity.Createcon = view.findViewById(R.id.Create_Con)
        LobbyActivity.CreateMap =view.findViewById(R.id.CreatePromise_Map)
        tv_Date = view.findViewById(R.id.CreatePromise_TextView_Date)
        tv_Friends = view.findViewById(R.id.CreatePromise_TextView_Participant)
        tv_Place = view.findViewById(R.id.CreatePromise_TextView_Place)
        tv_Time =view.findViewById(R.id.CreatePromise_TextView_Time)
        tv_Participant = view.findViewById(R.id.CreatePromise_TextView_Participant)
        et_Name = view.findViewById(R.id.CreatePromise_EditText_Name)
        et_ExtraAddress = view.findViewById(R.id.CreatePromise_EditText_ExtraAddress)
        et_Content = view.findViewById(R.id.CreatePromise_EditText_Content)

        bu_Date = view.findViewById(R.id.CreatePromise_Button_Date)
        bu_Invite = view.findViewById(R.id.CreatePromise_Button_Invite)
        bu_Time = view.findViewById(R.id.CreatePromise_Button_Time)
        bu_Create = view.findViewById(R.id.CreatePromise_Button_Create)


        tv_Participant!!.text="지현우,정문경"

        bu_Create!!.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {

                val part : ArrayList<String> = tv_Participant!!.text.toString().split(',') as ArrayList<String>

                val PRD : PromiseRoomData = PromiseRoomData(et_Name!!.text.toString(),tv_Date!!.text.toString(),tv_Time!!.text.toString(),tv_Place!!.text.toString(),
                        et_ExtraAddress!!.text.toString(),et_Content!!.text.toString(),part)

                myRef.push().setValue(PRD)

                //여기서 초대한 친구들의 DB에 약속방List에 약속방 번호 추가
                database.reference.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {


                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        var p00 = p0
                        var id : String?=null
                        var RoomNum : String? = null
                        for(i in 0..part.size-1)
                        {
                             database.reference.child("Account").addListenerForSingleValueEvent(object: ValueEventListener {
                                 override fun onCancelled(p0: DatabaseError) {


                                 }

                                 override fun onDataChange(p0: DataSnapshot) {

                                      for (fileSnapshot in p0.getChildren())
                                      {
                                          Log.d("ccheckk",""+fileSnapshot.key)
                                          if(fileSnapshot.child("name").value.toString().equals(part[i]))
                                          {
                                              Log.d("ccheckk","찾음"+fileSnapshot.key)
                                              id=fileSnapshot.key
                                              var ud =p0.child(id!!).getValue(UserData::class.java)

                                              /*   while(true)
                                                 {//Account를 찾아가던 중 내가 찾는 이름을 갖고 있지 않으면 계속해서 찾는다.
                                                     var aa = p0.child("Account").children.iterator().next()
                                                     if(aa.child("name").value.toString().equals(part[i]))
                                                     {
                                                         Log.d("ccheckk",aa.key.toString())
                                                     }

                                                 }*/

                                              Log.d("ccheckk",p00.child("PromiseRoom").children.last().key)
                                              RoomNum = p00.child("PromiseRoom").children.last().key


                                              ud!!.Promises!!.add(RoomNum!!)
                                              database.getReference("Account").child(id!!).setValue(ud)
                                          }
                                      }
                                 }
                             })






                        }
                    }
                })


            }
        })

        bu_Date!!.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {




                val dialog = DatePickerDialog(thiscontext, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

                    CreatePromise_TextView_Date.setText(year.toString() + "."+(month + 1).toString() + "."+dayOfMonth.toString())
                }, 2019, 0, 1)


                dialog.show()



            }
        })

        bu_Time!!.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {

                val timePickerDialog = TimePickerDialog(thiscontext, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->

                    tv_Time!!.text = hourOfDay.toString()+" : "+minute.toString()

                }, 18, 0, true)

                timePickerDialog.setMessage("메시지")
                timePickerDialog.show()
            }
        })



        LobbyActivity.CreateMap!!.setMapViewEventListener(this)
        LobbyActivity.CreateMap!!.setPOIItemEventListener(this)


        return view
    }



    override fun onResume() {
        super.onResume()
        Log.d("checkkk","resume")
        if(LobbyActivity.refresh==true) {

            val ft = fragmentManager!!.beginTransaction()
            if (Build.VERSION.SDK_INT >= 26) {
                ft.setReorderingAllowed(false);
            }
            ft.detach(this).attach(this).commit();//Fragment 재 실행
            LobbyActivity.refresh=false
        }

    }






    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {



    }

    override fun onCalloutBalloonOfPOIItemTouched(
            p0: MapView?,
            p1: MapPOIItem?,
            p2: MapPOIItem.CalloutBalloonButtonType?
    ) {

    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {

    }

    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {


    }

    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {

        //MapView 위를 클릭시 POI 깔기
        //현재 무한대로 늘어남
        //POI 클릭시 말풍선 나오고 말풍선 클릭시 다이얼로 intent 이동

        var poi : MapPOIItem = MapPOIItem()

        poi.itemName = "click위치"
        poi.mapPoint= MapPoint.mapPointWithGeoCoord(p1!!.mapPointGeoCoord.latitude,p1!!.mapPointGeoCoord.longitude)
        poi.markerType = net.daum.mf.map.api.MapPOIItem.MarkerType.CustomImage
        poi.isShowCalloutBalloonOnTouch=true
        poi.customImageResourceId = R.drawable.cat
        poi.leftSideButtonResourceIdOnCalloutBalloon = R.drawable.cat

        LobbyActivity.CreateMap!!.addPOIItem(poi)

        val reverseGeoCoder : MapReverseGeoCoder = MapReverseGeoCoder("4a70536a991d4cd7bd72f612b7ab81b8",poi.mapPoint,this,thiscontext as Activity)
        reverseGeoCoder.startFindingAddress()



    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewInitialized(p0: MapView?) {

    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {

    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

    }




    override fun onReverseGeoCoderFailedToFindAddress(p0: MapReverseGeoCoder?) {
        onFinishReverseGeoCoding("Fail")

    }

    override fun onReverseGeoCoderFoundAddress(p0: MapReverseGeoCoder?, p1: String?) {

        Log.d("check","Success to GeoCoder")
        p0.toString()
        onFinishReverseGeoCoding(p1!!)

    }

    private fun onFinishReverseGeoCoding(result: String) {
        Toast.makeText(thiscontext, "Reverse Geo-coding : " + result, Toast.LENGTH_SHORT).show();
        tv_Place!!.text = result
    }


}