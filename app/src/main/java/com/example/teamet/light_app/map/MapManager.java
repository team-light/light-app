package com.example.teamet.light_app.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.BackgroundGrid;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.example.teamet.light_app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


public class MapManager {

    private Context context;

    private MapView mMapView;
    private ArcGISMap map;

    private Callout mCallout;
    private GraphicsOverlay mGraphicsOverlay;

    private SQLiteDatabase db;

    private final String TAG = "MAP";
    private String[] shapefileNames = {
            "N03-20_200101.dbf",
            "N03-20_200101.geojson",
            "N03-20_200101.prj",
            "N03-20_200101.shp",
            "N03-20_200101.shx",
            "N03-20_200101.xml"
    };
    private final String dirName = "N03-200101_GML/";
    private String dirPath;
    private final String shapefileName = "N03-20_200101.shp";

    private static int borderEq;
    private static String borderAlarm;

    public MapManager(Context context, MapView mMapView, SQLiteDatabase db) {
        this.context = context;
        this.mMapView = mMapView;
        this.db = db;

        MapManager.readPreferences(context);

        dirPath = context.getFilesDir().getPath() + "/" + dirName;
        File dir = new File(dirPath);
        if(!dir.exists()) {
            if(dir.mkdir()) {
                downloadShapefile();
            }
        }
    }

    private void downloadShapefile() {
        try {
            Toast.makeText(context, "Loading files.", Toast.LENGTH_LONG).show();
            for (String filename : shapefileNames) {
                File file = new File(dirPath + filename);
                Log.v(TAG, "Loading ... [" + filename + "]");
                InputStream is = context.getAssets().open(dirName + filename);
                OutputStream os = new FileOutputStream(file);
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) >= 0) {
                    os.write(buf, 0, len);
                }
                is.close();
                os.close();
                Log.v(TAG, "[" + filename + "] Loading completed.");
            }
            Toast.makeText(context, "Loading completed.", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setMap() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if(info != null){
            ArcGISRuntimeEnvironment.setLicense(context.getString(R.string.arcgis_license_key));
            Basemap.Type basemapType = Basemap.Type.STREETS_VECTOR;
            double[] tokyo = Prefectures.GetCoodinate(Prefectures.TOKYO);
            int levelOfDetail = 8;
            ArcGISMap map = new ArcGISMap(basemapType, tokyo[0], tokyo[1], levelOfDetail);
            mMapView.setMap(map);
        }else {
            map = new ArcGISMap(SpatialReference.create(6668));
            mMapView.setMap(map);

            BackgroundGrid backgroundGrid = new BackgroundGrid();
            backgroundGrid.setColor(Color.BLACK);
            mMapView.setBackgroundGrid(backgroundGrid);

            ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(dirPath + shapefileName);
            shapefileFeatureTable.loadAsync();

            FeatureLayer featureLayer = new FeatureLayer(shapefileFeatureTable);
            SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.LTGRAY, 0.01f);
            SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.GRAY, lineSymbol);
            SimpleRenderer renderer = new SimpleRenderer(fillSymbol);
            featureLayer.setRenderer(renderer);

            map.getOperationalLayers().add(featureLayer);

