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

import static android.os.UserHandle.USER_SYSTEM;

import android.content.Context;
import android.content.om.IOverlayManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;

import androidx.fragment.app.Fragment;
import androidx.preference.*;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.banana.ThemesUtils;
import com.android.internal.util.banana.bananaUtils;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import com.banana.settings.preferences.QsTileStylePreferenceController;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class QSThemes extends DashboardFragment implements OnPreferenceChangeListener {

    public static final String TAG = "QSTintStyle";

    private static final String QS_TINT_FOOTER = "qs_tint_style_footer";
    private static final String BRIGHTNESS_SLIDER_STYLE = "brightness_slider_style";
    private static final String PREF_PANEL_BG = "panel_bg";

    private IOverlayManager mOverlayManager;
    private IOverlayManager mOverlayService;

    private ListPreference mBrightnessSliderStyle;
    private ListPreference mPanelBg;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOverlayService = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));

        findPreference(QS_TINT_FOOTER).setTitle(R.string.qs_tint_style_footer);

        mBrightnessSliderStyle = (ListPreference) findPreference(BRIGHTNESS_SLIDER_STYLE);
        int brightnessSliderValue = getOverlayPosition(ThemesUtils.BRIGHTNESS_SLIDER_THEMES);
        if (brightnessSliderValue != -1) {
            mBrightnessSliderStyle.setValue(String.valueOf(brightnessSliderValue + 2));
        } else {
            mBrightnessSliderStyle.setValue("1");
        }
        mBrightnessSliderStyle.setSummary(mBrightnessSliderStyle.getEntry());
        mBrightnessSliderStyle.setOnPreferenceChangeListener(this);

        mPanelBg = (ListPreference) findPreference(PREF_PANEL_BG);
        int mPanelValue = getOverlayPosition(ThemesUtils.PANEL_BG_STYLE);
        if (mPanelValue != -1) {
            mPanelBg.setValue(String.valueOf(mPanelValue + 2));
        } else {
            mPanelBg.setValue("1");
        }
        mPanelBg.setSummary(mPanelBg.getEntry());
        mPanelBg.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBrightnessSliderStyle) {
            String sliderStyle = (String) newValue;
            int sliderValue = Integer.parseInt(sliderStyle);
            mBrightnessSliderStyle.setValue(String.valueOf(sliderValue));
            String overlayName = getOverlayName(ThemesUtils.BRIGHTNESS_SLIDER_THEMES);
                if (overlayName != null) {
                    handleOverlays(overlayName, false, mOverlayManager);
                }
                if (sliderValue > 1) {
                    handleOverlays(ThemesUtils.BRIGHTNESS_SLIDER_THEMES[sliderValue - 2],
                            true, mOverlayManager);
            }
            mBrightnessSliderStyle.setSummary(mBrightnessSliderStyle.getEntry());
            return true;
        } else if (preference == mPanelBg) {
            String panelbg = (String) newValue;
            int panelBgValue = Integer.parseInt(panelbg);
            mPanelBg.setValue(String.valueOf(panelBgValue));
            String overlayName = getOverlayName(ThemesUtils.PANEL_BG_STYLE);
                if (overlayName != null) {
                    handleOverlays(overlayName, false, mOverlayManager);
                }
                if (panelBgValue > 1) {
                    bananaUtils.showSystemUiRestartDialog(getContext());
                    handleOverlays(ThemesUtils.PANEL_BG_STYLE[panelBgValue -2],
                            true, mOverlayManager);
            }
            mPanelBg.setSummary(mPanelBg.getEntry());
            return true;
        }
        return false;
    }

    private int getOverlayPosition(String[] overlays) {
        int position = -1;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (bananaUtils.isThemeEnabled(overlay)) {
                position = i;
            }
        }
        return position;
    }

    private String getOverlayName(String[] overlays) {
        String overlayName = null;
        for (int i = 0; i < overlays.length; i++) {
            String overlay = overlays[i];
            if (bananaUtils.isThemeEnabled(overlay)) {
                overlayName = overlay;
            }
        }
        return overlayName;
    }

    public void handleOverlays(String packagename, Boolean state, IOverlayManager mOverlayManager) {
        try {
            mOverlayService.setEnabled(packagename, state, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new QsTileStylePreferenceController(context));
        return controllers;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BANANADROID;
    }

    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.qs_themes;
    }

    /**
     * For Search.
     */

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.qs_themes);
}
