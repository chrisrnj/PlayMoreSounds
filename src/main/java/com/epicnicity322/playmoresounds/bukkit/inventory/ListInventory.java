package com.epicnicity322.playmoresounds.bukkit.inventory;

import com.epicnicity322.epicpluginlib.lang.MessageSender;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;

public class ListInventory implements PMSInventory
{
    public static NamespacedKey button;

    private Inventory inventory;
    private HashMap<String, Integer> count = new HashMap<>();

    public ListInventory(int page)
    {
        if (PlayMoreSounds.getPlugin() == null)
            throw new IllegalStateException("PlayMoreSounds isn't enabled.");

        if (PlayMoreSounds.CONFIG == null)
            throw new IllegalStateException("Configuration isn't loaded.");

        if (PlayMoreSounds.MESSAGE_SENDER == null)
            throw new IllegalStateException("Language isn't loaded.");

        if (button == null)
            button = new NamespacedKey(PlayMoreSounds.getPlugin(), "button");

        MessageSender lang = PlayMoreSounds.MESSAGE_SENDER;
        int rowsPerPage = PMSHelper.getConfig("config").getInt("Inventories.List.Rows Per Page");

        if (rowsPerPage > 4)
            rowsPerPage = 4;
        else if (rowsPerPage < 1)
            rowsPerPage = 1;

        int maxPerPage = rowsPerPage * 9;
        int count = 18;
        HashMap<Integer, LinkedHashSet<String>> soundList = PMSHelper.chopSet(PlayMoreSounds.SOUND_LIST, maxPerPage);
        LinkedHashSet<String> soundPage = soundList.get(page);

        inventory = Bukkit.createInventory(null, ((soundPage.size() + 8) / 9 * 9) + count,
                lang.getColored("List.GUI.Title").replace("<page>", Integer.toString(page))
                        .replace("<totalpages>", Integer.toString(soundList.size())));

        if (page > 1) {
            ItemStack previousPageItem = getItemStack(Items.PREVIOUS_PAGE);
            ItemMeta previousPageItemMeta = previousPageItem.getItemMeta();

            previousPageItemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING, "GOTO " + (page - 1));
            previousPageItem.setItemMeta(previousPageItemMeta);
            inventory.setItem(0, previousPageItem);
        }

        inventory.setItem(4, getItemStack(Items.STOP_SOUND));

        if (page != soundList.size()) {
            ItemStack nextPageItem = getItemStack(Items.NEXT_PAGE);
            ItemMeta nextPageItemMeta = nextPageItem.getItemMeta();

            nextPageItemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING, "GOTO " + (page + 1));
            nextPageItem.setItemMeta(nextPageItemMeta);
            inventory.setItem(8, nextPageItem);
        }

        for (String sound : soundPage) {
            ItemStack soundItem = getItemStack(Items.SOUND);
            ItemMeta soundItemMeta = soundItem.getItemMeta();

            soundItemMeta.setDisplayName(lang.getColored("List.GUI.Sound.Display Name").replace("<sound>",
                    sound));
            soundItemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING, sound);
            soundItem.setItemMeta(soundItemMeta);

            inventory.setItem(count, soundItem);
            ++count;
        }
    }

    @Override
    public Inventory getInventory()
    {
        return inventory;
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
            if (item.getId().equals("SOUND")) {
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
                    item.getConfigPath() + "'. Sound List GUI could not be created.", Level.WARNING);
            PlayMoreSounds.ERROR_LOGGER.report(ex, "Invalid material in config.yml at " + item.getConfigPath() + ":");
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setLore(Arrays.asList(lang.getColored(item.getLangPath() + ".Lore").split("<line>")));
        itemMeta.addItemFlags(ItemFlag.values());

        if (!item.getId().equals("SOUND")) {
            itemMeta.setDisplayName(lang.getColored(item.getLangPath() + ".Display Name"));
        }

        if (item.getId().equals("STOP_SOUND")) {
            itemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING, "STOP_SOUND");
        }

        if (config.getBoolean(item.getConfigPath() + ".Glowing")) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }


    public enum Items implements PMSInventoryItem
    {
        STOP_SOUND("Stop Sound"),
        NEXT_PAGE("Next Page"),
        PREVIOUS_PAGE("Previous Page"),
        SOUND("Sound");

        private String name;
        private String configPath;
        private String langPath;

        Items(String name)
        {
            this.name = name;
            configPath = "Inventories.List." + name + " Item";
            langPath = "List.GUI." + name;
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
    }
}
