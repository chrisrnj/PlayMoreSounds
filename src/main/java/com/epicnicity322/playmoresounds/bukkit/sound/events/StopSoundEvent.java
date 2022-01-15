/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2022 Christiano Rangel
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

package com.epicnicity322.playmoresounds.bukkit.sound.events;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class StopSoundEvent extends Event implements Cancellable
{
    private static final @NotNull HandlerList handlers = new HandlerList();
    private final @NotNull Sound sound;
    private final @NotNull String id;
    private final @NotNull HashSet<Player> players;
    private boolean cancelled;

    public StopSoundEvent(@NotNull Sound sound, @NotNull String id, @NotNull HashSet<Player> players)
    {
        this.sound = sound;
        this.id = id;
        this.players = players;
    }

    public static @NotNull HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean value)
    {
        cancelled = value;
    }

    public @NotNull Sound getSound()
    {
        return sound;
    }

    public @NotNull String getId()
    {
        return id;
    }

    public @NotNull HashSet<Player> getPlayers()
    {
        return players;
    }
}
