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
    BEFORE_CONFIGURATIONS,
    /**
     * Before PlayMoreSounds has loaded its sound listeners.
     */
    BEFORE_LISTENERS,
    /**
     * When PlayMoreSounds finishes starting up.
     */
    END,
    /**
     * When the server finishes the load of all plugins.
     */
    SERVER_LOAD_COMPLETE
}
