package com.epicnicity322.playmoresounds.bukkit.inventory;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InventoryUtils
{
    private static @Nullable NamespacedKey button;

    static {
        PlayMoreSounds.addOnInstanceRunnable(() -> button = new NamespacedKey(PlayMoreSounds.getInstance(),
                "button"));
    }

    private InventoryUtils()
    {
    }

    public static @NotNull NamespacedKey getButton()
    {
        if (button == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        return button;
    }
}
