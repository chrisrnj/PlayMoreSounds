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

package com.epicnicity322.playmoresounds.bukkit.inventory;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.reflection.ReflectionUtil;
import com.epicnicity322.epicpluginlib.core.tools.Downloader;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.epicpluginlib.core.util.ZipUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.subcommand.AddonsSubCommand;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import com.epicnicity322.playmoresounds.core.addons.AddonDescription;
import com.epicnicity322.playmoresounds.core.addons.PMSAddon;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO: Add change page button to Addon Management Inventory and Addon Install Inventory.
@SuppressWarnings("deprecation")
public final class AddonsInventory implements PMSInventory
{
    private static final @NotNull AtomicBoolean block = new AtomicBoolean(false);
    private static final @NotNull Path tempFolder = PlayMoreSoundsCore.getFolder().resolve("Temp");
    private static final @NotNull Path tempAddonsZip = tempFolder.resolve("Addons.zip");
    private static final @NotNull Path tempAddonsFolder = tempFolder.resolve("Addons To Install");
    private static final boolean hasTitles;
    private static final @NotNull HashSet<HumanEntity> allInventories = new HashSet<>();
    private static URL releasesURL;

    static {
        try {
            releasesURL = new URL("https://api.github.com/repos/Epicnicity322/PlayMoreSounds/releases");
        } catch (MalformedURLException ignored) {
            // Will never happen...
        }

        // Player#sendTitle(String, String, int, int, int) was added on Spigot v1.11.1
        hasTitles = ReflectionUtil.getMethod(Player.class, "sendTitle", String.class, String.class, int.class, int.class, int.class) != null;
    }

    private final @NotNull Inventory inventory;
    private final @NotNull HashMap<Integer, Consumer<InventoryClickEvent>> buttons;
    private final @NotNull HashMap<Integer, ArrayList<PMSAddon>> addonPages;

    public AddonsInventory()
    {
        if (PlayMoreSounds.getInstance() == null) throw new IllegalStateException("PlayMoreSounds is not loaded.");

        HashSet<PMSAddon> addons = PlayMoreSounds.getAddonManager().getAddons();
        MessageSender lang = PlayMoreSounds.getLanguage();

        if (addons.isEmpty()) {
            addonPages = new HashMap<>(0);
            buttons = new HashMap<>(1);
            inventory = Bukkit.createInventory(null, 9, lang.getColored("Addons.Inventory.Title.Empty"));

            inventory.setItem(4, InventoryUtils.getItemStack("Addons.Inventory.Items.Install"));
            buttons.put(4, event -> openInstallerInventory((Player) event.getWhoClicked()));
            InventoryUtils.fillWithGlass(inventory, 0, 8);
        } else {
            addonPages = PMSHelper.splitIntoPages(addons, 36);
            int size = 18 + (addons.size() % 9 == 0 ? addons.size() : addons.size() + (9 - (addons.size() % 9)));

            if (size > 54) size = 54;

            buttons = new HashMap<>(1 + addons.size());
            inventory = Bukkit.createInventory(null, size, lang.getColored("Addons.Inventory.Title.Default"));

            ItemStack info = InventoryUtils.getItemStack("Addons.Inventory.Items.Info");
            ItemMeta infoMeta = info.getItemMeta();
            infoMeta.setLore(Arrays.asList(lang.getColored("Addons.Inventory.Items.Info.Lore").replace("<addons>", Integer.toString(addons.size())).split("<line>")));
            info.setItemMeta(infoMeta);

            inventory.setItem(0, info);
            inventory.setItem(8, InventoryUtils.getItemStack("Addons.Inventory.Items.Install"));
            buttons.put(8, event -> openInstallerInventory((Player) event.getWhoClicked()));
            fillAddons();
            InventoryUtils.fillWithGlass(inventory, 9, 17);
        }
    }