            shapefileFeatureTable.addDoneLoadingListener(() -> {
                if (shapefileFeatureTable.getLoadStatus() == LoadStatus.LOADED) {
                    Log.v(TAG, "map: " + shapefileFeatureTable.getLoadStatus());
                    mMapView.setViewpointAsync(new Viewpoint(featureLayer.getFullExtent()));
                } else {
                    Log.e(TAG, shapefileFeatureTable.getLoadError().toString());
                }
            });

        }

        createGraphics();
    }

    @SuppressLint("Recycle")
    public void setWarning() {
        QueryParameters queryParameters = new QueryParameters();
        StringBuilder sbuilder;
        Cursor target;

        ShapefileFeatureTable warnShpTable = new ShapefileFeatureTable(dirPath + shapefileName);
        warnShpTable.loadAsync();

        FeatureLayer warnFeatureLayer = new FeatureLayer(warnShpTable);
        SimpleLineSymbol warnLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.LTGRAY, 0.01f);
        SimpleFillSymbol warnFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.YELLOW, warnLineSymbol);
        SimpleRenderer warnRenderer = new SimpleRenderer(warnFillSymbol);
        warnFeatureLayer.setRenderer(warnRenderer);

        target = db.query(
                "warn_info",
                new String[] {"city"},
                "alert LIKE %注意報%",
                null,
                null,
                null,
                null
        );
        target.moveToFirst();
        sbuilder = new StringBuilder("NOT IN(");
        while(target.getCount() > 0) {
            sbuilder.append(target.getInt(0)).append(",");
            target.moveToNext();
        }
        sbuilder.deleteCharAt(sbuilder.length() - 1).append(")");
        queryParameters.setWhereClause(sbuilder.toString());

        ListenableFuture<FeatureQueryResult> warnQueryResult = warnShpTable.queryFeaturesAsync(queryParameters);
        warnQueryResult.addDoneListener(() -> {
            try {
                FeatureQueryResult result = warnQueryResult.get();
                for (Feature feature : result) {
                    warnFeatureLayer.setFeatureVisible(feature, false);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        map.getOperationalLayers().add(warnFeatureLayer);


        ShapefileFeatureTable alertShpTable = new ShapefileFeatureTable(dirPath + shapefileName);
        alertShpTable.loadAsync();

        FeatureLayer alertFeatureLayer = new FeatureLayer(alertShpTable);
        SimpleLineSymbol alertLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.LTGRAY, 0.01f);
        SimpleFillSymbol alertFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.RED, alertLineSymbol);
        SimpleRenderer alertRenderer = new SimpleRenderer(alertFillSymbol);
        alertFeatureLayer.setRenderer(alertRenderer);

        target = db.query(
                "warn_info",
                new String[] {"city"},
                "alert LIKE %警報%",
                null,
                null,
                null,
                null
        );
        target.moveToFirst();
        sbuilder = new StringBuilder("NOT IN(");
        while(target.getCount() > 0) {
            sbuilder.append(target.getInt(0)).append(",");
            target.moveToNext();
        }
        sbuilder.deleteCharAt(sbuilder.length() - 1).append(")");
        queryParameters.setWhereClause(sbuilder.toString());

        ListenableFuture<FeatureQueryResult> alertQueryResult = alertShpTable.queryFeaturesAsync(queryParameters);
        alertQueryResult.addDoneListener(() -> {
            try {
                FeatureQueryResult result = alertQueryResult.get();
                for (Feature feature : result) {
                    alertFeatureLayer.setFeatureVisible(feature, false);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        map.getOperationalLayers().add(alertFeatureLayer);
    }
    
    @SuppressLint("ClickableViewAccessibility")
    public void setTouch(){
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(context, mMapView) {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
                Point mapPoint = mMapView.screenToLocation(screenPoint);
                Point wgs84Point = (Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84());
                TextView calloutContent = new TextView(context.getApplicationContext());
                calloutContent.setTextColor(Color.BLACK);

                int pref = Prefectures.GetNearPref(wgs84Point.getY(), wgs84Point.getX());
                calloutContent.setText(Prefectures.Pref[pref]);
                Point prefPoint = Prefectures.GetPoint(pref);

                mCallout = mMapView.getCallout();
                mCallout.setLocation(prefPoint);
                mCallout.setContent(calloutContent);
                mCallout.show();

                mMapView.setViewpointCenterAsync(prefPoint);

                return true;
            }
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    public void setTouchAlarm(){
        String[] alarmdata = new String[Prefectures.PREF_NUM];
        try{
            for(int i=0; i<alarmdata.length; i++){
                Cursor target = db.query(
                        "alert_view",
                        null,
                        "pref_name=?",
                        new String[] {Prefectures.Pref[i]},
                        null,
                        null,
                        null
                );
                target.moveToFirst();
                alarmdata[i] = "";
                Log.v("setTouchAlarm", Prefectures.Pref[i]+" "+target.getCount());
                for(int j=target.getCount(); 0<j; j--) {
                    String time = target.getString(0);
                    String area = target.getString(2);
                    String alarm = target.getString(3);
                    if(!alarm.equals("") && alarm.contains(borderAlarm)){
                        alarmdata[i] += time+" "+area+" "+alarm+"\n";
                        Log.v("setTouchAlarm", ">>"+ time+" "+area+" "+alarm+" "+alarm.contains(borderAlarm));
                    }
                    target.moveToNext();
                }
                target.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(context, mMapView) {

            @SuppressLint("SetTextI18n")
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
                Point mapPoint = mMapView.screenToLocation(screenPoint);
                Point wgs84Point = (Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84());
                TextView calloutContent = new TextView(context.getApplicationContext());
                calloutContent.setTextColor(Color.BLACK);

                int pref = Prefectures.GetNearPref(wgs84Point.getY(), wgs84Point.getX());
                calloutContent.setText(Prefectures.Pref[pref]+"\n"+alarmdata[pref]);
                Point prefPoint = Prefectures.GetPoint(pref);

                mCallout = mMapView.getCallout();
                mCallout.setLocation(prefPoint);
                mCallout.setContent(calloutContent);
                mCallout.show();

                mMapView.setViewpointCenterAsync(prefPoint);

                return true;
            }
        });
    }
    @SuppressLint("ClickableViewAccessibility")
    public void setTouchEq(){
        String[] eqdata = new String[Prefectures.PREF_NUM];
        for(int i=0; i<eqdata.length; i++) eqdata[i] = "";
        try{
            Cursor target = db.query(
                    "eq_info",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            target.moveToFirst();
            JSONArray city_list = new JSONArray(target.getString(8));
            target.close();
            for(int i=city_list.length()-1; 0<i; i--){
                JSONObject tag_ = city_list.getJSONObject(i);
                int max_int = tag_.getInt("max_int");
                if(MapManager.borderEq<=max_int){
                    JSONObject prefs = tag_.getJSONObject("prefs");
                    Iterator<String> pref_list = prefs.keys();
                    while (pref_list.hasNext()){
                        String pref = pref_list.next();
                        int pref_index = Prefectures.GetIndex(pref);
                        eqdata[pref_index] += "震度："+max_int+"\n";
                        JSONArray area = prefs.getJSONArray(pref);
                        for(int j=0; j<area.length(); j++) eqdata[pref_index] += area.getString(j)+", ";
                        eqdata[pref_index] += "\n";
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(context, mMapView) {

            @SuppressLint("SetTextI18n")
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
                Point mapPoint = mMapView.screenToLocation(screenPoint);
                Point wgs84Point = (Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84());
                TextView calloutContent = new TextView(context.getApplicationContext());
                calloutContent.setTextColor(Color.BLACK);

                int pref = Prefectures.GetNearPref(wgs84Point.getY(), wgs84Point.getX());
                calloutContent.setText(Prefectures.Pref[pref]+"\n"+eqdata[pref]);
                Point prefPoint = Prefectures.GetPoint(pref);

                mCallout = mMapView.getCallout();
                mCallout.setLocation(prefPoint);
                mCallout.setContent(calloutContent);
                mCallout.show();

                mMapView.setViewpointCenterAsync(prefPoint);

                return true;
            }
        });
    }

    private void createGraphics() {
        createGraphicsOverlay();
        createPointGraphics();
    }
    private void createGraphicsOverlay() {
        mGraphicsOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
    }
    private void createPointGraphics() {
        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.rgb(226, 119, 40), 10.0f);
        pointSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 2.0f));
        for(int i = 0; i< Prefectures.PREF_NUM; i++){
            Graphic pointGraphic = new Graphic(Prefectures.GetPoint(i), pointSymbol);
            mGraphicsOverlay.getGraphics().add(pointGraphic);
        }
    }

    public static void readPreferences(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        borderAlarm = sharedPreferences.getString("list_map_alarm", "");
        borderEq = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("list_map_eq", "1")));

        Log.v("Settings", "MapManager: borderEq="+borderEq+", borderAlarm="+borderAlarm);
    }
}