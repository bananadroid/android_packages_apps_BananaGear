/*
 * Copyright (C) 2021-2023 BananaDroid
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
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.android.internal.util.banana.BananaUtils;
import com.android.internal.util.banana.OmniJawsClient;

import com.banana.settings.fragments.UdfpsAnimation;
import com.banana.settings.fragments.UdfpsIconPicker;

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
    private static final String KEY_WEATHER = "lockscreen_weather_enabled";
    private static final String KEY_UDFPS_ICONS = "udfps_icon_picker";
    private static final String KEY_UDFPS_ANIMATIONS = "udfps_recognizing_animation_preview";

    private Preference mRippleEffect;
    private Preference mFingerprintVib;
    private Preference mFingerprintVibErr;
    private Preference mWeather;
    private Preference mUdfpsIcons;
    private Preference mUdfpsAnimations;

    private OmniJawsClient mWeatherClient;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.bg_lockscreen;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        PreferenceCategory gestCategory = (PreferenceCategory) findPreference(LOCKSCREEN_GESTURES_CATEGORY);

        FingerprintManager mFingerprintManager = (FingerprintManager)
                getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mRippleEffect = (Preference) findPreference(KEY_RIPPLE_EFFECT);
        mFingerprintVib = (Preference) findPreference(KEY_FP_SUCCESS_VIBRATE);
        mFingerprintVibErr = (Preference) findPreference(KEY_FP_ERROR_VIBRATE);
        mUdfpsIcons = (Preference) findPreference(KEY_UDFPS_ICONS);
        mUdfpsAnimations = (Preference) findPreference(KEY_UDFPS_ANIMATIONS);

        if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()) {
            gestCategory.removePreference(mRippleEffect);
            gestCategory.removePreference(mFingerprintVib);
            gestCategory.removePreference(mFingerprintVibErr);
            gestCategory.removePreference(mUdfpsIcons);
            gestCategory.removePreference(mUdfpsAnimations);
        } else {
            if (!BananaUtils.isPackageInstalled(getContext(), "com.banana.udfps.icons")) {
                gestCategory.removePreference(mUdfpsIcons);
            }
            if (!BananaUtils.isPackageInstalled(getContext(), "com.banana.udfps.animations")) {
                gestCategory.removePreference(mUdfpsAnimations);
            }
        }
        mWeather = (Preference) findPreference(KEY_WEATHER);
        mWeatherClient = new OmniJawsClient(getContext());
        updateWeatherSettings();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.ENABLE_RIPPLE_EFFECT, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.FP_ERROR_VIBRATE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.FP_SUCCESS_VIBRATE, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_ENABLED, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_LOCATION, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.LOCKSCREEN_WEATHER_TEXT, 1, UserHandle.USER_CURRENT);
        UdfpsIconPicker.reset(mContext);
        UdfpsAnimation.reset(mContext);
    }

    private void updateWeatherSettings() {
        if (mWeatherClient == null || mWeather == null) return;

        boolean weatherEnabled = mWeatherClient.isOmniJawsEnabled();
        mWeather.setEnabled(weatherEnabled);
        mWeather.setSummary(weatherEnabled ? R.string.lockscreen_weather_summary :
            R.string.lockscreen_weather_enabled_info);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWeatherSettings();
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
                        keys.add(KEY_UDFPS_ICONS);
                        keys.add(KEY_UDFPS_ANIMATIONS);
                    } else {
                        if (!BananaUtils.isPackageInstalled(context, "com.banana.udfps.icons")) {
                            keys.add(KEY_UDFPS_ICONS);
                        }
                        if (!BananaUtils.isPackageInstalled(context, "com.banana.udfps.animations")) {
                            keys.add(KEY_UDFPS_ANIMATIONS);
                        }
                    }
                    return keys;
                }
            };
}
