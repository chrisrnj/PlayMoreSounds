package com.epicnicity322.playmoresounds.bukkit.inventory;

import com.epicnicity322.epicpluginlib.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.Sound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
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

public class RichSoundInventory implements PMSInventory
{
    public static final HashMap<String, RichSoundInventory> openInventories = new HashMap<>();
    public static NamespacedKey button;
    private RichSound richSound;
    private Inventory inventory;
    private HashMap<String, Integer> count = new HashMap<>();

    public RichSoundInventory(@Nullable RichSound richSound)
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

        this.richSound = richSound;

        MessageSender lang = PlayMoreSounds.MESSAGE_SENDER;
        String title;

        if (richSound == null)
            title = lang.getColored("Editor.GUI.Rich Sound.New.Title");
        else
            title = lang.getColored("Editor.GUI.Rich Sound.Default.Title")
                    .replace("<name>", richSound.getName());

        inventory = Bukkit.createInventory(null, 54, title);

        if (richSound == null || richSound.isEnabled())
            inventory.setItem(1, getItemStack(Items.ENABLED));
        else
            inventory.setItem(1, getItemStack(Items.DISABLED));

        if (richSound == null) {
            inventory.setItem(31, getItemStack(Items.ADD_NEW_SOUND));
        } else {
            inventory.setItem(22, getItemStack(Items.ADD_NEW_SOUND));

            if (richSound.getChildSounds().size() > 8) {
                inventory.setItem(35, getItemStack(Items.NEXT_PAGE));
            }

            int count = 1;
            Collection<Sound> sounds = richSound.getChildSounds();

            for (int i = 27; i < 35; ++i) {
                if (count <= sounds.size()) {
                    inventory.setItem(i, getItemStack(Items.SOUND));
                } else {
                    break;
                }

                ++count;
            }
        }

        inventory.setItem(4, getItemStack(Items.NAME));
        inventory.setItem(7, getItemStack(Items.CANCELLABLE));
        inventory.setItem(48, getItemStack(Items.CANCEL));
        inventory.setItem(50, getItemStack(Items.DONE));

        animate();
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
            if (item.getId().equals("SOUND") || item.getId().equals("SEPARATOR")) {
                List<String> materials = config.getStringList(item.getConfigPath() + ".Material");

                if (!count.containsKey(item.getId()) || count.get(item.getId()) >= materials.size()) {
                    count.put(item.getId(), 0);
                }

                itemStack = new ItemStack(Material.valueOf(materials.get(count.get(item.getId()))));
                count.put(item.getId(), count.get(item.getId()) + 1);
            } else {
                itemStack = new ItemStack(Material.valueOf(config.getString(item.getConfigPath() + ".Material")));
            }
        } catch (IllegalArgumentException ex) {
            PlayMoreSounds.LOGGER.log("&cYour config.yml has an invalid Material in the path '" +
                    item.getConfigPath() + "'. Rich Sound Editor GUI could not be created.", Level.WARNING);
            PlayMoreSounds.ERROR_LOGGER.report(ex, "Invalid material in config.yml at " + item.getConfigPath() + ":");
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (richSound == null || ((Items) item).isGeneral()) {
            itemMeta.setDisplayName(lang.getColored(item.getLangPath() + ".Display Name"));

            if (!item.getId().equals("SEPARATOR")) {
                itemMeta.setLore(Arrays.asList(lang.getColored(item.getLangPath() + ".Lore").split(Pattern.quote("<line>"))));
                itemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING, item.getId());
            }
        } else {
            String target = "";
            String replacement = "";

            switch (item.getId()) {
                case "CANCELLABLE":
                    target = "<value>";
                    replacement = Boolean.toString(richSound.isCancellable());
                    break;
                case "SOUND":
                    target = "<id>";

                    if (richSound.getChildSounds().size() > 0) {
                        if (!count.containsKey("SOUNDS") || count.get("SOUNDS") >= richSound.getChildSounds().size()) {
                            count.put("SOUNDS", 0);
                        }

                        replacement = new ArrayList<>(richSound.getChildSounds()).get(count.get("SOUNDS")).getId();
                        count.put("SOUNDS", count.get("SOUNDS") + 1);
                    }

                    break;
                case "DELAY":
                    target = "<delay>";

                    if (richSound.getSection().contains("Delay"))
                        replacement = richSound.getSection().getString("Delay");

                    break;
                case "PERIOD":
                    target = "<period>";

                    if (richSound.getSection().contains("Period"))
                        replacement = richSound.getSection().getString("Period");

                    break;
                case "NAME":
                    target = "<name>";
                    replacement = richSound.getName();
                    break;
            }

            itemMeta.setDisplayName(lang.getColored("Editor.GUI.Rich Sound.Default." + item.getName() + ".Display Name")
                    .replace(target, replacement));
            itemMeta.setLore(Arrays.asList(lang.getColored("Editor.GUI.Rich Sound.Default." + item.getName() + ".Lore")
                    .replace(target, replacement).split(Pattern.quote("<line>"))));
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
        RichSoundInventory rsi = this;
        player.openInventory(getInventory());
        openInventories.put(player.getName(), rsi);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (openInventories.containsKey(player.getName())) {
                    rsi.animate();
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(PlayMoreSounds.getPlugin(), 0, 10);
    }

    /**
     * Animates the SEPARATOR item of this inventory.
     */
    public void animate()
    {
        ItemStack separator = getItemStack(Items.SEPARATOR);

        for (int i = 18; i < 27; ++i) {
            if (richSound == null || i != 22) {
                inventory.setItem(i, separator);
            }
        }
        for (int i = 36; i < 45; ++i) {
            inventory.setItem(i, separator);
        }
    }

    public RichSound getRichSound()
    {
        return richSound;
    }

    public enum Items implements PMSInventoryItem
    {
        ADD_NEW_SOUND("Add New Sound"),
        CANCEL("Cancel"),
        CANCELLABLE("Cancellable"),
        DELAY("Loop Start Delay"),
        DISABLED("Disabled"),
        DONE("Done"),
        ENABLED("Enabled"),
        NAME("Name"),
        NEXT_PAGE("Next Page"),
        PERIOD("Loop Period"),
        PREVIOUS_PAGE("Previous Page"),
        SEPARATOR("Separator"),
        SOUND("Sound");

        private String configPath;
        private String langPath;
        private String name;
        private boolean general = true;

        Items(String name)
        {
            this.name = name;
            configPath = "Inventories.Rich Sound Editor." + name + " Item";
            langPath = "Editor.GUI.Rich Sound.General." + name;

            List<String> nonGeneralButtons = Arrays.asList("CANCELLABLE", "DONE", "DELAY", "NAME", "PERIOD", "SOUND");

            if (nonGeneralButtons.contains(name())) {
                langPath = "Editor.GUI.Rich Sound.New." + name;
                general = false;
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
