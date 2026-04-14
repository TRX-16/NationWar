package com.nationwar.team;

import com.nationwar.NationWar;
import com.nationwar.core.CoreGson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import java.util.Collections;

import java.io.File;
import java.util.*;

public class TeamMain {
    private final Set<String> storageLock = new HashSet<>(); // 창고 잠금 목록

    public boolean isStorageLocked(String teamName) { return storageLock.contains(teamName); }
    public void lockStorage(String teamName) { storageLock.add(teamName); }
    public void unlockStorage(String teamName) { storageLock.remove(teamName); }
    private final NationWar plugin;
    private final File teamFile;
    private TeamGson.TeamData data;
    private final TeamChest teamChest;

    public TeamMain(NationWar plugin) {
        this.plugin = plugin;
        this.teamFile = new File(plugin.getDataFolder(), "team.json");
        this.data = TeamGson.load(teamFile);
        this.teamChest = new TeamChest(plugin); // 추가
    }

    public void checkLeaderActivity() {
        int inactiveDays = plugin.getConfig().getInt("team.leader-inactive-days", 7);
        long oneWeekMillis = inactiveDays * 24L * 60 * 60 * 1000;
        long currentTime = System.currentTimeMillis();
        boolean changed = false;

        for (String teamName : data.leaders.keySet()) {
            String leaderUUIDStr = data.leaders.get(teamName);
            UUID leaderUUID = UUID.fromString(leaderUUIDStr);
            OfflinePlayer leader = Bukkit.getOfflinePlayer(leaderUUID);

            // 마지막 접속 기록 확인 (접속한 적이 없으면 0 반환)
            long lastPlayed = leader.getLastPlayed();

            if (lastPlayed != 0 && (currentTime - lastPlayed) > oneWeekMillis) {
                // 일주일 이상 미접속 시 새로운 팀장 선출
                List<String> members = data.teams.get(teamName);
                if (members.size() > 1) {
                    // 팀장 제외하고 무작위 셔플
                    List<String> candidates = new ArrayList<>(members);
                    candidates.remove(leaderUUIDStr);
                    Collections.shuffle(candidates);

                    String newLeaderUUID = candidates.get(0);
                    data.leaders.put(teamName, newLeaderUUID);

                    Bukkit.getLogger().info("[NationWar] " + teamName + " 팀의 팀장이 미접속으로 인해 변경되었습니다.");
                    changed = true;
                }
            }
        }

        if (changed) {
            saveTeams();
        }
    }

    public void resetAllData() {
        // 모든 유저 방랑자로 변경
        data.teams.clear();
        data.leaders.clear();
        data.colors.clear();
        saveTeams();

        // 코어 소유권 초기화
        for (CoreGson.CoreInfo core : plugin.getCoreMain().getCoreData().cores) {
            core.owner = "없음";
            core.hp = plugin.getConfig().getDouble("core.hp", 5000);
        }
        plugin.getCoreMain().saveCores();

        Bukkit.broadcastMessage(plugin.getConfig().getString("format.reset-complete", "§c§l[!] §f모든 팀 데이터와 코어 상태가 초기화되었습니다."));
    }

