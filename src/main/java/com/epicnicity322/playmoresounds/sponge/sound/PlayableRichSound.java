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

package com.epicnicity322.playmoresounds.sponge.sound;

import com.epicnicity322.playmoresounds.core.sound.RichSound;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;

public class PlayableRichSound extends RichSound<PlayableSound> implements Playable
{
    public PlayableRichSound(@NotNull String name, boolean enabled, boolean cancellable, @Nullable Collection<PlayableSound> childSounds)
    {
        super(name, enabled, cancellable, childSounds);
    }

    public PlayableRichSound(@NotNull ConfigurationSection section)
    {
        super(section);
    }

    @Override
    protected @NotNull PlayableSound newCoreSound(@NotNull ConfigurationSection section)
    {
        return new PlayableSound(section);
    }

    @Override
    public void play(@Nullable Player player, @NotNull Location<World> sourceLocation)
    {
        if (isEnabled() && !getChildSounds().isEmpty()) {
            for (PlayableSound s : getChildSounds()) {
                s.play(player, sourceLocation);
            }
        }
    }
}
