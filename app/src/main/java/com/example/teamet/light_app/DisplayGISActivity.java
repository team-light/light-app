package com.example.teamet.light_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

public class DisplayGISActivity extends AppCompatActivity {

    private final static String TAG = "DisplayGISActivity";

    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_gis);

        // create a new map to display in the map view with a streets basemap
        mMapView = findViewById(R.id.mapView);
        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 35.665731, 139.731088, 16);
        mMapView.setMap(map);

        requestReadPermission();
    }

    /**
     * Creates a ShapefileFeatureTable from a service and, on loading, creates a FeatureLayer and add it to the map.
     */
    private void featureLayerShapefile() {
        Log.v(TAG, "featureLayerShapefile");
        // load the shapefile with a local path
        ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(
                getFilesDir().getPath() + "/TrailBikeNetwork.shp"
        );

        shapefileFeatureTable.loadAsync();
        shapefileFeatureTable.addDoneLoadingListener(() -> {
            if (shapefileFeatureTable.getLoadStatus() == LoadStatus.LOADED) {
                // create a feature layer to display the shapefile
                FeatureLayer shapefileFeatureLayer = new FeatureLayer(shapefileFeatureTable);

                // add the feature layer to the map
                mMapView.getMap().getOperationalLayers().add(shapefileFeatureLayer);

                // zoom the map to the extent of the shapefile
                mMapView.setViewpointAsync(new Viewpoint(shapefileFeatureLayer.getFullExtent()));
            } else {
                String error = "Shapefile feature table failed to load: " + shapefileFeatureTable.getLoadError().toString();
                Toast.makeText(DisplayGISActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Request read permission on the device.
     */
    private void requestReadPermission() {
        Log.v(TAG, "requestReadPermission");
        // define permission to request
        String[] reqPermission = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };
        int requestCode = 2;
        // For API level 23+ request permission at runtime
        if (ContextCompat.checkSelfPermission(DisplayGISActivity.this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "if_true");
            featureLayerShapefile();
        } else {
            Log.v(TAG, "if_false");
            // request permission
            ActivityCompat.requestPermissions(DisplayGISActivity.this, reqPermission, requestCode);

        }
    }

    /**
     * Handle the permissions request response.
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.v(TAG, "onRequestPermissionsResult");
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            featureLayerShapefile();
        } else {
            // report to user that permission was denied
            Toast.makeText(DisplayGISActivity.this,
                    getResources().getString(R.string.read_permission_denied),
                    Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }
}