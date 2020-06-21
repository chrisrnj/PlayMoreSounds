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
