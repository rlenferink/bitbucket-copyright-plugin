package com.roylenferink.bitbucket;


import com.atlassian.bitbucket.setting.Settings;

public class CopyrightSettingsHelper {

    public static String COPYRIGHT_REGEX_KEY = "copyright-regex";

    public static String getCopyrightRegexSettingRaw(Settings settings) {
        Object copyrightRegexObject = settings.asMap().get(COPYRIGHT_REGEX_KEY);
        return copyrightRegexObject != null ? copyrightRegexObject.toString() : null;
    }

}
