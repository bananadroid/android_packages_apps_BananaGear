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
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.banana.BananaUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private Preference mQSLayoutColumns;
    private Preference mQSLayoutColumnsLandscape;
    private Preference mQSTileVerticalLayout;
    private Preference mQSTileLabelHide;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.bg_quicksettings);

        mQSLayoutColumns = (Preference) findPreference("qs_layout_columns");
        mQSLayoutColumns.setOnPreferenceChangeListener(this);
        mQSLayoutColumnsLandscape = (Preference) findPreference("qs_layout_columns_landscape");
        mQSLayoutColumnsLandscape.setOnPreferenceChangeListener(this);
        mQSTileVerticalLayout = (Preference) findPreference("qs_tile_vertical_layout");
        mQSTileVerticalLayout.setOnPreferenceChangeListener(this);
        mQSTileLabelHide = (Preference) findPreference("qs_tile_label_hide");
        mQSTileLabelHide.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQSLayoutColumns || preference == mQSLayoutColumnsLandscape
                || preference == mQSTileVerticalLayout || preference == mQSTileLabelHide) {
            BananaUtils.showSystemUiRestartDialog(getContext());
            return true;
        }
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TRANSPARENCY, 100, UserHandle.USER_CURRENT);
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
