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
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.banana.BananaUtils;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.banana.settings.fragments.lockscreen.UdfpsSettings;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class Lockscreen extends DashboardFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    public static final String TAG = "Lockscreen";

    private static final String LOCKSCREEN_GESTURES_CATEGORY = "lockscreen_gestures_category";
    private static final String KEY_RIPPLE_EFFECT = "enable_ripple_effect";
    private static final String KEY_FP_SUCCESS_VIBRATE = "fp_success_vibrate";
    private static final String KEY_FP_ERROR_VIBRATE = "fp_error_vibrate";
    private static final String KEY_UDFPS_SETTINGS = "udfps_settings";
    private static final String KG_CUSTOM_CLOCK_COLOR_ENABLED = "kg_custom_clock_color_enabled";
    private static final String SHORTCUT_START_KEY = "lockscreen_shortcut_start";
    private static final String SHORTCUT_END_KEY = "lockscreen_shortcut_end";

    private static final String[] DEFAULT_START_SHORTCUT = new String[] { "home", "flashlight" };
    private static final String[] DEFAULT_END_SHORTCUT = new String[] { "wallet", "qr", "camera" };

    private Preference mRippleEffect;
    private Preference mFingerprintVib;
    private Preference mFingerprintVibErr;
    private Preference mUdfpsSettings;
    private SwitchPreference mKGCustomClockColor;
    private ListPreference mStartShortcut;
    private ListPreference mEndShortcut;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.bg_lockscreen;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        ContentResolver resolver = getActivity().getContentResolver();

        PreferenceCategory gestCategory = (PreferenceCategory) findPreference(LOCKSCREEN_GESTURES_CATEGORY);

        FingerprintManager mFingerprintManager = (FingerprintManager)
                getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mRippleEffect = (Preference) findPreference(KEY_RIPPLE_EFFECT);
        mFingerprintVib = (Preference) findPreference(KEY_FP_SUCCESS_VIBRATE);
        mFingerprintVibErr = (Preference) findPreference(KEY_FP_ERROR_VIBRATE);
        mUdfpsSettings = (Preference) findPreference(KEY_UDFPS_SETTINGS);

        if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()) {
            gestCategory.removePreference(mRippleEffect);
            gestCategory.removePreference(mFingerprintVib);
            gestCategory.removePreference(mFingerprintVibErr);
            gestCategory.removePreference(mUdfpsSettings);
        } else {
            if (!BananaUtils.isPackageInstalled(getContext(), "com.banana.udfps.icons")) {
                gestCategory.removePreference(mUdfpsSettings);
            } else {
                gestCategory.removePreference(mFingerprintVib);
                gestCategory.removePreference(mFingerprintVibErr);
            }
        }

mKGCustomClockColor = (SwitchPreference) findPreference(KG_CUSTOM_CLOCK_COLOR_ENABLED);
        boolean mKGCustomClockColorEnabled = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.KG_CUSTOM_CLOCK_COLOR_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        mKGCustomClockColor.setChecked(mKGCustomClockColorEnabled);
        mKGCustomClockColor.setOnPreferenceChangeListener(this);

        mStartShortcut = (ListPreference) findPreference(SHORTCUT_START_KEY);
        mEndShortcut = (ListPreference) findPreference(SHORTCUT_END_KEY);
        updateShortcutSelection();
        mStartShortcut.setOnPreferenceChangeListener(this);
        mEndShortcut.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mKGCustomClockColor) {
            boolean val = (Boolean) newValue;
            Settings.Secure.putIntForUser(resolver,
                Settings.Secure.KG_CUSTOM_CLOCK_COLOR_ENABLED, val ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mStartShortcut) {
            setShortcutSelection((String) newValue, true);
            return true;
        } else if (preference == mEndShortcut) {
            setShortcutSelection((String) newValue, false);
            return true;
        }
        return false;
    }

    private String getSettingsShortcutValue() {
        String value = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.KEYGUARD_QUICK_TOGGLES);
        if (value == null || value.isEmpty()) {
            for (String str : DEFAULT_START_SHORTCUT) value += str;
            value += ";";
            for (String str : DEFAULT_END_SHORTCUT) value += str;
        }
        return value;
    }

    private void updateShortcutSelection() {
        final String value = getSettingsShortcutValue();
        final String[] split = value.split(";");
        mStartShortcut.setValue(split[0].split(",")[0]);
        mStartShortcut.setSummary(mStartShortcut.getEntry());
        mEndShortcut.setValue(split[1].split(",")[0]);
        mEndShortcut.setSummary(mEndShortcut.getEntry());
    }

    private void setShortcutSelection(String value, boolean start) {
        final String oldValue = getSettingsShortcutValue();
        final int splitIndex = start ? 0 : 1;
        String[] split = oldValue.split(";");
        if (value.equals("none")) {
            split[splitIndex] = "none";
        } else {
            split[splitIndex] = value;
            final String[] def = start ? DEFAULT_START_SHORTCUT : DEFAULT_END_SHORTCUT;
            for (String str : def) {
                if (str.equals(value)) continue;
                split[splitIndex] += "," + str;
            }
        }
        Settings.System.putString(getActivity().getContentResolver(),
                Settings.System.KEYGUARD_QUICK_TOGGLES, split[0] + ";" + split[1]);

        if (start) {
            mStartShortcut.setValue(value);
            mStartShortcut.setSummary(mStartShortcut.getEntry());
        } else {
            mEndShortcut.setValue(value);
            mEndShortcut.setSummary(mEndShortcut.getEntry());
        }
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.ENABLE_RIPPLE_EFFECT, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.FP_ERROR_VIBRATE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.FP_SUCCESS_VIBRATE, 1, UserHandle.USER_CURRENT);
        Settings.System.putString(resolver,
                Settings.System.KEYGUARD_QUICK_TOGGLES, "home,flashlight;wallet,qr,camera");
        UdfpsSettings.reset(mContext);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BANANADROID;
    }

    @Override
    protected String getLogTag() {
        return TAG;
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

                    FingerprintManager mFingerprintManager = (FingerprintManager)
                            context.getSystemService(Context.FINGERPRINT_SERVICE);
                    if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()) {
                        keys.add(KEY_RIPPLE_EFFECT);
                        keys.add(KEY_FP_SUCCESS_VIBRATE);
                        keys.add(KEY_FP_ERROR_VIBRATE);
                        keys.add(KEY_UDFPS_SETTINGS);
                    } else {
                        if (!BananaUtils.isPackageInstalled(context, "com.banana.udfps.icons")) {
                            keys.add(KEY_UDFPS_SETTINGS);
                        } else {
                            keys.add(KEY_FP_SUCCESS_VIBRATE);
                            keys.add(KEY_FP_ERROR_VIBRATE);
                        }
                    }
                    return keys;
                }
            };
}
