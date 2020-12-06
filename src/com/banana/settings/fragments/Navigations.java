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
import android.provider.SearchIndexableResource;
import android.content.ContentResolver;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.banana.bananaUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class Navigations extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String NAVBAR_VISIBILITY = "navbar_visibility";
    private static final String LAYOUT_SETTINGS = "navbar_layout_views";
    private static final String NAVIGATION_BAR_INVERSE = "navbar_inverse_layout";
    private static final String PIXEL_NAV_ANIMATION = "pixel_nav_animation";

    private Preference mLayoutSettings;
    private SwitchPreference mSwapNavButtons;
    private SwitchPreference mNavbarVisibility;
    private SwitchPreference mPixelNavAnimation;

    private boolean mIsNavSwitchingMode = false;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.bg_navigation);
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mLayoutSettings = (Preference) findPreference(LAYOUT_SETTINGS);
        mSwapNavButtons = (SwitchPreference) findPreference(NAVIGATION_BAR_INVERSE);
        mNavbarVisibility = (SwitchPreference) findPreference(NAVBAR_VISIBILITY);

        if (!bananaUtils.isThemeEnabled("com.android.internal.systemui.navbar.threebutton")) {
            prefScreen.removePreference(mLayoutSettings);

        mPixelNavAnimation = (SwitchPreference) findPreference(PIXEL_NAV_ANIMATION);
        }

        boolean defaultToNavigationBar = bananaUtils.deviceSupportNavigationBar(getActivity());
        boolean showing = Settings.System.getInt(getContentResolver(),
                Settings.System.FORCE_SHOW_NAVBAR,
                defaultToNavigationBar ? 1 : 0) != 0;
        updateBarVisibleAndUpdatePrefs(showing);

        mNavbarVisibility.setOnPreferenceChangeListener(this);

        mHandler = new Handler();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mNavbarVisibility)) {
            if (mIsNavSwitchingMode) {
                return false;
            }
            mIsNavSwitchingMode = true;
            boolean showing = ((Boolean)newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.FORCE_SHOW_NAVBAR,
                    showing ? 1 : 0);
            updateBarVisibleAndUpdatePrefs(showing);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsNavSwitchingMode = false;
                }
            }, 1500);
            return true;
        }
        return false;
    }

    private void updateBarVisibleAndUpdatePrefs(boolean showing) {
        mNavbarVisibility.setChecked(showing);
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
                    sir.xmlResId = R.xml.bg_navigation;
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
