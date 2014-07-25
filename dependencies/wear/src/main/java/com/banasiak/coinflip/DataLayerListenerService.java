package com.banasiak.coinflip;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by variable on 7/24/14.
 */
public class DataLayerListenerService extends WearableListenerService {
    private static final String TAG = DataLayerListenerService.class.getSimpleName();

    @Override
    public void onPeerConnected(Node peer) {
        Log.d(TAG, "Peer connected: " + peer.getDisplayName());
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        Log.d(TAG, "Peer disconnected: " + peer.getDisplayName());
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged()");
//        final List<DataEvent> events = FreezableUtils
//                .freezeIterable(dataEvents);

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }

        for (DataEvent event : dataEvents) {
            Log.d(TAG, "Event URI: " + event.getDataItem().getUri());
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/coins")) {

                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                Log.d(TAG, "Creating assets");
                Asset aHeads = dataMapItem.getDataMap().getAsset("heads");
                Asset aTails = dataMapItem.getDataMap().getAsset("tails");
                Asset aEdge = dataMapItem.getDataMap().getAsset("edge");
                Asset aBackground = dataMapItem.getDataMap().getAsset("background");

                Log.d(TAG, "Receiving bitmaps");
                Bitmap heads = BitmapFactory.decodeStream(Wearable.DataApi.getFdForAsset(googleApiClient, aHeads).await().getInputStream());
                Bitmap tails = BitmapFactory.decodeStream(Wearable.DataApi.getFdForAsset(googleApiClient, aTails).await().getInputStream());
                Bitmap edge = BitmapFactory.decodeStream(Wearable.DataApi.getFdForAsset(googleApiClient, aEdge).await().getInputStream());
                Bitmap background = BitmapFactory.decodeStream(Wearable.DataApi.getFdForAsset(googleApiClient, aBackground).await().getInputStream());

                Log.d(TAG, "Saving bitmaps");
                saveToInternalStorage(heads, "heads");
                saveToInternalStorage(tails, "tails");
                saveToInternalStorage(edge, "edge");
                saveToInternalStorage(background, "background");

            }
        }
        googleApiClient.disconnect();
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String fileName) {
        Log.d(TAG, "saveToInternalStorage(bitmapImage, " + fileName + ")");
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File dir = cw.getDir("coins", Context.MODE_PRIVATE);
        File f = new File(dir, fileName + ".png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Unable to save image to internal storage");
        }
        return dir.getAbsolutePath();
    }

}
