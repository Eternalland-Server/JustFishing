package net.sakuragame.eternal.fishing.util;


import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.taylorswiftcn.justwei.util.MegumiUtil;
import ink.ptms.zaphkiel.ZaphkielAPI;
import ink.ptms.zaphkiel.api.Item;
import net.sakuragame.eternal.dragoncore.util.Pair;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Utils {

    private final static Random RANDOM = new Random();

    public static int randomInt(int scope) {
        return RANDOM.nextInt(scope);
    }

    public static String lottery(List<Pair<String, Double>> pool) {
        List<Double> place = new ArrayList<>();

        double totalWeight = 0d;
        double value = 0d;

        for (Pair<String, Double> pair : pool) {
            totalWeight += pair.getValue();
        }

        for (Pair<String, Double> pair : pool) {
            value += pair.getValue();
            place.add(value / totalWeight);
        }

        double random = Math.random();
        place.add(random);
        Collections.sort(place);
        int index = place.indexOf(random);

        return pool.get(index).getKey();
    }

    public static String getPlayerHeldRod(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        String rodID = getZapItemID(item);
        if (rodID == null) return null;
        if (!ConfigFile.rod.contains(rodID)) return null;

        return rodID;
    }

    public static String getPlayerUseStosh(Player player) {
        int i = player.getInventory().getHeldItemSlot();
        if (i == 8) return null;

        ItemStack item = player.getInventory().getItem(i + 37);
        String stoshID = getZapItemID(item);
        if (!ConfigFile.stosh.contains(stoshID)) return null;

        return stoshID;
    }

    public static String getZapItemID(ItemStack item) {
        if (MegumiUtil.isEmpty(item)) return null;

        Item zapItem = ZaphkielAPI.INSTANCE.getItem(item);
        if (zapItem == null) return null;

        return zapItem.getId();
    }

    public static boolean isFishRegion(Location location) {
        RegionManager manager = WorldGuardPlugin.inst().getRegionManager(location.getWorld());
        ProtectedRegion region = manager.getRegion(ConfigFile.region);
        if (region == null) return false;

        ApplicableRegionSet set = manager.getApplicableRegions(location);
        for (ProtectedRegion elm : set.getRegions()) {
            if (elm.getId().equals(region.getId())) {
                return true;
            }
        }

        return false;
    }
}
