package com.example.ds.jointest;

import android.os.AsyncTask;
import android.util.Log;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by DS on 2018-06-24.
 */

public class ConvertTask extends AsyncTask {
    String add=null;
    TMapData tmapdata;
    @Override
    protected Object doInBackground(Object[] objects) {
        tmapdata=new TMapData();
        TMapPoint point= (TMapPoint) objects[0];
        Log.d("contask","start");
        try {
            Log.d("contask","task");

            add=tmapdata.convertGpsToAddress(point.getLatitude(),point.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return add;
    }
}
