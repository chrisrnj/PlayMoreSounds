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

package com.epicnicity322.playmoresounds.sponge.sound;

import com.epicnicity322.playmoresounds.core.sound.CoreRichSound;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;

public class RichSound extends CoreRichSound<Sound> implements Playable
{
    public RichSound(@NotNull String name, boolean enabled, boolean cancellable, @Nullable Collection<Sound> childSounds)
    {
        super(name, enabled, cancellable, childSounds);
    }

    public RichSound(@NotNull ConfigurationSection section)
    {
        super(section);
    }

    @Override
    protected @NotNull Sound newCoreSound(@NotNull ConfigurationSection section)
    {
        return new Sound(section);
    }

    @Override
    public void play(@Nullable Player player, @NotNull Location<World> sourceLocation)
    {
        if (isEnabled() && !getChildSounds().isEmpty()) {
            for (Sound s : getChildSounds()) {
                s.play(player, sourceLocation);
            }
        }
    }
}
