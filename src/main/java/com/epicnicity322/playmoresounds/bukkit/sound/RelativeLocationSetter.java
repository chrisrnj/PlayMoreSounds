/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class RelativeLocationSetter implements Runnable
{
    private final @NotNull Player player;
    private @NotNull Location sourceLocation;
    private @Nullable Location relativeLocation;

    public RelativeLocationSetter(@NotNull Player player)
    {
        //TODO: Finish fancy relative location getter
        if (!VersionUtils.hasPersistentData())
            throw new UnsupportedOperationException("This class can only be used on bukkit 1.14+");

        this.player = player;
        sourceLocation = player.getLocation();
    }

    @Override
    public void run()
    {
        sourceLocation = player.getLocation();

    }

    public @NotNull Player getPlayer()
    {
        return player;
    }

    public @Nullable Location getRelativeLocation()
    {
        return relativeLocation;
    }

    public @NotNull Location getSourceLocation()
    {
        return sourceLocation;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof RelativeLocationSetter)) return false;

        RelativeLocationSetter that = (RelativeLocationSetter) o;

        return getPlayer().equals(that.getPlayer()) &&
                getSourceLocation().equals(that.getSourceLocation()) &&
                Objects.equals(getRelativeLocation(), that.getRelativeLocation());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getPlayer(), getSourceLocation(), getRelativeLocation());
    }
}
