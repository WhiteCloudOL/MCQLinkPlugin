package cn.meowyun.mcqlink.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import cn.meowyun.mcqlink.api.Socks;

public class PlayerBindQQCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("bindqq")){
            if(args.length == 0){
                sender.sendMessage("用法: /cmd <print|help>");
                return true;
            }
        }
        return false;
    }
}
