package com.nationwar.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CoreGson {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static class CoreInfo {
        public String owner = "\uD844\uDCB5";
        public double hp = 5000.0;
        public double x, y, z;
        public int id;
    }

    public static class CoreData {
        public List<CoreInfo> cores = new ArrayList<>();
    }

    public static void save(File file, CoreData data) {
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static CoreData load(File file) {
        if (!file.exists()) return new CoreData();
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, CoreData.class);
        } catch (IOException e) { return new CoreData(); }
    }
}