package net.sakuragame.eternal.fishing.api.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class FishCaughtEvent extends JustEvent {

    private final String fishID;
    private final ItemStack fishItem;

    public FishCaughtEvent(Player who, String fishID, ItemStack fishItem) {
        super(who);
        this.fishID = fishID;
        this.fishItem = fishItem;
    }
}
