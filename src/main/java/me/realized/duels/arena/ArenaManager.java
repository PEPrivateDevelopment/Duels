package me.realized.duels.arena;

import com.google.common.reflect.TypeToken;
import me.realized.duels.Core;
import me.realized.duels.data.ArenaData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ArenaManager {

    private final Core instance;
    private final File base;

    private List<Arena> arenas = new ArrayList<>();

    public ArenaManager(Core instance) {
        this.instance = instance;

        base = new File(instance.getDataFolder(), "arenas.json");

        try {
            boolean generated = base.createNewFile();

            if (generated) {
                instance.info("Generated arena file.");
            }

        } catch (IOException e) {
            instance.warn("Failed to generate arena file! (" + e.getMessage() + ")");
        }
    }

    public void load() {
        arenas.clear();

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(base))) {
            List<ArenaData> loaded = instance.getGson().fromJson(reader, new TypeToken<List<ArenaData>>() {}.getType());

            if (loaded != null && !loaded.isEmpty()) {
                for (ArenaData data : loaded) {
                    arenas.add(data.toArena());
                }
            }
        } catch (IOException ex) {
            instance.warn("Failed to load arenas from the file! (" + ex.getMessage() + ")");
        }

        instance.info("Loaded " + arenas.size() + " arena(s).");
    }

    public void save() {
        if (!arenas.isEmpty()) {
            for (Arena arena : arenas) {
                if (arena.isUsed()) {
                    for (UUID uuid : arena.getPlayers()) {
                        Player player = Bukkit.getPlayer(uuid);
                        
                        if (player != null) {
                            player.setHealth(0.0D);
                            player.sendMessage(ChatColor.RED + "[Duels] Plugin is disabling, matches are automatically finished by force.");
                        }
                    }
                }
            }
        }

        try {
            boolean generated = base.createNewFile();

            if (generated) {
                instance.info("Generated arena file!");
            }

            List<ArenaData> saved = new ArrayList<>();

            for (Arena arena : arenas) {
                saved.add(new ArenaData(arena));
            }

            Writer writer = new OutputStreamWriter(new FileOutputStream(base));
            instance.getGson().toJson(saved, writer);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            instance.warn("Failed to save arenas! (" + ex.getMessage() + ")");
        }
    }

    public Arena getArena(String name) {
        for (Arena arena : arenas) {
            if (arena.getName().equals(name)) {
                return arena;
            }
        }

        return null;
    }

    public Arena getArena(Player player) {
        for (Arena arena : arenas) {
            if (arena.getPlayers().contains(player.getUniqueId())) {
                return arena;
            }
        }

        return null;
    }

    public Arena getAvailableArena() {
        for (Arena arena : arenas) {
            if (!arena.isDisabled() && !arena.isUsed() && arena.getPositions().size() >= 2) {
                return arena;
            }
        }

        return null;
    }

    public boolean isInMatch(Player player) {
        for (Arena arena : arenas) {
            if (arena.getPlayers().contains(player.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    public void createArena(String name) {
        arenas.add(new Arena(name, false));
    }

    public void removeArena(Arena arena) {
        arenas.remove(arena);
    }

    public List<String> getArenas() {
        List<String> result = new ArrayList<>();

        if (arenas.isEmpty()) {
            result.add("No arenas are currently loaded.");
            return result;
        }

        for (Arena arena : arenas) {
            if (arena.isDisabled()) {
                result.add(ChatColor.DARK_RED + arena.getName());
                continue;
            }

            if (arena.getPositions().size() < 2) {
                result.add(ChatColor.BLUE + arena.getName());
                continue;
            }

            result.add((arena.isUsed() ? ChatColor.RED : ChatColor.GREEN) + arena.getName());
        }

        return result;
    }
}