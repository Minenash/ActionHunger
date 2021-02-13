package com.minenash.action_hunger.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

public class TinyConfig {

    public static void init(String modid, String modname, Class<?> config) {

        Gson gson = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT)
                .excludeFieldsWithModifiers(Modifier.PRIVATE)
                .setPrettyPrinting()
                .create();

        Path path = FabricLoader.getInstance().getConfigDir().resolve(modid + ".json");
        Logger logger = LogManager.getLogger(modid);

        try {
            gson.fromJson(Files.newBufferedReader(path), config);
        }
        catch (Exception e) {
            logger.error("[" + modname + ": Couldn't load config; reverting to defaults");
            try {
                if (!Files.exists(path)) Files.createFile(path);
                Files.write(path, gson.toJson(((Class) config).newInstance()).getBytes());
            } catch (Exception e2) {
                logger.error(modname + ": Couldn't save config.");
                e.printStackTrace();
            }
        }
    }

}
