/*
 * Copyright (C) 2019 Havoc-OS
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

package com.banana.settings.fragments;

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.bananadroid.support.preferences.SystemSettingListPreference;
import com.bananadroid.support.colorpicker.ColorPickerPreference;

public class StatusbarLogo extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String LOGO_COLOR = "status_bar_logo_color";
    private static final String LOGO_COLOR_PICKER = "status_bar_logo_color_picker";

    private SystemSettingListPreference mLogoColor;
    private ColorPickerPreference mLogoColorPicker;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        addPreferencesFromResource(R.xml.statusbar_logo);

        mLogoColor = (SystemSettingListPreference) findPreference(LOGO_COLOR);
        int logoColor = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_LOGO_COLOR, 0, UserHandle.USER_CURRENT);
        mLogoColor.setValue(String.valueOf(logoColor));
        mLogoColor.setSummary(mLogoColor.getEntry());
        mLogoColor.setOnPreferenceChangeListener(this);

        mLogoColorPicker = (ColorPickerPreference) findPreference(LOGO_COLOR_PICKER);
        int logoColorPicker = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_LOGO_COLOR_PICKER, 0xFFFFFFFF);
        mLogoColorPicker.setNewPreviewColor(logoColorPicker);
        String logoColorPickerHex = String.format("#%08x", (0xFFFFFFFF & logoColorPicker));
        if (logoColorPickerHex.equals("#ffffffff")) {
            mLogoColorPicker.setSummary(R.string.default_string);
        } else {
            mLogoColorPicker.setSummary(logoColorPickerHex);
        }
        mLogoColorPicker.setOnPreferenceChangeListener(this);

        updateColorPrefs(logoColor);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLogoColor) {
            int logoColor = Integer.valueOf((String) newValue);
            int index = mLogoColor.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_LOGO_COLOR, logoColor, UserHandle.USER_CURRENT);
            mLogoColor.setSummary(mLogoColor.getEntries()[index]);
            updateColorPrefs(logoColor);
            return true;
        } else if (preference == mLogoColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ffffffff")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_LOGO_COLOR_PICKER, intHex);
            return true;
        }
        return false;
    }

    private void updateColorPrefs(int logoColor) {
        if (mLogoColor != null) {
            if (logoColor == 2) {
                getPreferenceScreen().addPreference(mLogoColorPicker);
            } else {
                getPreferenceScreen().removePreference(mLogoColorPicker);
            }
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BANANADROID;
    }
}
