package com.epicnicity322.playmoresounds.bukkit.region;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegionManager
{
    public static boolean isOldRegion(Path file)
    {
        if (file == null) {
            return false;
        }
        if (!file.getFileName().endsWith(".yml")) {
            return false;
        }

        try {
            FileConfiguration conf = YamlConfiguration.loadConfiguration(file.toFile());

            if (!conf.contains("Name")) {
                return false;
            }
            if (!conf.contains("World")) {
                return false;
            }
            if (!conf.contains("Locations.P1.X")) {
                return false;
            }
            if (!conf.contains("Locations.P1.Y")) {
                return false;
            }
            if (!conf.contains("Locations.P1.Z")) {
                return false;
            }
            if (!conf.contains("Locations.P2.X")) {
                return false;
            }
            if (!conf.contains("Locations.P2.Y")) {
                return false;
            }
            if (!conf.contains("Locations.P2.Z")) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean isNewRegion(Path file)
    {
        if (!isOldRegion(file)) {
            return false;
        }

        try {
            FileConfiguration conf = YamlConfiguration.loadConfiguration(file.toFile());

            if (!conf.contains("CreationDate")) {
                return false;
            }
            if (!conf.contains("Creator")) {
                return false;
            }
            if (!conf.contains("Description")) {
                return false;
            }
            if (!conf.contains("Owner")) {
                return false;
            }
            if (!conf.contains("TeleportPoint.X")) {
                return false;
            }
            if (!conf.contains("TeleportPoint.Y")) {
                return false;
            }
            if (!conf.contains("TeleportPoint.Z")) {
                return false;
            }
            if (!conf.contains("TeleportPoint.Pitch")) {
                return false;
            }
            if (!conf.contains("TeleportPoint.Yaw")) {
                return false;
            }
            if (!conf.contains("ZoneId")) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean isRegion(String name)
    {
        for (SoundRegion region : getAllRegions()) {
            if (region.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public static ItemStack getWand()
    {
        boolean glowing = PMSHelper.getConfig("config").getBoolean("Sound Regions.Wand.Glowing");
        String name = PMSHelper.getConfig("config").getString("Sound Regions.Wand.Name");
        String material = PMSHelper.getConfig("config").getString("Sound Regions.Wand.Material");

        ItemStack item = new ItemStack(Material.valueOf(material), 1);

        if (glowing) {
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }

        ItemMeta meta = item.getItemMeta();

        if (glowing) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        item.setItemMeta(meta);

        return item;
    }

    /**
     * Checks if {@link Location} is part of a {@link SoundRegion}.
     *
     * @param loc The location to check if is part of a region
     */
    public static boolean isInRegion(Location loc)
    {
        Path folder = PlayMoreSounds.DATA_FOLDER.resolve("Regions");

        if (Files.exists(folder)) {
            try (Stream<Path> stream = Files.list(folder)) {
                for (Path file : stream.collect(Collectors.toCollection(HashSet::new))) {
                    if (isNewRegion(file)) {
                        FileConfiguration conf = YamlConfiguration.loadConfiguration(file.toFile());

                        Location P1 = new Location(Bukkit.getWorld(conf.getString("World")),
                                conf.getDouble("Locations.P1.X"), conf.getDouble("Locations.P1.Y"),
                                conf.getDouble("Locations.P1.Z"));
                        Location P2 = new Location(Bukkit.getWorld(conf.getString("World")),
                                conf.getDouble("Locations.P2.X"), conf.getDouble("Locations.P2.Y"),
                                conf.getDouble("Locations.P2.Z"));

                        int x1 = Math.min(P1.getBlockX(), P2.getBlockX());
                        int y1 = Math.min(P1.getBlockY(), P2.getBlockY());
                        int z1 = Math.min(P1.getBlockZ(), P2.getBlockZ());
                        int x2 = Math.max(P1.getBlockX(), P2.getBlockX());
                        int y2 = Math.max(P1.getBlockY(), P2.getBlockY());
                        int z2 = Math.max(P1.getBlockZ(), P2.getBlockZ());

                        if (loc.getBlockX() >= x1 & loc.getBlockX() <= x2 & loc.getBlockY() >= y1 & loc.getBlockY() <= y2
                                & loc.getBlockZ() >= z1 & loc.getBlockZ() <= z2) {
                            return true;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isInDenyPoint(Location loc)
    {
        Path folder = PlayMoreSounds.DATA_FOLDER.resolve("Data").resolve("Deny Points");

        if (Files.exists(folder)) {
            try (Stream<Path> stream = Files.list(folder)) {
                for (Path file : stream.collect(Collectors.toCollection(HashSet::new))) {
                    if (file.getFileName().toString().endsWith(".yml")) {
                        FileConfiguration conf = YamlConfiguration.loadConfiguration(file.toFile());

                        if (conf.contains("Locations")) {
                            Location P1 = new Location(Bukkit.getWorld(conf.getString("World")),
                                    conf.getDouble("Locations.P1.X"), conf.getDouble("Locations.P1.Y"),
                                    conf.getDouble("Locations.P1.Z"));
                            Location P2 = new Location(Bukkit.getWorld(conf.getString("World")),
                                    conf.getDouble("Locations.P2.X"), conf.getDouble("Locations.P2.Y"),
                                    conf.getDouble("Locations.P2.Z"));

                            int x1 = Math.min(P1.getBlockX(), P2.getBlockX());
                            int y1 = Math.min(P1.getBlockY(), P2.getBlockY());
                            int z1 = Math.min(P1.getBlockZ(), P2.getBlockZ());
                            int x2 = Math.max(P1.getBlockX(), P2.getBlockX());
                            int y2 = Math.max(P1.getBlockY(), P2.getBlockY());
                            int z2 = Math.max(P1.getBlockZ(), P2.getBlockZ());

                            if (loc.getBlockX() >= x1 & loc.getBlockX() <= x2 & loc.getBlockY() >= y1 & loc.getBlockY() <= y2
                                    & loc.getBlockZ() >= z1 & loc.getBlockZ() <= z2) {
                                return true;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Returns the {@link SoundRegion} that is in this location.
     *
     * @param loc The {@link Location} that the region is in.
     * @throws NullPointerException Just so the developer knows that this can be a
     *                              null value.
     */
    public static SoundRegion getRegion(Location loc) throws NullPointerException
    {
        for (SoundRegion region : getAllRegions()) {
            Location P1 = region.getPosition1();
            Location P2 = region.getPosition2();

            int x1 = Math.min(P1.getBlockX(), P2.getBlockX());
            int y1 = Math.min(P1.getBlockY(), P2.getBlockY());
            int z1 = Math.min(P1.getBlockZ(), P2.getBlockZ());
            int x2 = Math.max(P1.getBlockX(), P2.getBlockX());
            int y2 = Math.max(P1.getBlockY(), P2.getBlockY());
            int z2 = Math.max(P1.getBlockZ(), P2.getBlockZ());

            if (loc.getBlockX() >= x1 & loc.getBlockX() <= x2 & loc.getBlockY() >= y1 & loc.getBlockY() <= y2
                    & loc.getBlockZ() >= z1 & loc.getBlockZ() <= z2) {
                return region;
            }
        }

        return null;
    }

    public static SoundRegion getRegion(String name) throws NullPointerException
    {
        for (SoundRegion region : getAllRegions()) {
            if (region.getName().equals(name)) {
                return region;
            }
        }

        return null;
    }

    public static HashSet<SoundRegion> getAllRegions()
    {
        Path folder = PlayMoreSounds.DATA_FOLDER.resolve("Regions");
        HashSet<SoundRegion> regions = new HashSet<>();

        if (Files.exists(folder)) {
            try (Stream<Path> stream = Files.list(folder)) {
                for (Path file : stream.collect(Collectors.toCollection(HashSet::new))) {
                    if (isNewRegion(file)) {
                        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file.toFile());

                        regions.add(new SoundRegion(conf, file));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return regions;
    }

    public static HashSet<SoundRegion> getRegionsOfOwner(String owner)
    {
        Path folder = PlayMoreSounds.DATA_FOLDER.resolve("Regions");
        HashSet<SoundRegion> regions = new HashSet<>();

        if (Files.exists(folder)) {
            try (Stream<Path> stream = Files.list(folder)) {
                for (Path file : stream.collect(Collectors.toCollection(HashSet::new))) {
                    if (isNewRegion(file)) {
                        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file.toFile());

                        SoundRegion region = new SoundRegion(conf, file);

                        if (region.getOwnerName().equals(owner)) {
                            regions.add(region);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return regions;
    }

    public static HashSet<DenyPoint> getDenyPoints()
    {
        HashSet<DenyPoint> set = new HashSet<>();
        Path folder = PlayMoreSounds.DATA_FOLDER.resolve("Data").resolve("Deny Points");

        if (Files.exists(folder)) {
            try (Stream<Path> stream = Files.list(folder)) {
                for (Path file : stream.collect(Collectors.toCollection(HashSet::new))) {
                    if (file.getFileName().toString().endsWith(".yml")) {
                        FileConfiguration conf = YamlConfiguration.loadConfiguration(file.toFile());

                        if (conf.contains("Locations")) {
                            Location p1 = new Location(Bukkit.getWorld(conf.getString("World")),
                                    conf.getConfigurationSection("Locations").getConfigurationSection("P1").getDouble("X"),
                                    conf.getConfigurationSection("Locations").getConfigurationSection("P1").getDouble("Y"),
                                    conf.getConfigurationSection("Locations").getConfigurationSection("P1").getDouble("Z"));
                            Location p2 = new Location(Bukkit.getWorld(conf.getString("World")),
                                    conf.getConfigurationSection("Locations").getConfigurationSection("P2").getDouble("X"),
                                    conf.getConfigurationSection("Locations").getConfigurationSection("P2").getDouble("Y"),
                                    conf.getConfigurationSection("Locations").getConfigurationSection("P2").getDouble("Z"));

                            set.add(new DenyPoint(file, p1, p2));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return set;
    }
}
