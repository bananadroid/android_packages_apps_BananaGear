/*
 * Copyright (C) 2021 TenX-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.banana.settings.preferences;

import android.content.Context;
import android.content.ContentResolver;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

import com.bananadroid.support.colorpicker.ColorPickerPreference;

public class RGBGradientPickerPreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String GRADIENT_COLOR = "gradient_color";
    static final int DEFAULT_GRADIENT_COLOR = 0xfff3d324;

    private ColorPickerPreference mGradientColor;

    public RGBGradientPickerPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return GRADIENT_COLOR;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mGradientColor = (ColorPickerPreference) screen.findPreference(GRADIENT_COLOR);
        mGradientColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.GRADIENT_COLOR, DEFAULT_GRADIENT_COLOR, UserHandle.USER_CURRENT);
        String hexColor = String.format("#%08x", (0xfff3d324 & intColor));
        if (hexColor.equals("#fff3d324")) {
            mGradientColor.setSummary(R.string.default_string);
        } else {
            mGradientColor.setSummary(hexColor);
        }
        mGradientColor.setNewPreviewColor(intColor);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mGradientColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#fff3d324")) {
                mGradientColor.setSummary(R.string.default_string);
            } else {
                mGradientColor.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.GRADIENT_COLOR, intHex, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }
}
