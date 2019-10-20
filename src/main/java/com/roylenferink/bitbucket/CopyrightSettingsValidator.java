package com.roylenferink.bitbucket;

import com.atlassian.bitbucket.scope.Scope;
import com.atlassian.bitbucket.setting.Settings;
import com.atlassian.bitbucket.setting.SettingsValidationErrors;
import com.atlassian.bitbucket.setting.SettingsValidator;
import com.roylenferink.bitbucket.logger.PluginLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class CopyrightSettingsValidator implements SettingsValidator {

    private final Logger log;

    public CopyrightSettingsValidator() {
        PluginLoggerFactory lf = PluginLoggerFactory.getInstance();
        this.log =  lf.getLoggerForThis(this);
    }

    @Override
    public void validate(@Nonnull Settings settings, @Nonnull SettingsValidationErrors errors, @Nonnull Scope scope) {
        try {
            if (StringUtils.isBlank(CopyrightSettingsHelper.getCopyrightRegexSettingRaw(settings))) {
                errors.addFieldError(CopyrightSettingsHelper.COPYRIGHT_REGEX_KEY, "Non-blank regular expression must be provided.");
            }
        } catch (Exception e) {
            log.error("Failed to load commit policy settings", e);
            errors.addFieldError(CopyrightSettingsHelper.COPYRIGHT_REGEX_KEY, "Valid regular expression must be provided.");
        }
    }
}
