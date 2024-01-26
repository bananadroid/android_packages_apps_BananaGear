/*
 * Copyright (C) 2023 BananaDroid
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

package com.banana.settings.fragments.qs;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.banana.ThemeUtils;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.banana.support.preferences.CustomSeekBarPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class QSTilesSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    public static final String TAG = "QSTilesSettings";

    private static final String KEY_PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
    private static final String KEY_PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
    private static final String KEY_PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";
    private static final String KEY_QS_UI_STYLE  = "qs_tile_ui_style";
    private static final String KEY_QS_PANEL_STYLE  = "qs_panel_style";

    private ListPreference mTileAnimationStyle;
    private CustomSeekBarPreference mTileAnimationDuration;
    private ListPreference mTileAnimationInterpolator;
    private ListPreference mQsUI;
    private ListPreference mQsPanelStyle;

    private static ThemeUtils mThemeUtils;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.qs_tiles;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mThemeUtils = new ThemeUtils(getActivity());

        final Context mContext = getActivity().getApplicationContext();
        final ContentResolver resolver = mContext.getContentResolver();

        mTileAnimationStyle = (ListPreference) findPreference(KEY_PREF_TILE_ANIM_STYLE);
        mTileAnimationDuration = (CustomSeekBarPreference) findPreference(KEY_PREF_TILE_ANIM_DURATION);
        mTileAnimationInterpolator = (ListPreference) findPreference(KEY_PREF_TILE_ANIM_INTERPOLATOR);

        mTileAnimationStyle.setOnPreferenceChangeListener(this);

        int tileAnimationStyle = Settings.System.getIntForUser(resolver,
                Settings.System.QS_TILE_ANIMATION_STYLE, 0, UserHandle.USER_CURRENT);
        updateAnimTileStyle(tileAnimationStyle);

        mQsUI = (ListPreference) findPreference(KEY_QS_UI_STYLE);
        mQsUI.setOnPreferenceChangeListener(this);

        mQsPanelStyle = (ListPreference) findPreference(KEY_QS_PANEL_STYLE);
        mQsPanelStyle.setOnPreferenceChangeListener(this);

        checkQSOverlays(mContext);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mTileAnimationStyle) {
            int value = Integer.parseInt((String) newValue);
            updateAnimTileStyle(value);
            return true;
        } else if (preference == mQsUI) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.QS_TILE_UI_STYLE, value, UserHandle.USER_CURRENT);
            updateQsStyle(getActivity());
            checkQSOverlays(getActivity());
            return true;
        } else if (preference == mQsPanelStyle) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.QS_PANEL_STYLE, value, UserHandle.USER_CURRENT);
            updateQsPanelStyle(getActivity());
            checkQSOverlays(getActivity());
            return true;
        }
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_ANIMATION_STYLE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_ANIMATION_DURATION, 1, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_ANIMATION_INTERPOLATOR, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_PANEL_STYLE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_LAYOUT_COLUMNS_LANDSCAPE, 2, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QQS_LAYOUT_ROWS, 2, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QQS_LAYOUT_ROWS_LANDSCAPE, 2, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_LAYOUT_COLUMNS, 2, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_VERTICAL_LAYOUT, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_LABEL_HIDE, 0, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_LABEL_SIZE, 14, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_TILE_UI_STYLE, 0, UserHandle.USER_CURRENT);
        updateQsStyle(mContext);
        updateQsPanelStyle(mContext);
    }

    private void updateAnimTileStyle(int tileAnimationStyle) {
        mTileAnimationDuration.setEnabled(tileAnimationStyle != 0);
        mTileAnimationInterpolator.setEnabled(tileAnimationStyle != 0);
    }

    private static void updateQsStyle(Context context) {
        ContentResolver resolver = context.getContentResolver();

        boolean isA11Style = Settings.System.getIntForUser(resolver,
                Settings.System.QS_TILE_UI_STYLE , 0, UserHandle.USER_CURRENT) != 0;

	    String qsUIStyleCategory = "android.theme.customization.qs_ui";
        String overlayThemeTarget  = "com.android.systemui";
        String overlayThemePackage  = "com.android.system.qs.ui.A11";

        if (mThemeUtils == null) {
            mThemeUtils = new ThemeUtils(context);
        }

	    // reset all overlays before applying
        mThemeUtils.setOverlayEnabled(qsUIStyleCategory, overlayThemeTarget, overlayThemeTarget);

	    if (isA11Style) {
            mThemeUtils.setOverlayEnabled(qsUIStyleCategory, overlayThemePackage, overlayThemeTarget);
	    }
    }

    private static void updateQsPanelStyle(Context context) {
        ContentResolver resolver = context.getContentResolver();

        int qsPanelStyle = Settings.System.getIntForUser(resolver,
                Settings.System.QS_PANEL_STYLE, 0, UserHandle.USER_CURRENT);

        String qsPanelStyleCategory = "android.theme.customization.qs_panel";
        String overlayThemeTarget  = "com.android.systemui";
        String overlayThemePackage  = "com.android.systemui";

        switch (qsPanelStyle) {
            case 1:
              overlayThemePackage = "com.android.system.qs.outline";
              break;
            case 2:
            case 3:
              overlayThemePackage = "com.android.system.qs.twotoneaccent";
              break;
            case 4:
              overlayThemePackage = "com.android.system.qs.shaded";
              break;
            case 5:
              overlayThemePackage = "com.android.system.qs.cyberpunk";
              break;
            case 6:
              overlayThemePackage = "com.android.system.qs.neumorph";
              break;
            case 7:
              overlayThemePackage = "com.android.system.qs.reflected";
              break;
            case 8:
              overlayThemePackage = "com.android.system.qs.surround";
              break;
            case 9:
              overlayThemePackage = "com.android.system.qs.thin";
              break;
            case 10:
              overlayThemePackage = "com.android.system.qs.twotoneaccenttrans";
              break;
            default:
              break;
        }

        if (mThemeUtils == null) {
            mThemeUtils = new ThemeUtils(context);
        }

        // reset all overlays before applying
        mThemeUtils.setOverlayEnabled(qsPanelStyleCategory, overlayThemeTarget, overlayThemeTarget);

        if (qsPanelStyle > 0) {
            mThemeUtils.setOverlayEnabled(qsPanelStyleCategory, overlayThemePackage, overlayThemeTarget);
        }
    }

    private void checkQSOverlays(Context context) {
        ContentResolver resolver = context.getContentResolver();
        int isA11Style = Settings.System.getIntForUser(resolver,
                Settings.System.QS_TILE_UI_STYLE , 0, UserHandle.USER_CURRENT);
        int qsPanelStyle = Settings.System.getIntForUser(resolver,
                Settings.System.QS_PANEL_STYLE , 0, UserHandle.USER_CURRENT);

        if (isA11Style > 0) {
            mQsUI.setEnabled(true);
            mQsPanelStyle.setEnabled(false);
            if (qsPanelStyle > 0) {
                qsPanelStyle = 0;
                Settings.System.putIntForUser(resolver,
                        Settings.System.QS_PANEL_STYLE, 0, UserHandle.USER_CURRENT);
                updateQsPanelStyle(context);
            }
        } else if (qsPanelStyle > 0) {
            mQsPanelStyle.setEnabled(true);
            mQsUI.setEnabled(false);
            if (isA11Style > 0) {
                isA11Style = 0;
                Settings.System.putIntForUser(resolver,
                        Settings.System.QS_TILE_UI_STYLE, 0, UserHandle.USER_CURRENT);
                updateQsStyle(context);
            }
        } else {
            mQsUI.setEnabled(true);
            mQsPanelStyle.setEnabled(true);
        }

        // Update summaries
        int index = mQsUI.findIndexOfValue(Integer.toString(isA11Style));
        mQsUI.setValue(Integer.toString(isA11Style));
        mQsUI.setSummary(mQsUI.getEntries()[index]);

        index = mQsPanelStyle.findIndexOfValue(Integer.toString(qsPanelStyle));
        mQsPanelStyle.setValue(Integer.toString(qsPanelStyle));
        mQsPanelStyle.setSummary(mQsPanelStyle.getEntries()[index]);
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
                    sir.xmlResId = R.xml.qs_tiles;
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
