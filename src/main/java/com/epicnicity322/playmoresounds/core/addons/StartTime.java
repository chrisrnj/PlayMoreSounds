package com.epicnicity322.playmoresounds.core.addons;

public enum StartTime
{
    /**
     * Before PlayMoreSounds has loaded commands.
     */
    BEFORE_COMMANDS,
    /**
     * Before PlayMoreSounds has loaded configurations.
     */
    BEFORE_CONFIGURATION,
    /**
     * Before PlayMoreSounds has loaded its sound listeners.
     */
    BEFORE_EVENTS,
    /**
     * When PlayMoreSounds finishes starting up.
     */
    END,
    /**
     * When the server finishes the load of all plugins.
     */
    SERVER_LOAD_COMPLETE,
    /**
     * When the specified plugins are loaded.
     */
    HOOK_PLUGINS,
    /**
     * When the specified addons are started.
     */
    HOOK_ADDONS
}
