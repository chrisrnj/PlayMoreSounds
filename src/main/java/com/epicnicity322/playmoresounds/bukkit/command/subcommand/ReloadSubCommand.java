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

package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.listener.TimeTrigger;
import com.epicnicity322.playmoresounds.bukkit.util.ListenerRegister;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashSet;

public final class ReloadSubCommand extends Command implements Helpable
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();
    private static final @NotNull HashSet<Runnable> onReloadRunnables = new HashSet<>();

    /**
     * Reloads every configuration and events of PlayMoreSounds.
     *
     * @throws IOException If failed to save configurations.
     */
    public static void reload() throws IOException
    {
        Configurations.getConfigLoader().loadConfigurations();
        ListenerRegister.loadListeners();
        TimeTrigger.load();
        UpdateManager.check(Bukkit.getConsoleSender(), true);

        new Thread(() -> {
            for (Runnable runnable : onReloadRunnables)
                try {
                    runnable.run();
                } catch (Exception e) {
                    PlayMoreSounds.getPMSLogger().log("&cAn unknown error occurred on PlayMoreSounds reload.");
                    PlayMoreSounds.getErrorLogger().report(e, "PMSReloadingError (Unknown):");
                }
        }).start();
    }

    /**
     * Adds a runnable to run when PlayMoreSounds configurations are reloaded.
     *
     * @param runnable The runnable to run when configurations are reloaded.
     */
    public static void addOnReloadRunnable(@NotNull Runnable runnable)
    {
        onReloadRunnables.add(runnable);
    }

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> lang.send(sender, false, lang.get("Help.Reload").replace("<label>", label));
    }

    @Override
    public @NotNull String getName()
    {
        return "reload";
    }

    @Override
    public @Nullable String[] getAliases()
    {
        return new String[]{"rl"};
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.reload";
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> lang.send(sender, lang.get("General.No Permission"));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        try {
            reload();
            lang.send(sender, true, lang.get("Reload.Success"));
        } catch (Exception e) {
            lang.send(sender, true, lang.get("Reload.Error"));
            PlayMoreSounds.getErrorLogger().report(e, "Reload Config Exception:");
        }
    }
}
