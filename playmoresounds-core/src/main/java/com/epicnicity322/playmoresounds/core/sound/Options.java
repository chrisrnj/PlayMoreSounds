package com.epicnicity322.playmoresounds.core.sound;

import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Options(int delay, double radius, boolean ignoreToggle, @Nullable String permissionRequired,
                      @Nullable String permissionToListen) {
    @NotNull
    public static final Options DEFAULT_OPTIONS = new Options(0, 0.0, false, null, null);

    public Options(@NotNull ConfigurationSection section) {
        this(section.getNumber("Delay").orElse(0).intValue(), section.getNumber("Radius").orElse(0.0).doubleValue(), section.getBoolean("Ignore Toggle").orElse(false),
                section.getString("Permission Required").orElse(null), section.getString("Permission To Listen").orElse(null));
    }
}
