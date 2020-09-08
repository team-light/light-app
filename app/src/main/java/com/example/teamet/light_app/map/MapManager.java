package com.example.teamet.light_app.map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.BackgroundGrid;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;


public class MapManager {

    private Context context;

    private MapView mMapView;
    private ArcGISMap map;

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

    public MapManager(Context context, MapView mMapView, SQLiteDatabase db) {
        this.context = context;
        this.mMapView = mMapView;
        this.db = db;

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
                Iterator<Feature> resultIterator = result.iterator();
                while(resultIterator.hasNext()) {
                    Feature feature = resultIterator.next();
                    warnFeatureLayer.setFeatureVisible(feature, false);
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
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
                Iterator<Feature> resultIterator = result.iterator();
                while(resultIterator.hasNext()) {
                    Feature feature = resultIterator.next();
                    alertFeatureLayer.setFeatureVisible(feature, false);
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        map.getOperationalLayers().add(alertFeatureLayer);
    }
}