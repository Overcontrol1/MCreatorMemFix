package com.overcontrol1.mcreatormemfix;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record ModConfig(List<ModConfigEntry> entries, int spec, long hash) {
    public static final String FILE_NAME = "mcreator_mem_fix.json";
    public static final Path PATH = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    public static final int CURRENT_SPEC = 0;

    public static boolean REQUIRES_REGENERATION = false;
    public static boolean REGENERATION_LOCKED = false;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static ModConfig instance;

    public static ModConfig instance() {
        return instance;
    }

    public static void clear() {
        instance = null;
    }

    public static void load() {
        if (!Files.exists(PATH)) {
            REQUIRES_REGENERATION = true;
            return;
        }

        try (var reader = Files.newBufferedReader(PATH)) {
            var object = JsonParser.parseReader(reader).getAsJsonObject();

            JsonPrimitive specJson = object.getAsJsonPrimitive("spec");
            int spec = specJson == null ? -1 : specJson.getAsInt();

            JsonPrimitive lockedJson = object.getAsJsonPrimitive("locked");
            REGENERATION_LOCKED = lockedJson != null && lockedJson.getAsBoolean();

            ArrayList<ModConfigEntry> entries = new ArrayList<>();
            var mods = object.getAsJsonArray("mods");
            for (JsonElement e : mods) {
                var o = e.getAsJsonObject();
                entries.add(new ModConfigEntry(o.get("package").getAsJsonPrimitive().getAsString(), o.get("className").getAsJsonPrimitive().getAsString()));
            }


            JsonPrimitive hashJson = object.getAsJsonPrimitive("hash");
            long hash;
            if (hashJson == null) {
                REQUIRES_REGENERATION = true;
                hash = -1;
            } else {
               hash = Long.parseLong(hashJson.getAsString(), 16);
            }

            instance = new ModConfig(entries, spec, hash);
        } catch (IOException | IllegalStateException e) {
            throw new RuntimeException("[MCreatorMemFix] Error occurred loading config: %s".formatted(e));
        }
    }

    public void save() {
        var modsArray = new JsonArray();
        for (var entry : this.entries) {
            var object = new JsonObject();
            object.addProperty("package", entry.packageName());
            object.addProperty("className", entry.className());

            modsArray.add(object);
        }

        var outerObject = new JsonObject();
        outerObject.add("mods", modsArray);

        outerObject.addProperty("spec", spec);
        outerObject.addProperty("hash", Long.toHexString(hash));

        try (var writer = new JsonWriter(Files.newBufferedWriter(PATH))) {
            gson.toJson(outerObject, writer);
        } catch (IOException e) {
            throw new RuntimeException("[MCreatorMemFix] Error occurred saving config: %s".formatted(e));
        }
    }
}
