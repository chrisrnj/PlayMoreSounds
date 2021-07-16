/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2021 Christiano Rangel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.epicpluginlib.core.tools.Version;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Utility for using bukkit methods and being compatible on all versions.
 */
public final class UniversalVersionMethods
{
    private static final @NotNull OnlinePlayersGetter onlinePlayersGetter;

    static {
        if (VersionUtils.getBukkitVersion().compareTo(new Version("1.7.9")) <= 0) {
            Method onlinePlayersMethod = getMethod(Bukkit.class, "getOnlinePlayers");
            ArrayList<? extends Player> empty = new ArrayList<>();

            if (onlinePlayersMethod == null) {
                onlinePlayersGetter = () -> empty;
            } else {
                onlinePlayersGetter = () -> {
                    try {
                        Player[] playerArray = (Player[]) onlinePlayersMethod.invoke(null);

                        return Arrays.asList(playerArray);
                    } catch (Exception e) {
                        return empty;
                    }
                };
            }
        } else {
            onlinePlayersGetter = Bukkit::getOnlinePlayers;
        }
    }

    private static @Nullable Method getMethod(@NotNull Class<?> clazz, @NotNull String name)
    {
        try {
            return clazz.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static @NotNull Collection<? extends Player> getOnlinePlayers()
    {
        return onlinePlayersGetter.getOnlinePlayers();
    }

    private interface OnlinePlayersGetter
    {
        @NotNull Collection<? extends Player> getOnlinePlayers();
    }
}
