package org.andstatus.todoagenda.prefs.colors;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.rarepebble.colorpicker.ColorPreference;
import com.rarepebble.colorpicker.ColorPreferenceDialog;

import org.andstatus.todoagenda.MainActivity;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.RemoteViewsFactory;
import org.andstatus.todoagenda.prefs.ApplicationPreferences;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.widget.TimeSection;

import static org.andstatus.todoagenda.WidgetConfigurationActivity.FRAGMENT_TAG;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_DIFFERENT_COLORS_FOR_DARK;
import static org.andstatus.todoagenda.prefs.colors.ThemeColors.PREF_TEXT_COLOR_SOURCE;

/** AndroidX version created by yvolk@yurivolkov.com
 *   based on this answer: https://stackoverflow.com/a/53290775/297710
 *   and on the code of https://github.com/koji-1009/ChronoDialogPreference
 */
public class ColorsPreferencesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String EXTRA_GOTO_COLORS_PREFERENCES = RemoteViewsFactory.PACKAGE + ".extra.GOTO_COLORS_PREFERENCES";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setTitle();
        addPreferencesFromResource(R.xml.preferences_colors);
        removeUnavailablePreferences();
    }

    private void setTitle() {
        ApplicationPreferences.getEditingColorThemeType(getActivity()).setTitle(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();
        removeUnavailablePreferences();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        showTextSources();
    }

    private void showTextSources() {
        Context context = getActivity();
        if (context != null) {
            TextColorSource textColorSource = ApplicationPreferences.getTextColorSource(context);
            Preference preference = findPreference(PREF_TEXT_COLOR_SOURCE);
            if (preference != null) {
                preference.setSummary(context.getString(textColorSource.summaryResId));
            }
            switch (textColorSource) {
                case AUTO:
                    break;
                case SHADING:
                    showShadings();
                    break;
                case COLORS:
                    showTextColors();
                    break;
            }
        }
    }

    private void removeUnavailablePreferences() {
        Context context = getActivity();
        if (context == null) return;

        ColorThemeType colorThemeType = ApplicationPreferences.getColorThemeType(context);
        if (!ColorThemeType.canHaveDifferentColorsForDark() ||
                colorThemeType == ColorThemeType.LIGHT ||
                colorThemeType == ColorThemeType.SINGLE && !InstanceSettings.isDarkThemeOn(context)) {
            PreferenceScreen screen = getPreferenceScreen();
            Preference preference = findPreference(PREF_DIFFERENT_COLORS_FOR_DARK);
            if (screen != null && preference != null) {
                screen.removePreference(preference);
            }
        }
        if (ApplicationPreferences.noPastEvents(context)) {
            PreferenceScreen screen = getPreferenceScreen();
            Preference preference = findPreference(TimeSection.PAST.preferenceCategoryKey);
            if (screen != null && preference != null) {
                screen.removePreference(preference);
            }
        }
        switch (ApplicationPreferences.getTextColorSource(context)) {
            case AUTO:
                removeShadings();
                removeTextColors();
                break;
            case SHADING:
                removeTextColors();
                break;
            case COLORS:
                removeShadings();
                break;
        }
    }

    private void removeShadings() {
        for (TextShadingPref pref : TextShadingPref.values()) {
            removePreferenceImproved(pref.preferenceName);
        }
    }

    private void removePreferenceImproved(String preferenceName) {
        Preference preference = findPreference(preferenceName);
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null && preference != null) {
            PreferenceGroup group = preference.getParent();
            if (group != null) {
                group.removePreference(preference);
            } else {
                screen.removePreference(preference);
            }
        }
    }

    private void removeTextColors() {
        // TODO
    }

    private void showShadings() {
        for (TextShadingPref shadingPref : TextShadingPref.values()) {
            showShading(shadingPref);
        }
    }

    private void showTextColors() {
        // TODO
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        FragmentActivity activity = getActivity();
        switch (key) {
            case PREF_DIFFERENT_COLORS_FOR_DARK:
                if (activity != null) {
                    if (ApplicationPreferences.getEditingColorThemeType(activity) == ColorThemeType.NONE) {
                        activity.startActivity(MainActivity.intentToConfigure(activity, ApplicationPreferences.getWidgetId(activity)));
                        activity.finish();
                        return;
                    };
                    setTitle();
                }
                break;
            case PREF_TEXT_COLOR_SOURCE:
                if (activity != null) {
                    Intent intent = activity.getIntent();
                    intent.putExtra(EXTRA_GOTO_COLORS_PREFERENCES, true);
                    activity.startActivity(intent);
                    activity.finish();
                    return;
                }
                break;
            default:
                showTextSources();
                break;
        }
    }

    private void showShading(TextShadingPref pref) {
        ListPreference preference = (ListPreference) findPreference(pref.preferenceName);
        if (preference != null) {
            TextShading shading = TextShading.fromName(preference.getValue(), pref.defaultShading);
            preference.setSummary(getActivity().getString(shading.titleResId));
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof ColorPreference) {
            dialogFragment = new ColorPreferenceDialog((ColorPreference) preference);
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), FRAGMENT_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}