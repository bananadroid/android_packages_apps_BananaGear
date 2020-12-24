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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.IOverlayManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;

import androidx.fragment.app.Fragment;
import androidx.preference.*;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.banana.ThemesUtils;
import com.android.internal.util.banana.bananaUtils;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.development.OverlayCategoryPreferenceController;
import com.android.settings.display.FontPickerPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;

import com.banana.settings.preferences.AccentColorPreferenceController;
import com.banana.settings.preferences.SwitchStylePreferenceController;
import com.banana.settings.preferences.QsTileStylePreferenceController;
import com.banana.settings.preferences.UiBlurPreferenceController;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class Themes extends DashboardFragment implements OnPreferenceChangeListener {

    public static final String TAG = "Themes";

    private static final String PREF_ROUNDED_CORNER = "rounded_ui";
    private static final String PREF_SB_HEIGHT = "statusbar_height";

    private IOverlayManager mOverlayManager;
    private IOverlayManager mOverlayService;
    private ListPreference mRoundedUi;
    private ListPreference mSbHeight;

    private IntentFilter mIntentFilter;
    private static FontPickerPreferenceController mFontPickerPreference;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.android.server.ACTION_FONT_CHANGED")) {
                mFontPickerPreference.stopProgress();
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        mRoundedUi = (ListPreference) findPreference(PREF_ROUNDED_CORNER);
        int roundedValue = getOverlayPosition(ThemesUtils.UI_RADIUS);
        if (roundedValue != -1) {
            mRoundedUi.setValue(String.valueOf(roundedValue + 2));
        } else {
            mRoundedUi.setValue("1");
        }
        mRoundedUi.setSummary(mRoundedUi.getEntry());
        mRoundedUi.setOnPreferenceChangeListener(this);

        mSbHeight = (ListPreference) findPreference(PREF_SB_HEIGHT);
        int sbHeightValue = getOverlayPosition(ThemesUtils.STATUSBAR_HEIGHT);
        if (sbHeightValue != -1) {
            mSbHeight.setValue(String.valueOf(sbHeightValue + 2));
        } else {
            mSbHeight.setValue("1");
        }
        mSbHeight.setSummary(mSbHeight.getEntry());
        mSbHeight.setOnPreferenceChangeListener(this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.android.server.ACTION_FONT_CHANGED");
    }

    public void handleOverlays(String packagename, Boolean state, IOverlayManager mOverlayManager) {
        try {
            mOverlayService.setEnabled(packagename, state, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mRoundedUi) {
            String rounded = (String) newValue;
            int roundedValue = Integer.parseInt(rounded);
            mRoundedUi.setValue(String.valueOf(roundedValue));
            String overlayName = getOverlayName(ThemesUtils.UI_RADIUS);
            if (overlayName != null) {
                handleOverlays(overlayName, false, mOverlayManager);
            }
            if (roundedValue > 1) {
                handleOverlays(ThemesUtils.UI_RADIUS[roundedValue -2],
                        true, mOverlayManager);
            }
            mRoundedUi.setSummary(mRoundedUi.getEntry());
            return true;
        } else if (preference == mSbHeight) {
            String sbheight = (String) newValue;
            int sbheightValue = Integer.parseInt(sbheight);
            mSbHeight.setValue(String.valueOf(sbheightValue));
            String overlayName = getOverlayName(ThemesUtils.STATUSBAR_HEIGHT);
            if (overlayName != null) {
                handleOverlays(overlayName, false, mOverlayManager);
            }
            if (sbheightValue > 1) {
                handleOverlays(ThemesUtils.STATUSBAR_HEIGHT[sbheightValue -2],
                        true, mOverlayManager);
            }
            mSbHeight.setSummary(mSbHeight.getEntry());
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

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BANANADROID;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.bg_themes;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new AccentColorPreferenceController(context));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.adaptive_icon_shape"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.signal_icon"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.wifi_icon"));
        controllers.add(new SwitchStylePreferenceController(context));
        controllers.add(mFontPickerPreference = new FontPickerPreferenceController(context, lifecycle));
        controllers.add(new QsTileStylePreferenceController(context));
        controllers.add(new UiBlurPreferenceController(context));
        return controllers;
    }

    @Override
    public void onResume() {
        super.onResume();
        final Context context = getActivity();
        context.registerReceiver(mIntentReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        final Context context = getActivity();
        context.unregisterReceiver(mIntentReceiver);
        mFontPickerPreference.stopProgress();
    }

    /**
     * For Search.
     */

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.bg_themes);
}
