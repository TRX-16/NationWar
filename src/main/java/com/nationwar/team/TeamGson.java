package com.nationwar.team;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamGson {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static class TeamData {
        // 팀별 멤버 리스트
        public Map<String, List<String>> teams = new HashMap<>();
        // 팀별 팀장 UUID
        public Map<String, String> leaders = new HashMap<>();
        // 팀별 색상 정보 (여기에 저장됩니다!)
        public Map<String, String> colors = new HashMap<>();
    }

    public static void save(File file, TeamData data) {
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static TeamData load(File file) {
        if (!file.exists()) return new TeamData();
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, TeamData.class);
        } catch (IOException e) { return new TeamData(); }
    }
}