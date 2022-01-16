package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.loc_dialog.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


//This class allows you to interact with the map by adding markers, styling its appearance and
// displaying the user's location.
final class MapsActivity : FragmentActivity(), OnMapReadyCallback, LocationListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MyOnClickListener {

    private var mMap: GoogleMap? = null
    internal lateinit var mLastLocation: Location
    internal lateinit var mLocationCallback: LocationCallback
    internal var mCurrLocationMarker: Marker? = null
    internal var mGoogleApiClient: GoogleApiClient? = null
    lateinit var dialog : BottomSheetDialog
    lateinit var dialog2 : BottomSheetDialog
    private val REQUEST_LOCATION_PERMISSION = 1
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var con:ConstraintLayout


    lateinit var recyclerView:RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mFusedLocationClient=LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val places:Button=findViewById(R.id.button3)
         con=findViewById<ConstraintLayout>(R.id.con)
        con.visibility=View.VISIBLE


        places.setOnClickListener{
            con.visibility=View.GONE
            dialog = BottomSheetDialog(this)

            val view = layoutInflater.inflate(R.layout.bottom_sheet, null)
            recyclerView=view.findViewById<RecyclerView>(R.id.recycle)
            view.findViewById<Button>(R.id.popular).setOnClickListener {
                recyclerView.visibility=View.VISIBLE
            }
            view.findViewById<Button>(R.id.fav).setOnClickListener {
                recyclerView.visibility=View.GONE
            }
            view.findViewById<Button>(R.id.visited).setOnClickListener {
                recyclerView.visibility=View.GONE
            }
            val formList: List<HashMap<String, String>> = parseMyJson()

            val homeAdapter2 = HomeAdapter(formList,this)
            recyclerView.adapter = homeAdapter2

            val btnClose = view.findViewById<ImageButton>(R.id.ibCancel)
            btnClose.setOnClickListener {
                dialog.dismiss()
                con.visibility=View.VISIBLE
            }
            dialog.setCancelable(false)
            dialog.setContentView(view)
            dialog.show()


        }
        fetchLocation()
    }

    private fun fetchLocation() {
        val task: Task<Location> =mFusedLocationClient.lastLocation

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),101)
            return
        }
       task.addOnSuccessListener {
           if(it!=null){
              val latitude= it.latitude
               val longitude=it.longitude
               //Toast.makeText(this,"${latitude} ${longitude}",Toast.LENGTH_SHORT).show()
               val latLng = LatLng(latitude,longitude)
               val zoomLevel = 15f

               val homeLatLng = LatLng(latitude, longitude)
               mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
               val googleOverlay = GroundOverlayOptions()
                   .image(BitmapDescriptorFactory.fromResource(R.drawable.h))
                   .position(latLng, 100f)
               mMap!!.addGroundOverlay(googleOverlay)
           }
       }
    }



    private fun parseMyJson(): List<HashMap<String, String>> {
        val formList = ArrayList<HashMap<String, String>>()
        var m_li: HashMap<String, String>
        try {

            val obj = JSONObject(loadJSONFromAsset())
            val m_jArry: JSONArray = obj.getJSONArray("loc")

            for (i in 0 until m_jArry.length()) {
                val jo_inside = m_jArry.getJSONObject(i)

                Log.d("Details-->", jo_inside.getString("lat"))
                val name = jo_inside.getString("name")
                val img = jo_inside.getString("img")
                val place = jo_inside.getString("place")
                val lat = jo_inside.getString("lat")
                val long = jo_inside.getString("lot")


                m_li = HashMap()
                m_li["name"] = name
                m_li["img"]=img
                m_li["place"]=place
                m_li["lat"] = lat
                m_li["lot"] = long
                formList.add(m_li)
                 val latLng=LatLng(lat.toDouble(),long.toDouble())
                 mMap!!.addMarker( MarkerOptions()?.position(latLng).title(name)
                     .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                 mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                 mMap!!.maxZoomLevel
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return formList

    }

    private fun loadJSONFromAsset(): String? {
        var json: String? = null
        json = try {
            val js = this.assets.open("loc.json")

            val size = js.available()
            val buffer = ByteArray(size)
            js.read(buffer)
            js.close()
            String(buffer)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = false
        mMap?.uiSettings?.isZoomControlsEnabled = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient()
                mMap?.isMyLocationEnabled = true
            }
        } else {
            buildGoogleApiClient()
            mMap?.isMyLocationEnabled = true
        }

    }

    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()


    }

    override fun onConnected(bundle: Bundle?) {

    }


    override fun onLocationChanged(location: Location) {
        val overlaySize=100f
        mLastLocation = location
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        //Place current location marker
        val latLng = LatLng(location.latitude, location.longitude)

        val googleOverlay = GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(R.drawable.girl))
            .position(latLng, overlaySize)
        mMap!!.addGroundOverlay(googleOverlay)


        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Current Position")
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.girl))
        //mCurrLocationMarker = mMap!!.addMarker(markerOptions)
        mMap!!.addMarker(markerOptions)

        //move map camera
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))

        //stop location updates
        if (mGoogleApiClient != null) {
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Toast.makeText(applicationContext,"connection failed", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionSuspended(p0: Int) {
        Toast.makeText(applicationContext,"connection suspended", Toast.LENGTH_SHORT).show()
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mMap?.isMyLocationEnabled  = true
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    override fun onClick(name:String,place:String,lat:Double,long:Double,img:String) {
        dialog2 = BottomSheetDialog(this)

        val view2 = layoutInflater.inflate(R.layout.loc_dialog, null)
        dialog.dismiss()
        dialog2.setCancelable(false)
        dialog2.setContentView(view2)
        dialog2.show()

        Glide.with(view2.dimg.context)
            .load(img)
            .into(view2.dimg)
        view2.dname.text=name
        view2.dplace.text=place
        view2.send.setOnClickListener {
            dialog2.dismiss()
            con.visibility=View.VISIBLE
            mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        val zoomLevel = 15f

        val homeLatLng = LatLng(lat, long)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        mMap!!.addMarker(MarkerOptions().position(homeLatLng))
        mMap!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
    }

    override fun onBackPressed() {
        super.onBackPressed()
      //  dialog2.dismiss()
    }
}