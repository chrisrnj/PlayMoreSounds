package com.epicnicity322.playmoresounds.bukkit.listener;

import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.command.subcommand.FactorySubCommand;
import com.epicnicity322.playmoresounds.bukkit.command.subcommand.ListSubCommand;
import com.epicnicity322.playmoresounds.bukkit.inventory.ListInventory;
import com.epicnicity322.playmoresounds.bukkit.inventory.RichSoundInventory;
import com.epicnicity322.playmoresounds.bukkit.inventory.SoundInventory;
import com.epicnicity322.playmoresounds.bukkit.sound.RelativeLocationSetter;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.Sound;
import com.epicnicity322.playmoresounds.bukkit.sound.SoundType;
import com.epicnicity322.playmoresounds.bukkit.util.PMSHelper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class InventoryListener implements Listener
{

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e)
    {
        Inventory inventory = e.getClickedInventory();
        Player player = (Player) e.getWhoClicked();

        if (inventory != null) {
            if (PlayMoreSounds.HAS_PERSISTENT_DATA_CONTAINER) {
                try {
                    ItemStack item = e.getCurrentItem();

                    if (item != null) {
                        if (ListSubCommand.openListGUIs.contains(player)) {
                            e.setCancelled(true);

                            String button = item.getItemMeta().getPersistentDataContainer().get(ListInventory.button,
                                    PersistentDataType.STRING);

                            if (button.startsWith("GOTO")) {
                                ListSubCommand.toOpenAgain.add(player);
                                player.openInventory(new ListInventory(Integer.parseInt(button.split(" ")[1])).getInventory());
                            } else if (button.equals("STOP_SOUND")) {
                                PMSHelper.stopSound(player, null, 0);
                            } else {
                                player.playSound(player.getLocation(), SoundType.valueOf(button).getSoundOnVersion(), 10,
                                        1);
                            }
                        }
                        if (RichSoundInventory.openInventories.containsKey(player.getName())) {
                            e.setCancelled(true);

                            String button = item.getItemMeta().getPersistentDataContainer().get(RichSoundInventory.button,
                                    PersistentDataType.STRING);
                            RichSoundInventory rsi = RichSoundInventory.openInventories.get(player.getName());

                            if (button != null) {
                                switch (button.split(" ")[0]) {
                                    case "CANCEL":
                                        player.closeInventory();
                                        RichSoundInventory.openInventories.remove(player.getName());
                                        break;

                                    case "ADD_NEW_SOUND":
                                        RichSoundInventory.openInventories.remove(player.getName());

                                        SoundInventory child = new SoundInventory(null, rsi, item.getType());

                                        child.openInventory(player);

                                        break;

                                    case "DONE":
                                        FactorySubCommand.cachedSounds.put(player, rsi.getRichSound());
                                        player.closeInventory();
                                        RichSoundInventory.openInventories.remove(player.getName());
                                        break;

                                    case "SOUND":
                                        RichSoundInventory.openInventories.remove(player.getName());

                                        Sound sound = null;

                                        for (Sound sound1 : rsi.getRichSound().getChildSounds()) {
                                            if (sound1.getId().equals(button.split(" ")[1])) {
                                                sound = sound1;
                                                break;
                                            }
                                        }

                                        SoundInventory child1 = new SoundInventory(sound, rsi, item.getType());

                                        child1.openInventory(player);

                                        break;
                                }
                            }
                        }
                        if (SoundInventory.openInventories.containsKey(player.getName())) {
                            e.setCancelled(true);

                            String button = item.getItemMeta().getPersistentDataContainer().get(SoundInventory.button,
                                    PersistentDataType.STRING);
                            SoundInventory si = SoundInventory.openInventories.get(player.getName());

                            if (button != null) {
                                switch (button.split(" ")[0]) {
                                    case "RELATIVE_LOCATION":
                                        player.closeInventory();
                                        SoundInventory.openInventories.remove(player.getName());

                                        RelativeLocationSetter rls = new RelativeLocationSetter(player);

                                        rls.setOnSneakRunnable(() -> {
                                            si.openInventory(player);
                                            System.out.println(rls.getRelativeLocation());
                                        });
                                        rls.start();
                                        break;
                                    case "CANCEL":
                                        player.closeInventory();
                                        SoundInventory.openInventories.remove(player.getName());
                                        break;
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    e.setCancelled(true);
                    player.closeInventory();
                    return;
                }
            }

            ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Inventory Click");
            RichSound sound = new RichSound(section);

            if (!e.isCancelled() || !sound.isCancellable()) {
                sound.play(player);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e)
    {
        Player player = (Player) e.getPlayer();

        RichSoundInventory.openInventories.remove(player.getName());
        SoundInventory.openInventories.remove(player.getName());

        if (!ListSubCommand.toOpenAgain.contains(player))
            ListSubCommand.openListGUIs.remove(player);

        ListSubCommand.toOpenAgain.remove(player);

        ConfigurationSection section = PMSHelper.getConfig("sounds").getConfigurationSection("Inventory Close");

        new RichSound(section).play(player);
    }
}
