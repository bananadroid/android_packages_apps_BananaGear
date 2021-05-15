/*
 * Copyright (C) 2021 BananaDroid
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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.fuelgauge.PowerUsageSummary;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.banana.settings.preferences.Utils;
import com.bananadroid.support.preferences.SystemSettingListPreference;
import com.bananadroid.support.preferences.SystemSettingSeekBarPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class Lockscreen extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String KEY_LOCKSCREEN_BLUR = "lockscreen_blur";

    private SystemSettingListPreference mBatteryTempUnit;
    private SystemSettingSeekBarPreference mLockscreenBlur;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.bg_lockscreen);
        ContentResolver resolver = getActivity().getContentResolver();

        int unitMode = Settings.System.getIntForUser(resolver,
                Settings.System.LOCKSCREEN_BATTERY_INFO_TEMP_UNIT, 0, UserHandle.USER_CURRENT);
        mBatteryTempUnit = (SystemSettingListPreference) findPreference(
                "lockscreen_charge_temp_unit");
        mBatteryTempUnit.setValue(String.valueOf(unitMode));
        mBatteryTempUnit.setSummary(mBatteryTempUnit.getEntry());
        mBatteryTempUnit.setOnPreferenceChangeListener(this);

        mLockscreenBlur = (SystemSettingSeekBarPreference) findPreference(KEY_LOCKSCREEN_BLUR);
        if (!Utils.isBlurSupported()) {
            mLockscreenBlur.setVisible(false);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryTempUnit) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.LOCKSCREEN_BATTERY_INFO_TEMP_UNIT, value,
                    UserHandle.USER_CURRENT);
            int index = mBatteryTempUnit.findIndexOfValue((String) newValue);
            mBatteryTempUnit.setSummary(
            mBatteryTempUnit.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BANANADROID;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.bg_lockscreen;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
