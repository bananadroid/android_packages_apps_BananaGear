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
import android.content.res.Resources;
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

import com.banana.settings.fragments.NetworkTrafficSettings;
import com.banana.settings.utils.TelephonyUtils;
import com.banana.support.preferences.SystemSettingListPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class StatusBar extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String KEY_STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String KEY_STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String KEY_STATUS_BAR_BATTERY_TEXT_CHARGING = "status_bar_battery_text_charging";
    private static final String KEY_USE_OLD_MOBILETYPE = "use_old_mobiletype";
    private static final String KEY_SHOW_ROAMING = "roaming_indicator_icon";
    private static final String KEY_SHOW_FOURG = "show_fourg_icon";
    private static final String KEY_SHOW_DATA_DISABLED = "data_disabled_icon";

    private static final int BATTERY_STYLE_PORTRAIT = 0;
    private static final int BATTERY_STYLE_TEXT = 4;
    private static final int BATTERY_STYLE_HIDDEN = 5;

    private SwitchPreference mBatteryTextCharging;
    private SwitchPreference mOldMobileType;
    private SwitchPreference mShowRoaming;
    private SwitchPreference mShowFourg;
    private SwitchPreference mDataDisabled;
    private SystemSettingListPreference mBatteryPercent;
    private SystemSettingListPreference mBatteryStyle;

    private ListPreference mQuickPulldown;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.bg_statusbar);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Context mContext = getActivity().getApplicationContext();

        final PreferenceScreen prefScreen = getPreferenceScreen();

        int batterystyle = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_PORTRAIT, UserHandle.USER_CURRENT);
        int batterypercent = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0, UserHandle.USER_CURRENT);

        mBatteryStyle = (SystemSettingListPreference) findPreference(KEY_STATUS_BAR_BATTERY_STYLE);
        mBatteryStyle.setOnPreferenceChangeListener(this);

        mBatteryPercent = (SystemSettingListPreference) findPreference(KEY_STATUS_BAR_SHOW_BATTERY_PERCENT);
        mBatteryPercent.setEnabled(
                batterystyle != BATTERY_STYLE_TEXT && batterystyle != BATTERY_STYLE_HIDDEN);
        mBatteryPercent.setOnPreferenceChangeListener(this);

        mBatteryTextCharging = (SwitchPreference) findPreference(KEY_STATUS_BAR_BATTERY_TEXT_CHARGING);
        mBatteryTextCharging.setEnabled(batterystyle == BATTERY_STYLE_HIDDEN ||
                (batterystyle != BATTERY_STYLE_TEXT && batterypercent != 2));

        int qpmode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 0, UserHandle.USER_CURRENT);
        mQuickPulldown = (ListPreference) findPreference("qs_quick_pulldown");
        mQuickPulldown.setValue(String.valueOf(qpmode));
        mQuickPulldown.setSummary(mQuickPulldown.getEntry());
        mQuickPulldown.setOnPreferenceChangeListener(this);

        mOldMobileType = (SwitchPreference) findPreference(KEY_USE_OLD_MOBILETYPE);
        mShowRoaming = (SwitchPreference) findPreference(KEY_SHOW_ROAMING);
        mShowFourg = (SwitchPreference) findPreference(KEY_SHOW_FOURG);
        mDataDisabled = (SwitchPreference) findPreference(KEY_SHOW_DATA_DISABLED);

        if (!TelephonyUtils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(mOldMobileType);
            prefScreen.removePreference(mShowRoaming);
            prefScreen.removePreference(mShowFourg);
            prefScreen.removePreference(mDataDisabled);
        } else {
            boolean mConfigUseOldMobileType = mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_useOldMobileIcons);
            boolean showing = Settings.System.getIntForUser(resolver,
                    Settings.System.USE_OLD_MOBILETYPE,
                    mConfigUseOldMobileType ? 1 : 0, UserHandle.USER_CURRENT) != 0;
            mOldMobileType.setChecked(showing);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryStyle) {
            int value = Integer.parseInt((String) newValue);
            int batterypercent = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0, UserHandle.USER_CURRENT);
            mBatteryPercent.setEnabled(
                    value != BATTERY_STYLE_TEXT && value != BATTERY_STYLE_HIDDEN);
            mBatteryTextCharging.setEnabled(value == BATTERY_STYLE_HIDDEN ||
                    (value != BATTERY_STYLE_TEXT && batterypercent != 2));
            return true;
        } else if (preference == mBatteryPercent) {
            int value = Integer.parseInt((String) newValue);
            int batterystyle = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY_STYLE, BATTERY_STYLE_PORTRAIT, UserHandle.USER_CURRENT);
            mBatteryTextCharging.setEnabled(batterystyle == BATTERY_STYLE_HIDDEN ||
                    (batterystyle != BATTERY_STYLE_TEXT && value != 2));
            return true;
        } else if (preference == mQuickPulldown) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, value,
                    UserHandle.USER_CURRENT);
            int index = mQuickPulldown.findIndexOfValue((String) newValue);
            mQuickPulldown.setSummary(
                    mQuickPulldown.getEntries()[index]);
            return true;
        }
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        NetworkTrafficSettings.reset(mContext);
        boolean mConfigUseOldMobileType = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_useOldMobileIcons);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUSBAR_COLORED_ICONS, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUSBAR_NOTIF_COUNT, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.USE_OLD_MOBILETYPE, mConfigUseOldMobileType ? 1 : 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.ROAMING_INDICATOR_ICON, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.SHOW_FOURG_ICON, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.DATA_DISABLED_ICON, 1, UserHandle.USER_CURRENT);
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

                    if (!TelephonyUtils.isVoiceCapable(context)) {
                        keys.add(KEY_USE_OLD_MOBILETYPE);
                        keys.add(KEY_SHOW_ROAMING);
                        keys.add(KEY_SHOW_FOURG);
                        keys.add(KEY_SHOW_DATA_DISABLED);
                    }

                    return keys;
                }
            };
}
