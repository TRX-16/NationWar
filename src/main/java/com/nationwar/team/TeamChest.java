package com.nationwar.team;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nationwar.NationWar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TeamChest {
    private final NationWar plugin;
    private final File file;
    private JsonObject chestData;

    public TeamChest(NationWar plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "teamchest.json");
        loadFromFile();
    }

    // 파일에서 전체 창고 데이터 로드
    private void loadFromFile() {
        if (!file.exists()) {
            chestData = new JsonObject();
            return;
        }
        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            chestData = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            chestData = new JsonObject();
            e.printStackTrace();
        }
    }

    // 파일에 전체 창고 데이터 저장
    public void saveToFile() {
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write(chestData.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 창고 열기 (데이터 로드 포함)
    public Inventory open(Player player, String teamName) {
        // [복사 방지] 이미 사용 중인지 확인
        if (plugin.getTeamMain().isStorageLocked(teamName)) {
            player.sendMessage("§c[!] 다른 팀원이 창고를 사용 중입니다.");
            return null;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "§0" + teamName + " 국가 창고");
        if (chestData.has(teamName)) {
            deserialize(inv, chestData.get(teamName).getAsString());
        }

        // 잠금 및 로그
        plugin.getTeamMain().lockStorage(teamName);
        plugin.getLogger().info("[Storage] " + player.getName() + " -> " + teamName + " 창고 열음");

        return inv;
    }

    public void close(Player player, String teamName, Inventory inv) {
        updateTeamChest(teamName, inv);
        plugin.getTeamMain().unlockStorage(teamName);
        plugin.getLogger().info("[Storage] " + player.getName() + " -> " + teamName + " 창고 닫음 (저장완료)");
    }


    // 특정 팀의 창고 데이터를 업데이트 (닫을 때 호출용)
    public void updateTeamChest(String teamName, Inventory inventory) {
        String base64 = serialize(inventory);
        chestData.addProperty(teamName, base64);
        saveToFile(); // 실시간 저장
    }

    public String serialize(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(inventory.getSize());
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void deserialize(Inventory inventory, String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int size = dataInput.readInt();
            for (int i = 0; i < size; i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }
            dataInput.close();
        } catch (Exception e) {
            // 빈 데이터이거나 오류 시 무시
        }
    }
}