package net.sakuragame.eternal.fishing.util;

import net.minecraft.server.v1_12_R1.EntityFishingHook;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.FishHook;

import java.lang.reflect.Field;

public class HookUtils {

    private static Field HOOK_STATE;

    static {
        try {
            HOOK_STATE = EntityFishingHook.class.getDeclaredField("av");
            HOOK_STATE.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getState(FishHook hook) {
        try {
            EntityFishingHook craftHook = (EntityFishingHook) ((CraftEntity) hook).getHandle();
            return String.valueOf(HOOK_STATE.get(craftHook));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
