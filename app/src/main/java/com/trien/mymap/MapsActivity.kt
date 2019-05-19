package com.trien.mymap

import android.graphics.Bitmap

import androidx.fragment.app.FragmentActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

import java.util.ArrayList

class MapsActivity : FragmentActivity(), OnMapReadyCallback, OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener {


    /**
     * GoogleMap instance.
     */
    private var mMap: GoogleMap? = null

    /**
     * List of GroundOverlay objects.
     */
    private val mGroundOverlay = ArrayList<GroundOverlay>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
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
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap

        // Move camera to wanted area.
        moveCameraToWantedArea()

        // Draw all polylines.
        drawAllPolyLines()

        // All all markers (bus stops) to the map.
        addAllBusStopMarkers()

        // Add all annotations as markers on map.
        addAllAnnotationsAsMarkers()

        // Add all directional arrows.
        addAllDirectionArrowsAsGroundOverlay()
    }

    /**
     * Method to move camera to wanted bus area.
     */
    private fun moveCameraToWantedArea() {
        mMap!!.setOnMapLoadedCallback {
            // Set up the bounds coordinates for the area we want the user's viewpoint to be.
            val bounds = LatLngBounds.Builder()
                    .include(BOUND1)
                    .include(BOUND2)
                    .build()
            // Move the camera now.
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, BOUNDS_PADDING))
        }
    }

    /**
     * Method to draw all poly lines.
     */
    private fun drawAllPolyLines() {
        // Add a blue Polyline.
        mMap!!.addPolyline(PolylineOptions()
                .color(resources.getColor(R.color.colorPolyLineBlue))
                .width(POLYLINE_WIDTH)
                .clickable(false)
                .addAll(readEncodedPolyLinePointsFromCSV(this, LINE_BLUE)))
        // Add a violet Polyline.
        mMap!!.addPolyline(PolylineOptions()
                .color(resources.getColor(R.color.colorPolyLineViolet))
                .width(POLYLINE_WIDTH)
                .clickable(false)
                .addAll(readEncodedPolyLinePointsFromCSV(this, LINE_VIOLET)))
        // Add an orange Polyline.
        mMap!!.addPolyline(PolylineOptions()
                .color(resources.getColor(R.color.colorPolyLineOrange))
                .width(POLYLINE_WIDTH)
                .clickable(false)
                .addAll(readEncodedPolyLinePointsFromCSV(this, LINE_ORANGE)))
        // Add a green Polyline.
        mMap!!.addPolyline(PolylineOptions()
                .color(resources.getColor(R.color.colorPolyLineGreen))
                .width(POLYLINE_WIDTH)
                .clickable(false)
                .addAll(readEncodedPolyLinePointsFromCSV(this, LINE_GREEN)))
        // Add a pink Polyline.
        mMap!!.addPolyline(PolylineOptions()
                .color(resources.getColor(R.color.colorPolyLinePink))
                .width(POLYLINE_WIDTH)
                .clickable(false)
                .addAll(readEncodedPolyLinePointsFromCSV(this, LINE_PINK)))
    }

    /**
     * Method to add all bus stop markers based on line colors.
     */
    private fun addAllBusStopMarkers() {
        addBulkMarkers(MAIN_MARKER)
        addBulkMarkers(LINE_BLUE)
        addBulkMarkers(LINE_ORANGE)
        addBulkMarkers(LINE_VIOLET)
        addBulkMarkers(LINE_GREEN)
        addBulkMarkers(LINE_PINK)
    }

    /**
     * Helper method to add bulk markers to map as per line keyword.
     */
    private fun addBulkMarkers(lineKeyword: String) {

        // Create a list of LatLng objects and load all of the markers coordinates from CSV file.
        val latLngList = readMarkersFromCSV(this, lineKeyword)

        // Create a Bitmap object that holds the right resized marker image based on line color.
        var resizedBitmap = resizeMarker(this, R.drawable.marker_blue_dark)
        when (lineKeyword) {
            LINE_BLUE -> resizedBitmap = resizeMarker(this, R.drawable.marker_blue_light)
            LINE_ORANGE -> resizedBitmap = resizeMarker(this, R.drawable.marker_orange)
            LINE_VIOLET -> resizedBitmap = resizeMarker(this, R.drawable.marker_violet)
            LINE_GREEN -> resizedBitmap = resizeMarker(this, R.drawable.marker_green)
            LINE_PINK -> resizedBitmap = resizeMarker(this, R.drawable.marker_pink)
            MAIN_MARKER -> resizedBitmap = resizeMarker(this, R.drawable.marker_blue_dark)
        }

        // For each of LatLng object in the latLngList, add a relevant marker with the right latitude
        // and longitude values and bitmap image.
        for (latLng in latLngList) {
            mMap!!.addMarker(MarkerOptions()
                    .position(LatLng(latLng.latitude, latLng.longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap)))
            //newMarker.showInfoWindow();
            //newMarker.setTag();
        }
    }

    /**
     * Method to add all annotations on map as markers. This case the texts won't scale when we zoom the map
     * Use helper method resizeCommonAnnotation() to adjust the size of every annotation image accordingly
     * The anchor() method specifies the anchor at a particular point in the marker image.
     * The anchor specifies the point in the icon image that is anchored to the marker's position on the Earth's surface.
     * The anchor point is specified in the continuous space [0.0, 1.0] x [0.0, 1.0], where (0, 0) is
     * the top-left corner of the image, and (1, 1) is the bottom-right corner. The anchoring point
     * in a W x H image is the nearest discrete grid point in a (W + 1) x (H + 1) grid, obtained by
     * scaling the then rounding. For example, in a 4 x 2 image, the anchor point (0.7, 0.6) resolves
     * to the grid point at (3, 1).
     */
    /*      *-----+-----+-----+-----*
            |     |     |     |     |
            |     |     |     |     |
            +-----+-----+-----+-----+
            |     |     |   X |     |   (U, V) = (0.7, 0.6)
            |     |     |     |     |
            *-----+-----+-----+-----*

            *-----+-----+-----+-----*
            |     |     |     |     |
            |     |     |     |     |
            +-----+-----+-----X-----+   (X, Y) = (3, 1)
            |     |     |     |     |
            |     |     |     |     |
            *-----+-----+-----+-----*      */
    private fun addAllAnnotationsAsMarkers() {

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_whalers_inn)))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.5863, 138.59842))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_yilki_store)))
                .anchor(0f, 0f)
                .position(LatLng(-35.574789, 138.602354))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_victor_harbor_holiday_park)))
                .anchor(1f, 1f)
                .position(LatLng(-35.559881, 138.608045))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_beachfront_holiday_park)))
                .anchor(0f, 0f)
                .position(LatLng(-35.559180000000005, 138.61157))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_warland_reserve)))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.55678, 138.62361))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_adare_caravan_park)))
                .anchor(0f, 0f)
                .position(LatLng(-35.542840000000005, 138.63001))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_repco_bus_stop6)))
                .anchor(0.5f, 1f)
                .position(LatLng(-35.536411, 138.639775))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_bus_stop8)))
                .anchor(0.5f, 0f)
                .position(LatLng(-35.534787, 138.650920))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_port_elliot_bus_stop)))
                .anchor(0.5f, 1f)
                .position(LatLng(-35.530169, 138.681719))
        )
        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_port_elliot_holiday_park)))
                .anchor(0f, 0f)
                .position(LatLng(-35.530570000000004, 138.68959))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_middleton_store_stop12)))
                .anchor(0.3f, 0f)
                .position(LatLng(-35.510994, 138.703837))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_chapman_rd_stop13)))
                .anchor(0.3f, 1f)
                .position(LatLng(-35.509204, 138.721063))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_goolwa_camping_tourist_park)))
                .anchor(0.5f, 1f)
                .position(LatLng(-35.498173, 138.772636))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_goolwa_caltex)))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.499598, 138.78091))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_goolwa_stratco)))
                .anchor(0.5f, 0f)
                .position(LatLng(-35.504947, 138.779322))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_beach_td)))
                .anchor(0.5f, 0f)
                .position(LatLng(-35.515146, 138.774872))
        )

        mMap!!.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeCommonAnnotation(this, R.drawable.anno_bus_stop14)))
                .anchor(0.5f, 0f)
                .position(LatLng(-35.505026, 138.772299))
        )
    }

    /**
     * Method to add all directional arrows as ground overlay images.
     * The bearing() method specifies the bearing of the ground overlay in degrees clockwise from north.
     * The rotation is performed about the anchor point.
     * If not specified, the default is 0 (i.e., up on the image points north).
     * Values outside the range [0, 360) will be normalized (Google developer docs).
     * DIRECTION_ARROW_WIDTH specifies the default width of the Ground Overlays
     */
    private fun addAllDirectionArrowsAsGroundOverlay() {

        // Blue arrows.
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_blue))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.586970, 138.597452), DIRECTION_ARROW_WIDTH)
                .bearing(250f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_blue))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.579636, 138.598846), DIRECTION_ARROW_WIDTH)
                .bearing(100f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_blue))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.571573, 138.592793), DIRECTION_ARROW_WIDTH)
                .bearing(55f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_blue))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.574142, 138.604587), DIRECTION_ARROW_WIDTH)
                .bearing(315f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_blue))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.563598, 138.601641), DIRECTION_ARROW_WIDTH)
                .bearing(170f)
                .clickable(false)))

        // Orange arrows.
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_orange))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.559907, 138.616066), DIRECTION_ARROW_WIDTH)
                .bearing(340f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_orange))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.556842, 138.625798), DIRECTION_ARROW_WIDTH)
                .bearing(265f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_orange))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.555459, 138.619425), DIRECTION_ARROW_WIDTH)
                .bearing(165f)
                .clickable(false)))

        // Violet arrows.
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_violet))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.549445, 138.622413), DIRECTION_ARROW_WIDTH)
                .bearing(305f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_violet))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.544262, 138.630482), DIRECTION_ARROW_WIDTH)
                .bearing(130f)
                .clickable(false)))

        // Green arrows.
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_green))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.533821, 138.662244), DIRECTION_ARROW_WIDTH)
                .bearing(170f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_green))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.530991, 138.667883), DIRECTION_ARROW_WIDTH)
                .bearing(350f)
                .clickable(false)))

        // Pink arrows.
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_pink))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.517387, 138.685901), DIRECTION_ARROW_WIDTH)
                .bearing(300f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_pink))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.513056, 138.698840), DIRECTION_ARROW_WIDTH)
                .bearing(170f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_pink))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.507226, 138.736613), DIRECTION_ARROW_WIDTH)
                .bearing(355f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_pink))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.508623, 138.747492), DIRECTION_ARROW_WIDTH)
                .bearing(175f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_pink))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.504478, 138.760327), DIRECTION_ARROW_WIDTH)
                .bearing(280f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_pink))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.497321, 138.771540), DIRECTION_ARROW_WIDTH)
                .bearing(355f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_pink))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.508181, 138.777899), DIRECTION_ARROW_WIDTH)
                .bearing(100f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_pink))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.512730, 138.773930), DIRECTION_ARROW_WIDTH)
                .bearing(280f)
                .clickable(false)))
        mGroundOverlay.add(mMap!!.addGroundOverlay(GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.arrow_pink))
                .anchor(0f, 0.5f)
                .position(LatLng(-35.506263, 138.770638), DIRECTION_ARROW_WIDTH)
                .bearing(175f)
                .clickable(false)))
    }

    companion object {

        /**
         * Default log tag name for log message.
         */
        private val LOG_TAG = MapsActivity::class.java.name

        /**
         * Bound values for camera focus on app start.
         * BOUND1 is the relative coordinates of the bottom-left corner of the bounded area on the map.
         * BOUND2 is the relative coordinates of the top-right corner of the bounded area on the map.
         */
        private val BOUND1 = LatLng(-35.595209, 138.585857)
        private val BOUND2 = LatLng(-35.494644, 138.805927)

        /**
         * Keyword constants for reading values from polylines.csv.
         * Important: these keywords values must be exactly the same as ones in polylines.csv file in raw folder.
         */
        val LINE_BLUE = "lineBlue"
        val LINE_ORANGE = "lineOrange"
        val LINE_VIOLET = "lineViolet"
        val LINE_GREEN = "lineGreen"
        val LINE_PINK = "linePink"
        val MAIN_MARKER = "mainMarker"

        /**
         * Default width of directional arrows.
         */
        private val DIRECTION_ARROW_WIDTH = 400f

        /**
         * Default polyline width.
         */
        private val POLYLINE_WIDTH = 20f

        /**
         * Default padding for the bounded area.
         */
        private val BOUNDS_PADDING = 50
    }
}
