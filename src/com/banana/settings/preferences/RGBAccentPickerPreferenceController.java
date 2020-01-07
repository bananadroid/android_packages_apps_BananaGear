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

public class RGBAccentPickerPreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String ACCENT_COLOR = "accent_color";
    static final int DEFAULT_ACCENT_COLOR = 0xfff3d324;

    private ColorPickerPreference mAccentColor;

    public RGBAccentPickerPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return ACCENT_COLOR;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mAccentColor = (ColorPickerPreference) screen.findPreference(ACCENT_COLOR);
        mAccentColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.ACCENT_COLOR, DEFAULT_ACCENT_COLOR, UserHandle.USER_CURRENT);
        String hexColor = String.format("#%08x", (0xfff3d324 & intColor));
        if (hexColor.equals("#fff3d324")) {
            mAccentColor.setSummary(R.string.default_string);
        } else {
            mAccentColor.setSummary(hexColor);
        }
        mAccentColor.setNewPreviewColor(intColor);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAccentColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#fff3d324")) {
                mAccentColor.setSummary(R.string.default_string);
            } else {
                mAccentColor.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.ACCENT_COLOR, intHex, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }
}
