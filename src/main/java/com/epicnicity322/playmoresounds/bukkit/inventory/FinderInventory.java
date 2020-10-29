package com.epicnicity322.playmoresounds.bukkit.inventory;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.config.PluginConfig;
import com.epicnicity322.epicpluginlib.core.logger.ErrorLogger;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import com.epicnicity322.yamlhandler.exceptions.InvalidConfigurationException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FinderInventory implements PMSInventory, Listener
{
    private static final @NotNull YamlConfigurationLoader loader = new YamlConfigurationLoader();
    private static final @NotNull PluginConfig config = Configurations.CONFIG.getPluginConfig();
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();
    private static final @NotNull Logger logger = PlayMoreSounds.getPMSLogger();
    private static final @NotNull ErrorLogger errorLogger = PlayMoreSounds.getErrorLogger();
    private static NamespacedKey button;
    private final @NotNull Path path;
    private final @NotNull Inventory inventory;
    private final @Nullable Configuration configuration;
    private final @NotNull HashSet<HumanEntity> openInventories = new HashSet<>();

    public FinderInventory(@NotNull Path path, long page) throws IOException, InvalidConfigurationException
    {
        if (!VersionUtils.hasPersistentData())
            throw new UnsupportedOperationException("This class only works with bukkit 1.14+.");

        if (PlayMoreSounds.getInstance() == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        if (!path.toAbsolutePath().startsWith(PlayMoreSounds.getFolder().toAbsolutePath()))
            throw new IllegalArgumentException("Path is not a child of PlayMoreSounds data folder.");

        if (button == null)
            button = InventoryUtils.getButton();

        HashMap<Long, ArrayList<ItemStack>> pages;

        if (Files.isDirectory(path)) {
            LinkedHashSet<ItemStack> items = new LinkedHashSet<>();
            File[] files = path.toFile().listFiles(file -> file.isDirectory() || file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"));
            Arrays.sort(files, (a, b) -> Boolean.compare(b.isDirectory(), a.isDirectory()));

            for (File file : files) {
                Items item;

                if (file.isDirectory())
                    item = Items.FOLDER;
                else
                    item = Items.FILE;

                ItemStack itemStack = item.getItemStack();
                ItemMeta itemMeta = itemStack.getItemMeta();
                String langPath = "Finder." + item.getName();
                String name = file.getName();

                itemMeta.setDisplayName(lang.getColored(langPath + ".Display Name")
                        .replace("<name>", name));
                itemMeta.setLore(Arrays.asList(lang.getColored(langPath + ".Lore")
                        .replace("<name>", name).split("<line>")));
                itemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING, "ENTER " +
                        file.toPath().toAbsolutePath());
                itemStack.setItemMeta(itemMeta);
                items.add(itemStack);
            }

            configuration = null;
            pages = PMSHelper.splitIntoPages(items, 36);
        } else if (Files.isReadable(path)) {
            if (path.toString().endsWith(".yml") || path.toString().endsWith(".yaml")) {
                configuration = loader.load(path);
                LinkedHashSet<ItemStack> items = new LinkedHashSet<>();

                for (String key : configuration.getAbsoluteNodes().keySet()) {
                    if (key.contains(".") && key.endsWith("Enabled")) {
                        String sectionPath = key.substring(0, key.lastIndexOf('.'));
                        ConfigurationSection section = configuration.getConfigurationSection(sectionPath);

                        if (section.getObject("Sounds").orElse(null) instanceof ConfigurationSection) {
                            ItemStack itemStack = Items.SOUND.getItemStack();
                            ItemMeta itemMeta = itemStack.getItemMeta();
                            String name = section.getPath();

                            itemMeta.setDisplayName(lang.getColored("Finder.Sound.Display Name")
                                    .replace("<name>", name));
                            itemMeta.setLore(Arrays.asList(lang.getColored("Finder.Sound.Lore")
                                    .replace("<name>", name).split("<line>")));
                            itemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING, "SOUND " +
                                    name);
                            itemStack.setItemMeta(itemMeta);
                            items.add(itemStack);
                        }
                    }
                }

                pages = PMSHelper.splitIntoPages(items, 36);
            } else
                throw new IllegalArgumentException("Path is not a readable yaml file.");
        } else
            throw new IllegalArgumentException("Path is neither readable nor a directory.");

        if (page < 1)
            page = 1;
        else if (page > pages.size())
            page = Math.max(pages.size(), 1);

        ArrayList<ItemStack> itemList = pages.get(page);

        PlayMoreSounds.addOnDisableRunnable(() -> {
            openInventories.forEach(HumanEntity::closeInventory);
            openInventories.clear();
        });

        this.path = path;

        int i = 0;
        int size = 9;

        boolean nextPage = page != Math.max(pages.size(), 1), back = !path.toAbsolutePath().equals(PlayMoreSounds.getFolder().toAbsolutePath()), previousPage = page > 1;
        if (nextPage || back || previousPage) {
            i = 9;

            if (itemList != null)
                size = 9 + ((itemList.size() + 8) / 9 * 9);
        }

        inventory = Bukkit.createInventory(null, size,
                lang.getColored("Finder.Title." + (configuration == null ? "Folder" : "Sound"))
                        .replace("<path>", path.getFileName().toString())
                        .replace("<page>", Long.toString(page))
                        .replace("<totalPages>", Integer.toString(Math.max(pages.size(), 1))));

        if (previousPage) {
            ItemStack previousPageItem = Items.PREVIOUS_PAGE.getItemStack();
            ItemMeta previousPageItemMeta = previousPageItem.getItemMeta();

            previousPageItemMeta.setDisplayName(lang.getColored("Finder.Previous Page.Display Name"));
            previousPageItemMeta.setLore(Arrays.asList(lang.getColored("Finder.Previous Page.Lore").split("<line>")));
            previousPageItemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING,
                    "GOTO " + (page - 1));
            previousPageItem.setItemMeta(previousPageItemMeta);
            inventory.setItem(0, previousPageItem);
        }

        if (back) {
            ItemStack backItem = Items.BACK.getItemStack();
            ItemMeta backItemMeta = backItem.getItemMeta();

            backItemMeta.setDisplayName(lang.getColored("Finder.Back.Display Name"));
            backItemMeta.setLore(Arrays.asList(lang.getColored("Finder.Back.Lore").replace("<path>", path.getParent().getFileName().toString()).split("<line>")));
            backItemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING,
                    "ENTER " + path.getParent().toAbsolutePath());
            backItem.setItemMeta(backItemMeta);
            inventory.setItem(configuration == null ? 4 : 3, backItem);
        }

        if (configuration != null) {

        }

        if (nextPage) {
            ItemStack nextPageItem = Items.NEXT_PAGE.getItemStack();
            ItemMeta nextPageItemMeta = nextPageItem.getItemMeta();

            nextPageItemMeta.setDisplayName(lang.getColored("Finder.Next Page.Display Name"));
            nextPageItemMeta.setLore(Arrays.asList(lang.getColored("Finder.Next Page.Lore").split("<line>")));
            nextPageItemMeta.getPersistentDataContainer().set(button, PersistentDataType.STRING,
                    "GOTO " + (page + 1));
            nextPageItem.setItemMeta(nextPageItemMeta);
            inventory.setItem(8, nextPageItem);
        }

        if (itemList != null)
            for (ItemStack itemStack : itemList)
                inventory.setItem(i++, itemStack);
    }

    @Override
    public @NotNull PMSInventoryItem[] getItems()
    {
        return Items.values();
    }

    @Override
    public @NotNull Inventory getInventory()
    {
        return inventory;
    }

    @Override
    public void openInventory(@NotNull HumanEntity humanEntity)
    {
        humanEntity.openInventory(inventory);
        openInventories.add(humanEntity);
        Bukkit.getPluginManager().registerEvents(this, PlayMoreSounds.getInstance());
    }

    @EventHandler
    public final void onInventoryClick(InventoryClickEvent event)
    {
        ItemStack itemStack = event.getCurrentItem();

        if (itemStack != null) {
            HumanEntity humanEntity = event.getWhoClicked();

            if (openInventories.contains(humanEntity)) {
                event.setCancelled(true);

                String button = itemStack.getItemMeta().getPersistentDataContainer().get(FinderInventory.button,
                        PersistentDataType.STRING);
                String[] args = new String[2];
                int index = button.indexOf(" ");

                args[0] = button.substring(0, index);
                args[1] = button.substring(index + 1);

                try {
                    switch (args[0]) {
                        case "ENTER":
                            new FinderInventory(Paths.get(args[1]), 1).openInventory(humanEntity);
                            break;
                        case "GOTO":
                            new FinderInventory(path, Long.parseLong(args[1])).openInventory(humanEntity);
                            break;
                        case "SOUND":
                            new RichSoundInventory(new RichSound(configuration.getConfigurationSection(args[1])))
                                    .openInventory(humanEntity);
                            break;
                    }
                } catch (Exception ex) {
                    lang.send(humanEntity, lang.get("Finder.Error"));
                    PlayMoreSounds.getErrorLogger().report(ex, "Operation: " + button + "\nFinder operation exception:");
                    humanEntity.closeInventory();
                }
            }
        }
    }

    @EventHandler
    public final void onInventoryClose(InventoryCloseEvent event)
    {
        openInventories.remove(event.getPlayer());

        if (openInventories.isEmpty())
            HandlerList.unregisterAll(this);
    }

    public enum Items implements PMSInventoryItem
    {
        BACK("Back"),
        FILE("File"),
        FOLDER("Folder"),
        NEXT_PAGE("Next Page"),
        PREVIOUS_PAGE("Previous Page"),
        SOUND("Sound");

        private final @NotNull String name;
        private final @NotNull String configPath;
        private final @NotNull String id = name();

        Items(@NotNull String name)
        {
            this.name = name;
            configPath = "Inventories.Finder." + name + " Item";
        }

        @Override
        public @NotNull String getName()
        {
            return name;
        }

        @Override
        public @NotNull String getConfigPath()
        {
            return configPath;
        }

        @Override
        public @NotNull String getId()
        {
            return id;
        }

        @Override
        public @NotNull ItemStack getItemStack()
        {
            Configuration yamlConfig = config.getConfiguration();
            ItemStack itemStack;

            try {
                itemStack = new ItemStack(Material.valueOf(yamlConfig.getString(configPath + ".Material").orElse("MUSIC_DISC_13")));
            } catch (IllegalArgumentException ex) {
                logger.log("&cYour config.yml has an invalid Material in the path '" + configPath + "'.");
                errorLogger.report(ex, "Invalid material in config.yml at " + configPath + ":");
                itemStack = new ItemStack(Material.MUSIC_DISC_13);
            }

            ItemMeta itemMeta = itemStack.getItemMeta();

            itemMeta.addItemFlags(ItemFlag.values());
            itemStack.setItemMeta(itemMeta);

            if (yamlConfig.getBoolean(configPath + ".Glowing").orElse(false))
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);

            return itemStack;
        }
    }
}
