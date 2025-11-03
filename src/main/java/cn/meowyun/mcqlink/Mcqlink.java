package cn.meowyun.mcqlink;

import cn.meowyun.mcqlink.api.PlaceHolder;
import org.bukkit.event.HandlerList;
import cn.meowyun.mcqlink.commands.AdminCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import cn.meowyun.mcqlink.listener.PlayerMessageListener;
import me.clip.placeholderapi.PlaceholderAPI;

public final class Mcqlink extends JavaPlugin {
    public FileConfiguration config;
    public PlayerMessageListener playerListener;  // 改为 public 以便 AdminCommand 访问

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.config = getConfig();

        getLogger().info("服务器IP: " + this.config.getString("server-ip"));
        getLogger().info("服务器端口: " + this.config.getInt("server-port"));
        getLogger().info("Token: " + this.config.getString("token"));


        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceHolder placeHolder = new PlaceHolder();
            getLogger().info("PlaceholderAPI 支持已启用");
        } else {
            getLogger().info("PlaceholderAPI 未找到，相关功能将不可用");
        }

        commandRegister();
        eventListenerRegister();
        getLogger().info("MCQLink-Q群互联插件已加载！");
    }

    public void commandRegister(){
        getCommand("mcqlink").setExecutor(new AdminCommand(this));  // 现在可以正确传递 plugin 实例
    }

    public void eventListenerUnregister() {
        if (this.playerListener != null) {
            HandlerList.unregisterAll(this.playerListener);
            if (this.playerListener.socks != null) {
                this.playerListener.socks.close();
            }
            this.playerListener = null;
        }
    }

    public void eventListenerRegister(){
        eventListenerUnregister();
        this.playerListener = new PlayerMessageListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);
    }

    @Override
    public void onDisable() {
        if (playerListener != null && playerListener.socks != null) {
            playerListener.socks.close();
        }
        getLogger().info("MCQLink-Q群互联插件已卸载!");
    }
}