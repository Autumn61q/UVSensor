package com.example.uvsensor.service.utils;

import android.util.Log;

import com.amap.api.maps.model.LatLng;
import com.example.uvsensor.data.State;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class exportKML {
    private static String currentTime;

    public static String export(String outDir, List<State> States) {

        if (States == null || States.isEmpty()) {
            Log.e("exportKML", "No states to export");
            return "";
        }

        if (outDir == null || outDir.trim().isEmpty()) {
            Log.e("exportKML", "Invalid output directory");
            return "";
        }
        Log.d("exportKML", States.toString());
        StringWriter xmlWriter = new StringWriter();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlSerializer xmlSerializer = factory.newSerializer();
            xmlSerializer.setOutput(xmlWriter);
            xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            xmlSerializer.startDocument("UTF-8", true);

            xmlSerializer.startTag(null, "kml");
            xmlSerializer.attribute(null, "xmlns", "http://www.opengis.net/kml/2.2");
            xmlSerializer.attribute(null, "xmlns:gx", "http://www.google.com/kml/ext/2.2");

            xmlSerializer.startTag(null, "Document");

            xmlSerializer.startTag(null, "name");
            xmlSerializer.text(getCurrentTime.get());
            xmlSerializer.endTag(null, "name");

            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", "trackStyle");
            xmlSerializer.startTag(null, "LineStyle");
            xmlSerializer.startTag(null, "color");
            xmlSerializer.text("ff60ff00");
            xmlSerializer.endTag(null, "color");
            xmlSerializer.startTag(null, "width");
            xmlSerializer.text("6");
            xmlSerializer.endTag(null, "width");
            xmlSerializer.endTag(null, "LineStyle");
            xmlSerializer.endTag(null, "Style");

            xmlSerializer.startTag(null, "Placemark");
            xmlSerializer.startTag(null, "name");
            xmlSerializer.text("GPS Track");
            xmlSerializer.endTag(null, "name");
            xmlSerializer.startTag(null, "styleUrl");
            xmlSerializer.text("#trackStyle");
            xmlSerializer.endTag(null, "styleUrl");

            // Create track with timestamps
            createTrackWithTimestamps(xmlSerializer, States);

            xmlSerializer.endTag(null, "Placemark");
            xmlSerializer.endTag(null, "Document");
            xmlSerializer.endTag(null, "kml");
            xmlSerializer.endDocument();

        } catch (XmlPullParserException | IOException e) {
            throw new RuntimeException(e);
        }

        String xmlStr = xmlWriter.toString();
        return writeKmlFile(outDir, xmlStr);
    }

    private static void createTrackWithTimestamps(XmlSerializer xmlSerializer, List<State> States) throws IOException {

        xmlSerializer.startTag("http://www.google.com/kml/ext/2.2", "Track");

        int size = States.size();
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());  // "T" simply separates DATE from TIME, means "Time follows"
        isoFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        for (int i = 0; i < size; i++) {
            double latitude = States.get(i).latitude;
            double longitude = States.get(i).longitude;

            xmlSerializer.startTag("http://www.google.com/kml/ext/2.2", "coord");
            xmlSerializer.text(longitude + " " + latitude + " 0");
            xmlSerializer.endTag("http://www.google.com/kml/ext/2.2", "coord");
        }

        for (int i = 0; i < size; i++) {
            Long timestamp = States.get(i).timeStamp;

            xmlSerializer.startTag(null, "when");
            xmlSerializer.text(isoFormat.format(new Date(timestamp)));
            xmlSerializer.endTag(null, "when");
        }

        xmlSerializer.endTag("http://www.google.com/kml/ext/2.2", "Track");
    }

    private static String writeKmlFile(String outDir, String xmlContent) {
        File dir = new File(outDir);
        if (!dir.exists() || !dir.isDirectory()) {
            if (!dir.mkdirs()) {
                return "";
            }
        }

        try {
            currentTime = getCurrentTime.get();
            File file = new File(dir, "track-" + currentTime + ".kml");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(xmlContent);
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            return "";
        }
    }
}