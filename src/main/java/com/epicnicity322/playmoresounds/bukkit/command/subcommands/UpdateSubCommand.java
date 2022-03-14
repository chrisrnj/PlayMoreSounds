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

package com.epicnicity322.playmoresounds.bukkit.command.subcommands;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import com.epicnicity322.epicpluginlib.core.tools.SpigotUpdateChecker;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UpdateSubCommand extends Command implements Helpable
{
    private final @NotNull PlayMoreSounds instance;

    public UpdateSubCommand(@NotNull PlayMoreSounds instance)
    {
        this.instance = instance;
    }

    @Override
    public @NotNull String getName()
    {
        return "update";
    }

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, false, PlayMoreSounds.getLanguage().get("Help.Update").replace("<label>", label));
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.update";
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, PlayMoreSounds.getLanguage().get("General.No Permission"));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length > 1 && args[1].equalsIgnoreCase("download")) {
            var lang = PlayMoreSounds.getLanguage();
            if (args.length > 2 && args[2].equalsIgnoreCase("--force")) {
                lang.send(sender, lang.get("Update.Download.Downloading.Forcefully"));
                download(sender);
                return;
            }

            if (UpdateManager.isUpdateAvailable()) {
                lang.send(sender, lang.get("Update.Download.Downloading.Default"));
                download(sender);
                return;
            }

            SpigotUpdateChecker checker = new SpigotUpdateChecker(37429, PlayMoreSoundsVersion.getVersion());

            lang.send(sender, lang.get("Update.Download.Checking"));

            checker.check((available, version) -> {
                if (available) {
                    UpdateManager.check(null);
                    lang.send(sender, lang.get("Update.Download.Downloading.Default"));
                    download(sender);
                } else {
                    lang.send(sender, lang.get("Update.Not Available"));
                }
            }, (result, ex) -> {
                switch (result) {
                    case OFFLINE -> lang.send(sender, lang.get("Update.Error.Offline"));
                    case TIMEOUT -> lang.send(sender, lang.get("Update.Error.Timeout"));
                    case UNEXPECTED_ERROR -> {
                        lang.send(sender, lang.get("Update.Error.Default"));
                        PlayMoreSoundsCore.getErrorHandler().report(ex, "Unexpected Error On Check Before Update Download:");
                    }
                }
            });

            return;
        }

        UpdateManager.check(sender);
    }

    private void download(CommandSender sender)
    {
        new Thread("PMS Update Downloader")
        {
            @Override public void run()
            {
                String downloadedVersion = UpdateManager.downloadLatest(sender, instance);

                if (downloadedVersion != null) {
                    PlayMoreSounds.getLanguage().send(sender, PlayMoreSounds.getLanguage().get("Update.Download.Success").replace("<version>", downloadedVersion));
                }
            }
        }.start();
    }
}
