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
import com.epicnicity322.epicpluginlib.core.util.StringUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.CommandUtils;
import com.epicnicity322.playmoresounds.bukkit.gui.inventories.RegionSoundInventory;
import com.epicnicity322.playmoresounds.bukkit.listeners.OnPlayerInteract;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.util.UniqueRunnable;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsCore;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class RegionSubCommand extends Command implements Helpable {
    /**
     * Borders are quite heavy on performance so there is a maximum amount of borders that can be shown at the same time.
     */
    private static final @NotNull AtomicInteger showingBorders = new AtomicInteger(0);
    private static final ExecutorService regionExecutor = Executors.newSingleThreadExecutor();
    private final @NotNull PlayMoreSounds plugin;

    public RegionSubCommand(@NotNull PlayMoreSounds plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull CommandRunnable onHelp() {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, false, PlayMoreSounds.getLanguage().get("Help.Region").replace("<label>", label));
    }

    @Override
    public @NotNull String getName() {
        return "region";
    }

    @Override
    public @Nullable String[] getAliases() {
        return new String[]{"regions", "rg"};
    }

    @Override
    public @NotNull String getPermission() {
        return "playmoresounds.region";
    }

    @Override
    public int getMinArgsAmount() {
        return 2;
    }

    @Override
    protected @NotNull CommandRunnable getNoPermissionRunnable() {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, PlayMoreSounds.getLanguage().get("General.No Permission"));
    }

    @Override
    protected @NotNull CommandRunnable getNotEnoughArgsRunnable() {
        return (label, sender, args) -> PlayMoreSounds.getLanguage().send(sender, PlayMoreSounds.getLanguage().get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", "<create|info|list|remove|rename|set|teleport|wand>"));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args) {
        switch (args[1].toLowerCase()) {
            case "create", "new" -> {
                if (!CommandUtils.parsePermission(sender, "playmoresounds.region.create")) return;

                regionExecutor.execute(() -> create(label, sender, args));
            }
            case "info" -> {
                if (!CommandUtils.parsePermission(sender, "playmoresounds.region.info")) return;

                info(label, sender, args);
            }
            case "list", "l" -> {
                if (!CommandUtils.parsePermission(sender, "playmoresounds.region.list")) return;

                list(label, sender, args);
            }
            case "remove", "delete", "rm", "del" -> {
                if (!CommandUtils.parsePermission(sender, "playmoresounds.region.remove")) return;

                remove(label, sender, args);
            }
            case "rename", "newname", "setname", "rn" -> {
                if (!CommandUtils.parsePermission(sender, "playmoresounds.region.rename")) return;

                rename(label, sender, args);
            }
            case "set" -> {
                if (!CommandUtils.parsePermission(sender, "playmoresounds.region.description", "playmoresounds.region.select.command",
                        "playmoresounds.region.sound.enter", "playmoresounds.region.sound.leave", "playmoresounds.region.sound.loop"))
                    return;

                set(label, sender, args);
            }
            case "teleport", "tp" -> {
                if (!CommandUtils.parsePermission(sender, "playmoresounds.region.teleport")) return;

                teleport(label, sender, args);
            }
            case "wand", "tool", "wandtool", "selectiontool" -> {
                if (!CommandUtils.parsePermission(sender, "playmoresounds.region.wand")) return;

                wand(sender);
            }
            default -> getNotEnoughArgsRunnable().run(label, sender, args);
        }
    }

    private void create(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args) {
        var lang = PlayMoreSounds.getLanguage();
        UUID creator;

        if (sender instanceof Player player) creator = player.getUniqueId();
        else creator = null;

        Location[] selected = OnPlayerInteract.getSelectedDiagonals(creator);

        if (selected == null || selected[0] == null || selected[1] == null) {
            lang.send(sender, lang.get("Region.Create.Error.Not Selected").replace("<label>", label).replace("<label2>", args[0]));
            return;
        } else if (!selected[0].getWorld().equals(selected[1].getWorld())) {
            lang.send(sender, lang.get("Region.Create.Error.Different Worlds"));
            return;
        }

        String name;
        var config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();

        if (args.length > 2) {
            name = args[2];

            if (RegionManager.getRegions().stream().anyMatch(region -> region.getName().equalsIgnoreCase(name))) {
                lang.send(sender, lang.get("Region.Create.Error.Already Exists"));
                return;
            }
            if (!SoundRegion.ALLOWED_REGION_NAME_CHARS.matcher(name).matches()) {
                lang.send(sender, lang.get("Region.Create.Error.Illegal Characters"));
                return;
            }

            int maxCharacters = config.getNumber("Sound Regions.Max Name Characters").orElse(20).intValue();

            if (name.length() > maxCharacters) {
                lang.send(sender, lang.get("Region.Create.Error.Max Name Characters").replace("<max>", Integer.toString(maxCharacters)));
                return;
            }
        } else name = PMSHelper.getRandomString(8);

        String description;

        if (args.length > 3 && sender.hasPermission("playmoresounds.region.description")) {
            var builder = new StringBuilder();

            for (int i = 3; i < args.length; ++i)
                builder.append(" ").append(args[i]);

            description = builder.toString().trim();
        } else description = lang.getColored("Region.Create.Default Description");

        var region = new SoundRegion(name, selected[0], selected[1], creator, description);

        // Checking if the player exceeds the max created regions specified on config.
        if (!sender.hasPermission("playmoresounds.region.create.unlimited.regions")) {
            long amount = RegionManager.getRegions().stream().filter(soundRegion -> Objects.equals(soundRegion.getCreator(), creator)).count();
            long maxAmount = config.getNumber("Sounds Regions.Max Regions").orElse(5).longValue();

            if (amount >= maxAmount) {
                lang.send(sender, lang.get("Region.Create.Error.Max Regions").replace("<max>", Long.toString(maxAmount)));
                return;
            }
        }

        // Checking if the region area is bigger than the specified on config.
        if (!sender.hasPermission("playmoresounds.region.create.unlimited.area")) {
            var min = region.getMinDiagonal();
            var max = region.getMaxDiagonal();

            long xSize = max.getBlockX() - min.getBlockX();
            long ySize = max.getBlockY() - min.getBlockY();
            long zSize = max.getBlockZ() - min.getBlockZ();

            long maxArea = config.getNumber("Sound Regions.Max Area").orElse(15625).longValue();

            if (xSize * ySize * zSize > maxArea) {
                lang.send(sender, lang.get("Region.Create.Error.Max Area").replace("<max>", Long.toString(maxArea)));
                return;
            }
        }

        // Checking if any block of the selected area is inside another already existing region.
        if (!sender.hasPermission("playmoresounds.region.select.overlap")) {
            var min = region.getMinDiagonal();
            var max = region.getMaxDiagonal();

            // Filtering so it can check only the regions that are on this world and the regions that are not owned by the sender.
            Set<SoundRegion> regionsOnWorld = RegionManager.getRegions().stream().filter(otherRegion -> !Objects.equals(otherRegion.getCreator(), creator) && otherRegion.getMaxDiagonal().getWorld().equals(max.getWorld())).collect(Collectors.toSet());

            for (int x = min.getBlockX(); x <= max.getBlockX(); ++x)
                for (int y = min.getBlockY(); y <= max.getBlockY(); ++y)
                    for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z)
                        for (SoundRegion otherRegion : regionsOnWorld) {
                            var otherMin = otherRegion.getMinDiagonal();
                            var otherMax = otherRegion.getMaxDiagonal();

                            if (x >= otherMin.getBlockX() && x <= otherMax.getBlockX() & y >= otherMin.getBlockY() && y <= otherMax.getBlockY() & z >= otherMin.getBlockZ() && z <= otherMax.getBlockZ()) {
                                lang.send(sender, lang.get("Region.Select.Error.Overlap"));
                                return;
                            }
                        }
        }

        RegionManager.add(region);
        lang.send(sender, lang.get("Region.Create.Success").replace("<name>", name).replace("<label>", label).replace("<label2>", args[0]));
        OnPlayerInteract.selectDiagonal(creator, null, true);
        OnPlayerInteract.selectDiagonal(creator, null, false);
    }

    private void info(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args) {
        var lang = PlayMoreSounds.getLanguage();
        var config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();
        Set<SoundRegion> regions;

        if (args.length > 2) {
            regions = new HashSet<>();
            var region = getRegion(args[2], sender, null);

            if (region == null) {
                lang.send(sender, lang.get("Region.General.Error.Not Found." + (args[2].contains("-") ? "UUID" : "Name")).replace("<label>", label).replace("<label2>", args[0]));
                return;
            }

            regions.add(region);
        } else {
            if (sender instanceof Player player) {
                var location = player.getLocation();
                regions = RegionManager.getRegionsAt(location);

                if (regions.isEmpty()) {
                    lang.send(sender, lang.get("Region.Info.Error.No Regions"));
                    return;
                }
            } else {
                lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " <" + lang.get("Region.Region") + ">"));
                return;
            }
        }

        var random = new Random();

        for (SoundRegion region : regions) {
            // Checking if particles should be sent.
            if (sender instanceof Player player && showingBorders.get() < config.getNumber("Sound Regions.Border.Max Showing Borders").orElse(10).intValue()) {
                int particleCount;
                double r, g, b;

                // If there is more than one region, each region must have a different particle color.
                if (regions.size() == 1) {
                    particleCount = 1;
                    r = 0;
                    g = 0;
                    b = 0;
                } else {
                    particleCount = 0;
                    r = random.nextDouble();
                    g = random.nextDouble();
                    b = random.nextDouble();
                }

                showingBorders.incrementAndGet();

                BukkitTask repeatingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    for (Location border : region.getBorder())
                        player.spawnParticle(Particle.NOTE, border, particleCount, r, g, b);
                }, 0, 5);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    repeatingTask.cancel();
                    showingBorders.decrementAndGet();
                }, config.getNumber("Sound Regions.Border.Showing Time").orElse(140).longValue());
            }

            lang.send(sender, lang.get("Region.Info.Header").replace("<name>", region.getName()));
            lang.send(sender, false, lang.get("Region.Info.Owner").replace("<owner>", findOwner(region.getCreator())));
            lang.send(sender, false, lang.get("Region.Info.Id").replace("<uuid>", region.getId().toString()));
            lang.send(sender, false, lang.get("Region.Info.World").replace("<world>", region.getMaxDiagonal().getWorld().getName()));
            lang.send(sender, false, lang.get("Region.Info.Creation Date").replace("<date>", region.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))));
            lang.send(sender, false, lang.get("Region.Info.Description").replace("<description>", Objects.requireNonNullElse(region.getDescription(), "null")));
        }
    }

    /**
     * @return "CONSOLE" if the creator is null, the player's name if one with this {@link UUID} was found, or the {@link UUID}
     * itself if no player was found.
     */
    private @NotNull String findOwner(@Nullable UUID creator) {
        if (creator == null) return "CONSOLE";

        var player = Bukkit.getOfflinePlayer(creator);

        return Objects.requireNonNullElse(player.getName(), creator.toString());
    }

    private Set<SoundRegion> getSenderRegions(CommandSender sender) {
        if (sender instanceof Player player) return RegionManager.getRegionsOf(player.getUniqueId());
        else return RegionManager.getRegionsOf(null);
    }

    private void list(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args) {
        var lang = PlayMoreSounds.getLanguage();

        Set<SoundRegion> regions;
        String who;

        // Getting the regions.
        if (args.length > 2) {
            String specific = args[2];
            if (specific.equalsIgnoreCase("me") || specific.equalsIgnoreCase(sender.getName())) {
                regions = getSenderRegions(sender);
                who = null;
            } else {
                if (!sender.hasPermission("playmoresounds.region.list.others")) {
                    lang.send(sender, lang.get("Region.List.Error.Others"));
                    return;
                }

                if (specific.equalsIgnoreCase("console")) {
                    regions = RegionManager.getRegionsOf(null);
                    who = "CONSOLE";
                } else {
                    OfflinePlayer player;

                    if (PlayMoreSoundsCore.isPaper()) {
                        player = Bukkit.getOfflinePlayerIfCached(specific);
                    } else {
                        player = Bukkit.getPlayer(specific);
                    }

                    if (player == null) {
                        lang.send(sender, lang.get("General.Player Not Found").replace("<player>", specific));
                        return;
                    } else {
                        regions = RegionManager.getRegionsOf(player.getUniqueId());
                        who = Objects.equals(player.getName(), sender.getName()) ? null : player.getName();
                    }
                }
            }
        } else {
            regions = getSenderRegions(sender);
            who = null;
        }

        if (regions.isEmpty()) {
            lang.send(sender, lang.get("Region.List.Error.No Regions").replace("<targets>", who == null ? lang.get("General.You") : who));
            return;
        }

        int page = 1;

        if (args.length > 3) {
            try {
                page = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                lang.send(sender, lang.get("General.Not A Number").replace("<number>", args[3]));
                return;
            }
        }

        if (page < 1) page = 1;

        HashMap<Integer, ArrayList<SoundRegion>> pages = PMSHelper.splitIntoPages(regions, 5);

        if (page > pages.size()) {
            lang.send(sender, lang.get("Region.List.Error.Not Exists").replace("<page>", Integer.toString(page)).replace("<totalPages>", Integer.toString(pages.size())));
            return;
        }

        if (who == null) {
            lang.send(sender, lang.get("Region.List.Header.Default").replace("<page>", Integer.toString(page)).replace("<totalPages>", Integer.toString(pages.size())));
        } else {
            lang.send(sender, lang.get("Region.List.Header.Player").replace("<targets>", who).replace("<page>", Integer.toString(page)).replace("<totalPages>", Integer.toString(pages.size())));
        }

        for (SoundRegion region : pages.get(page))
            lang.send(sender, false, lang.get("Region.List.Region").replace("<uuid>", region.getId().toString()).replace("<name>", region.getName()));

        if (page != pages.size()) {
            lang.send(sender, false, lang.get("Region.List.Footer").replace("<label>", label).replace("<label2>", args[0]).replace("<label3>", args[1]).replace("<label4>", args.length > 2 ? args[2] : "me").replace("<next>", Integer.toString(page + 1)));
        }
    }

    private void remove(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args) {
        var lang = PlayMoreSounds.getLanguage();
        if (args.length < 3) {
            lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " <name|uuid>"));
            return;
        }

        var region = getRegion(args[2], sender, "playmoresounds.region.remove.others");

        if (region == null) {
            lang.send(sender, lang.get("Region.General.Error.Not Found." + (args[2].contains("-") ? "UUID" : "Name")).replace("<label>", label).replace("<label2>", args[0]));
            return;
        }

        var name = region.getName();

        lang.send(sender, lang.get("Region.Remove.Confirm").replace("<label>", label).replace("<region>", name));

        ConfirmSubCommand.addPendingConfirmation(sender, new UniqueRunnable(region.getId()) {
            @Override
            public void run() {
                RegionManager.remove(region);
                lang.send(sender, lang.get("Region.Remove.Success").replace("<region>", name));
            }
        }, lang.get("Region.Remove.Description").replace("<region>", name));
    }

    private void rename(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args) {
        var lang = PlayMoreSounds.getLanguage();
        if (args.length < 4) {
            lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " <" + lang.get("Region.Region") + "> <" + lang.get("Region.Rename.New Name") + ">"));
            return;
        }

        String oldName = args[2];
        String newName = args[3];

        if (oldName.equals(newName)) {
            lang.send(sender, lang.get("Region.Rename.Error.Same"));
            return;
        }

        if (RegionManager.getRegions().stream().anyMatch(region -> region.getName().equalsIgnoreCase(newName))) {
            lang.send(sender, lang.get("Region.Rename.Error.Already Exists"));
            return;
        }

        var region = getRegion(oldName, sender, "playmoresounds.region.rename.others");

        if (region == null) {
            lang.send(sender, lang.get("Region.General.Error.Not Found." + (oldName.contains("-") ? "UUID" : "Name")).replace("<label>", label).replace("<label2>", args[0]));
            return;
        }

        // Fixing case.
        oldName = region.getName();

        if (!SoundRegion.ALLOWED_REGION_NAME_CHARS.matcher(newName).matches()) {
            lang.send(sender, lang.get("Region.General.Error.Illegal Characters"));
            return;
        }

        var config = Configurations.CONFIG.getConfigurationHolder().getConfiguration();
        int maxCharacters = config.getNumber("Sound Regions.Max Name Characters").orElse(20).intValue();

        if (newName.length() > maxCharacters) {
            lang.send(sender, lang.get("Region.General.Error.Max Name Characters").replace("<max>", Integer.toString(maxCharacters)));
            return;
        }

        region.setName(newName);
        lang.send(sender, lang.get("Region.Rename.Success").replace("<region>", oldName).replace("<newName>", newName));
    }

    private void set(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args) {
        var lang = PlayMoreSounds.getLanguage();
        if (args.length < 3) {
            lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " <p1|p2|description>"));
            return;
        }

        final boolean p1;

        switch (args[2].toLowerCase()) {
            case "description", "desc" -> {
                if (!CommandUtils.parsePermission(sender, "playmoresounds.region.description")) {
                    lang.send(sender, lang.get("General.No Permission"));
                }

                if (args.length < 5) {
                    lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " " + args[2] + " <" + lang.get("Region.Region") + "> <" + lang.get("General.Description") + ">"));
                    return;
                }

                var region = getRegion(args[3], sender, "playmoresounds.region.description.others");

                if (region == null) {
                    lang.send(sender, lang.get("Region.General.Error.Not Found." + (args[3].contains("-") ? "UUID" : "Name")).replace("<label>", label).replace("<label2>", args[0]));
                    return;
                }

                var descriptionBuilder = new StringBuilder();

                for (int i = 4; i < args.length; ++i) {
                    descriptionBuilder.append(" ").append(args[i]);
                }

                var description = descriptionBuilder.substring(1);

                if (description.length() > 100) {
                    lang.send(sender, lang.get("Region.Set.Description.Error.Max Characters"));
                    return;
                }

                region.setDescription(description);
                lang.send(sender, lang.get("Region.Set.Description.Success").replace("<region>", region.getName()).replace("<description>", description));
                return;
            }
            case "sounds", "sound" -> {
                if (!CommandUtils.parsePermission(sender, "playmoresounds.region.sound.enter", "playmoresounds.region.sound.leave", "playmoresounds.region.sound.loop")) {
                    return;
                }

                sounds(label, sender, args);
                return;
            }
            case "p1", "pone", "one", "position1", "positionone", "firstposition", "first" -> p1 = true;
            case "p2", "ptwo", "two", "position2", "positiontwo", "secondposition", "second" -> p1 = false;
            default -> {
                lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " <p1|p2|description|sound>"));
                return;
            }
        }

        if (!CommandUtils.parsePermission(sender, "playmoresounds.region.select.command")) {
            return;
        }

        Location location;

        if (args.length < 7) {
            if (sender instanceof Player player) {
                location = player.getLocation();
            } else {
                lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " " + args[2] + " <" + lang.get("General.World") + "> <x> <y> <z>"));
                return;
            }
        } else {
            try {
                var world = Bukkit.getWorld(args[3]);

                if (world == null) {
                    lang.send(sender, lang.get("Region.Set.Select.Error.Not A World").replace("<value>", args[3]));
                    return;
                }

                location = new Location(world, Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.parseDouble(args[6]));
            } catch (NumberFormatException ex) {
                var notNumber = "";

                if (!StringUtils.isNumeric(args[4])) notNumber = args[4];
                else if (!StringUtils.isNumeric(args[5])) notNumber = args[5];
                else if (!StringUtils.isNumeric(args[6])) notNumber = args[6];

                lang.send(sender, lang.get("General.Not A Number").replace("<number>", notNumber));
                return;
            }
        }

        UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        OnPlayerInteract.selectDiagonal(uuid, location, p1);
        lang.send(sender, lang.get("Region.Set.Select.Position." + (p1 ? "First" : "Second")).replace("<w>", location.getWorld().getName()).replace("<x>", Integer.toString(location.getBlockX())).replace("<y>", Integer.toString(location.getBlockY())).replace("<z>", Integer.toString(location.getBlockZ())));
    }

    private void teleport(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args) {
        var lang = PlayMoreSounds.getLanguage();
        if (!(sender instanceof Player)) {
            lang.send(sender, lang.get("General.Not A Player"));
            return;
        }
        if (args.length < 3) {
            lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " <" + lang.get("Region.Region") + ">"));
            return;
        }

        var region = getRegion(args[2], sender, "playmoresounds.region.teleport.others");

        if (region == null) {
            lang.send(sender, lang.get("Region.General.Error.Not Found." + (args[2].contains("-") ? "UUID" : "Name")).replace("<label>", label).replace("<label2>", args[0]));
            return;
        }

        lang.send(sender, lang.get("Region.Teleport.Success").replace("<region>", region.getName()));
        ((Player) sender).teleport(region.getMinDiagonal(), PlayerTeleportEvent.TeleportCause.COMMAND);
    }

    private void wand(@NotNull CommandSender sender) {
        var lang = PlayMoreSounds.getLanguage();
        if (!(sender instanceof Player)) {
            lang.send(sender, lang.get("General.Not A Player"));
            return;
        }

        var wand = RegionManager.getWand();

        ((Player) sender).getInventory().addItem(wand);
        lang.send(sender, lang.get("Region.Wand.Success"));
    }

    private void sounds(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args) {
        var lang = PlayMoreSounds.getLanguage();

        if (!(sender instanceof Player player)) {
            lang.send(sender, lang.get("General.Not A Player"));
            return;
        }

        final SoundRegion region;
        boolean multipleFound = false;

        if (args.length > 3) {
            region = getRegion(args[3], sender, "playmoresounds.region.sound.others");

            if (region == null) {
                lang.send(sender, lang.get("Region.General.Error.Not Found." + (args[3].contains("-") ? "UUID" : "Name")).replace("<label>", label).replace("<label2>", args[0]));
                return;
            }
        } else {
            Set<SoundRegion> locationRegions = RegionManager.getRegionsAt(player.getLocation());

            if (locationRegions.isEmpty()) {
                lang.send(sender, lang.get("Region.Set.Sounds.Error.No Regions").replace("<label>", label).replace("<label2>", args[0]).replace("<label4>", args[2]));
                return;
            }

            if (!sender.hasPermission("playmoresounds.region.sound.others")) {
                locationRegions.removeIf(otherRegion -> !Objects.equals(otherRegion.getCreator(), player.getUniqueId()));

                if (locationRegions.isEmpty()) {
                    lang.send(sender, lang.get("Region.Set.Sounds.Error.No Owning Regions"));
                    return;
                }
            }

            region = locationRegions.iterator().next();
            if (locationRegions.size() > 1) multipleFound = true;
        }

        lang.send(sender, lang.get("Region.Set.Sounds.Editing." + (multipleFound ? "Multiple" : "Default")).replace("<region>", region.getName()));
        new RegionSoundInventory(region, player).openInventory();
    }

    /**
     * Gets a region by its name or {@link UUID}.
     *
     * @param nameOrUUID The name or uuid of the region.
     * @param sender     The owner of the region.
     * @param permission The permission if this player is allowed to get other peoples regions.
     * @return The region with this name or uuid, or null if not found.
     */
    private SoundRegion getRegion(@NotNull String nameOrUUID, @NotNull CommandSender sender, @Nullable String permission) {
        UUID creator = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        if (creator == null) permission = null;
        // Checking if nameOrUUID is an uuid since region names cannot contain '-'.
        boolean checkUUID = nameOrUUID.contains("-");

        // Only players with the permission are allowed to get regions made by other players.
        if (permission == null || sender.hasPermission(permission)) {
            for (var rg : RegionManager.getRegions())
                if (checkUUID ? rg.getId().toString().equalsIgnoreCase(nameOrUUID) : rg.getName().equalsIgnoreCase(nameOrUUID))
                    return rg;
        } else {
            for (var rg : RegionManager.getRegions())
                if (Objects.equals(rg.getCreator(), creator))
                    if (checkUUID ? rg.getId().toString().equalsIgnoreCase(nameOrUUID) : rg.getName().equalsIgnoreCase(nameOrUUID))
                        return rg;
        }
        return null;
    }
}
