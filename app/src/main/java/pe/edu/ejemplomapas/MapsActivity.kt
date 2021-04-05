package pe.edu.ejemplomapas

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import java.util.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private lateinit var mMap: GoogleMap

    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION

    private val CODIGO_SOLICITUD_PERMISO = 100

    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var locationRequest: LocationRequest? = null

    private var callback: LocationCallback? = null

    private var listaMarcadores:ArrayList<Marker>? = null

    private var miPosicion:LatLng? = null

    //Marcadores de mapa
    private var marcadorGolden:Marker? = null
    private var marcadorPiramides:Marker? = null
    private var marcadorTorre:Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = FusedLocationProviderClient(this)
        inicializarLocationRequest()

        callback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                if(mMap != null) {
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    for (ubicacion in locationResult?.locations!!) {
                        Toast.makeText(applicationContext, ubicacion.latitude.toString() + " , " + ubicacion.longitude.toString(), Toast.LENGTH_LONG).show()
                        miPosicion = LatLng(ubicacion.latitude, ubicacion.longitude)
                        mMap.addMarker(MarkerOptions().position(miPosicion!!).title("Aqui estoy!"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(miPosicion))
                    }
                }
            }
        }
    }

    private fun inicializarLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.interval = 1000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        cambiarEstiloMapa()

        marcadoresEstaticos()

        crearListeners()

        prepararMarcadores()

        dibujarLineas()
    }

    private fun dibujarLineas() {
        val coordenadasLineas = PolylineOptions()
                .add(LatLng(19.438035150511304 , -99.15029436349869))
                .add(LatLng(19.44104913340259 , -99.14651446044444))
                .add(LatLng(19.44404092953131 , -99.14057102054359))
                .add(LatLng(19.437794547975827 , -99.13751095533371))
                .pattern(arrayListOf<PatternItem>(Dot(), Gap(10f)))
                .color(Color.CYAN)
                .width(30f)

        val coordenadasPoligonos = PolygonOptions()
                .add(LatLng(19.433383649089755 , -99.41161515563728))
                .add(LatLng(19.438134426617825 , -99.13905724883081))
                .add(LatLng(19.42880157493221 , -99.13845140486956))
                .strokePattern(arrayListOf<PatternItem>(Dash(10f), Gap(20f)))
                .strokeColor(Color.BLUE)
                .fillColor(Color.GREEN)
                .strokeWidth(10f)

        val coordenadasCirculos = CircleOptions()
                .center(LatLng(19.434200011141158 , -99.1477056965232))
                .radius(120.0)
                .strokePattern(arrayListOf<PatternItem>(Dash(10f), Gap(10f)))
                .strokeColor(Color.WHITE)
                .fillColor(Color.YELLOW)
                .strokeWidth(15f)

        mMap.addPolyline(coordenadasLineas)
        mMap.addPolygon(coordenadasPoligonos)
        mMap.addCircle(coordenadasCirculos)
    }

    private fun cambiarEstiloMapa() {
        val exitoCambioMapa = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.estilo_mapa))

        if(!exitoCambioMapa) {
            //Mencionar que hubo un problema al cambiar el tipo de mapa
        }
    }

    private fun crearListeners() {
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)
    }

    private fun marcadoresEstaticos() {
        //Golden Gate 37.8199286, -122.4782551
        //Piramides de Giza: 29.9772962, 31.1324955
        //Torre de Pisa: 43.722952, 10.396597
        val GOLDEN_GATE = LatLng(37.8199286, -122.4782551)
        val PIRAMIDES = LatLng(29.9772962, 31.1324955)
        val TORRE_PISA = LatLng(43.722952, 10.396597)

        marcadorGolden = mMap.addMarker(MarkerOptions()
                .position(GOLDEN_GATE)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_tren))
                .snippet("Metro de San Francisco")
                .alpha(1f)
                .title("Golden Gate"))
        marcadorGolden?.tag = 0

        marcadorPiramides = mMap.addMarker(MarkerOptions()
                .position(PIRAMIDES)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_tren))
                .snippet("Metro de Giza")
                .alpha(0.6f)
                .title("Piramides de Giza"))
        marcadorPiramides?.tag = 0

        marcadorTorre = mMap.addMarker(MarkerOptions()
                .position(TORRE_PISA)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                .snippet("Metro de Pisa")
                .alpha(0.9f)
                .title("Torre de Pisa"))
        marcadorTorre?.tag = 0
    }

    private fun prepararMarcadores() {
        listaMarcadores = ArrayList()

        mMap.setOnMapLongClickListener {
            location: LatLng? ->

            listaMarcadores!!.add(mMap.addMarker(MarkerOptions()
                    .position(location!!)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                    .snippet("Metro de Pisa")
                    .alpha(0.9f)
                    .title("Torre de Pisa"))
            )
            listaMarcadores?.last()!!.isDraggable = true

            val coordenadas = LatLng(listaMarcadores?.last()!!.position.latitude,listaMarcadores?.last()!!.position.longitude)
            val origen = "origin=" + miPosicion?.latitude + "," + miPosicion?.longitude + "&"
            var destino = "destination=" + coordenadas.latitude + "," + coordenadas.longitude + "&"
            val parametros = origen + destino + "sensor=false&mode=driving"

            cargarURL("http://maps.googleapis.com/maps/api/directions/json?" + parametros)
        }
    }

    override fun onMarkerDragEnd(marcador: Marker?) {
        Toast.makeText(this, "Acabo el evento Drag & Drop", Toast.LENGTH_SHORT).show()
        Log.d("MARCADOR FINAL", marcador?.position?.latitude.toString() + " , " + marcador?.position?.longitude.toString())
    }

    override fun onMarkerDragStart(marcador: Marker?) {
        Toast.makeText(this, "Empezando a mover el marcador", Toast.LENGTH_SHORT).show()
        Log.d("MARCADOR INCIAL", marcador?.position?.latitude.toString())
    }

    override fun onMarkerDrag(marcador: Marker?) {
        title = marcador?.position?.latitude.toString() + " - " + marcador?.position?.longitude.toString()
    }

    override fun onMarkerClick(marcador: Marker?): Boolean {
        var  numeroClicks = marcador?.tag as? Int

        if (numeroClicks != null) {
            numeroClicks++
            marcador?.tag = numeroClicks

            Toast.makeText(this, "Se han dado " + numeroClicks.toString() + " clicks", Toast.LENGTH_LONG).show()
        }
        return true
    }

    private fun validarPermisiosUbicacion():Boolean {
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(this, permisoFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria = ActivityCompat.checkSelfPermission(this, permisoCoarseLocation) == PackageManager.PERMISSION_GRANTED

        return hayUbicacionPrecisa && hayUbicacionOrdinaria
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        /*fusedLocationClient?.lastLocation?.addOnSuccessListener(this, object: OnSuccessListener<Location>{
            override fun onSuccess(location: Location?) {
                if (location != null) {
                    Toast.makeText(applicationContext, location?.latitude.toString() + " - " + location?.longitude.toString(), Toast.LENGTH_LONG).show()
                }
            }
        })*/
        fusedLocationClient?.requestLocationUpdates(locationRequest, callback, null)
    }

    private fun perdirPermisos() {
        val deboProveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(this, permisoFineLocation)

        if(deboProveerContexto) {
            //mandar un mensaje con explicacion adicional
            Toast.makeText(this, "Holi", Toast.LENGTH_LONG).show()
            solicitudPermiso()
        }else{
            solicitudPermiso()
        }
    }

    private fun solicitudPermiso() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(permisoFineLocation, permisoCoarseLocation), CODIGO_SOLICITUD_PERMISO)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CODIGO_SOLICITUD_PERMISO -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //obtener ubicacion
                    obtenerUbicacion()
                }else{
                    Toast.makeText(this, "No diste permiso para acceder a la ubicacion", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun detenerActualizacionUbicacion() {
        fusedLocationClient?.removeLocationUpdates(callback)
    }

    private fun cargarURL(url:String) {
        val queue = Volley.newRequestQueue(this)
        val solicitud = StringRequest(Request.Method.GET, url, Response.Listener<String> {
            response ->
            Log.d("HTTP", response)

            val coordenadas = obtenerCoordenadas(response)
            mMap.addPolyline(coordenadas)

        }, Response.ErrorListener {})
        queue.add(solicitud)
    }

    private fun obtenerCoordenadas(json:String):PolylineOptions {
        val gson = Gson()
        val objeto = gson.fromJson(json, pe.edu.ejemplomapas.Response::class.java)
        val puntos = objeto.routes?.get(0)!!.legs?.get(0)!!.steps!!
        var coordenadas = PolylineOptions()

        for (punto in puntos) {
            coordenadas.add(punto.start_location?.toLatLng())
            coordenadas.add(punto.end_location?.toLatLng())
        }
        coordenadas.color(Color.CYAN)
            .width(15f)

        return coordenadas
    }

    override fun onStart() {
        super.onStart()

        if(validarPermisiosUbicacion()) {
            obtenerUbicacion()
        }else{
            perdirPermisos()
        }
    }

    override fun onPause() {
        super.onPause()
        detenerActualizacionUbicacion()
    }
}