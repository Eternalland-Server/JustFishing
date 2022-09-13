package net.sakuragame.eternal.fishing.util;


import com.taylorswiftcn.justwei.util.MegumiUtil;
import ink.ptms.zaphkiel.ZaphkielAPI;
import ink.ptms.zaphkiel.api.Item;
import net.sakuragame.eternal.dragoncore.util.Pair;
import net.sakuragame.eternal.fishing.file.sub.ConfigFile;
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

    public static String getZapItemID(ItemStack item) {
        if (MegumiUtil.isEmpty(item)) return null;

        Item zapItem = ZaphkielAPI.INSTANCE.getItem(item);
        if (zapItem == null) return null;

        return zapItem.getId();
    }
}
