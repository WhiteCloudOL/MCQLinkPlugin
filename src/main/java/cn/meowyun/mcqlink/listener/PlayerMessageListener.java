package cn.meowyun.mcqlink.listener;

import cn.meowyun.mcqlink.Mcqlink;
import cn.meowyun.mcqlink.api.Socks;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerMessageListener implements Listener {
    public Mcqlink plugin;
    private FileConfiguration config;
    public Socks socks;

    public PlayerMessageListener(Mcqlink plugin){
        this.plugin = plugin;
        this.config = plugin.config;

        // 安全地获取配置值，提供默认值
        String serverIp = config != null ? config.getString("server-ip", "127.0.0.1") : "127.0.0.1";
        int serverPort = config != null ? config.getInt("server-port", 6215) : 6215;
        String token = config != null ? config.getString("token", "") : "";

        // 记录连接信息
        plugin.getLogger().info("正在初始化 WebSocket 连接: " + serverIp + ":" + serverPort);

        this.socks = new Socks(plugin, serverIp, serverPort, token);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if (socks == null || !socks.isConnected()) {
            return;
        }

        Player player = event.getPlayer();
        if(config != null && config.getBoolean("player_join_message" +
                "", true)){
            socks.sendPlayerJoin(player.getName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        if (socks == null || !socks.isConnected()) {
            return;
        }

        Player player = event.getPlayer();
        if(config != null && config.getBoolean("player_join_message", true)){
            socks.sendPlayerQuit(player.getName());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event){
        if (socks == null || !socks.isConnected()) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage();
        if(config != null && config.getBoolean("sync_message", true)){
            socks.sendChatMessage(player.getName(), message);
        }
    }
}