    public void createTeam(String teamName, Player leader) {
        if (data.teams.containsKey(teamName)) {
            leader.sendMessage("§c이미 사용 중인 팀 이름입니다. 다른 이름을 선택해주세요.");
            return;
        }
        if (!getPlayerTeam(leader.getUniqueId()).equals("방랑자")) {
            leader.sendMessage("§c이미 소속된 팀이 있습니다. 먼저 팀을 탈퇴해야 합니다.");
            return;
        }
        List<String> members = new ArrayList<>();
        members.add(leader.getUniqueId().toString());
        data.teams.put(teamName, members);
        data.leaders.put(teamName, leader.getUniqueId().toString()); // 팀장 등록
        saveTeams();
        leader.sendMessage(" ");
        for (String line : plugin.getConfig().getStringList("team-create-message.lines")) {
            leader.sendMessage(line.replace("{team}", teamName));
        }
        leader.sendMessage(" ");
        leader.playSound(leader.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
    }

    public void deleteTeam(String teamName) {
        data.teams.remove(teamName);
        data.colors.remove(teamName);
        data.leaders.remove(teamName);
        saveTeams();
    }

    public void leaveTeam(Player player) {
        String teamName = getPlayerTeam(player.getUniqueId());
        if (teamName.equals("방랑자")) return;

        String uuid = player.getUniqueId().toString();
        List<String> members = data.teams.get(teamName);
        boolean wasLeader = isLeader(teamName, player);

        members.remove(uuid);

        if (members.isEmpty()) {
            // 마지막 멤버가 나가면 팀 자체 삭제
            data.teams.remove(teamName);
            data.colors.remove(teamName);
            data.leaders.remove(teamName);
            Bukkit.broadcastMessage("§7[!] §f" + teamName + " 팀이 인원 부족으로 해체되었습니다.");
        } else if (wasLeader) {
            // 팀장이 나가면 다음 멤버에게 팀장 이양
            String newLeaderUUID = members.get(0);
            data.leaders.put(teamName, newLeaderUUID);
            org.bukkit.OfflinePlayer newLeader = Bukkit.getOfflinePlayer(java.util.UUID.fromString(newLeaderUUID));
            Bukkit.broadcastMessage("§7[!] §f" + teamName + " 팀의 새 팀장: §e" + newLeader.getName());
        }

        saveTeams();
        updateDisplay(player);
        player.sendMessage("§f" + teamName + " §7팀에서 탈퇴했습니다.");
    }

    public void updateDisplay(Player player) {
        // 1. JSON 데이터에서 최신 정보 가져오기
        String teamName = getPlayerTeam(player.getUniqueId());
        String colorStr = data.colors.getOrDefault(teamName, "WHITE");

        org.bukkit.ChatColor color;
        try {
            color = org.bukkit.ChatColor.valueOf(colorStr.toUpperCase());
        } catch (Exception e) {
            color = org.bukkit.ChatColor.WHITE;
        }

        // 2. 표시할 텍스트 생성: [팀이름] 닉네임
        String prefix = color + "[" + teamName + "] §f";
        String fullName = prefix + player.getName();

        // 3. 플레이어 객체에 직접 반영 (탭 리스트 및 채팅 이름)
        player.setDisplayName(fullName);
        player.setPlayerListName(fullName);

        // 4. 머리 위 이름표 색상을 위한 최소한의 스코어보드 처리
        // (마인크래프트 엔진 한계상 머리 위 이름 색상은 스코어보드 팀이 반드시 필요합니다)
        org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        org.bukkit.scoreboard.Team sTeam = sb.getTeam(teamName);

        if (sTeam == null) {
            sTeam = sb.registerNewTeam(teamName);
        }

        sTeam.setPrefix(prefix);
        sTeam.setColor(color);
        if (!sTeam.hasEntry(player.getName())) {
            sTeam.addEntry(player.getName());
        }
    }

    public void changeColor(String teamName, String colorName) {
        // JSON 데이터 업데이트 및 파일 저장
        data.colors.put(teamName, colorName.toUpperCase());
        saveTeams(); // 이 메서드 내에서 가공된 GSON이 파일로 쓰여집니다.

        // 저장된 JSON 데이터를 바탕으로 모든 온라인 플레이어 화면 갱신
        for (Player p : Bukkit.getOnlinePlayers()) {
            updateDisplay(p);
        }
    }

    public boolean isLeader(String teamName, Player player) {
        // 기준서: 생성자가 팀장 (리스트의 0번째 인덱스)
        List<String> members = data.teams.get(teamName);
        return members != null && members.get(0).equals(player.getUniqueId().toString());
    }

    public String getPlayerTeam(UUID uuid) {
        for (Map.Entry<String, List<String>> entry : data.teams.entrySet()) {
            if (entry.getValue().contains(uuid.toString())) return entry.getKey();
        }
        return "방랑자"; // 기준서: 기본 상태
    }

    public TeamChest getTeamChest() { return teamChest; }
    public TeamGson.TeamData getData() { return data; }

    public boolean sameTeam(Player p1, Player p2) {
        String t1 = getPlayerTeam(p1.getUniqueId());
        String t2 = getPlayerTeam(p2.getUniqueId());
        return !t1.equals("방랑자") && t1.equals(t2);
    }

    public void saveTeams() { TeamGson.save(teamFile, data); }
}