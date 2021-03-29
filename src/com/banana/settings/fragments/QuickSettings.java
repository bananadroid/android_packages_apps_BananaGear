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

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.banana.bananaUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.banana.settings.helpers.QsTileConfigDialog;
import com.bananadroid.support.preferences.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String OOS_CLEAR_ALL = "oos_dismiss_all_button";

    private SystemSettingSwitchPreference mClearAll;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.bg_quicksettings);

        Preference qsTileConfig = findPreference("custom_qs_tile_config_dialog");
        qsTileConfig.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

                QsTileConfigDialog newFragment = QsTileConfigDialog .newInstance();
                newFragment.show(ft, "QsTileConfigDialog");
                return true;
            }
        });

        mClearAll = (SystemSettingSwitchPreference) findPreference(OOS_CLEAR_ALL);
        mClearAll.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.OOS_DISMISS_ALL_BUTTON, 0) == 1));
        mClearAll.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mClearAll) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.OOS_DISMISS_ALL_BUTTON, value ? 1 : 0);
            bananaUtils.showSystemUiRestartDialog(getContext());
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
                    sir.xmlResId = R.xml.bg_quicksettings;
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
