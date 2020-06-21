package com.epicnicity322.playmoresounds.bukkit.command.subcommand;

import com.epicnicity322.epicpluginlib.bukkit.command.Command;
import com.epicnicity322.epicpluginlib.bukkit.command.CommandRunnable;
import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.listener.TimeTrigger;
import com.epicnicity322.playmoresounds.bukkit.util.ListenerRegister;
import com.epicnicity322.playmoresounds.bukkit.util.UpdateManager;
import com.epicnicity322.playmoresounds.core.config.Configurations;
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
        UpdateManager.check(true);

        new Thread(() -> {
            for (Runnable runnable : onReloadRunnables)
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
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
