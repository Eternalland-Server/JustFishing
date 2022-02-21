package net.sakuragame.eternal.fishing.core;

import lombok.Getter;
import net.sakuragame.eternal.fishing.JustFish;
import org.bukkit.Bukkit;

import java.util.UUID;

@Getter
public class FishAccount {

    private final UUID uuid;
    private int amount;
    private boolean change;

    public FishAccount(UUID uuid) {
        this.uuid = uuid;
        this.amount = 0;
        this.change = false;
    }

    public FishAccount(UUID uuid, int amount) {
        this.uuid = uuid;
        this.amount = amount;
        this.change = false;
    }

    public void setAmount(int amount) {
        if (this.amount == amount) return;
        this.amount = amount;
        this.change = true;
    }

    public void addAmount(int i) {
        this.amount += i;
        this.change = true;
    }

    public void save() {
        if (!change) return;
        Bukkit.getScheduler().runTaskAsynchronously(JustFish.getInstance(), () -> JustFish.getStorageManager().saveData(uuid, amount));
    }
}
