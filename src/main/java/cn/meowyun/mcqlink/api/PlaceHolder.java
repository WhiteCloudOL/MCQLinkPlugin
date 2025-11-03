package cn.meowyun.mcqlink.api;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;

public class PlaceHolder {
    public boolean isPlaceHolderAPIEnabled = false;

    public PlaceHolder(){
        isPlaceHolderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public boolean checkPlaceHolderAPI(){
        return isPlaceHolderAPIEnabled;
    }
}
