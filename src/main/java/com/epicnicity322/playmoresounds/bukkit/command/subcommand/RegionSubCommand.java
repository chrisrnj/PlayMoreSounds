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
import com.epicnicity322.epicpluginlib.core.config.PluginConfig;
import com.epicnicity322.epicpluginlib.core.util.StringUtils;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.CommandUtils;
import com.epicnicity322.playmoresounds.bukkit.listener.OnPlayerInteract;
import com.epicnicity322.playmoresounds.bukkit.region.RegionManager;
import com.epicnicity322.playmoresounds.bukkit.region.SoundRegion;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class RegionSubCommand extends Command implements Helpable
{
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();
    private static final @NotNull Pattern allowedRegionNameChars = Pattern.compile("^[A-Za-z0-9_]+$");
    private static final @NotNull PluginConfig config = Configurations.CONFIG.getPluginConfig();
    private static final @NotNull AtomicInteger showingBorders = new AtomicInteger(0);

    @Override
    public @NotNull CommandRunnable onHelp()
    {
        return (label, sender, args) -> lang.send(sender, false, lang.get("Help.Region").replace("<label>", label));
    }

    @Override
    public @NotNull String getName()
    {
        return "region";
    }

    @Override
    public @Nullable String[] getAliases()
    {
        return new String[]{"regions", "rg"};
    }

    @Override
    public @Nullable String getPermission()
    {
        return "playmoresounds.region";
    }

    @Override
    public int getMinArgsAmount()
    {
        return 2;
    }

    @Override
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return (label, sender, args) -> lang.send(sender, lang.get("General.No Permission"));
    }

    @Override
    protected @Nullable CommandRunnable getNotEnoughArgsRunnable()
    {
        return (label, sender, args) -> lang.send(sender, lang.get("General.Invalid Arguments")
                .replace("<label>", label).replace("<label2>", args[0])
                .replace("<args>", "<create|info|list|remove|rename|set|teleport|wand>"));
    }

    @Override
    public void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        switch (args[1].toLowerCase()) {
            case "create":
            case "new":
                if (!sender.hasPermission("playmoresounds.region.create")) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return;
                }

                new Thread(() -> create(label, sender, args), "Region Builder").start();
                break;
            case "info":
            case "description":
                if (!sender.hasPermission("playmoresounds.region.info")) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return;
                }

                info(label, sender, args);
                break;
            case "list":
            case "l":
                if (!sender.hasPermission("playmoresounds.region.list")) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return;
                }

                list(label, sender, args);
                break;
            case "remove":
            case "delete":
            case "rm":
            case "del":
                if (!sender.hasPermission("playmoresounds.region.remove")) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return;
                }

                remove(label, sender, args);
                break;
            case "rename":
            case "newname":
            case "setname":
            case "rn":
                if (!sender.hasPermission("playmoresounds.region.rename")) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return;
                }

                rename(label, sender, args);
                break;
            case "set":
                if (!sender.hasPermission("playmoresounds.region.description") && !sender.hasPermission("playmoresounds.region.select.command")) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return;
                }

                set(label, sender, args);
                break;
            case "teleport":
            case "tp":
                if (!sender.hasPermission("playmoresounds.region.teleport")) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return;
                }

                teleport(label, sender, args);
                break;
            case "wand":
            case "tool":
            case "wandtool":
            case "selectiontool":
                if (!sender.hasPermission("playmoresounds.region.wand")) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return;
                }

                wand(sender);
                break;
            default:
                getNotEnoughArgsRunnable().run(label, sender, args);
                break;
        }
    }

    private void create(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        UUID creator;

        if (sender instanceof Player)
            creator = ((Player) sender).getUniqueId();
        else
            creator = null;

        Location[] selected = OnPlayerInteract.getSelectedDiagonals(creator);

        if (selected == null || selected[0] == null || selected[1] == null) {
            lang.send(sender, lang.get("Region.Create.Error.Not Selected").replace("<label>", label)
                    .replace("<label2>", args[0]));
            return;
        } else if (!selected[0].getWorld().equals(selected[1].getWorld())) {
            lang.send(sender, lang.get("Region.Create.Error.Different Worlds"));
            return;
        }

        String name;
        Configuration config = Configurations.CONFIG.getPluginConfig().getConfiguration();

        if (args.length > 2) {
            name = args[2];

            if (RegionManager.getAllRegions().stream().anyMatch(region -> region.getName().equalsIgnoreCase(name))) {
                lang.send(sender, lang.get("Region.Create.Error.Already Exists"));
                return;
            }

            if (!allowedRegionNameChars.matcher(name).matches()) {
                lang.send(sender, lang.get("Region.Create.Error.Illegal Characters"));
                return;
            }

            int maxCharacters = config.getNumber("Sound Regions.Max Name Characters").orElse(20).intValue();

            if (name.length() > maxCharacters) {
                lang.send(sender, lang.get("Region.Create.Error.Max Name Characters").replace("<max>", Integer.toString(maxCharacters)));
                return;
            }
        } else
            name = PMSHelper.getRandomString(8);

        String description;

        if (args.length > 3) {
            StringBuilder builder = new StringBuilder();

            for (int i = 3; i < args.length; ++i)
                builder.append(" ").append(args[i]);

            description = builder.toString().trim();
        } else
            description = lang.getColored("Region.Create.Default Description");

        SoundRegion region = new SoundRegion(name, selected[0], selected[1], creator, description);

        // Checking if the player exceeds the max created regions specified on config.
        if (sender instanceof Player) {
            if (!sender.hasPermission("playmoresounds.region.create.unlimited.regions")) {
                UUID playerId = ((Player) sender).getUniqueId();
                long amount = RegionManager.getAllRegions().stream().filter(soundRegion -> soundRegion.getCreator().equals(playerId)).count();
                long maxAmount = config.getNumber("Sounds Regions.Max Regions").orElse(5).longValue();

                if (amount >= maxAmount) {
                    lang.send(sender, lang.get("Region.Create.Error.Max Regions").replace("<max>", Long.toString(maxAmount)));
                    return;
                }
            }
        }

        // Checking if the region area is bigger than the specified on config.
        if (!sender.hasPermission("playmoresounds.region.create.unlimited.area")) {
            Location min = region.getMinDiagonal();
            Location max = region.getMaxDiagonal();

            int xSize = max.getBlockX() - min.getBlockX();
            int ySize = max.getBlockY() - min.getBlockY();
            int zSize = max.getBlockZ() - min.getBlockZ();

            long maxArea = config.getNumber("Sound Regions.Max Area").orElse(1000000).longValue();

            if (xSize * ySize * zSize > maxArea) {
                lang.send(sender, lang.get("Region.Create.Error.Max Area").replace("<max>", Long.toString(maxArea)));
                return;
            }
        }

        // Checking if any block of the selected area is inside another already existing region.
        if (!sender.hasPermission("playmoresounds.region.select.overlap")) {
            Location min = region.getMinDiagonal();
            Location max = region.getMaxDiagonal();
            UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;

            // Filtering so it can check only the regions that are on this world and the regions that are not owned by the sender.
            Set<SoundRegion> regionsOnWorld = RegionManager.getAllRegions().stream().filter(otherRegion ->
                    !Objects.equals(otherRegion.getCreator(), uuid) && otherRegion.getMaxDiagonal().getWorld().equals(max.getWorld())).collect(Collectors.toSet());

            for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
                for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                    for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                        for (SoundRegion otherRegion : regionsOnWorld) {
                            Location otherMin = otherRegion.getMinDiagonal();
                            Location otherMax = otherRegion.getMaxDiagonal();

                            if (x >= otherMin.getBlockX() && x <= otherMax.getBlockX() &
                                    y >= otherMin.getBlockY() && y <= otherMax.getBlockY() &
                                    z >= otherMin.getBlockZ() && z <= otherMax.getBlockZ()) {
                                lang.send(sender, lang.get("Region.Select.Error.Overlap"));
                                return;
                            }
                        }
                    }
                }
            }
        }

        try {
            region.save();
            lang.send(sender, lang.get("Region.Create.Success").replace("<name>", name));
            OnPlayerInteract.selectDiagonal(creator, null, true);
            OnPlayerInteract.selectDiagonal(creator, null, false);
        } catch (IOException e) {
            lang.send(sender, lang.get("Region.Create.Error.Default").replace("<name>", name));
            PlayMoreSounds.getErrorLogger().report(e, "Error while creating region \"" + name + "\":");
        }
    }

    private void info(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        Set<SoundRegion> regions;

        if (args.length > 2) {
            regions = new HashSet<>();

            SoundRegion region = getRegion(args[2], sender, null);

            if (region == null) {
                lang.send(sender, lang.get("Region.General.Error.Not Found." + (args[2].contains("-") ? "UUID" : "Name"))
                        .replace("<label>", label).replace("<label2>", args[0]));
                return;
            }

            regions.add(region);
        } else {
            if (sender instanceof Player) {
                Location location = ((Player) sender).getLocation();

                regions = RegionManager.getAllRegions().stream().filter(region -> region.isInside(location)).collect(Collectors.toSet());

                if (regions.isEmpty()) {
                    lang.send(sender, lang.get("Region.Info.Error.No Regions"));
                    return;
                }
            } else {
                lang.send(sender, lang.get("General.Invalid Arguments")
                        .replace("<label>", label).replace("<label2>", args[0])
                        .replace("<args>", "<" + lang.get("Region.Region") + ">"));
                return;
            }
        }

        Random random = new Random();

        for (SoundRegion region : regions) {
            // Checking if particles should be sent.
            if (VersionUtils.hasOffHand() && (sender instanceof Player) && showingBorders.get() < config.getConfiguration().getNumber("Sound Regions.Border.Max Showing Borders").orElse(30).intValue()) {
                int count;
                double r, g, b;

                // If the player is standing on multiple regions, they should make different color particles.
                if (regions.size() == 1) {
                    count = 1;
                    r = 0;
                    g = 0;
                    b = 0;
                } else {
                    count = 0;
                    r = random.nextDouble();
                    g = random.nextDouble();
                    b = random.nextDouble();
                }

                showingBorders.incrementAndGet();

                BukkitTask task = Bukkit.getScheduler().runTaskTimer(PlayMoreSounds.getInstance(), () -> {
                    for (Location border : region.getBorder())
                        ((Player) sender).spawnParticle(Particle.NOTE, border, count, r, g, b);
                }, 0, 5);

                Bukkit.getScheduler().runTaskLater(PlayMoreSounds.getInstance(), () -> {
                    task.cancel();
                    showingBorders.decrementAndGet();
                }, config.getConfiguration().getNumber("Sound Regions.Border.Showing Time").orElse(100).longValue());
            }

            lang.send(sender, lang.get("Region.Info.Header").replace("<name>", region.getName()));
            lang.send(sender, false, lang.get("Region.Info.Owner").replace("<owner>", Bukkit.getOfflinePlayer(region.getCreator()).getName()));
            lang.send(sender, false, lang.get("Region.Info.Id").replace("<uuid>", region.getId().toString()));
            lang.send(sender, false, lang.get("Region.Info.World").replace("<world>", region.getMaxDiagonal().getWorld().getName()));
            lang.send(sender, false, lang.get("Region.Info.Creation Date").replace("<date>", region.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))));
            lang.send(sender, false, lang.get("Region.Info.Description").replace("<description>", region.getDescription()));
        }
    }

    private void list(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        Set<SoundRegion> regions;
        String who;

        if (args.length > 2) {
            HashSet<Player> targets;

            if (sender instanceof Player) {
                targets = CommandUtils.getTargets(sender, args, 2, "", "playmoresounds.region.list.others");

                if (targets == null)
                    return;

                who = CommandUtils.getWho(targets, sender);
            } else {
                String target = args[2].toLowerCase();

                if (target.equals("me") || target.equals("self") || target.equals("myself")) {
                    targets = new HashSet<>();

                    who = lang.get("General.You");
                } else {
                    targets = CommandUtils.getTargets(sender, args, 2, "", "playmoresounds.region.list.others");

                    if (targets == null)
                        return;

                    who = CommandUtils.getWho(targets, sender);
                }
            }

            HashSet<UUID> uuidTargets = new HashSet<>();

            targets.forEach(target -> uuidTargets.add(target.getUniqueId()));

            regions = RegionManager.getAllRegions().stream().filter(region -> {
                if (targets.isEmpty())
                    return region.getCreator() == null;
                else
                    return uuidTargets.contains(region.getCreator());
            }).collect(Collectors.toSet());
        } else {
            if (sender instanceof Player) {
                UUID id = ((Player) sender).getUniqueId();

                regions = RegionManager.getAllRegions().stream().filter(region -> region.getCreator().equals(id)).collect(Collectors.toSet());
            } else {
                regions = RegionManager.getAllRegions().stream().filter(region -> region.getCreator() == null).collect(Collectors.toSet());
            }

            who = lang.get("General.You");
        }

        if (regions.isEmpty()) {
            lang.send(sender, lang.get("Region.List.Error.No Regions").replace("<targets>", who));
            return;
        }

        long page = 1;

        if (args.length > 3) {
            if (StringUtils.isNumeric(args[3])) {
                page = Long.parseLong(args[3]);
            } else {
                lang.send(sender, lang.get("General.Not A Number").replace("<number>", args[3]));
                return;
            }
        }

        if (page < 1)
            page = 1;

        HashMap<Long, ArrayList<SoundRegion>> pages = PMSHelper.splitIntoPages(regions, 5);

        if (page > pages.size()) {
            lang.send(sender, lang.get("Region.List.Error.Not Exists").replace("<page>", Long.toString(page)).replace("<totalPages>", Integer.toString(pages.size())));
            return;
        }

        if (who.equals(lang.get("General.You")))
            lang.send(sender, lang.get("Region.List.Header.Default").replace("<page>", Long.toString(page)).replace("<totalPages>", Integer.toString(pages.size())));
        else
            lang.send(sender, lang.get("Region.List.Header.Player").replace("<targets>", who).replace("<page>", Long.toString(page)).replace("<totalPages>", Integer.toString(pages.size())));

        for (SoundRegion region : pages.get(page))
            lang.send(sender, false, lang.get("Region.List.Region").replace("<uuid>", region.getId().toString()).replace("<name>", region.getName()));

        if (page < pages.size())
            lang.send(sender, false, lang.get("Region.List.Footer").replace("<label>", label).replace("<label2>", args[0]).replace("<label3>", args[1]).replace("<label4>", args.length > 2 ? args[2] : "me").replace("<next>", Long.toString(page + 1)));
    }

    private void remove(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length < 3) {
            lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " <name|uuid>"));
            return;
        }

        SoundRegion region = getRegion(args[2], sender, "playmoresounds.region.remove.others");

        if (region == null) {
            lang.send(sender, lang.get("Region.General.Error.Not Found." + (args[2].contains("-") ? "UUID" : "Name"))
                    .replace("<label>", label).replace("<label2>", args[0]));
            return;
        }

        String name = region.getName();

        lang.send(sender, lang.get("Region.Remove.Confirm").replace("<label>", label).replace("<region>", name));

        ConfirmSubCommand.addPendingConfirmation(sender, () -> {
            try {
                region.delete();
                lang.send(sender, lang.get("Region.Remove.Success").replace("<region>", name));
            } catch (Exception ex) {
                lang.send(sender, lang.get("Region.Remove.Error").replace("<region>", name));
                PlayMoreSounds.getErrorLogger().report(ex, "Error while deleting region " + name);
            }
        }, lang.get("Region.Remove.Description").replace("<region>", name));
    }

    private void rename(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
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

        if (RegionManager.getAllRegions().stream().anyMatch(region -> region.getName().equalsIgnoreCase(newName))) {
            lang.send(sender, lang.get("Region.Rename.Error.Already Exists"));
            return;
        }

        SoundRegion region = getRegion(oldName, sender, "playmoresounds.region.rename.others");

        if (region == null) {
            lang.send(sender, lang.get("Region.General.Error.Not Found." + (oldName.contains("-") ? "UUID" : "Name"))
                    .replace("<label>", label).replace("<label2>", args[0]));
            return;
        }

        // Fixing case.
        oldName = region.getName();

        if (!allowedRegionNameChars.matcher(newName).matches()) {
            lang.send(sender, lang.get("Region.General.Error.Illegal Characters"));
            return;
        }

        Configuration config = Configurations.CONFIG.getPluginConfig().getConfiguration();
        int maxCharacters = config.getNumber("Sound Regions.Max Name Characters").orElse(20).intValue();

        if (newName.length() > maxCharacters) {
            lang.send(sender, lang.get("Region.General.Error.Max Name Characters").replace("<max>", Integer.toString(maxCharacters)));
            return;
        }

        region.setName(newName);

        try {
            region.save();
            lang.send(sender, lang.get("Region.Rename.Success").replace("<region>", oldName).replace("<newName>", newName));
        } catch (Exception ex) {
            lang.send(sender, lang.get("Region.General.Error.Save").replace("<region>", region.getName()));
            PlayMoreSounds.getErrorLogger().report(ex, region.getName() + " Region Save Error:");
        }
    }

    private void set(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length < 3) {
            lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " <p1|p2|description>"));
            return;
        }

        boolean p1 = false;

        switch (args[2].toLowerCase()) {
            case "description":
            case "desc":
            case "info":
            case "information":
                if (!sender.hasPermission("playmoresounds.region.description")) {
                    lang.send(sender, lang.get("General.No Permission"));
                    return;
                }

                if (args.length < 5) {
                    lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " description <" + lang.get("Region.Region") + "> <" + lang.get("General.Description") + ">"));
                    return;
                }

                SoundRegion region = getRegion(args[3], sender, "playmoresounds.region.description.others");

                if (region == null) {
                    lang.send(sender, lang.get("Region.General.Error.Not Found." + (args[3].contains("-") ? "UUID" : "Name"))
                            .replace("<label>", label).replace("<label2>", args[0]));
                    return;
                }

                StringBuilder description = new StringBuilder();

                for (int i = 4; i < args.length; ++i) {
                    description.append(" ").append(args[i]);
                }

                String string = description.substring(1);

                if (string.length() > 100) {
                    lang.send(sender, lang.get("Region.Set.Description.Error.Max Characters"));
                    return;
                }

                region.setDescription(string);
                try {
                    region.save();
                    lang.send(sender, lang.get("Region.Set.Description.Success").replace("<region>", region.getName()).replace("<description>", string));
                } catch (Exception ex) {
                    lang.send(sender, lang.get("Region.General.Error.Save").replace("<region>", region.getName()));
                    PlayMoreSounds.getErrorLogger().report(ex, region.getName() + " Region Save Error:");
                }

                return;
            case "p1":
            case "pone":
            case "one":
            case "position1":
            case "positionone":
            case "firstposition":
            case "first":
                p1 = true;
                break;
            case "p2":
            case "ptwo":
            case "two":
            case "position2":
            case "positiontwo":
            case "secondposition":
            case "second":
                break;
            default:
                lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " <p1|p2|description>"));
                return;
        }

        if (!sender.hasPermission("playmoresounds.region.select.command")) {
            lang.send(sender, lang.get("General.No Permission"));
            return;
        }

        Location location;

        if (args.length < 7) {
            if (sender instanceof Player) {
                location = ((Player) sender).getLocation();
            } else {
                lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " " + args[2] + " <" + lang.get("General.World") + "> <x> <y> <z>"));
                return;
            }
        } else {
            try {
                World world = Bukkit.getWorld(args[3]);

                if (world == null) {
                    lang.send(sender, lang.get("Region.Set.Select.Error.Not A World").replace("<value>", args[3]));
                    return;
                }

                location = new Location(world, Integer.valueOf(args[4]).doubleValue(), Integer.valueOf(args[5]).doubleValue(), Integer.valueOf(args[6]).doubleValue());
            } catch (NumberFormatException ex) {
                String notNumber = "";

                if (!StringUtils.isNumeric(args[4]))
                    notNumber = args[4];
                if (!StringUtils.isNumeric(args[5]))
                    notNumber = args[5];
                if (!StringUtils.isNumeric(args[6]))
                    notNumber = args[6];

                lang.send(sender, lang.get("General.Not A Number").replace("<number>", notNumber));
                return;
            }
        }

        UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        OnPlayerInteract.selectDiagonal(uuid, location, p1);
        lang.send(sender, lang.get("Region.Set.Select.Position." + (p1 ? "First" : "Second"))
                .replace("<w>", location.getWorld().getName())
                .replace("<x>", Integer.toString(location.getBlockX()))
                .replace("<y>", Integer.toString(location.getBlockY()))
                .replace("<z>", Integer.toString(location.getBlockZ())));
    }

    private void teleport(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args)
    {
        if (!(sender instanceof Player)) {
            lang.send(sender, lang.get("General.Not A Player"));
            return;
        }
        if (args.length < 3) {
            lang.send(sender, lang.get("General.Invalid Arguments").replace("<label>", label).replace("<label2>", args[0]).replace("<args>", args[1] + " <" + lang.get("Region.Region") + ">"));
            return;
        }

        SoundRegion region = getRegion(args[2], sender, "playmoresounds.region.teleport.others");

        if (region == null) {
            lang.send(sender, lang.get("Region.General.Error.Not Found." + (args[2].contains("-") ? "UUID" : "Name"))
                    .replace("<label>", label).replace("<label2>", args[0]));
            return;
        }

        lang.send(sender, lang.get("Region.Teleport.Success").replace("<region>", region.getName()));
        ((Player) sender).teleport(region.getMinDiagonal(), PlayerTeleportEvent.TeleportCause.COMMAND);
    }

    private void wand(@NotNull CommandSender sender)
    {
        if (!(sender instanceof Player)) {
            lang.send(sender, lang.get("General.Not A Player"));
            return;
        }

        ItemStack wand = RegionManager.getWand();

        if (wand == null) {
            lang.send(sender, lang.get("Region.Wand.Error.Config"));
            return;
        }

        ((Player) sender).getInventory().addItem(wand);
        lang.send(sender, lang.get("Region.Wand.Success"));
    }

    private SoundRegion getRegion(String nameOrUUID, CommandSender sender, String permission)
    {
        SoundRegion region = null;
        UUID creator = sender instanceof Player ? ((Player) sender).getUniqueId() : null;

        for (SoundRegion soundRegion : RegionManager.getAllRegions()) {
            if (permission == null || sender.hasPermission(permission) || Objects.equals(soundRegion.getCreator(), creator)) {
                // Checking if nameOrUUID is an uuid since region names cannot contain '-'.
                if (nameOrUUID.contains("-") ? soundRegion.getId().toString().equalsIgnoreCase(nameOrUUID)
                        : soundRegion.getName().equalsIgnoreCase(nameOrUUID)) {
                    region = soundRegion;
                    break;
                }
            }
        }

        return region;
    }
}
