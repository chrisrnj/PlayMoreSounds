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

package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.core.config.PluginConfig;
import com.epicnicity322.epicpluginlib.core.util.ObjectUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.subcommand.ReloadSubCommand;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundManager;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OnPlayerInteract implements Listener
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();
    private static final @NotNull HashMap<UUID, Location[]> selectedDiagonals = new HashMap<>();
    private static final @NotNull HashMap<String, ItemStack> customDiscs = new HashMap<>();
    private static final @NotNull HashMap<ItemStack, RichSound> customDiscsSounds = new HashMap<>();
    private static final @NotNull PluginConfig customDiscsPluginConfig = Configurations.CUSTOM_DISCS.getPluginConfig();
    private static final @NotNull UUID console = UUID.randomUUID();
    private static NamespacedKey customDiscNBT;

    static {
        if (VersionUtils.hasPersistentData()) {
            Runnable customDiscUpdater = () -> {
                if (customDiscNBT == null)
                    customDiscNBT = new NamespacedKey(PlayMoreSounds.getInstance(), "pms_customdisc");

                customDiscs.clear();
                customDiscsSounds.clear();

                Configuration customDiscsConfig = customDiscsPluginConfig.getConfiguration();

                for (Map.Entry<String, Object> node : customDiscsConfig.getNodes().entrySet()) {
                    // Disc ids that have spaces are not be obtainable through commands.
                    if (!node.getKey().contains(" ") && node.getValue() instanceof ConfigurationSection) {
                        try {
                            String id = node.getKey();
                            ConfigurationSection disc = (ConfigurationSection) node.getValue();
                            ItemStack discItem = new ItemStack(Material.valueOf(disc.getString("Item.Material").orElse(null).toUpperCase()));
                            ItemMeta discMeta = discItem.getItemMeta();

                            discMeta.setUnbreakable(true);
                            discMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', disc.getString("Item.Name").orElse(null)));
                            discMeta.setLore(Arrays.asList(disc.getString("Item.Lore").orElse(null).split("<line>")));
                            discMeta.getPersistentDataContainer().set(customDiscNBT, PersistentDataType.STRING, id);

                            if (disc.getBoolean("Item.Glowing").orElse(false))
                                discMeta.addEnchant(Enchantment.DURABILITY, 1, false);

                            discMeta.addItemFlags(ItemFlag.values());
                            discItem.setItemMeta(discMeta);

                            customDiscsSounds.put(discItem, new RichSound(disc));
                            customDiscs.put(id, discItem);
                        } catch (Exception ignored) {
                            // Ignore if the custom disc is misconfigured.
                        }
                    }
                }
            };

            PlayMoreSounds.addOnEnableRunnable(customDiscUpdater);
            ReloadSubCommand.addOnReloadRunnable(customDiscUpdater);
        }
    }

    /**
     * Gets a custom disc set on {@link Configurations#CUSTOM_DISCS} by the ID.
     *
     * @param id The id of the custom disc.
     * @return The custom disc item or null if the disc was not found or is not loaded.
     */
    public static @Nullable ItemStack getCustomDisc(@NotNull String id)
    {
        ItemStack item = customDiscs.get(id);

        if (item == null)
            return null;
        else
            return item.clone();
    }

    /**
     * Gets the selected diagonals from this player.
     *
     * @param player The uuid of the player you want to get the selections from.
     * @return The locations the player selected or null if this player didn't select any location.
     */
    public static @Nullable Location[] getSelectedDiagonals(@Nullable UUID player)
    {
        return selectedDiagonals.get(ObjectUtils.getOrDefault(player, console));
    }

    /**
     * Sets a player selection to the specified location.
     *
     * @param player   The player to set or null if you want to set to console.
     * @param location The location to set the selection or null to remove the selection.
     * @param diagonal If this is the first diagonal. Set to false if you want to set the second.
     */
    public static void selectDiagonal(@Nullable UUID player, @Nullable Location location, boolean diagonal)
    {
        Location[] selection = getSelectedDiagonals(player);

        if (selection == null)
            selection = new Location[2];

        selection[diagonal ? 0 : 1] = location;
        selectedDiagonals.put(ObjectUtils.getOrDefault(player, console), selection);
    }

    // Other region plugins may cancel the event, so priority is set to high. If you wanted to play a sound on this event
    //you should use HIGHEST.
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        // Checking if event is cancelled.
        if (event.useInteractedBlock() != Event.Result.DENY && event.useInteractedBlock() != Event.Result.DENY) {
            Player player = event.getPlayer();

            // If theres a disc configured on Configurations#CUSTOM_DISCS then check for it.
            // customDiscsSounds map is only populated if the version of bukkit is 1.14+, so it's ok to use PersistentDataContainer.
            if (!customDiscsSounds.isEmpty()) {
                Block clickedBlock = event.getClickedBlock();

                if (clickedBlock.getType() == Material.JUKEBOX) {
                    Jukebox jukebox = (Jukebox) clickedBlock.getState();
                    Location clickedLocation = clickedBlock.getLocation();

                    // Checking if jukebox is empty.
                    if (jukebox.getPlaying() == Material.AIR) {
                        PersistentDataContainer jukeboxContainer = jukebox.getPersistentDataContainer();
                        // Getting the currently playing sound on the jukebox by their id, null if no custom disc sound is playing.
                        ItemStack currentlyPlayingDisc = customDiscs.get(jukeboxContainer.get(customDiscNBT, PersistentDataType.STRING));

                        // If sound is playing then stop it, if its not playing then check if item in hand is a custom disc to play.
                        if (currentlyPlayingDisc == null) {
                            // Players must have this permission to play a custom disc.
                            if (player.hasPermission("playmoresounds.disc.use")) {
                                ItemStack itemInHand = event.getItem();
                                RichSound customDisc = customDiscsSounds.get(itemInHand);

                                if (customDisc != null) {
                                    event.setUseItemInHand(Event.Result.DENY);
                                    event.setUseInteractedBlock(Event.Result.DENY);

                                    // Removing the disc from player inventory.
                                    if (player.getGameMode() != GameMode.CREATIVE)
                                        player.getInventory().removeItem(itemInHand);

                                    // Adding the id of the custom disc to the jukebox data and playing it.
                                    jukeboxContainer.set(customDiscNBT, PersistentDataType.STRING,
                                            itemInHand.getItemMeta().getPersistentDataContainer().get(customDiscNBT, PersistentDataType.STRING));
                                    jukebox.update();
                                    customDisc.play(player, clickedLocation);
                                }
                            }
                        } else {
                            event.setUseItemInHand(Event.Result.DENY);
                            event.setUseInteractedBlock(Event.Result.DENY);

                            HashSet<String> sounds = new HashSet<>();

                            // Getting sounds to stop and stopping them.
                            customDiscsSounds.get(currentlyPlayingDisc).getChildSounds().forEach(sound -> sounds.add(sound.getSound()));
                            SoundManager.stopSounds(player, sounds, 0);

                            // Removing the data of the custom disc and dropping it.
                            jukeboxContainer.remove(customDiscNBT);
                            jukebox.update();
                            clickedLocation.getWorld().dropItem(clickedLocation.clone().add(0.0, 1.0, 0.0), currentlyPlayingDisc);
                        }
                    }
                }
            }

            if (player.hasPermission("playmoresounds.region.select.wand")) {
                ItemStack item = event.getItem();
                UUID uuid = player.getUniqueId();

                if (item.isSimilar(RegionManager.getWand())) {
                    Action action = event.getAction();

                    if (action.toString().endsWith("BLOCK")) {
                        Location clicked = event.getClickedBlock().getLocation();
                        Location[] diagonals;

                        if (selectedDiagonals.containsKey(uuid))
                            diagonals = selectedDiagonals.get(uuid);
                        else
                            diagonals = new Location[2];

                        event.setUseInteractedBlock(Event.Result.DENY);
                        event.setUseItemInHand(Event.Result.DENY);

                        if (!player.hasPermission("playmoresounds.region.select.overlap")) {
                            if (RegionManager.getAllRegions().stream().anyMatch(region ->
                                    !Objects.equals(region.getCreator(), player.getUniqueId()) && region.isInside(clicked))) {
                                lang.send(player, lang.get("Region.Select.Error.Overlap"));
                                return;
                            }
                        }

                        int i = 0;

                        if (action == Action.RIGHT_CLICK_BLOCK)
                            i = 1;

                        if (!Objects.equals(diagonals[i], clicked)) {
                            diagonals[i] = clicked;
                            selectedDiagonals.put(uuid, diagonals);
                            lang.send(player, lang.get("Region.Set.Select.Position." + (i == 0 ? "First" : "Second"))
                                    .replace("<w>", clicked.getWorld().getName())
                                    .replace("<x>", Integer.toString(clicked.getBlockX()))
                                    .replace("<y>", Integer.toString(clicked.getBlockY()))
                                    .replace("<z>", Integer.toString(clicked.getBlockZ())));
                        }
                    }
                }
            }
        }
    }
}
