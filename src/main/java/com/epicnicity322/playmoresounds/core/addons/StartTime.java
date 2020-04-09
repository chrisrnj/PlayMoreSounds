package com.epicnicity322.playmoresounds.core.addons;

public enum StartTime
{
    /**
     * Before PlayMoreSounds's configuration is loaded.
     */
    BEFORE_CONFIGURATION,
    /**
     * Before PlayMoreSounds's events are loaded.
     */
    BEFORE_EVENTS,
    /**
     * When PlayMoreSounds finishes starting up.
     */
    END,
    /**
     * Persists loaded even if PMS fails to load.
     */
    END_PERSISTENT,
    /**
     * When the server finishes the load of all plugins.
     */
    SERVER_LOAD_COMPLETE,
    HOOK_PLUGINS,
    HOOK_ADDONS
}
