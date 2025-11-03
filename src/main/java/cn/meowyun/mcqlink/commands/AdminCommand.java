package cn.meowyun.mcqlink.commands;

import cn.meowyun.mcqlink.Mcqlink;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {
    private Mcqlink plugin;

    public AdminCommand(Mcqlink plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("mcqlink")) {
            if (args.length == 0) {
                sender.sendMessage("§7------ MCQLink 插件帮助 §7------");
                sender.sendMessage("§7/mcqlink help §f- 显示帮助信息");
                sender.sendMessage("§7/mcqlink status §f- 查看连接状态");
                sender.sendMessage("§7/mcqlink connect §f- 尝试连接QQ机器人服务端");
                sender.sendMessage("§7/mcqlink reload §f- 重载配置");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "help":
                    sender.sendMessage("§7------ MCQLink 插件帮助 §7------");
                    sender.sendMessage("§7/mcqlink help §f- 显示帮助信息");
                    sender.sendMessage("§7/mcqlink status §f- 查看连接状态");
                    sender.sendMessage("§7/mcqlink connect §f- 尝试连接QQ机器人服务端");
                    sender.sendMessage("§7/mcqlink reload §f- 重载配置");
                    break;

                case "status":
                    if (!sender.hasPermission("mcqlink.admin")) {
                        sender.sendMessage("§c你没有权限执行此命令。");
                        return true;
                    }
                    if (plugin.playerListener != null && plugin.playerListener.socks != null) {
                        boolean connected = plugin.playerListener.socks.isConnected();
                        sender.sendMessage("§6WebSocket 连接状态: " + (connected ? "§a已连接" : "§c未连接"));
                        sender.sendMessage("§7服务器: " + plugin.config.getString("server-ip") + ":" + plugin.config.getInt("server-port"));
                    } else {
                        sender.sendMessage("§cWebSocket 客户端未初始化");
                    }
                    break;
                case "connect":
                    if (!sender.hasPermission("mcqlink.admin")) {
                        sender.sendMessage("§8[§6MCQLINK§8] §c你没有权限执行此命令。");
                        return true;
                    }
                    if (plugin.playerListener != null && plugin.playerListener.socks != null) {
                        plugin.playerListener.socks.close();
                    }
                    plugin.eventListenerRegister();
                    sender.sendMessage("§8[§6MCQLINK§8] §aWebSocket 连接已重启！");
                    break;
                case "reload":
                    if (!sender.hasPermission("mcqlink.admin")) {
                        sender.sendMessage("§8[§6MCQLINK§8] §c你没有权限执行此命令。");
                        return true;
                    }

                    plugin.reloadConfig();
                    plugin.config = plugin.getConfig();
                    sender.sendMessage("§8[§6MCQLINK§8] §a配置已重载！");

                    if (plugin.playerListener != null && plugin.playerListener.socks != null) {
                        plugin.playerListener.socks.close();
                    }
                    plugin.eventListenerRegister();
                    sender.sendMessage("§8[§6MCQLINK§8] §aWebSocket 连接已重启！");
                    break;

                default:
                    sender.sendMessage("§8[§6MCQLINK§8] §c未知子命令。使用 §7/mcqlink help §c查看帮助。");
                    break;
            }
            return true;
        }
        return false;
    }
}