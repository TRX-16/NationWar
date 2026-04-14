package com.nationwar;

import com.nationwar.command.*;
import com.nationwar.core.CoreMain;
import com.nationwar.listeners.*;
import com.nationwar.team.TeamInviteManager;
import com.nationwar.team.TeamMain;
import com.nationwar.tpa.TpaMain;
import com.nationwar.menu.GUIManager; // 추가
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NationWar extends JavaPlugin {
    private static NationWar instance;
    public Object getCoreDamageListener;
    private TeamMain teamMain;
    private CoreMain coreMain;
    private TpaMain tpaMain;
    private GUIManager guiManager; // 추가
    private TeamInviteManager teamInviteManager;
    private PlayerDistanceDetect distanceDetect; // 변수 선언
    public CoreDamageListener coreDamageListener;
    private PvpListener pvpListener;
    private GameState gameState;

    @Override
    public void onEnable() {
        instance = this;
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        saveDefaultConfig(); // config.yml 로드

        // GameState 가장 먼저 초기화
        this.gameState = new GameState(this);
        getServer().getPluginManager().registerEvents(this.gameState, this);


        // 2. [중요] 감지기 객체를 생성하여 변수에 저장 (이게 null 에러 해결책)
        // 1. 매니저들 초기화 (순서 조정)
        this.teamInviteManager = new TeamInviteManager(this);
        this.teamMain = new TeamMain(this);

        // [중요] 리스너를 CoreMain보다 먼저 생성해야 합니다!
        this.coreDamageListener = new CoreDamageListener(this);

        this.coreMain = new CoreMain(this); // 이제 CoreMain이 내부에서 리스너를 불러도 안전합니다.
        this.tpaMain = new TpaMain(this);
        this.guiManager = new GUIManager(this);

        // 2. 거리 감지기 객체 생성
        this.distanceDetect = new PlayerDistanceDetect(this);
        this.distanceDetect.runTaskTimer(this, 0L, 20L);

        // 3. 명령어 등록
        getCommand("메뉴").setExecutor(new MenuCommand(this));
        getCommand("gamestart").setExecutor(new GamestartCommand(this));
        getCommand("팀").setExecutor(new TeamCommand(this));
        getCommand("팀탈퇴").setExecutor(new TeamLeaveCommand(this));
        getCommand("팀정보").setExecutor(new TeamInfoCommand(this));
        getCommand("tpa").setExecutor(new TpaCommand(this));
        getCommand("국가창고").setExecutor(new TeamChestCommand(this));
        getCommand("gamecontinue").setExecutor(new GameContinueCommand(this));

        // 4. 리스너 등록
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuClickListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockProtection(), this);
        getServer().getPluginManager().registerEvents(this.coreDamageListener, this);
        this.pvpListener = new PvpListener(this);
        getServer().getPluginManager().registerEvents(this.pvpListener, this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatLogoutListener(this), this);

        coreMain.startTimeChecker();

        // 서버 시작 시 대기 상태로 초기화
        gameState.deactivate();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            this.teamMain.checkLeaderActivity();
        }, 100L);
    }

    @Override
    public void onDisable() {
        if (this.gameState != null) this.gameState.restore();
        if (this.coreMain != null) this.coreMain.removeAllCoreGhasts();
        if (this.teamMain != null) this.teamMain.saveTeams();
        getLogger().info("NationWar 플러그인이 안전하게 종료되었습니다.");
    }

    public CoreDamageListener getCoreDamageListener() {
        return coreDamageListener;
    }

    public static NationWar getInstance() { return instance; }
    public TeamMain getTeamMain() { return teamMain; }
    public CoreMain getCoreMain() { return coreMain; }
    public TpaMain getTpaMain() { return tpaMain; }
    public GUIManager getGUIManager() { return guiManager; }// 추가됨
    public TeamInviteManager getTeamInviteManager() { return teamInviteManager; }
    public PlayerDistanceDetect getDistanceDetect() {
        return distanceDetect;
    }
    public PvpListener getPvpListener() { return pvpListener; }
    public GameState getGameState() { return gameState; }
}
