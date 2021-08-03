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

import android.content.ContentResolver;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.bananadroid.support.preferences.SystemSettingMasterSwitchPreference;
import com.bananadroid.support.preferences.SystemSettingListPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class StatusBar extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String NETWORK_TRAFFIC = "network_traffic_state";
    private static final String STATUSBAR_CLOCK = "statusbar_clock";
    private static final String STATUS_BAR_LOGO = "status_bar_logo";
    private static final String VO_ICON_PICKER = "vo_icon_picker";

    private SystemSettingMasterSwitchPreference mNetworkTraffic;
    private SystemSettingMasterSwitchPreference mStatusBarClockShow;
    private SystemSettingMasterSwitchPreference mStatusBarLogo;
    private SystemSettingListPreference mVo;

    private Handler mHandler;
    private IOverlayManager mOverlayManager;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.bg_statusbar);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        Context mContext = getContext();
        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
        mVo = (SystemSettingListPreference) findPreference(VO_ICON_PICKER);
        mCustomSettingsObserver.observe();

        updateMasterPrefs();
    }

    private void updateMasterPrefs() {
        mNetworkTraffic = (SystemSettingMasterSwitchPreference) findPreference(NETWORK_TRAFFIC);
        mNetworkTraffic.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NETWORK_TRAFFIC_STATE, 0) == 1));
        mNetworkTraffic.setOnPreferenceChangeListener(this);

        mStatusBarClockShow = (SystemSettingMasterSwitchPreference) findPreference(STATUSBAR_CLOCK);
        mStatusBarClockShow.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUSBAR_CLOCK, 1) == 1));
        mStatusBarClockShow.setOnPreferenceChangeListener(this);

        mStatusBarLogo = (SystemSettingMasterSwitchPreference) findPreference(STATUS_BAR_LOGO);
        mStatusBarLogo.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_LOGO, 0) == 1));
        mStatusBarLogo.setOnPreferenceChangeListener(this);
    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {
        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.VO_ICON_PICKER ),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.VO_ICON_PICKER ))) {
                updateVo();
            }
        }
    }

    private void updateVo() {
        ContentResolver resolver = getActivity().getContentResolver();

        boolean VoDef = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.VO_ICON_PICKER , 0, UserHandle.USER_CURRENT) == 0;
        boolean VoVivo = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.VO_ICON_PICKER , 0, UserHandle.USER_CURRENT) == 1;

        if (VoDef) {
            setDefaultVo(mOverlayManager);
        } else if (VoVivo) {
            enableSettingsVo(mOverlayManager, "com.android.theme.systemui_voiconpack.vivo");
        }
    }

    public static void setDefaultVo(IOverlayManager overlayManager) {
        for (int i = 0; i < VO.length; i++) {
            String vo = VO[i];
            try {
                overlayManager.setEnabled(vo, false, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void enableSettingsVo(IOverlayManager overlayManager, String overlayName) {
        try {
            for (int i = 0; i < VO.length; i++) {
                String vo = VO[i];
                try {
                    overlayManager.setEnabled(vo, false, USER_SYSTEM);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            overlayManager.setEnabled(overlayName, true, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void handleOverlays(String packagename, Boolean state, IOverlayManager mOverlayManager) {
        try {
            mOverlayManager.setEnabled(packagename,
                    state, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static final String[] VO = {
        "com.android.theme.systemui_voiconpack.vivo"
    };

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNetworkTraffic) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE, value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarClockShow) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK, value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarLogo) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_LOGO, value ? 1 : 0);
            return true;
        } else if (preference == mVo) {
            mCustomSettingsObserver.observe();
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMasterPrefs();
    }

    @Override
    public void onPause() {
        super.onPause();
        updateMasterPrefs();
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
                    sir.xmlResId = R.xml.bg_statusbar;
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
