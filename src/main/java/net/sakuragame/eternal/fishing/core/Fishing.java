package net.sakuragame.eternal.fishing.core;

import com.taylorswiftcn.megumi.uifactory.generate.function.Statements;
import net.sakuragame.eternal.dragoncore.network.PacketSender;
import net.sakuragame.eternal.fishing.util.Utils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Fishing extends BukkitRunnable {

    private final Player player;
    private final int goal;
    private int current;
    private final double time;
    private final long start;

    public Fishing(Player player, int goal, double time) {
        this.player = player;
        this.goal = goal;
        this.current = (goal > 39.5) ? goal - 30 : goal + 30;
        this.time = time;
        this.start = System.currentTimeMillis();

        this.start(current);
    }

    private void start(int pointer) {
        Statements statements = new Statements()
                .add("func.Component_Set('goal', 'x', 'track.x+" + this.goal + "');")
                .add("func.Component_Set('pointer', 'x', 'track.x+" + pointer + "');")
                .add("func.Component_Set('icon', 'visible', '0');");

        PacketSender.sendRunFunction(player, Fishery.SCREEN_ID, statements.build(), false);
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }

        String param;

        int offset = Utils.randomInt(25);
        Direction force = getForce();

        if (force != Direction.NONE) {
            if (force == Direction.LEFT) {
                param = "track.x+" + (current - offset) + "+" + offset + "*(var.time-(func.Time_Current - var.start))/var.time";
                current = current - offset;
            }
            else {
                param = "track.x+" + current + "+" + offset + "*(func.Time_Current - var.start)/var.time";
                current = current + offset;
            }
        }
        else {
            double attractWeight = getLifeSecond() / time;
            if (Math.random() < attractWeight) {
                if (goal > current) {
                    offset = (current + offset > 74) ? (74 - current) : offset;
                    param = "track.x+" + current + "+" + offset + "*(func.Time_Current - var.start)/var.time";
                    current = current + offset;
                }
                else {
                    offset = (current - offset < 0) ? current : offset;
                    param = "track.x+" + (current - offset) + "+" + offset + "*(var.time-(func.Time_Current - var.start))/var.time";
                    current = current - offset;
                }
            }
            else {
                if (Math.random() < 0.25) {
                    offset = (current + offset > 74) ? (74 - current) : offset;
                    param = "track.x+" + current + "+" + offset + "*(func.Time_Current - var.start)/var.time";
                    current = current + offset;
                }
                else {
                    offset = (current - offset < 0) ? current : offset;
                    param = "track.x+" + (current - offset) + "+" + offset + "*(var.time-(func.Time_Current - var.start))/var.time";
                    current = current - offset;
                }
            }
        }

        Statements statements = new Statements()
                .add("var.start = func.Time_Current;")
                .add("func.Component_Set('pointer', 'x', '" + param + "');")
                .add("func.delay(var.time);")
                .add("func.Component_Set('pointer', 'x', 'track.x+" + current + "');");

        PacketSender.sendRunFunction(player, Fishery.SCREEN_ID, statements.build(), true);

    }

    private int getLifeSecond() {
        return (int) (System.currentTimeMillis() - start) / 1000;
    }

    private Direction getForce() {
        switch (current) {
            case 0: return Direction.RIGHT;
            case 74: return Direction.LEFT;
        }
        return Direction.NONE;
    }

    private enum Direction {
        NONE,
        LEFT,
        RIGHT
    }
}
