/*
 * Copyright (C) 2021 NezukoOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.banana.settings.preferences;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.app.WallpaperColors;
import android.widget.TextView;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.palette.graphics.Palette;
import androidx.core.graphics.ColorUtils;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

import com.android.settingslib.Utils;
import com.android.settingslib.widget.LayoutPreference;

public class WallAc extends LayoutPreference {

    private boolean mAllowDividerAbove;
    private boolean mAllowDividerBelow;

    private View mRootView;

    private static ImageButton Vibrant;
    private static ImageButton VibrantLight;
    private static ImageButton Dominant;
    private static ImageButton DominantBright;
    private static ImageButton Average;

    private static TextView VibrantT;
    private static TextView VibrantT1;
    private static TextView VibrantLightT;
    private static TextView VibrantLightT1;
    private static TextView DominantT;
    private static TextView DominantBrightT;
    private static TextView AverageT;

    private WallpaperManager mWallManager;
    private int fallbackColor = 0xFFF3D324;

    private static final String TAG = "WallAc";

    public WallAc(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0 /* defStyleAttr */);
    }

    public WallAc(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference);
        mAllowDividerAbove = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerAbove,
                R.styleable.Preference_allowDividerAbove, false);
        mAllowDividerBelow = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerBelow,
                R.styleable.Preference_allowDividerBelow, false);
        a.recycle();

        a = context.obtainStyledAttributes(
                attrs, R.styleable.Preference, defStyleAttr, 0);
        int layoutResource = a.getResourceId(R.styleable.Preference_android_layout, 0);
        if (layoutResource == 0) {
            throw new IllegalArgumentException("LayoutPreference requires a layout to be defined");
        }
        a.recycle();

        // Need to create view now so that findViewById can be called immediately.
        final View view = LayoutInflater.from(getContext())
                .inflate(layoutResource, null, false);
        setView(view, context);
    }

    private void setView(View view, Context context) {
        setLayoutResource(R.layout.layout_preference_frame);
        mRootView = view;
        setShouldDisableView(false);


        Vibrant = findViewById(R.id.vib);
        VibrantLight = findViewById(R.id.viblight);
        Dominant = findViewById(R.id.dom);
        DominantBright = findViewById(R.id.domlight);
        Average = findViewById(R.id.avg);

        VibrantT = findViewById(R.id.vibt);
        VibrantLightT = findViewById(R.id.viblt);
        DominantT = findViewById(R.id.domt);
        DominantBrightT = findViewById(R.id.dombt);
        AverageT = findViewById(R.id.avgt);
        VibrantT1 = findViewById(R.id.vibt1);
        VibrantLightT1 = findViewById(R.id.viblt1);

        int defaultColor = 0x000000;
        int defaultColor2 = 0x00000000;
        int white = 0xFFFFFF;
        int white2 = 0xFFFFFFFF;
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bitmap = ((BitmapDrawable)wallpaperDrawable).getBitmap();
        Palette p = Palette.from(bitmap).generate();
        int keks = fallbackColor;

        if (p.getVibrantColor(defaultColor) == 0 || p.getVibrantColor(defaultColor) == defaultColor2 || p.getVibrantColor(defaultColor) == white || p.getVibrantColor(defaultColor) == white2 || p.getVibrantColor(defaultColor) == defaultColor){
            Vibrant.setColorFilter(p.getDominantColor(defaultColor));
            int vibc = p.getDominantColor(defaultColor);
            String colorHexV = String.format("%08x", (0xFFFFFFFF & vibc));
            VibrantT.setText(colorHexV);
            VibrantT1.setText("Vibrant (Using Dominant as fallback)");
        } else{
            Vibrant.setColorFilter(p.getVibrantColor(defaultColor));
            int vibc = p.getVibrantColor(defaultColor);
            String colorHexV = String.format("%08x", (0xFFFFFFFF & vibc));
            VibrantT.setText(colorHexV);
        }
        if (p.getLightVibrantColor(defaultColor) == 0 || p.getLightVibrantColor(defaultColor) == defaultColor || p.getLightVibrantColor(defaultColor) == defaultColor2 || p.getLightVibrantColor(defaultColor) == white || p.getLightVibrantColor(defaultColor) == white2){
            VibrantLight.setColorFilter(p.getDominantColor(defaultColor));
            int viblc = p.getDominantColor(defaultColor);
            String colorHexVL = String.format("%08x", (0xFFFFFFFF & viblc));
            VibrantLightT.setText(colorHexVL);
            VibrantLightT1.setText("Vibrant Light (Using Dominant as fallback)");
        } else{
            VibrantLight.setColorFilter(p.getLightVibrantColor(defaultColor));
            int viblc = p.getLightVibrantColor(defaultColor);
            String colorHexVL = String.format("%08x", (0xFFFFFFFF & viblc));
            VibrantLightT.setText(colorHexVL);
        }

        Dominant.setColorFilter(p.getDominantColor(defaultColor));
        int dc = p.getDominantColor(defaultColor);
        String colorHexD = String.format("%08x", (0xFFFFFFFF & dc));
        DominantT.setText(colorHexD);

        keks = p.getDominantColor(defaultColor);
        keks = ColorUtils.blendARGB(keks, Color.WHITE, 0.5f);
        DominantBright.setColorFilter(keks);

        String colorHexDB = String.format("%08x", (0xFFFFFFFF & keks));
        DominantBrightT.setText(colorHexDB);

        Average.setColorFilter(getAvgColor(bitmap));
        String colorHexA = String.format("%08x", (0xFFFFFFFF & getAvgColor(bitmap)));
        AverageT.setText(colorHexA);

    }

    private static int getAvgColor(Bitmap bitmap) {
        if (bitmap == null) {
            return Color.TRANSPARENT;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;
        int pixels[] = new int[size];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int color;
        int r = 0;
        int g = 0;
        int b = 0;
        int a;
        int count = 0;
        for (int i = 0; i < pixels.length; i++) {
            color = pixels[i];
            a = Color.alpha(color);
            if (a > 0) {
                r += Color.red(color);
                g += Color.green(color);
                b += Color.blue(color);
                count++;
            }
        }
        r /= count;
        g /= count;
        b /= count;
        r = (r << 16) & 0x00FF0000;
        g = (g << 8) & 0x0000FF00;
        b = b & 0x000000FF;
        color = 0xFF000000 | r | g | b;
        return color;
    }

}
