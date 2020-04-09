package com.epicnicity322.playmoresounds.bukkit.inventory;

import com.epicnicity322.epicpluginlib.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.Sound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundOptions;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class SoundInventory implements PMSInventory
{
    public static final HashMap<String, SoundInventory> openInventories = new HashMap<>();
    public static NamespacedKey button;
    private Sound sound;
    private Inventory inventory;
    private Material materialOfIdItem;
    private RichSoundInventory parent;
    private int count = 0;

    public SoundInventory(@Nullable Sound sound, @NotNull RichSoundInventory parent, @NotNull Material parentItem)
    {
        if (!PlayMoreSounds.HAS_PERSISTENT_DATA_CONTAINER)
            throw new IllegalStateException("This version of MC is not supported.");

        if (PlayMoreSounds.getPlugin() == null)
            throw new IllegalStateException("PlayMoreSounds isn't enabled.");

        if (PlayMoreSounds.CONFIG == null)
            throw new IllegalStateException("Configuration isn't loaded.");

        if (PlayMoreSounds.MESSAGE_SENDER == null)
            throw new IllegalStateException("Language isn't loaded.");

        if (button == null)
            button = new NamespacedKey(PlayMoreSounds.getPlugin(), "button");

        Validate.notNull(parent, "parent is null");
        Validate.notNull(parentItem, "parentItem is null");

        this.sound = sound;
        this.parent = parent;
        materialOfIdItem = parentItem;

        MessageSender lang = PlayMoreSounds.MESSAGE_SENDER;
        String title;

        if (sound == null)
            title = lang.getColored("Editor.GUI.Sound.New.Title");
        else
            title = lang.getColored("Editor.GUI.Sound.Default.Title")
                    .replace("<name>", sound.getName());

        inventory = Bukkit.createInventory(null, 54, title);

        animate();
        inventory.setItem(4, getItemStack(Items.ID));
        inventory.setItem(18, getItemStack(Items.SOUND));
        inventory.setItem(20, getItemStack(Items.VOLUME));
        inventory.setItem(22, getItemStack(Items.PITCH));
        inventory.setItem(24, getItemStack(Items.DELAY));
        inventory.setItem(26, getItemStack(Items.RELATIVE_LOCATION));
        inventory.setItem(27, getItemStack(Items.RADIUS));
        inventory.setItem(29, getItemStack(Items.PERMISSION_REQUIRED));
        inventory.setItem(31, getItemStack(Items.IGNORES_TOGGLE));
        inventory.setItem(33, getItemStack(Items.PERMISSION_TO_LISTEN));

        if (sound == null || !sound.getOptions().isEyeLocation())
            inventory.setItem(35, getItemStack(Items.FEET_LOCATION));
        else {
            inventory.setItem(35, getItemStack(Items.EYE_LOCATION));
        }

        inventory.setItem(48, getItemStack(Items.CANCEL));
        inventory.setItem(50, getItemStack(Items.DONE));

        HashSet<Integer> decorationPos = new HashSet<>(Arrays.asList(0, 1, 2, 3, 5, 6, 7, 8, 45, 46, 47, 49, 51, 52, 53));
        ItemStack decorationItem = getItemStack(Items.DECORATION);

        for (int i : decorationPos) {
            inventory.setItem(i, decorationItem);
        }
    }

    @Override
    public PMSInventoryItem[] getItems()
    {
        return Items.values();
    }

    @Override
    public ItemStack getItemStack(PMSInventoryItem item)
    {
        FileConfiguration config = PMSHelper.getConfig("config");
        MessageSender lang = PlayMoreSounds.MESSAGE_SENDER;
        ItemStack itemStack = new ItemStack(Material.MUSIC_DISC_13);

        try {
            if (item.getId().equals("SEPARATOR")) {
                List<String> materials = config.getStringList(item.getConfigPath() + ".Material");

                if (count >= materials.size()) {
                    count = 0;
                }

                itemStack = new ItemStack(Material.valueOf(materials.get(count)));
                ++count;
            } else if (item.getId().equals("ID")) {
                if (config.getString(item.getConfigPath() + ".Material").equalsIgnoreCase("parent")) {
                    itemStack = new ItemStack(materialOfIdItem);
                } else {
                    itemStack = new ItemStack(Material.valueOf(config.getString(item.getConfigPath() + ".Material")));
                }
            } else {
                itemStack = new ItemStack(Material.valueOf(config.getString(item.getConfigPath() + ".Material")));
            }
        } catch (IllegalArgumentException ex) {
            PlayMoreSounds.LOGGER.log("&cYour config.yml has an invalid Material in the path '" +
                    item.getConfigPath() + "'. Sound Editor GUI could not be created.", Level.WARNING);
            PlayMoreSounds.ERROR_LOGGER.report(ex, "Invalid material in config.yml at " + item.getConfigPath() + ":");
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (sound == null || ((SoundInventory.Items) item).isGeneral()) {
            if (item.getId().equals("SEPARATOR") || item.getId().equals("DECORATION")) {
                itemMeta.setDisplayName(" ");
            } else {
                itemMeta.setDisplayName(lang.getColored(item.getLangPath() + ".Display Name"));

                if (parent.getRichSound() != null && (item.getId().equals("CANCEL") || item.getId().equals("DONE"))) {
                    itemMeta.setLore(Arrays.asList(lang.getColored(item.getLangPath() + ".Lore Back")
                            .replace("<parent>", parent.getRichSound().getName())
                            .split(Pattern.quote("<line>"))));
                } else {
                    itemMeta.setLore(Arrays.asList(lang.getColored(item.getLangPath() + ".Lore").split(Pattern.quote("<line>"))));
                }

                itemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING, item.getId());
            }
        } else {
            String target = "";
            String replacement = "";

            switch (item.getId()) {
                case "ID":
                    target = "<id>";
                    replacement = sound.getId();
                    break;
                case "IGNORES_TOGGLE":
                    target = "<value>";
                    replacement = Boolean.toString(sound.getOptions().ignoresToggle());
                    break;
                case "RADIUS":
                    target = "<radius>";
                    replacement = Double.toString(sound.getOptions().getRadius());
                    break;
                case "PERMISSION_REQUIRED":
                    target = "<permissionrequired>";
                    replacement = sound.getOptions().getPermissionRequired();
                    break;
                case "PERMISSION_TO_LISTEN":
                    target = "<permissiontolisten>";
                    replacement = sound.getOptions().getPermissionToListen();
                    break;
                case "SOUND":
                    target = "<sound>";
                    replacement = sound.getSound();
                    break;
                case "DELAY":
                    target = "<delay>";
                    replacement = Long.toString(sound.getDelay());
                    break;
                case "VOLUME":
                    target = "<volume>";
                    replacement = Float.toString(sound.getVolume());
                    break;
                case "PITCH":
                    target = "<pitch>";
                    replacement = Float.toString(sound.getPitch());
                    break;
                case "RELATIVE_LOCATION":
                    target = null;

                    Map<SoundOptions.Direction, Double> directions = sound.getOptions().getRelativeLocation();

                    itemMeta.setLore(Arrays.asList(lang.getColored("Editor.GUI.Sound.Default.Relative Location.Lore")
                            .replace("<up>", Double.toString(directions.getOrDefault(SoundOptions.Direction.UP, 0.0)))
                            .replace("<back>", Double.toString(directions.getOrDefault(SoundOptions.Direction.BACK, 0.0)))
                            .replace("<front>", Double.toString(directions.getOrDefault(SoundOptions.Direction.FRONT, 0.0)))
                            .replace("<back>", Double.toString(directions.getOrDefault(SoundOptions.Direction.BACK, 0.0)))
                            .replace("<right>", Double.toString(directions.getOrDefault(SoundOptions.Direction.RIGHT, 0.0)))
                            .replace("<left>", Double.toString(directions.getOrDefault(SoundOptions.Direction.LEFT, 0.0)))
                            .split(Pattern.quote("<line>"))));
                    break;
            }

            itemMeta.setDisplayName(lang.getColored("Editor.GUI.Sound.Default." + item.getName() + ".Display Name")
                    .replace(target, replacement).replace("<parent>", parent.getRichSound().getName()));

            if (target != null) {
                itemMeta.setLore(Arrays.asList(lang.getColored("Editor.GUI.Sound.Default." + item.getName() + ".Lore")
                        .replace(target, replacement).split(Pattern.quote("<line>"))));
            }

            itemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING, item.getId() +
                    (replacement.equals("") ? "" : " " + replacement));
        }

        itemMeta.addItemFlags(ItemFlag.values());

        if (config.getBoolean(item.getConfigPath() + ".Glowing")) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    /**
     * Gets the inventory with all the items in place. However, the separators won't be animated.
     *
     * @return The rich sound editor inventory
     * @see #openInventory(Player)
     */
    @Override
    public Inventory getInventory()
    {
        return inventory;
    }

    /**
     * Opens and animates the inventory.
     *
     * @param player The player to open the inventory to.
     */
    public void openInventory(@NotNull Player player)
    {
        SoundInventory si = this;
        player.openInventory(getInventory());
        openInventories.put(player.getName(), si);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (openInventories.containsKey(player.getName())) {
                    si.animate();
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(PlayMoreSounds.getPlugin(), 0, 10);
    }

    public Sound getSound()
    {
        return sound;
    }

    public RichSoundInventory getParent()
    {
        return parent;
    }

    public void animate()
    {
        HashSet<Integer> separatorPos = new HashSet<>(Arrays.asList(9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 21, 23, 25,
                28, 30, 32, 34, 36, 37, 38, 39, 40, 41, 42, 43, 44));
        ItemStack separatorItem = getItemStack(Items.SEPARATOR);

        for (int i : separatorPos) {
            inventory.setItem(i, separatorItem);
        }
    }

    public enum Items implements PMSInventoryItem
    {
        ID("Id"),
        SEPARATOR("Separator"),
        IGNORES_TOGGLE("Ignores Toggle"),
        DECORATION("Decoration"),
        RADIUS("Radius"),
        EYE_LOCATION("Eye Location"),
        FEET_LOCATION("Feet Location"),
        RELATIVE_LOCATION("Relative Location"),
        PERMISSION_REQUIRED("Permission Required"),
        PERMISSION_TO_LISTEN("Permission To Listen"),
        SOUND("Sound"),
        DELAY("Delay"),
        VOLUME("Volume"),
        PITCH("Pitch"),
        DONE("Done"),
        CANCEL("Cancel");

        private String name;
        private String configPath;
        private String langPath;
        private boolean general = false;

        Items(String name)
        {
            this.name = name;
            this.configPath = "Inventories.Sound Editor." + name + " Item";

            langPath = "Editor.GUI.Sound.New." + name;

            List<String> generalButtons = Arrays.asList("CANCEL", "SEPARATOR", "DECORATION", "EYE_LOCATION", "FEET_LOCATION");

            if (generalButtons.contains(name())) {
                langPath = "Editor.GUI.Sound.General." + name;
                general = true;
            }
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public String getConfigPath()
        {
            return configPath;
        }

        @Override
        public String getLangPath()
        {
            return langPath;
        }

        @Override
        public String getId()
        {
            return name();
        }

        public boolean isGeneral()
        {
            return general;
        }
    }
}
