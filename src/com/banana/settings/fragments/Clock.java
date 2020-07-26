/*
 * Copyright (C) 2017-2018 The Dirty Unicorns Project
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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.banana.bananaUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.bananadroid.support.colorpicker.ColorPickerPreference;
import com.bananadroid.support.preferences.CustomSeekBarPreference;
import com.bananadroid.support.preferences.SystemSettingSwitchPreference;

import java.util.Date;

public class Clock extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, CompoundButton.OnCheckedChangeListener {

    private static final String CLOCK_CATEGORY = "clock_category";
    private static final String DATE_CATEGORY = "date_category";
    private static final String STATUS_BAR_CLOCK_SECONDS = "status_bar_clock_seconds";
    private static final String STATUS_BAR_CLOCK_STYLE = "statusbar_clock_style";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_CLOCK_DATE_DISPLAY = "clock_date_display";
    private static final String STATUS_BAR_CLOCK_DATE_STYLE = "clock_date_style";
    private static final String STATUS_BAR_CLOCK_DATE_FORMAT = "clock_date_format";
    private static final String STATUS_BAR_CLOCK_DATE_POSITION = "statusbar_clock_date_position";
    public static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
    public static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
    private static final int CUSTOM_CLOCK_DATE_FORMAT_INDEX = 18;
    private static final String STATUS_BAR_CLOCK_COLOR = "status_bar_clock_color";
    private static final String STATUS_BAR_CLOCK_SIZE  = "status_bar_clock_size";
    private static final String STATUS_BAR_CLOCK_FONT_STYLE  = "status_bar_clock_font_style";
    private static final String QS_HEADER_CLOCK_SIZE = "qs_header_clock_size";
    private static final String QS_HEADER_CLOCK_FONT_STYLE = "qs_header_clock_font_style";
    private static final String QS_HEADER_CLOCK_COLOR = "qs_header_clock_color";

    static final int DEFAULT_STATUS_CLOCK_COLOR = 0xffffffff;

    private PreferenceCategory mClockCategory;
    private PreferenceCategory mDateCategory;
    private SystemSettingSwitchPreference mStatusBarSecondsShow;
    private ListPreference mStatusBarClock;
    private ListPreference mStatusBarAmPm;
    private ListPreference mClockDateDisplay;
    private ListPreference mClockDateStyle;
    private ListPreference mClockDateFormat;
    private ListPreference mClockDatePosition;
    private ListPreference mClockFontStyle;
    private ColorPickerPreference mClockColor;
    private CustomSeekBarPreference mClockSize;
    private CustomSeekBarPreference mQsClockSize;
    private ListPreference mQsClockFontStyle;
    private ColorPickerPreference mQsClockColor;

    private TextView mTextView;
    private View mSwitchBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.clock_options);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mClockCategory = (PreferenceCategory) findPreference(CLOCK_CATEGORY);
        mDateCategory = (PreferenceCategory) findPreference(DATE_CATEGORY);
        mStatusBarSecondsShow = (SystemSettingSwitchPreference) findPreference(STATUS_BAR_CLOCK_SECONDS);
        mStatusBarClock = (ListPreference) findPreference(STATUS_BAR_CLOCK_STYLE);
        mStatusBarAmPm = (ListPreference) findPreference(STATUS_BAR_AM_PM);
        mClockDateDisplay = (ListPreference) findPreference(STATUS_BAR_CLOCK_DATE_DISPLAY);
        mClockDateStyle = (ListPreference) findPreference(STATUS_BAR_CLOCK_DATE_STYLE);
        mClockDatePosition = (ListPreference) findPreference(STATUS_BAR_CLOCK_DATE_POSITION);

        mStatusBarSecondsShow.setChecked((Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_SECONDS, 0) == 1));
        mStatusBarSecondsShow.setOnPreferenceChangeListener(this);

        int clockStyle = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_STYLE, 0);
        CharSequence[] NonNotchEntries = { getResources().getString(R.string.status_bar_clock_style_left),
                getResources().getString(R.string.status_bar_clock_style_center),
                getResources().getString(R.string.status_bar_clock_style_right) };
        CharSequence[] NotchEntries = { getResources().getString(R.string.status_bar_clock_style_left),
                getResources().getString(R.string.status_bar_clock_style_right) };
        CharSequence[] NonNotchValues = {"0", "1" , "2"};
        CharSequence[] NotchValues = {"0", "2"};
        mStatusBarClock.setEntries(bananaUtils.hasNotch(getActivity()) ? NotchEntries : NonNotchEntries);
        mStatusBarClock.setEntryValues(bananaUtils.hasNotch(getActivity()) ? NotchValues : NonNotchValues);
        mStatusBarClock.setValue(String.valueOf(clockStyle));
        mStatusBarClock.setSummary(mStatusBarClock.getEntry());
        mStatusBarClock.setOnPreferenceChangeListener(this);

        if (DateFormat.is24HourFormat(getActivity())) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
        } else {
            int statusBarAmPm = Settings.System.getInt(resolver,
                    Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, 2);
            mStatusBarAmPm.setValue(String.valueOf(statusBarAmPm));
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntry());
            mStatusBarAmPm.setOnPreferenceChangeListener(this);
        }

        int clockDateDisplay = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY, 0);
        mClockDateDisplay.setValue(String.valueOf(clockDateDisplay));
        mClockDateDisplay.setSummary(mClockDateDisplay.getEntry());
        mClockDateDisplay.setOnPreferenceChangeListener(this);

        int clockDateStyle = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_DATE_STYLE, 0);
        mClockDateStyle.setValue(String.valueOf(clockDateStyle));
        mClockDateStyle.setSummary(mClockDateStyle.getEntry());
        mClockDateStyle.setOnPreferenceChangeListener(this);

        mClockDateFormat = (ListPreference) findPreference(STATUS_BAR_CLOCK_DATE_FORMAT);
        mClockDateFormat.setOnPreferenceChangeListener(this);
        String value = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.STATUSBAR_CLOCK_DATE_FORMAT);
        if (value == null || value.isEmpty()) {
            value = "EEE";
        }
        int index = mClockDateFormat.findIndexOfValue((String) value);
        if (index == -1) {
            mClockDateFormat.setValueIndex(CUSTOM_CLOCK_DATE_FORMAT_INDEX);
        } else {
            mClockDateFormat.setValue(value);
        }
        parseClockDateFormats();

        int clockDatePosition = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_CLOCK_DATE_POSITION, 0);
        mClockDatePosition.setValue(String.valueOf(clockDatePosition));
        mClockDatePosition.setSummary(mClockDatePosition.getEntry());
        mClockDatePosition.setOnPreferenceChangeListener(this);

        setDateOptions();

        int intColor;
        String hexColor;

        mClockColor = (ColorPickerPreference) findPreference(STATUS_BAR_CLOCK_COLOR);
        mClockColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CLOCK_COLOR, DEFAULT_STATUS_CLOCK_COLOR);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mClockColor.setSummary(hexColor);
        mClockColor.setNewPreviewColor(intColor);

        mClockSize = (CustomSeekBarPreference) findPreference(STATUS_BAR_CLOCK_SIZE);
        int clockSize = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CLOCK_SIZE, 14);
        mClockSize.setValue(clockSize / 1);
        mClockSize.setOnPreferenceChangeListener(this);

        mClockFontStyle = (ListPreference) findPreference(STATUS_BAR_CLOCK_FONT_STYLE);
        int showClockFont = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CLOCK_FONT_STYLE, 0);
        mClockFontStyle.setValue(String.valueOf(showClockFont));
        mClockFontStyle.setOnPreferenceChangeListener(this);

        mQsClockSize = (CustomSeekBarPreference) findPreference(QS_HEADER_CLOCK_SIZE);
        int qsClockSize = Settings.System.getInt(resolver,
                Settings.System.QS_HEADER_CLOCK_SIZE, 14);
        mQsClockSize.setValue(qsClockSize / 1);
        mQsClockSize.setOnPreferenceChangeListener(this);

        mQsClockFontStyle = (ListPreference) findPreference(QS_HEADER_CLOCK_FONT_STYLE);
        int qsClockFont = Settings.System.getInt(resolver,
                Settings.System.QS_HEADER_CLOCK_FONT_STYLE, 0);
        mQsClockFontStyle.setValue(String.valueOf(qsClockFont));
        mQsClockFontStyle.setOnPreferenceChangeListener(this);

        mQsClockColor = (ColorPickerPreference) findPreference(QS_HEADER_CLOCK_COLOR);
        mQsClockColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                Settings.System.QS_HEADER_CLOCK_COLOR, 0xffffffff);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mQsClockColor.setSummary(hexColor);
        mQsClockColor.setNewPreviewColor(intColor);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.master_setting_switch, container, false);
        ((ViewGroup) view).addView(super.onCreateView(inflater, container, savedInstanceState));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUSBAR_CLOCK, 1) == 1;

        mTextView = view.findViewById(R.id.switch_text);
        mTextView.setText(getString(enabled ?
                R.string.switch_on_text : R.string.switch_off_text));

        mSwitchBar = view.findViewById(R.id.switch_bar);
        Switch switchWidget = mSwitchBar.findViewById(android.R.id.switch_widget);
        switchWidget.setChecked(enabled);
        switchWidget.setOnCheckedChangeListener(this);
        mSwitchBar.setActivated(enabled);
        mSwitchBar.setOnClickListener(v -> {
            switchWidget.setChecked(!switchWidget.isChecked());
            mSwitchBar.setActivated(switchWidget.isChecked());
        });

        mClockCategory.setEnabled(enabled);
        mDateCategory.setEnabled(enabled);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Settings.System.putInt(getContentResolver(),
                Settings.System.STATUSBAR_CLOCK, isChecked ? 1 : 0);
        mTextView.setText(getString(isChecked ? R.string.switch_on_text : R.string.switch_off_text));
        mSwitchBar.setActivated(isChecked);

        mClockCategory.setEnabled(isChecked);
        mDateCategory.setEnabled(isChecked);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BANANADROID;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        AlertDialog dialog;
	    ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarSecondsShow) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_SECONDS, value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarClock) {
            int clockStyle = Integer.parseInt((String) newValue);
            int index = mStatusBarClock.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_STYLE, clockStyle);
            mStatusBarClock.setSummary(mStatusBarClock.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarAmPm) {
            int statusBarAmPm = Integer.valueOf((String) newValue);
            int index = mStatusBarAmPm.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, statusBarAmPm);
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntries()[index]);
            return true;
        } else if (preference == mClockDateDisplay) {
            int clockDateDisplay = Integer.valueOf((String) newValue);
            int index = mClockDateDisplay.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY, clockDateDisplay);
            mClockDateDisplay.setSummary(mClockDateDisplay.getEntries()[index]);
            setDateOptions();
            return true;
        } else if (preference == mClockDateStyle) {
            int clockDateStyle = Integer.valueOf((String) newValue);
            int index = mClockDateStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_STYLE, clockDateStyle);
            mClockDateStyle.setSummary(mClockDateStyle.getEntries()[index]);
            parseClockDateFormats();
            return true;
        } else if (preference == mClockDateFormat) {
            int index = mClockDateFormat.findIndexOfValue((String) newValue);

            if (index == CUSTOM_CLOCK_DATE_FORMAT_INDEX) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.clock_date_string_edittext_title);
                alert.setMessage(R.string.clock_date_string_edittext_summary);

                final EditText input = new EditText(getActivity());
                String oldText = Settings.System.getString(
                    getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_FORMAT);
                if (oldText != null) {
                    input.setText(oldText);
                }
                alert.setView(input);

                alert.setPositiveButton(R.string.menu_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        String value = input.getText().toString();
                        if (value.equals("")) {
                            return;
                        }
                        Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.STATUSBAR_CLOCK_DATE_FORMAT, value);
                        return;
                    }
                });

                alert.setNegativeButton(R.string.menu_cancel,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int which) {
                        return;
                    }
                });
                dialog = alert.create();
                dialog.show();
            } else {
                if ((String) newValue != null) {
                    Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_CLOCK_DATE_FORMAT, (String) newValue);
                }
            }
            return true;
        } else if (preference == mClockDatePosition) {
            int val = Integer.parseInt((String) newValue);
            int index = mClockDatePosition.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_DATE_POSITION, val);
            mClockDatePosition.setSummary(mClockDatePosition.getEntries()[index]);
            parseClockDateFormats();
            return true;
        } else if (preference == mClockColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_CLOCK_COLOR, intHex);
                return true;
        } else if (preference == mClockSize) {
            int width = ((Integer)newValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_CLOCK_SIZE, width);
            return true;
        } else if (preference == mClockFontStyle) {
            int showClockFont = Integer.valueOf((String) newValue);
            int index = mClockFontStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.
                STATUS_BAR_CLOCK_FONT_STYLE, showClockFont);
            mClockFontStyle.setSummary(mClockFontStyle.getEntries()[index]);
            return true;
      	} else if (preference == mQsClockSize) {
            int width = ((Integer)newValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.QS_HEADER_CLOCK_SIZE, width);
            return true;
        }  else if (preference == mQsClockFontStyle) {
            int qsClockFont = Integer.valueOf((String) newValue);
            int index = mQsClockFontStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.
                QS_HEADER_CLOCK_FONT_STYLE, qsClockFont);
            mQsClockFontStyle.setSummary(mQsClockFontStyle.getEntries()[index]);
            return true;
        } else if (preference == mQsClockColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.QS_HEADER_CLOCK_COLOR, intHex);
            return true;
        }
        return false;
    }

    private void parseClockDateFormats() {
        String[] dateEntries = getResources().getStringArray(R.array.clock_date_format_entries_values);
        CharSequence parsedDateEntries[];
        parsedDateEntries = new String[dateEntries.length];
        Date now = new Date();

        int lastEntry = dateEntries.length - 1;
        int dateFormat = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_DATE_STYLE, 0);
        for (int i = 0; i < dateEntries.length; i++) {
            if (i == lastEntry) {
                parsedDateEntries[i] = dateEntries[i];
            } else {
                String newDate;
                CharSequence dateString = DateFormat.format(dateEntries[i], now);
                if (dateFormat == CLOCK_DATE_STYLE_LOWERCASE) {
                    newDate = dateString.toString().toLowerCase();
                } else if (dateFormat == CLOCK_DATE_STYLE_UPPERCASE) {
                    newDate = dateString.toString().toUpperCase();
                } else {
                    newDate = dateString.toString();
                }

                parsedDateEntries[i] = newDate;
            }
        }
        mClockDateFormat.setEntries(parsedDateEntries);
    }

    private void setDateOptions() {
        int enableDateOptions = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.STATUSBAR_CLOCK_DATE_DISPLAY, 0);
        if (enableDateOptions == 0) {
            mClockDateStyle.setEnabled(false);
            mClockDateFormat.setEnabled(false);
            mClockDatePosition.setEnabled(false);
        } else {
            mClockDateStyle.setEnabled(true);
            mClockDateFormat.setEnabled(true);
            mClockDatePosition.setEnabled(true);
        }
    }
}
