package com.epicnicity322.playmoresounds.sponge;

import com.epicnicity322.playmoresounds.sponge.utils.PMSLogger;
import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginManager;

import java.nio.file.Paths;

@Plugin(id = "playmoresounds", name = "PlayMoreSounds", version = "3.0.0-SNAPSHOT#7", description = "Plays sounds at player events.")
public class PlayMoreSounds
{
    public static String FILE_NAME;
    public static String SPONGE_VERSION;
    public static PlayMoreSounds PLUGIN;
    public static Game GAME;
    private static EventManager em = Sponge.getEventManager();
    @Inject
    private PluginManager pm;

    @Inject
    private Game game;

    @Listener
    public void onServerStart(GameStartedServerEvent e)
    {
        boolean success = true;

        try {
            GAME = game;
            FILE_NAME = pm.getPlugin("playmoresounds").get().getSource().orElse(Paths.get("PlayMoreSounds-LIGHT.jar")).getFileName().toString();
            SPONGE_VERSION = GAME.getPlatform().getContainer(Platform.Component.GAME).getVersion().orElse("0");
            PLUGIN = this;

            /*if (!PMSConfig.loadConfig()) {
                throw new Exception("Unable to load sounds.yml or config.yml configurations");
            }*/

            PMSLogger.log("&6-> &eConfiguration not loaded.");

            /*for (PMSSound s : PMSSound.values()) {
                if (s.bukkitSound() != null) {
                    SOUND_LIST.add(s.toString());
                }
            }

            for (Instrument i : Instrument.values()) {
                INSTRUMENT_LIST.add(i.toString());
            }*/

            PMSLogger.log("&6-> &e&n000&e sounds loaded.");

            /*
            pm.registerEvents(new InventoryClick(), this);
            pm.registerEvents(new JoinServer(), this);
            pm.registerEvents(new PlayerMove(), this);
            pm.registerEvents(new PlayerTeleport(), this);
            pm.registerEvents(new LeaveServer(), this);
            WorldTiming.time();
*/
            PMSLogger.log("&6-> &e&n000&e events loaded.");



            /*
            PluginCommand cmd = getCommand("playmoresounds");
            cmd.setExecutor(new CommandsHandler());
            CommandsHandler.loadCommands();
            cmd.setTabCompleter(new TabCompleterHandler());

            pm.registerEvents(new AreaSelector(), this);
            pm.registerEvents(new InappropriateEvents(), this);

            UpdaterManager.loadUpdater();

             */
        } catch (Exception ex) {
            success = false;
            //Error report
        } finally {
            if (success) {
                PMSLogger.log("&6============================================");
                PMSLogger.log("&aPlayMoreSounds isn't working with sponge yet.");
                PMSLogger.log("&aI'm sorry.");
                PMSLogger.log("&aVersion v" + SPONGE_VERSION + " detected");
                PMSLogger.log("&6============================================");
            } else {
                PMSLogger.log("&6============================================");
                PMSLogger.log("&cSomething went wrong while loading PMS");
                PMSLogger.log("&cPlease report this error to the developer");
                PMSLogger.log("&6============================================");
                PMSLogger.log("&4ERROR.LOG generated, please check.");
                PMSLogger.log("&4Plugin disabled.");
            }
        }
    }
}
