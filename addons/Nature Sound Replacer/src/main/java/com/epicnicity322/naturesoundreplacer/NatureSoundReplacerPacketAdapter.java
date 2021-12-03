package com.epicnicity322.naturesoundreplacer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.PlayableRichSound;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.PlayMoreSoundsVersion;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import com.epicnicity322.playmoresounds.core.sound.SoundType;
import com.epicnicity322.yamlhandler.ConfigurationSection;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class NatureSoundReplacerPacketAdapter extends PacketAdapter
{
    public static final @NotNull ConfigurationHolder natureSoundReplacerConfig;
    private static final @NotNull HashMap<String, PlayableRichSound> sounds = new HashMap<>();
    private static NatureSoundReplacerPacketAdapter instance;
    private static boolean registered = false;

    static {
        natureSoundReplacerConfig = new ConfigurationHolder(Configurations.BIOMES.getConfigurationHolder().getPath().getParent().resolve("nature sound replacer.yml"),
                "# Replace any sound played by nature in your server.\n" +
                        "#\n" +
                        "#  When a sound here is played, PlayMoreSounds interrupts the sound packets from being sent to the\n" +
                        "# players and plays the sound set here instead. This way you can take advantage of PlayMoreSounds\n" +
                        "# features, like play multiple sounds, delayed sounds, toggleable sounds, permissible sounds,\n" +
                        "# resource pack sounds etc.\n" +
                        "#\n" +
                        "# Warnings:\n" +
                        "# >> ProtocolLib is required for this feature to work.\n" +
                        "# >> Only sounds played by the server are replaceable, sounds played to the client (like walking or\n" +
                        "# building) are replaceable only if the source is another player that's not you.\n" +
                        "#\n" +
                        "#  To replace a sound, create a section with the sound name and set the replacing sound in it, for\n" +
                        "# example:\n" +
                        "#\n" +
                        "#ENTITY_ZOMBIE_HURT: # This is the sound that I want to replace.\n" +
                        "#  Enabled: true\n" +
                        "#  Sounds: # The sounds that will play instead.\n" +
                        "#    '0':\n" +
                        "#      Delay: 0\n" +
                        "#      Options:\n" +
                        "#        Ignores Disabled: true\n" +
                        "#        #Permission Required: '' # Permission Required is available but it's not recommended, use Permission To Listen instead.\n" +
                        "#        Permission To Listen: 'listen.zombiehurt'\n" +
                        "#        Radius: 0.0 # Radius > 0 is not recommended\n" +
                        "#        Relative Location:\n" +
                        "#          FRONT_BACK: 0.0\n" +
                        "#          RIGHT_LEFT: 0.0\n" +
                        "#          UP_DOWN: 0.0\n" +
                        "#      Pitch: 0.5\n" +
                        "#      Sound: ENTITY_SKELETON_HURT\n" +
                        "#      Volume: 1.0\n" +
                        "#\n" +
                        "#  If you want to completely stop a sound from being played in your server, add as in the example:\n" +
                        "#\n" +
                        "#ENTITY_ZOMBIE_AMBIENT: # This is the sound that I want to stop from playing in my server.\n" +
                        "#  Enabled: true\n" +
                        "#  #Sounds: # Don't add 'Sounds' section since you don't want sounds to play.\n" +
                        "#\n" +
                        "#  A more in depth tutorial of all sound options can be found in sounds.yml file.\n" +
                        "#  If you have any other doubts on how to set this configuration up, feel free to ask in\n" +
                        "# PlayMoreSounds' discord: https://discord.gg/eAHPbc3\n" +
                        "\n" +
                        "Version: '" + PlayMoreSoundsVersion.version + "'");
    }

    private NatureSoundReplacerPacketAdapter(@NotNull PlayMoreSounds plugin)
    {
        // It is changing sounds so priority is set to lowest.
        super(plugin, ListenerPriority.LOWEST, PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    public synchronized static void loadNatureSoundReplacer(@NotNull PlayMoreSounds plugin)
    {
        if (instance == null) {
            instance = new NatureSoundReplacerPacketAdapter(plugin);
        }

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        boolean anySoundEnabled = false;

        sounds.clear();

        for (Map.Entry<String, Object> node : natureSoundReplacerConfig.getConfiguration().getNodes().entrySet()) {
            String key = node.getKey();
            Object value = node.getValue();

            if (!(value instanceof ConfigurationSection)) continue;

            ConfigurationSection section = (ConfigurationSection) value;

            if (!section.getBoolean("Enabled").orElse(false)) continue;

            SoundType type;

            try {
                type = SoundType.valueOf(key);
            } catch (IllegalArgumentException ex) {
                PlayMoreSounds.getConsoleLogger().log("&cInvalid sound to replace on nature sound replacer.yml: " + key);
                continue;
            }

            String bukkitSound;

            if (VersionUtils.hasSoundEffects()) {
                bukkitSound = toBukkit(type);
            } else {
                bukkitSound = type.getSound().orElse(null);
            }

            if (bukkitSound == null) {
                PlayMoreSounds.getConsoleLogger().log("&cInvalid sound to replace on nature sound replacer.yml: " + key);
                continue;
            }

            try {
                sounds.put(bukkitSound, new PlayableRichSound(section));
                anySoundEnabled = true;
            } catch (IllegalArgumentException ignored) {
                //Not a sound.
            }

        }

        if (anySoundEnabled && !registered) {
            protocolManager.addPacketListener(instance);
            registered = true;
        } else if (!anySoundEnabled && registered) {
            protocolManager.removePacketListener(instance);
            registered = false;
        }
    }

    private static String toBukkit(SoundType type)
    {
        Optional<String> soundKey = type.getSound();

        if (!soundKey.isPresent()) return null;

        String soundKeyString = soundKey.get();

        for (Sound sound : Sound.values()) {
            if (soundKeyString.equals(sound.getKey().getKey())) {
                return sound.name();
            }
        }

        return null;
    }

    @Override
    public void onPacketSending(PacketEvent event)
    {
        PlayableRichSound sound;

        if (VersionUtils.hasSoundEffects()) {
            sound = sounds.get(event.getPacket().getSoundEffects().read(0).name());
        } else {
            sound = sounds.get(event.getPacket().getStrings().read(0));
        }

        if (sound != null) {
            Player player = event.getPlayer();
            StructureModifier<Integer> xyz = event.getPacket().getIntegers();

            event.setCancelled(true);
            // The server multiplies the xyz by 8 before sending the packet.
            sound.play(player, new Location(player.getWorld(), xyz.read(0) / 8.0, xyz.read(1) / 8.0, xyz.read(2) / 8.0));
        }
    }
}
