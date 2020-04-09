package com.epicnicity322.playmoresounds.bukkit.sound;

import com.epicnicity322.epicpluginlib.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;

public class RelativeLocationSetter
{
    public static final HashMap<String, RelativeLocationSetter> waitingSneak = new HashMap<>();
    public static NamespacedKey locked;
    public static Scoreboard scoreboard;
    public static Team greenTeam;
    private final HashSet<Runnable> onSneakRunnables = new HashSet<>();
    private MessageSender lang;
    private Player player;
    private BukkitRunnable runnable;
    private Location relativeLocation;
    private Location sourceLocation;
    private ArmorStand relative;
    private ArmorStand source;

    public RelativeLocationSetter(Player player)
    {
        if (!PlayMoreSounds.HAS_PERSISTENT_DATA_CONTAINER)
            throw new IllegalStateException("This version of MC is not supported.");

        if (PlayMoreSounds.getPlugin() == null)
            throw new IllegalStateException("PlayMoreSounds isn't enabled.");

        if (PlayMoreSounds.CONFIG == null)
            throw new IllegalStateException("Configuration isn't loaded.");

        if (PlayMoreSounds.MESSAGE_SENDER == null)
            throw new IllegalStateException("Language isn't loaded.");

        if (locked == null)
            locked = new NamespacedKey(PlayMoreSounds.getPlugin(), "locked");

        if (scoreboard == null) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();

            scoreboard = manager.getMainScoreboard();
        }

        lang = PlayMoreSounds.MESSAGE_SENDER;
        this.player = player;
    }

    public void start()
    {
        Location playerLocation = player.getLocation();
        Location sourceLocation = new Location(playerLocation.getWorld(), playerLocation.getBlockX() + 0.5,
                playerLocation.getBlockY(), playerLocation.getBlockZ() + 0.5, 0, 0);

        source = (ArmorStand) player.getWorld().spawnEntity(sourceLocation, EntityType.ARMOR_STAND);

        source.setInvulnerable(true);
        source.setGravity(false);
        source.setBasePlate(false);
        source.getPersistentDataContainer().set(locked, PersistentDataType.STRING, "true");
        source.setCustomName(lang.getColored("Relative Location Setter.Sound Source"));
        source.setCustomNameVisible(true);
        source.setGlowing(true);

        EntityEquipment equipment = source.getEquipment();
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        meta.setOwningPlayer(player);
        head.setItemMeta(meta);

        equipment.setHelmet(head);
        equipment.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        equipment.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        equipment.setBoots(new ItemStack(Material.LEATHER_BOOTS));

        relative = (ArmorStand) playerLocation.getWorld().spawnEntity(new Location(playerLocation.getWorld(),
                        playerLocation.getX(), playerLocation.getY() - 0.4, playerLocation.getZ(), 0, 0),
                EntityType.ARMOR_STAND);

        relative.setInvulnerable(true);
        relative.getPersistentDataContainer().set(locked, PersistentDataType.STRING, "true");
        relative.setBasePlate(false);
        relative.setVisible(false);
        relative.setSmall(true);
        relative.getEquipment().setHelmet(new ItemStack(Material.NOTE_BLOCK));
        relative.setCustomName(lang.getColored("Relative Location Setter.Where To Play"));
        relative.setCustomNameVisible(true);
        relative.setGravity(false);

        runnable = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (player.isSneaking()) {
                    sneak();
                } else {
                    if (relative != null && player.isOnline() && player.getWorld() == sourceLocation.getWorld()) {
                        Location playerLocation = player.getLocation().clone().subtract(0.0, 0.4, 0.0);

                        playerLocation.setPitch(0);
                        playerLocation.setYaw(0);
                        relative.teleport(playerLocation);
                    } else {
                        stop();
                    }
                }
            }
        };

        runnable.runTaskTimer(PlayMoreSounds.getPlugin(), 0, 1);

        if (greenTeam == null) {
            // Registering PlayMoreSounds Relative Location Setter Green Team for Green Glowing effect on source armor stand
            if (scoreboard.getTeam("PMSRLS GreenTeam") == null) {
                greenTeam = scoreboard.registerNewTeam("PMSRLS GreenTeam");
            } else {
                greenTeam = scoreboard.getTeam("PMSRLS GreenTeam");
            }

            greenTeam.setColor(ChatColor.DARK_GREEN);
        }

        greenTeam.addEntry(source.getUniqueId().toString());
        waitingSneak.put(player.getName(), this);
    }

    public void stop()
    {
        waitingSneak.remove(player.getName());

        if (source != null) {
            greenTeam.removeEntry(source.getUniqueId().toString());
            source.remove();
            source = null;
        }
        if (relative != null) {
            relative.remove();
            relative = null;
        }
        if (runnable != null) {
            runnable.cancel();
            runnable = null;
        }
        if (greenTeam != null) {
            greenTeam.unregister();
            greenTeam = null;
        }
    }

    /**
     * Set a runnable that will run when the player sneaks.
     *
     * @param runnable The runnable to run.
     */
    public void setOnSneakRunnable(@NotNull Runnable runnable)
    {
        Validate.notNull(runnable);
        onSneakRunnables.add(runnable);
    }

    private void sneak()
    {
        if (source != null)
            sourceLocation = source.getLocation();

        if (relative != null)
            relativeLocation = relative.getLocation();

        stop();

        for (Runnable runnable : onSneakRunnables)
            runnable.run();
    }

    @Nullable
    public Location getRelativeLocation()
    {
        return relativeLocation;
    }

    @Nullable
    public Location getSourceLocation()
    {
        return sourceLocation;
    }
}