    /**
     * Goes through the assets {@link JSONArray} looking for the download url of Addons.zip file. PlayMoreSounds currently
     * has only PlayMoreSounds.jar and Addons.zip in assets, but I can't predict if I want to add more stuff in the future.
     */
    private static String findAddonsDownloadURL(JSONArray assets)
    {
        for (Object asset : assets) {
            JSONObject jsonAsset = (JSONObject) asset;

            if (!jsonAsset.get("name").equals("Addons.zip")) continue;

            return jsonAsset.get("browser_download_url").toString();
        }

        return null;
    }

    private static void openInstallerInventory(@NotNull Player player)
    {
        block.set(true);
        allInventories.forEach(HumanEntity::closeInventory);

        new Thread(() -> {
            MessageSender lang = PlayMoreSounds.getLanguage();

            try {
                downloadAddons(player, true);
                ZipUtils.extractZip(tempAddonsZip, tempAddonsFolder);

                if (unsupportedAddonsVersion()) {
                    lang.send(player, lang.get("Addons.Download.Unsupported Version"));
                    downloadAddons(player, false);
                }

                new AddonInstallerInventory(player);
            } catch (NullPointerException ignored) {
                // Error message already logged.
            } catch (Exception e) {
                lang.send(player, lang.get("Addons.Download.Error.Unknown"));
                PlayMoreSoundsCore.getErrorHandler().report(e, "Download Addons Exception:");
                block.set(false);
            }
        }, "Addon Downloader").start();
    }

