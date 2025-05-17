package io.noks.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import net.minecraft.server.Entity;
import net.minecraft.server.EnumProtocol;
import net.minecraft.server.IChatBaseComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;
import net.minecraft.server.PacketListener;
import net.minecraft.server.PacketPlayInAbilities;
import net.minecraft.server.PacketPlayInArmAnimation;
import net.minecraft.server.PacketPlayInBlockDig;
import net.minecraft.server.PacketPlayInBlockPlace;
import net.minecraft.server.PacketPlayInChat;
import net.minecraft.server.PacketPlayInClientCommand;
import net.minecraft.server.PacketPlayInCloseWindow;
import net.minecraft.server.PacketPlayInCustomPayload;
import net.minecraft.server.PacketPlayInEnchantItem;
import net.minecraft.server.PacketPlayInEntityAction;
import net.minecraft.server.PacketPlayInFlying;
import net.minecraft.server.PacketPlayInHeldItemSlot;
import net.minecraft.server.PacketPlayInKeepAlive;
import net.minecraft.server.PacketPlayInSetCreativeSlot;
import net.minecraft.server.PacketPlayInSettings;
import net.minecraft.server.PacketPlayInSteerVehicle;
import net.minecraft.server.PacketPlayInTabComplete;
import net.minecraft.server.PacketPlayInTransaction;
import net.minecraft.server.PacketPlayInUpdateSign;
import net.minecraft.server.PacketPlayInUseEntity;
import net.minecraft.server.PacketPlayInWindowClick;
import net.minecraft.server.PlayerConnection;
import net.minecraft.server.WorldServer;

public class DummyPlayerConnection extends PlayerConnection {
	private boolean disconnected = false;

	public DummyPlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityNPC entitynpc) {
		super(minecraftserver, networkmanager, entitynpc);
		networkmanager.a((PacketListener) this);
		entitynpc.playerConnection = this;
	}

	@Override
	public CraftPlayer getPlayer() {
		return (this.player == null) ? null : this.player.getBukkitEntity();
	}

	@Override
	public void a() {
	}

	@Override
	public NetworkManager b() {
		return this.networkManager;
	}

	@Override
	public void disconnect(String s) {
		WorldServer worldserver = this.player.r();
		worldserver.kill((Entity) this.player);
		worldserver.getPlayerChunkMap().removePlayer(this.player);
		(((CraftServer) Bukkit.getServer()).getHandle()).players.remove(this.player);
		this.disconnected = true;
	}

	@Override
	public void a(PacketPlayInSteerVehicle packetplayinsteervehicle) {
	}

	@Override
	public void a(PacketPlayInFlying packetplayinflying) {
	}

	@Override
	public void a(double d0, double d1, double d2, float f, float f1) {
	}

	@Override
	public void teleport(Location dest) {
	}

	@Override
	public void a(PacketPlayInBlockDig packetplayinblockdig) {
	}

	@Override
	public void a(PacketPlayInBlockPlace packetplayinblockplace) {
	}

	@Override
	public void a(IChatBaseComponent ichatbasecomponent) {
	}

	@Override
	public void sendPacket(Packet packet) {
	}

	@Override
	public void a(PacketPlayInHeldItemSlot packetplayinhelditemslot) {
	}

	@Override
	public void a(PacketPlayInChat packetplayinchat) {
	}

	@Override
	public void chat(String s, boolean async) {
	}

	@Override
	public void a(PacketPlayInArmAnimation packetplayinarmanimation) {
	}
	
	@Override
	public void a(PacketPlayInEntityAction packetplayinentityaction) {
	}
	
	@Override
	public void a(PacketPlayInUseEntity packetplayinuseentity) {
		super.a(packetplayinuseentity);
	}

	@Override
	public void a(PacketPlayInClientCommand packetplayinclientcommand) {
	}

	@Override
	public void a(PacketPlayInCloseWindow packetplayinclosewindow) {
	}
	
	@Override
	public void a(PacketPlayInWindowClick packetplayinwindowclick) {
	}

	@Override
	public void a(PacketPlayInEnchantItem packetplayinenchantitem) {
	}

	@Override
	public void a(PacketPlayInSetCreativeSlot packetplayinsetcreativeslot) {
	}

	@Override
	public void a(PacketPlayInTransaction packetplayintransaction) {
	}

	@Override
	public void a(PacketPlayInUpdateSign packetplayinupdatesign) {
	}

	@Override
	public void a(PacketPlayInKeepAlive packetplayinkeepalive) {
	}

	@Override
	public void a(PacketPlayInAbilities packetplayinabilities) {
	}

	@Override
	public void a(PacketPlayInTabComplete packetplayintabcomplete) {
	}

	@Override
	public void a(PacketPlayInSettings packetplayinsettings) {
	}

	@Override
	public void a(PacketPlayInCustomPayload packetplayincustompayload) {
	}

	@Override
	public void a(EnumProtocol enumprotocol, EnumProtocol enumprotocol1) {
	}
}