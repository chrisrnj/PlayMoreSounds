/*
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

package com.epicnicity322.customdiscs;

import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.nbssongplayer.NBSSongPlayer;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.CommandLoader;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CustomDiscs extends PMSAddon {
    public static final @NotNull ConfigurationHolder CUSTOM_DISCS_CONFIG = new ConfigurationHolder(Configurations.BIOMES.getConfigurationHolder().getPath().getParent().resolve("custom discs.yml"), "# Set a sound to play when a player clicks at a jukebox with a specific item.\n" +
            "#\n" +
            "# Warnings: \n" +
            "#   >> You must be on version 1.14+!\n" +
            "#   >> Players must have the permission 'playmoresounds.disc.use'.\n" +
            "#   >> Delayed sounds will not stop when the disc is removed.\n" +
            "#   >> For performance reasons, the sound will only play if you have only 1 disc in your hand.\n" +
            "#   >> When the disc is removed the sound will only stop for the player who removed it,\n" +
            "#   meaning if the sound has a radius the sound will not be stopped to the players in\n" +
            "#   the radius.\n" +
            "#\n" +
            "# To set a sound, just create a configuration section with an id and set the item name,\n" +
            "# material and lore or just copy the sample.\n" +
            "# Item material list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html\n" +
            "#\n" +
            "# Usage In-Game: \n" +
            "#   Get the disc with the command '/pms disc <id>'\n" +
            "#   Click on a jukebox with one of the discs that you set here to play the sound.\n" +
            "#\n" +
            "# Sample:\n" +
            "# (Take a note that this is a sample and the sounds and items may not be available on\n" +
            "# your MC version.)\n" +
            "\n" +
            "PLING_DISC: # This is the ID of the custom disc. Here I named this disc PLING_DISC. Disc IDs can not have spaces.\n" +
            "  Enabled: true\n" +
            "  Item:\n" +
            "    Material: GOLDEN_APPLE # The material of the custom disc item.\n" +
            "    Name: '&2&lPling Disc' # The name of the custom disc item.\n" +
            "    Lore: 'Different pitched pling sounds!' # The lore of the custom disc item. Use <line> to break a line.\n" +
            "    Glowing: true # If this disc should glow.\n" +
            "  Sounds: # The sounds to play when a player uses this disc.\n" +
            "    '0':\n" +
            "      Delay: 0\n" +
            "      Options:\n" +
            "        Radius: 20.0\n" +
            "      Pitch: 1.0\n" +
            "      Sound: BLOCK_NOTE_BLOCK_PLING\n" +
            "      Volume: 10.0\n" +
            "    '1':\n" +
            "      Delay: 20\n" +
            "      Options:\n" +
            "        Radius: 20.0\n" +
            "      Pitch: 2.0\n" +
            "      Sound: BLOCK_NOTE_BLOCK_PLING\n" +
            "      Volume: 10.0\n" +
            "    '2':\n" +
            "      Delay: 40\n" +
            "      Options:\n" +
            "        Radius: 20.0\n" +
            "      Pitch: 0.0\n" +
            "      Sound: BLOCK_NOTE_BLOCK_PLING\n" +
            "      Volume: 10.0\n" +
            "\n" +
            "# More information about sounds on sounds.yml\n" +
            "\n" +
            "Version: '" + PlayMoreSoundsVersion.version + "'");
    private static final @NotNull AtomicBoolean registered = new AtomicBoolean(false);
    private static final @NotNull HashMap<String, ItemStack> customDiscs = new HashMap<>();
    private static final @NotNull HashMap<ItemStack, PlayableRichSound> customDiscsSounds = new HashMap<>();
    private static final @NotNull NamespacedKey customDiscNBT = new NamespacedKey(PlayMoreSounds.getInstance(), "customdisc");
    private static boolean NBS_SONG_PLAYER = false;
    private static final @NotNull Listener listener = new Listener() {
        @EventHandler(priority = EventPriority.NORMAL)
        public void onPlayerInteract(PlayerInteractEvent event) {
            if ((event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) || event.useInteractedBlock() == Event.Result.DENY)
                return;

            Block clickedBlock = event.getClickedBlock();
            Player player = event.getPlayer();
            ItemStack itemInHand = event.getItem();

            if (clickedBlock == null || clickedBlock.getType() != Material.JUKEBOX || !player.hasPermission("playmoresounds.disc.use"))
                return;

            Jukebox jukebox = (Jukebox) clickedBlock.getState();
            Location clickedLocation = clickedBlock.getLocation();

            // When jukebox is not empty, minecraft will eject the current playing disc.
            if (jukebox.getPlaying() != Material.AIR) return;

            PersistentDataContainer jukeboxContainer = jukebox.getPersistentDataContainer();
            // Getting the currently playing sound on the jukebox by their id, null if no custom disc sound is playing.
            ItemStack currentlyPlayingDisc = customDiscs.get(jukeboxContainer.get(customDiscNBT, PersistentDataType.STRING));

            // Checking if there's a currently playing disc that should be ejected.
            if (currentlyPlayingDisc == null) {
                // Checking if player has a custom disc in hand that should be played.
                PlayableRichSound customDiscSound = customDiscsSounds.get(itemInHand);

                if (customDiscSound == null) return;

                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);

                // Removing the disc from player inventory.
                if (player.getGameMode() != GameMode.CREATIVE)
                    player.getInventory().removeItem(itemInHand);

                // Adding the id of the custom disc to the jukebox data and playing it.
                jukeboxContainer.set(customDiscNBT, PersistentDataType.STRING, itemInHand.getItemMeta().getPersistentDataContainer().get(customDiscNBT, PersistentDataType.STRING));
                jukebox.update();
                customDiscSound.play(player, clickedLocation);
            } else {
                event.setUseItemInHand(Event.Result.DENY);
                event.setUseInteractedBlock(Event.Result.DENY);

                // Ejecting disc and stopping the playing sounds.
                HashSet<String> sounds = new HashSet<>();

                // Getting sounds to stop and stopping them.
                for (PlayableSound sound : customDiscsSounds.get(currentlyPlayingDisc).getChildSounds()) {
                    String soundName = sound.getSound();

                    if (NBS_SONG_PLAYER && soundName.startsWith("nbs:")) {
                        NBSSongPlayer.stop(player, soundName.substring(4));
                    } else {
                        sounds.add(soundName);
                    }
                }

                if (!sounds.isEmpty())
                    SoundManager.stopSounds(player, sounds, 0);

                // Removing the data of the custom disc and dropping it.
                jukeboxContainer.remove(customDiscNBT);
                jukebox.update();
                clickedLocation.getWorld().dropItem(clickedLocation.clone().add(0.0, 1.0, 0.0), currentlyPlayingDisc);
            }
        }
    };

    public static void reload() {
        if (!VersionUtils.hasPersistentData())
            throw new UnsupportedOperationException("Custom Discs is not compatible with versions lower than 1.14");

        synchronized (customDiscs) {
            customDiscs.clear();
            customDiscsSounds.clear();

            Configuration customDiscsConfig = CUSTOM_DISCS_CONFIG.getConfiguration();

            for (Map.Entry<String, Object> node : customDiscsConfig.getNodes().entrySet()) {
                // Disc ids that have spaces are not be obtainable through commands.
                if (!(node.getValue() instanceof ConfigurationSection) || node.getKey().contains(" ")) continue;

                String id = node.getKey();
                ConfigurationSection disc = (ConfigurationSection) node.getValue();

                if (!disc.getBoolean("Enabled").orElse(false)) continue;

                Material discMaterial = Material.matchMaterial(disc.getString("Item.Material").orElse("").toUpperCase());

                if (discMaterial == null || discMaterial.isAir()) {
                    PlayMoreSounds.getConsoleLogger().log("[Custom Discs] Disc with id " + id + " in custom discs configuration has an invalid material.", ConsoleLogger.Level.WARN);
                    continue;
                }

                ItemStack discItem = new ItemStack(discMaterial);
                ItemMeta discMeta = discItem.getItemMeta();

                discMeta.setUnbreakable(true);
                discMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', disc.getString("Item.Name").orElse("")));
                discMeta.setLore(Arrays.asList(disc.getString("Item.Lore").orElse("").split("<line>")));
                discMeta.getPersistentDataContainer().set(customDiscNBT, PersistentDataType.STRING, id);

                if (disc.getBoolean("Item.Glowing").orElse(false))
                    discMeta.addEnchant(Enchantment.DURABILITY, 1, false);

                discMeta.addItemFlags(ItemFlag.values());
                discItem.setItemMeta(discMeta);

                customDiscsSounds.put(discItem, new PlayableRichSound(disc));
                customDiscs.put(id, discItem);
            }
        }

        if (customDiscs.isEmpty()) {
            if (registered.getAndSet(false)) {
                HandlerList.unregisterAll(listener);
            }
        } else {
            if (!registered.getAndSet(true)) {
                Bukkit.getPluginManager().registerEvents(listener, PlayMoreSounds.getInstance());
            }
        }
    }

    /**
     * Gets a custom disc set on {@link #CUSTOM_DISCS_CONFIG} by the ID.
     *
     * @param id The id of the custom disc.
     * @return The custom disc item or null if the disc was not found or is not loaded.
     */
    public static @Nullable ItemStack getCustomDisc(@NotNull String id) {
        ItemStack item = customDiscs.get(id);

        if (item == null)
            return null;
        else
            return item.clone();
    }

    @Override
    protected void onStart() {
        if (!VersionUtils.hasPersistentData()) {
            PlayMoreSounds.getConsoleLogger().log("[Custom Discs] Custom Discs addon is not compatible with versions lower than 1.14.", ConsoleLogger.Level.ERROR);
            return;
        }

        Configurations.getConfigurationLoader().registerConfiguration(CUSTOM_DISCS_CONFIG, new Version("3.3.0"), PlayMoreSoundsVersion.getVersion());
        PlayMoreSounds.getConsoleLogger().log("[Custom Discs] &eCustom Discs configuration was registered.");
        CommandLoader.addCommand(new DiscCommand());

        // Running when server has fully started.
        Bukkit.getScheduler().runTask(PlayMoreSounds.getInstance(), () -> {
            for (Configurations language : Configurations.values()) {
                if (!language.name().startsWith("LANGUAGE") || language.getConfigurationHolder().getConfiguration().contains("Custom Discs"))
                    continue;
                String data = "";

                switch (language) {
                    case LANGUAGE_EN_US:
                        data = "\n\nCustom Discs:\n" +
                                "  Error:\n" +
                                "    Not Found: '&cA disc with the ID \"&7<id>&c\" was not found.'\n" +
                                "  Help: |-\n" +
                                "    &e/<label> disc <id> [target]\n" +
                                "    &7 > Gives a configured custom disc.\n" +
                                "  Success: '&7Giving the disc &f<id>&7 to &f<target>&7.'";
                        break;
                    case LANGUAGE_ES_LA:
                        data = "\n\nCustom Discs:\n" +
                                "  Error:\n" +
                                "    Not Found: '&cNo se encontró un disco con el ID \"&7<id>&c\".'\n" +
                                "  Help: |-\n" +
                                "    &e/<label> disc <id> [objetivo]\n" +
                                "    &7 > Da un disco personalizado configurado.\n" +
                                "  Success: '&7Dando el disco &f<id>&7 a &f<target>&7.'";
                        break;
                    case LANGUAGE_PT_BR:
                        data = "\n\nCustom Discs:\n" +
                                "  Error:\n" +
                                "    Not Found: '&cNão foi encontrado um disco com o ID \"&7<id>&c\".'\n" +
                                "  Help: |-\n" +
                                "    &e/<label> disc <id> [alvo]\n" +
                                "    &7 > Da um disco customizado da configuração.\n" +
                                "  Success: '&7Dando o disco &f<id>&7 a &f<target>&7.'";
                        break;
                    case LANGUAGE_ZH_CN:
                        data = "\n\nCustom Discs:\n" +
                                "  Error:\n" +
                                "    Not Found: '&c找不到ID为 \"&7<id>&c\" 的光盘'\n" +
                                "  Help: |-\n" +
                                "    &e/<label> disc <ID> [目标]\n" +
                                "    &7 > 提供配置的自定义光盘\n" +
                                "  Success: '&7将光盘 &f<id>&7 赋予 &f<target>'";
                        break;
                }

                try {
                    PathUtils.write(data, language.getConfigurationHolder().getPath());
                    PlayMoreSounds.getConsoleLogger().log("[Custom Discs] &eAdded Custom Discs language keys to " + language.name() + ".");
                } catch (Exception ignored) {
                }
            }

            Configurations.getConfigurationLoader().loadConfigurations();

            NBS_SONG_PLAYER = PlayMoreSounds.getAddonManager().getAddons().stream().anyMatch(addon -> addon.getDescription().getName().equals("NBS Song Player"));
            reload();
            PlayMoreSounds.onReload(CustomDiscs::reload);

            PlayMoreSounds.getConsoleLogger().log("[Custom Discs] &aCustom Discs was enabled successfully.");
        });
    }
}
