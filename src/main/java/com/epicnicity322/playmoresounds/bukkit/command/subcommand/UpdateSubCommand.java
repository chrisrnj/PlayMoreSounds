package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.updater.Updater;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UpdateSubCommand extends Command implements Helpable
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();

    @Override
    public @NotNull String getName()
    {
        return "update";
    }

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> lang.send(sender, false, lang.get("Help.Update").replace("<label>", label));
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.update";
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> lang.send(sender, lang.get("General.No Permission"));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("download")) {
                if (sender.hasPermission("playmoresounds.update.download")) {
                    lang.send(sender, "Download was disabled temporarily.");
                    /*
                    boolean force = false;

                    if (args.length > 2) {
                        if (args[2].equalsIgnoreCase("--force")) {
                            if (!sender.hasPermission("playmoresounds.update.download.force")) {
                                lang.send(sender, lang.get("General.No Permission"));
                                return;
                            }

                            force = true;
                        }
                    }

                    if (!UpdateManager.isUpdateAvailable()) {
                        if (!force) {
                            lang.send(sender, lang.get("Update.Not Available"));
                            return;
                        }
                    }

                    if (UpdateManager.isUpdateDownloaded()) {
                        if (!force) {
                            lang.send(sender, lang.get("Update.Download.Error"));
                            return;
                        }
                    }

                    if (force)
                        lang.send(sender, lang.get("Update.Download.Latest"));
                    else
                        lang.send(sender, lang.get("Update.Download.Default"));

                    Downloader.Result result = UpdateManager.download(sender instanceof Player);

                    switch (result) {
                        case SUCCESS:
                            Version version = UpdateManager.getUpdater().getLatestVersion();

                            lang.send(sender, lang.get("Update.Download.Success").replace("<version>", version.getVersion()));

                            if (version.compareTo(PlayMoreSounds.getVersion()) < 0)
                                lang.send(sender, lang.get("Update.Download.Lower"));

                            break;
                        case OFFLINE:
                            lang.send(sender, lang.get("Update.Error.Offline"));
                            break;
                        case TIMEOUT:
                            lang.send(sender, lang.get("Update.Error.Timeout"));
                            break;
                        case UNEXPECTED_ERROR:
                            lang.send(sender, lang.get("Update.Error.Default"));
                            break;
                    }
                     */
                } else {
                    lang.send(sender, lang.get("General.No Permission"));
                }
            } else {
                lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label)
                        .replace("<label2>", args[0]).replace("<args>", "[download]"));
            }
        } else {
            lang.send(sender, lang.get("Update.Check"));

            Updater.CheckResult result = UpdateManager.check(sender instanceof Player);

            switch (result) {
                case NOT_AVAILABLE:
                    lang.send(sender, lang.get("Update.Not Available"));
                    break;
                case OFFLINE:
                    lang.send(sender, lang.get("Update.Error.Offline"));
                    break;
                case AVAILABLE:
                    lang.send(sender, lang.get("Update.Available").replace("<label>", label)
                            .replace("<version>", UpdateManager.getUpdater().getLatestVersion().getVersion()));
                    break;
                case TIMEOUT:
                    lang.send(sender, lang.get("Update.Error.Timeout"));
                    break;
                case UNEXPECTED_ERROR:
                    lang.send(sender, lang.get("Update.Error.Default"));
                    break;
            }
        }
    }


}
