package com.epicnicity322.playmoresounds.core;

import com.epicnicity322.epicpluginlib.logger.ErrorLogger;
import com.epicnicity322.epicpluginlib.logger.Logger;
import com.epicnicity322.playmoresounds.core.addons.AddonManager;

import java.nio.file.Path;

public interface PlayMoreSounds
{
    ErrorLogger getErrorLogger();

    Logger getPMSLogger();

    Path getFolder();

    AddonManager getAddonManager();
}
