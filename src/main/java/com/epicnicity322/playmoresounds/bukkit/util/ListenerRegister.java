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

package com.epicnicity322.playmoresounds.bukkit.util;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.listener.*;
import com.epicnicity322.playmoresounds.bukkit.sound.Sound;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ListenerRegister
{
    private static final @NotNull HashSet<PMSListener> listeners = new HashSet<>();
    /**
     * Scary sounds of halloween event.
     */
    private static final @NotNull List<String> scarySounds = Arrays.asList("ENTITY_GHAST_AMBIENT", "ENTITY_GHAST_HURT",
            "ENTITY_ENDER_DRAGON_GROWL", "ENTITY_ENDERMAN_SCREAM", "ENTITY_GHAST_SCREAM");
    private static final @NotNull Random random = new Random();

    static {
        PlayMoreSounds.addOnInstanceRunnable(() -> {
            PlayMoreSounds instance = PlayMoreSounds.getInstance();

            listeners.add(new OnAsyncPlayerChat(instance));
            listeners.add(new OnCraftItem(instance));
            listeners.add(new OnEntityDamageByEntity(instance));
            listeners.add(new OnFurnaceExtract(instance));
            listeners.add(new OnInventoryClick(instance));
            listeners.add(new OnInventoryClose(instance));
            listeners.add(new OnPlayerBedEnter(instance));
            listeners.add(new OnPlayerBedLeave(instance));
            listeners.add(new OnPlayerCommandPreprocess(instance));
            listeners.add(new OnPlayerDeath(instance));
            listeners.add(new OnPlayerDropItem(instance));
            listeners.add(new OnPlayerGameModeChange(instance));
            listeners.add(new OnPlayerItemHeld(instance));
            listeners.add(new OnPlayerKick(instance));
            listeners.add(new OnPlayerLevelChange(instance));
            listeners.add(new OnPlayerToggleFlight(instance));
            listeners.add(new OnPlayerToggleSneak(instance));
            listeners.add(new OnRegionEnterLeave(instance));

            // Jump events are only available on PaperMC.
            if (VersionUtils.isPaperMC()) {
                listeners.add(new OnPlayerJump(instance));
                listeners.add(new OnEntityJump(instance));
            }
        });
    }

    private ListenerRegister()
    {
    }

    /**
     * Adds a listener to the list of listeners to be loaded on {@link #loadListeners()}.
     *
     * @param listener The listener to add to be registered.
     */
    public static void addListener(@NotNull PMSListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Removes a listener from the list of listeners to be loaded on {@link #loadListeners()}.
     *
     * @param listener The listener to remove.
     */
    public static void removeListener(@NotNull PMSListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * @return A set with all the listeners that are being loaded on {@link #loadListeners()}.
     */
    public static @NotNull Set<PMSListener> getListeners()
    {
        return Collections.unmodifiableSet(listeners);
    }

    /**
     * Registers all sound listeners.
     *
     * @return The amount of listeners that were loaded.
     */
    public static int loadListeners()
    {
        int loadedListeners = 0;
        boolean halloween = PMSHelper.halloweenEvent();

        for (PMSListener listener : listeners) {
            try {
                listener.load();

                if (halloween && listener.getRichSound() != null && !listener.getName().equals("Change Held Item"))
                    for (Sound sound : listener.getRichSound().getChildSounds()) {
                        sound.setSound(scarySounds.get(random.nextInt(scarySounds.size() - 1)));
                        sound.setPitch(1);
                        sound.setVolume(10);
                    }

                if (listener.isLoaded())
                    ++loadedListeners;
            } catch (Exception ex) {
                PlayMoreSounds.getPMSLogger().log("&cCould not load the listener " + listener.getName() + ".");
                PlayMoreSounds.getErrorLogger().report(ex, listener.getName() + " listener load exception:");
            }
        }

        return loadedListeners;
    }
}
