package net.minecraft.server;

import javax.swing.JList;
import java.util.ArrayList;
import java.util.List;

public class PlayerListBox extends JList<String> implements IUpdatePlayerListBox {
	private static final byte UPDATE_INTERVAL = 30; // Rinny - use byte instead of int (update every second and half)
    private final MinecraftServer minecraftServer;
    private byte tickCount;

    public PlayerListBox(MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
        minecraftServer.a(this);
    }

    public void resize() {
        if (tickCount++ % UPDATE_INTERVAL == 0) {
            List<String> playerNames = new ArrayList<>();
            for (EntityPlayer player : minecraftServer.getPlayerList().players) {
                playerNames.add(player.getName());
            }
            setListData(playerNames.toArray(new String[0]));
            tickCount = 0;
        }
    }
}
