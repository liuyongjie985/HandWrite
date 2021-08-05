/*
 * Copyright (c) MyScript. All rights reserved.
 */

package com.myscript.iink.samples.batchmode;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.myscript.iink.Configuration;
import com.myscript.iink.ContentPackage;
import com.myscript.iink.ContentPart;
import com.myscript.iink.Editor;
import com.myscript.iink.Engine;
import com.myscript.iink.MimeType;
import com.myscript.iink.PointerEvent;
import com.myscript.iink.PointerEventType;
import com.myscript.iink.Renderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // To enable text recognition for a specific language,
    // see also: https://developer.myscript.com/support/recognition-assets
    private static final String language = "zh_CN";
    private static String packageName = "package.iink";
    // Choose type of content ("Text", "Math", "Diagram", "Raw Content")
    private static String partType = "Text";
    private Engine engine;
    private Editor editor;
    private DisplayMetrics displayMetrics;
    private ContentPackage contentPackage;
    private ContentPart contentPart;


    private void readfile(String assetDir, String dir, Renderer renderer) {
        String[] files;
        float total = 0;
        float error = 0;
        try {
            // 获得Assets一共有几多文件
            System.out.println("开始读取文件");
            files = this.getResources().getAssets().list(assetDir);
            System.out.println("读取文件完毕");

            File param3 = getExternalFilesDir(null);
            String param4 = File.separator + "right_file.txt";

            File outFile1 = new File(param3, param4);

            if (outFile1.exists())
                outFile1.delete();

            OutputStream out1 = new FileOutputStream(outFile1);

            File param5 = getExternalFilesDir(null);
            String param6 = File.separator + "error_file.txt";

            File outFile3 = new File(param5, param6);

            if (outFile3.exists())
                outFile3.delete();

            OutputStream out3 = new FileOutputStream(outFile3);

            for (int i = 0; i < files.length; i++) {
                // 获得每个文件的名字
                String fileName = files[i];
                String suffix = fileName.substring(fileName.length() - 5);
                if (suffix.equals(".json")) {
                    try {

                        // Load the pointerEvents from the right .json file depending of the part type
                        PointerEvent[] pointerEvents = loadPointerEvents(assetDir + "/" + fileName);
                        int sign = 0;
                        ArrayList<PointerEvent> new_pointerEvents = new ArrayList<PointerEvent>();

                        ArrayList<PointerEvent> temp_pointerEvents = null;

                        for (int j = 0; j < pointerEvents.length; j++) {
                            //                        如果遇到DOWN,则为一个新的开始
                            if (pointerEvents[j].eventType.toString().equals("DOWN")) {
                                sign -= 1;
                                temp_pointerEvents = new ArrayList<PointerEvent>();
                                temp_pointerEvents.add(pointerEvents[j]);
                                if (sign != -1) {
                                    sign = -1;
                                }
                            }
                            if (pointerEvents[j].eventType.toString().equals("UP")) {
                                sign += 1;

                                if (sign == 0) {
                                    temp_pointerEvents.add(pointerEvents[j]);
                                    new_pointerEvents.addAll(temp_pointerEvents);

                                }

                            }
                            if (pointerEvents[j].eventType.toString().equals("MOVE")) {
                                if (sign == -1) {
                                    temp_pointerEvents.add(pointerEvents[j]);
                                }


                            }
                        }

//                        if (sign != 0) {
//                            error += 1;
//                            out3.write(fileName.getBytes());
//                            out3.write("\n".getBytes());
//                            out3.flush();
//                        }

                        pointerEvents = new PointerEvent[new_pointerEvents.size()];
                        for (int j = 0; j < pointerEvents.length; j++) {
                            pointerEvents[j] = new_pointerEvents.get(j);
                        }


                        packageName = i + "package.iink";

                        // Create a new package
                        contentPackage = engine.createPackage(packageName);

                        // Create a new part
                        contentPart = contentPackage.createPart(partType);
                        // Associate editor with the new part
                        editor.setPart(contentPart);

                        // Feed the editor
                        editor.pointerEvents(pointerEvents, false);

                        // Export the result of the recognition into a file
                        export(fileName);
                        out1.write(fileName.getBytes());
                        out1.write("\n".getBytes());
                        out1.flush();
                        System.out.println(total + " done!");
                    } catch (Exception e1) {

                        e1.printStackTrace();
//                    editor.clear();
                        editor.setPart(null);
                        contentPart.close();
                        contentPackage.close();

////                editor
////
                        try {
                            engine.deletePackage(packageName);
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }


                    }
                    total += 1;
                }


            }
            out1.close();
            File param1 = getExternalFilesDir(null);
            String param2 = File.separator + "error_rate.txt";
            // Exported file is stored in the Virtual SD Card : "Android/data/com.myscript.iink.batchmode/files"
            File outFile2 = new File(param1, param2);

            if (outFile2.exists())
                outFile2.delete();

            OutputStream out = new FileOutputStream(outFile2);
            out.write(new Float(error).toString().getBytes());
            out.write("\n".getBytes());
            out.write(new Float(total).toString().getBytes());
            out.write("\n".getBytes());
            out.write(new Float(error / total).toString().getBytes());
            out.close();


            finish();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        engine = IInkApplication.getEngine();
        // Configure recognition
        Configuration conf = engine.getConfiguration();
        String confDir = "zip://" + getPackageCodePath() + "!/assets/conf";
        conf.setStringArray("configuration-manager.search-path", new String[]{confDir});
        conf.setString("content-package.temp-folder", getFilesDir().getPath() + File.separator + "tmp");
        conf.setString("lang", language);
        // Configure the engine to disable guides (recommended)
        conf.setBoolean("text.guides.enable", false);
        // Create a renderer with a null render target
        displayMetrics = getResources().getDisplayMetrics();
        Renderer renderer = engine.createRenderer(displayMetrics.xdpi, displayMetrics.ydpi, null);
        // Create the editor
        editor = engine.createEditor(renderer);
        // The editor requires a font metrics provider and a view size *before* calling setPart()
        editor.setFontMetricsProvider(null);
        editor.setViewSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
        readfile("conf/pointerEvents/" + partType.toLowerCase() + "/" + language + "/output", "./", renderer);
        finish();
    }

    @Override
    protected void onDestroy() {
        // IInkApplication has the ownership, do not close here
        engine = null;

        super.onDestroy();
    }

    void export(String exportFileName) {
        MimeType mimeType = MimeType.TEXT;

        switch (partType) {
            case "Math":
                mimeType = MimeType.LATEX;
                break;

            case "Diagram":
                mimeType = MimeType.SVG;
                break;

            case "Raw Content":
                mimeType = MimeType.JIIX;
                break;

            default:
                break;
        }
        File param1 = getExternalFilesDir(null);
        String param2 = File.separator + exportFileName + mimeType.getFileExtensions();
        // Exported file is stored in the Virtual SD Card : "Android/data/com.myscript.iink.batchmode/files"
        File file = new File(param1, param2);
        editor.waitForIdle();

        try {
            editor.export_(null, file.getAbsolutePath(), mimeType, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        editor.setPart(null);
        contentPart.close();
        contentPackage.close();


        try {
            engine.deletePackage(packageName);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    private PointerEvent[] loadPointerEvents(String pointerEventsPath) {
        PointerEvent[] pointerEvents = null;

        try {
            // Loading the content of the pointerEvents JSON file
            InputStream inputStream = getResources().getAssets().open(pointerEventsPath);

            // Mapping the content into a JsonResult class
            JsonResult jsonResult = new Gson().fromJson(new InputStreamReader(inputStream), JsonResult.class);

            int pointerEventsCount = 0;

            // Calculating the number of pointerEvents
            for (Stroke stroke : jsonResult.getStrokes()) {
                pointerEventsCount += stroke.getX().length;
            }

            // Allocating the size of the array of pointerEvents
            pointerEvents = new PointerEvent[pointerEventsCount];

            int i = 0;

            for (Stroke stroke : jsonResult.getStrokes()) {
                float[] strokeX = stroke.getX();
                float[] strokeY = stroke.getY();
                long[] strokeT = stroke.getT();
                float[] strokeP = stroke.getP();
                int length = stroke.getX().length;

                for (int j = 0; j < length; j++) {
                    PointerEvent pointerEvent = new PointerEvent();
                    pointerEvent.pointerType = stroke.getPointerType();
                    pointerEvent.pointerId = stroke.getPointerId();

                    if (j == 0) {
                        pointerEvent.eventType = PointerEventType.DOWN;
                    } else if (j == length - 1) {
                        pointerEvent.eventType = PointerEventType.UP;
                    } else {
                        pointerEvent.eventType = PointerEventType.MOVE;
                    }

                    // Transform the x and y coordinates of the stroke from mm to px
                    // This is needed to be adaptive for each device
                    pointerEvent.x = (strokeX[j] / 25.4f) * displayMetrics.xdpi;
                    pointerEvent.y = (strokeY[j] / 25.4f) * displayMetrics.ydpi;

                    pointerEvent.t = strokeT[j];
                    pointerEvent.f = strokeP[j];
                    pointerEvents[i++] = pointerEvent;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pointerEvents;
    }
}
