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

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.PortalCreateEvent;
import org.jetbrains.annotations.NotNull;

public final class OnPortalCreate extends PMSListener
{
    public OnPortalCreate(@NotNull PlayMoreSounds plugin)
    {
        super(plugin);

        //OnPortalCreate listener uses methods that were changed in 1.14
        if (PlayMoreSoundsCore.getServerVersion().compareTo(new Version("1.14")) < 0)
            throw new UnsupportedOperationException("This listener is not supported in " + PlayMoreSoundsCore.getServerVersion());
    }

    @Override
    public @NotNull String getName()
    {
        return "Portal Create";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortalCreate(PortalCreateEvent event)
    {
        PlayableRichSound sound = getRichSound();

        // Other plugins might change the blocks list.
        if (event.getBlocks().isEmpty()) return;

        if (!event.isCancelled() || !sound.isCancellable()) {
            sound.play(event.getBlocks().get(0).getLocation());
        }
    }
}