    /**
     * Downloads addons info and zip files from PlayMoreSounds GitHub, sends the information in form of titles to the player.
     *
     * @param player The player to send information how download is going.
     * @param latest If the addons should be downloaded from latest tag, false for PlayMoreSounds current version.
     */
    private static void downloadAddons(@NotNull Player player, boolean latest) throws Exception
    {
        BukkitTask repeatingTitle = null;
        MessageSender lang = PlayMoreSounds.getLanguage();

        try {
            if (Files.notExists(tempFolder)) {
                Files.createDirectories(tempFolder);
            } else {
                if (Files.deleteIfExists(tempAddonsZip)) {
                    lang.send(player, lang.get("Addons.Download.Already Exists"));
                }
            }

            JSONObject releaseData = null;

            // Getting the github release information.
            if (hasTitles)
                repeatingTitle = Bukkit.getScheduler().runTaskTimer(PlayMoreSounds.getInstance(), () -> player.sendTitle(lang.getColored("Addons.Download.Downloading.Title"), lang.getColored("Addons.Download.Downloading.Subtitle.Info"), 5, 10, 5), 0, 25);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Downloader downloader = new Downloader(releasesURL, baos);
                downloader.run();

                if (downloader.getResult() != Downloader.Result.SUCCESS) {
                    if (hasTitles) {
                        repeatingTitle.cancel();
                        Bukkit.getScheduler().runTask(PlayMoreSounds.getInstance(), () -> player.sendTitle(lang.getColored("Addons.Download.Error.Title"), lang.getColored("Addons.Download.Error.Subtitle"), 10, 20, 10));
                    }
                    throw downloader.getException();
                }

                JSONArray releases = (JSONArray) new JSONParser().parse(baos.toString());

                if (latest) {
                    releaseData = (JSONObject) releases.get(0);
                } else {
                    for (Object tagObject : releases) {
                        JSONObject jsonTag = (JSONObject) tagObject;

                        if (jsonTag.get("tag_name").equals(PlayMoreSoundsVersion.version)) {
                            releaseData = jsonTag;
                            break;
                        }
                    }

                    if (releaseData == null) {
                        if (hasTitles) {
                            repeatingTitle.cancel();
                            Bukkit.getScheduler().runTask(PlayMoreSounds.getInstance(), () -> player.sendTitle(lang.getColored("Addons.Download.Error.Title"), lang.getColored("Addons.Download.Error.Subtitle"), 10, 20, 10));
                        }
                        lang.send(player, lang.get("Addons.Download.Error.Not Found").replace("<version>", PlayMoreSoundsVersion.version));
                        throw new NullPointerException();
                    }
                }
            }

            if (hasTitles) repeatingTitle.cancel();

            String addonsDownloadURL = findAddonsDownloadURL((JSONArray) releaseData.get("assets"));

            if (addonsDownloadURL == null) {
                if (hasTitles) {
                    Bukkit.getScheduler().runTask(PlayMoreSounds.getInstance(), () -> player.sendTitle(lang.getColored("Addons.Download.Error.Title"), lang.getColored("Addons.Download.Error.Subtitle"), 10, 20, 10));
                }
                lang.send(player, lang.get("Addons.Download.Error.Not Found").replace("<version>", PlayMoreSoundsVersion.version));
                throw new NullPointerException();
            }

            // Downloading addons zip to PlayMoreSounds data folder.
            if (hasTitles)
                repeatingTitle = Bukkit.getScheduler().runTaskTimer(PlayMoreSounds.getInstance(), () -> player.sendTitle(lang.getColored("Addons.Download.Downloading.Title"), lang.getColored("Addons.Download.Downloading.Subtitle.Files"), 5, 10, 5), 0, 25);
            try (FileOutputStream fos = new FileOutputStream(tempAddonsZip.toFile())) {
                Downloader downloader = new Downloader(new URL(addonsDownloadURL), fos);
                downloader.run();

                if (downloader.getResult() != Downloader.Result.SUCCESS) {
                    if (hasTitles) {
                        repeatingTitle.cancel();
                        Bukkit.getScheduler().runTask(PlayMoreSounds.getInstance(), () -> player.sendTitle(lang.getColored("Addons.Download.Error.Title"), lang.getColored("Addons.Download.Error.Subtitle"), 10, 20, 10));
                    }
                    throw downloader.getException();
                }

                if (hasTitles) {
                    repeatingTitle.cancel();
                    Bukkit.getScheduler().runTask(PlayMoreSounds.getInstance(), () -> player.sendTitle(lang.getColored("Addons.Download.Success.Title"), lang.getColored("Addons.Download.Success.Subtitle"), 10, 20, 10));
                }
            }
        } finally {
            if (repeatingTitle != null && !repeatingTitle.isCancelled()) repeatingTitle.cancel();
        }
    }

    /**
     * Checks if the downloaded addons' minimum version is greater than PlayMoreSounds' current version.
     *
     * @return If addons are supported on this PlayMoreSounds version.
     */
    private static boolean unsupportedAddonsVersion() throws IOException
    {
        return new Version(PathUtils.read(tempAddonsFolder.resolve(".version"))).compareTo(PlayMoreSoundsVersion.getVersion()) > 0;
    }

    private static String lastColor(String last)
    {
        String lore = PlayMoreSounds.getLanguage().getColored("Addons.Inventory.Items.Addon.Lore");
        int index = lore.lastIndexOf(last) - 1;
        if (index < 0) return "";
        return ChatColor.getLastColors(lore.substring(0, index));
    }

    private static String breakLore(String lore, int maxInFirstLine, String color)
    {
        StringBuilder builder = new StringBuilder();

        if (lore.length() > maxInFirstLine) {
            builder.append(lore, 0, maxInFirstLine).append('-');

            String rest = lore.substring(maxInFirstLine);
            int count = 0;
            while (rest.length() != 0) {
                //Max of three lines.
                if (++count == 3) {
                    // Forgive and append anyways if the rest is just 5 characters. Otherwise,
                    //appending "..." to the end to inform that there's more string and not enough space.
                    if (rest.length() < 5) {
                        builder = new StringBuilder(builder.substring(0, builder.length() - 1));
                        builder.append(rest);
                    } else {
                        builder = new StringBuilder(builder.substring(0, builder.length() - 3));
                        builder.append("...");
                    }
                    break;
                }

                builder.append("<line>").append(color);

                if (rest.length() > 35) {
                    builder.append(rest, 0, 35).append(builder.toString().endsWith(" ") ? ' ' : '-');
                    rest = rest.substring(35);
                } else {
                    builder.append(rest);
                    rest = "";
                }
            }
        } else {
            builder.append(lore);
        }

        return builder.toString();
    }

    private void fillAddons()
    {
        ArrayList<PMSAddon> addons = addonPages.get(1);
        MessageSender lang = PlayMoreSounds.getLanguage();

        if (addons == null) return;

        int slot = 18;

        for (PMSAddon addon : addons) {
            if (slot > 54) break;
            boolean toBeUninstalled = AddonsSubCommand.ADDONS_TO_UNINSTALL.contains(addon);
            AddonDescription description = addon.getDescription();
            ItemStack addonItem = InventoryUtils.getItemStack("Addons.Inventory.Items.Addon");
            ItemMeta meta = addonItem.getItemMeta();
            String name = addon.getDescription().getName();

            meta.setDisplayName(lang.getColored("Addons.Inventory.Items.Addon.Display Name").replace("<name>", name));
            meta.setLore(Arrays.asList(lang.getColored(toBeUninstalled ? "Addons.Inventory.Items.Addon.To be uninstalled lore" : "Addons.Inventory.Items.Addon.Lore").replace("<description>", breakLore(description.getDescription(), 22, lastColor("<description>"))).replace("<authors>", breakLore(description.getAuthors().toString(), 24, lastColor("<authors>"))).replace("<version>", breakLore(description.getVersion().getVersion(), 26, lastColor("<version>"))).split("<line>")));
            addonItem.setItemMeta(meta);

            inventory.setItem(slot, addonItem);

            if (toBeUninstalled) {
                buttons.put(slot, event -> {
                    AddonsSubCommand.ADDONS_TO_UNINSTALL.remove(addon);
                    event.getWhoClicked().closeInventory();
                    lang.send(event.getWhoClicked(), lang.get("Addons.Uninstall.Cancel").replace("<addon>", name));
                });
            } else {
                buttons.put(slot, event -> {
                    HumanEntity player = event.getWhoClicked();
                    HashSet<String> dependants = new HashSet<>();

                    for (PMSAddon a : PlayMoreSounds.getAddonManager().getAddons()) {
                        if (a.getDescription().getRequiredAddons().contains(name)) {
                            dependants.add(a.getDescription().getName());
                        }
                    }

                    if (!dependants.isEmpty()) {
                        player.closeInventory();
                        lang.send(player, lang.get(dependants.size() == 1 ? "Addons.Uninstall.Error.Dependants.Singular" : "Addons.Uninstall.Error.Dependants.Plural").replace("<dependants>", dependants.toString()).replace("<addon>", name));
                        return;
                    }

                    new ConfirmationInventory(
                            lang.getColored("Addons.Uninstall.Confirmation.Title").replace("<addon>", name),
                            () -> {
                                AddonsSubCommand.ADDONS_TO_UNINSTALL.add(addon);
                                lang.send(player, lang.get("Addons.Uninstall.Success").replace("<addon>", name));
                                //Closing for all viewers since the addon item needs to be changed.
                                InventoryUtils.closeInventory(inventory);
                            }, null).openInventory(player);
                });
            }
            slot++;
        }
    }

    public void openInventory(@NotNull HumanEntity humanEntity)
    {
        MessageSender lang = PlayMoreSounds.getLanguage();

        if (block.get()) {
            lang.send(humanEntity, lang.get("Addons.Error.Blocked"));
            return;
        }

        allInventories.add(humanEntity);
        InventoryUtils.openInventory(inventory, buttons, humanEntity);
    }

    @Override
    public @NotNull Inventory getInventory()
    {
        return inventory;
    }

    @Override
    public @NotNull HashMap<Integer, Consumer<InventoryClickEvent>> getButtons()
    {
        return buttons;
    }

    private static final class AddonInstallerInventory
    {
        private final @NotNull HashMap<Integer, Consumer<InventoryClickEvent>> buttons = new HashMap<>();
        private final @NotNull HashMap<Integer, ArrayList<Path>> addonPages;
        private final @NotNull LinkedHashMap<Path, List<String>> addons;
        private final @NotNull Inventory inventory;

        private AddonInstallerInventory(@NotNull HumanEntity humanEntity) throws IOException
        {
            addons = new LinkedHashMap<>();

            // Getting addon descriptions.
            try (Stream<Path> addonFolders = Files.list(tempAddonsFolder)) {
                //Collecting so IOException can be thrown.
                for (Path addonFolder : addonFolders.filter(Files::isDirectory).collect(Collectors.toList())) {
                    addons.put(addonFolder, Arrays.asList(ChatColor.translateAlternateColorCodes('&', PathUtils.read(addonFolder.resolve("description.txt"))).split("\n")));
                }
            }

            addonPages = PMSHelper.splitIntoPages(addons.keySet(), 45);

            int size = 9 + (addons.size() % 9 == 0 ? addons.size() : addons.size() + (9 - (addons.size() % 9)));

            if (size > 54) size = 54;

            inventory = Bukkit.createInventory(humanEntity, size, PlayMoreSounds.getLanguage().getColored("Addons.Inventory.Title.Installer"));

            inventory.setItem(size - 5, InventoryUtils.getItemStack("Addons.Inventory.Items.Done"));
            buttons.put(size - 5, event -> event.getWhoClicked().closeInventory());
            fillAddons();
            Bukkit.getScheduler().runTask(PlayMoreSounds.getInstance(), () -> InventoryUtils.openInventory(inventory, buttons, humanEntity, event -> {
                block.set(false);

                try {
                    Files.deleteIfExists(tempAddonsZip);
                    PathUtils.deleteAll(tempAddonsFolder);

                    try (Stream<Path> files = Files.list(tempFolder)) {
                        // Checking if temp folder is empty before deleting.
                        if (!files.findAny().isPresent()) {
                            Files.delete(tempFolder);
                        }
                    }
                } catch (Exception e) {
                    PlayMoreSoundsCore.getErrorHandler().report(e, "Temp Folder Delete Exception:");
                }
            }));
        }

        private void fillAddons()
        {
            MessageSender lang = PlayMoreSounds.getLanguage();
            int slot = -1;

            for (Path addon : addonPages.get(1)) {
                ItemStack addonItem = InventoryUtils.getItemStack("Addons.Inventory.Items.Addon");
                ItemMeta meta = addonItem.getItemMeta();

                meta.setDisplayName(lang.getColored("Addons.Inventory.Items.Addon.Display Name").replace("<name>", addon.getFileName().toString()));
                meta.setLore(addons.get(addon));
                addonItem.setItemMeta(meta);

                int finalSlot = ++slot;

                inventory.setItem(finalSlot, addonItem);
                buttons.put(finalSlot, event -> {
                    try (Stream<Path> jars = Files.list(addon)) {
                        Path addonsFolder = PlayMoreSoundsCore.getFolder().resolve("Addons");

                        if (Files.notExists(addonsFolder)) Files.createDirectories(addonsFolder);

                        // Collecting so IOException can be thrown.
                        for (Path jar : jars.filter(path -> path.toString().endsWith(".jar")).collect(Collectors.toList())) {
                            Files.move(jar, addonsFolder.resolve(jar.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                        }

                        lang.send(event.getWhoClicked(), lang.get("Addons.Install.Success").replace("<addon>", addon.getFileName().toString()));

                        buttons.put(finalSlot, clickAgain -> lang.send(clickAgain.getWhoClicked(), lang.get("Addons.Install.Installed").replace("<addon>", addon.getFileName().toString())));
                    } catch (IOException ex) {
                        lang.send(event.getWhoClicked(), lang.get("Addons.Install.Error").replace("<addon>", addon.getFileName().toString()));
                        PlayMoreSoundsCore.getErrorHandler().report(ex, "Addon: " + addon + "\nAddon Install Exception:");
                    }
                });
            }

            InventoryUtils.fillWithGlass(inventory, inventory.getSize() - 9, inventory.getSize() - 1);
        }
    }
}
