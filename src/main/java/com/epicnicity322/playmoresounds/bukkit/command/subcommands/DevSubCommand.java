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
import com.epicnicity322.epicpluginlib.core.util.StringUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.gui.inventories.InputGetterInventory;
import com.epicnicity322.playmoresounds.bukkit.gui.inventories.RichSoundInventory;
import com.epicnicity322.playmoresounds.bukkit.listeners.OnPlayerInteract;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayRichSoundEvent;
import com.epicnicity322.playmoresounds.bukkit.sound.PlaySoundEvent;
import com.epicnicity322.playmoresounds.bukkit.util.ListenerRegister;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DevSubCommand extends Command
{
    private static final @NotNull AtomicBoolean soundLoggerRegistered = new AtomicBoolean(false);
    private static final @NotNull Listener soundLogger = new Listener()
    {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayRichSound(PlayRichSoundEvent event)
        {
            var logger = PlayMoreSounds.getConsoleLogger();
            var sound = event.getRichSound();
            var player = event.getSourcePlayer();
            String name = "null";
            String uuid = "null";

            if (player != null) {
                name = player.getName();
                uuid = player.getUniqueId().toString();
            }

            if (event.isCancelled()) {
                logger.log("RICH SOUND EVENT CANCELLED -> " + sound + " | TO PLAYER/UUID -> " + name + "/" + uuid);
            } else {
                logger.log("PLAYING RICH SOUND -> " + sound + " | TO PLAYER/UUID -> " + name + "/" + uuid);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlaySound(PlaySoundEvent event)
        {
            var logger = PlayMoreSounds.getConsoleLogger();
            var sound = event.getSound();
            var player = event.getSourcePlayer();
            String name = "null";
            String uuid = "null";

            if (player != null) {
                name = player.getName();
                uuid = player.getUniqueId().toString();
            }

            if (event.isCancelled()) {
                logger.log("SOUND EVENT CANCELLED -> " + sound + " | FROM PLAYER/UUID -> " + name + "/" + uuid + " | AT LOCATION -> " + event.getLocation() + " | TO LISTENER(S) -> " + event.getValidListeners());
            } else {
                logger.log("PLAYING SOUND -> " + sound + " | FROM PLAYER/UUID -> " + name + "/" + uuid + " | AT LOCATION -> " + event.getLocation() + " | TO LISTENER(S) -> " + event.getValidListeners());
            }
        }
    };

    @Override
    public @NotNull String getName()
    {
        return "dev";
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.dev";
    }

    @Override
    public int getMinArgsAmount()
    {
        return 2;
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        String command = join(args);

        switch (command) {
            case "test edit" ->
                    new RichSoundInventory(ListenerRegister.getListeners().stream().filter(l -> l.getName().equals("Change Held Item")).findFirst().orElseThrow().getRichSound(), Configurations.SOUNDS.getConfigurationHolder()).openInventory((Player) sender);
            case "open anvil" ->
                    new InputGetterInventory((Player) sender, "Testing", sender::sendMessage).openInventory();
            case "register addons" -> {
                try {
                    PlayMoreSounds.getAddonManager().registerAddons();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case "experimental addon commands" ->
                    AddonsSubCommand.experimentalCommands.set(!AddonsSubCommand.experimentalCommands.get());
            case "stop all addons" -> PlayMoreSounds.getAddonManager().stopAddons();
            case "log sounds" -> {
                if (soundLoggerRegistered.get()) {
                    sender.sendMessage("No longer logging sounds to console.");
                    HandlerList.unregisterAll(soundLogger);
                    soundLoggerRegistered.set(false);
                } else {
                    sender.sendMessage("Logging all sounds to console.");
                    Bukkit.getPluginManager().registerEvents(soundLogger, PlayMoreSounds.getInstance());
                    soundLoggerRegistered.set(true);
                }
            }
            default -> {
                if (args.length < 3) return;
                var lang = PlayMoreSounds.getLanguage();

                // Allows you to list confirmations of the specified player.
                if (command.startsWith("get confirmations")) {
                    UUID uuid = null;

                    if (args.length > 4) {
                        var player = getPlayer(args[4]);
                        uuid = player == null ? null : player.getUniqueId();
                    }

                    LinkedHashMap<Runnable, String> confirmations;
                    if (ConfirmSubCommand.pendingConfirmations == null ||
                            (confirmations = ConfirmSubCommand.pendingConfirmations.get(uuid)) == null || confirmations.isEmpty()) {
                        lang.send(sender, false, "no confirmations");
                        return;
                    }
                    int id = 1;

                    lang.send(sender, lang.get("Confirm.List.Header"));
                    for (String description : confirmations.values()) {
                        lang.send(sender, false, lang.get("Confirm.List.Confirmation").replace("<id>", Integer.toString(id++)).replace("<description>", description));
                    }
                    return;
                }

                // Allows you to confirm something for the specified player.
                if (args[1].equalsIgnoreCase("confirm") && StringUtils.isNumeric(args[2])) {
                    UUID uuid = null;

                    if (args.length > 4) {
                        var player = getPlayer(args[4]);
                        uuid = player == null ? null : player.getUniqueId();
                    }

                    LinkedHashMap<Runnable, String> confirmations;
                    if (ConfirmSubCommand.pendingConfirmations == null ||
                            (confirmations = ConfirmSubCommand.pendingConfirmations.get(uuid)) == null || confirmations.isEmpty()) {
                        lang.send(sender, lang.get("Confirm.Error.Nothing Pending"));
                        return;
                    }
                    int id = 1;
                    try {
                        id = Integer.parseInt(args[2]);
                    } catch (NumberFormatException ignored) {
                    }
                    if (id < 0 || id > confirmations.size()) {
                        lang.send(sender, false, "unknown id");
                        return;
                    }
                    int count = 1;

                    for (Map.Entry<Runnable, String> entry : new LinkedHashSet<>(confirmations.entrySet())) {
                        if (count++ == id) {
                            try {
                                lang.send(sender, false, "confirming: " + entry.getValue());
                                entry.getKey().run();
                            } catch (Throwable t) {
                                lang.send(sender, false, "something went wrong");
                                t.printStackTrace();
                            }
                            confirmations.remove(entry.getKey());
                            break;
                        }
                    }
                    return;
                }

                // Allows you to create a region with the specified player as owner, use "null" to refer to console.
                // The region is created with the senders positions.
                if (args[1].equalsIgnoreCase("rgnew")) {
                    new Thread(() -> {
                        UUID creator = null;
                        if (!args[2].equalsIgnoreCase("null")) {
                            OfflinePlayer player = getPlayer(args[2]);

                            if (player == null) {
                                sender.sendMessage("player not found");
                                return;
                            }

                            creator = player.getUniqueId();
                        }

                        Location[] selected = OnPlayerInteract.getSelectedDiagonals(sender instanceof Player player ? player.getUniqueId() : null);

                        if (selected == null || selected[0] == null || selected[1] == null) {
                            lang.send(sender, lang.get("Region.Create.Error.Not Selected").replace("<label>", label).replace("<label2>", "region"));
                            return;
                        } else if (!selected[0].getWorld().equals(selected[1].getWorld())) {
                            lang.send(sender, lang.get("Region.Create.Error.Different Worlds"));
                            return;
                        }

                        String name = PMSHelper.getRandomString(32);
                        var region = new SoundRegion(name, selected[0], selected[1], creator, null);
                        try {
                            RegionManager.save(region);
                            lang.send(sender, lang.get("Region.Create.Success").replace("<name>", name).replace("<label>", label).replace("<label2>", "rg"));
                            OnPlayerInteract.selectDiagonal(creator, null, true);
                            OnPlayerInteract.selectDiagonal(creator, null, false);
                        } catch (IOException e) {
                            lang.send(sender, lang.get("Region.Create.Error.Default").replace("<name>", name));
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        }
    }

    private String join(String[] args)
    {
        var builder = new StringBuilder();

        for (int i = 1; i < args.length; ++i)
            builder.append(" ").append(args[i]);

        return builder.toString().trim().toLowerCase();
    }

    private OfflinePlayer getPlayer(String nameOrUUID)
    {
        OfflinePlayer player = Bukkit.getPlayer(nameOrUUID);
        try {
            if (player == null) player = Bukkit.getOfflinePlayer(UUID.fromString(nameOrUUID));
        } catch (IllegalArgumentException ignored) {
        }
        return player;
    }
}
