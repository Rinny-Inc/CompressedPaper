package net.minecraft.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
// CraftBukkit start
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.LazyPlayerSet;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerHeadMoveEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.server.AntiCheatEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

import io.noks.utils.EntityNPC;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.util.com.google.common.base.Charsets;
import net.minecraft.util.io.netty.buffer.Unpooled;
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;

public class PlayerConnection implements PacketPlayInListener {

	private static final Logger logger = LogManager.getLogger();
	public final NetworkManager networkManager;
	private final MinecraftServer minecraftServer;
	public EntityPlayer player;
	private int e;
	private int f;
	// private boolean g;
	private int h;
	private long i;
	private long k;
	private int x;
	private IntHashMap n;
	private double y;
	private double z;
	private double q;
	public boolean checkMovement; // CraftBukkit - private -> public
	private boolean processedDisconnect; // CraftBukkit - added
	private boolean disconnecting; // KigPaper - added

	public PlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
		this.minecraftServer = minecraftserver;
		this.networkManager = networkmanager;
		networkmanager.a((PacketListener) this);
		this.player = entityplayer;
		this.n = new IntHashMap();
		this.checkMovement = true;
		this.lastDropTick = MinecraftServer.currentTick;
		this.dropCount = 0;
		entityplayer.playerConnection = this;

		// CraftBukkit start - add fields and methods
		this.server = minecraftserver.server;
	}

	public PlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityNPC entitynpc) {
		this.minecraftServer = minecraftserver;
		this.networkManager = networkmanager;
		this.player = entitynpc;
		// CraftBukkit start - add fields and methods
		this.server = minecraftserver.server;
	}

	private final org.bukkit.craftbukkit.CraftServer server;
	// private int lastTick = MinecraftServer.currentTick;
	private int lastDropTick;
	private int dropCount;
	private static final int SURVIVAL_PLACE_DISTANCE_SQUARED = 6 * 6;
	private static final int CREATIVE_PLACE_DISTANCE_SQUARED = 7 * 7;

	// Get position of last block hit for BlockDamageLevel.STOPPED
	private double lastPosX = Double.MAX_VALUE;
	private double lastPosY = Double.MAX_VALUE;
	private double lastPosZ = Double.MAX_VALUE;
	private float lastPitch = Float.MAX_VALUE;
	private float lastYaw = Float.MAX_VALUE;
	private boolean justTeleported = false;
	private boolean hasMoved; // Spigot

	// For the PacketPlayOutBlockPlace hack :(
	Long lastPacket;

	// Store the last block right clicked and what type it was
	private Item lastMaterial;

	public CraftPlayer getPlayer() {
		return (this.player == null) ? null : (CraftPlayer) this.player.getBukkitEntity();
	}

	private final static HashSet<Integer> invalidItems = new HashSet<Integer>(
			java.util.Arrays.asList(8, 9, 10, 11, 26, 34, 36, 43, 51, 52, 55, 59, 60, 62, 63, 64, 68, 71, 74, 75, 83,
					90, 92, 93, 94, 104, 105, 115, 117, 118, 119, 125, 127, 132, 140, 141, 142, 144)); // TODO: Check
																										// after every
																										// update.
	// CraftBukkit end

	public void a() {
		// this.g = false;
		if (this.networkManager.getVersion() >= 107) {
		      double x = this.player.locX;
		      double y = this.player.locY;
		      double z = this.player.locZ;
		      boolean onGround = this.player.onGround;
		      this.player.i();
		      this.player.setLocation(x, y, z, this.player.yaw, this.player.pitch);
		      this.player.onGround = onGround;
		} 
		++this.e;
		if ((long) this.e - this.k > 40L) {
			this.k = (long) this.e;
			this.i = this.getCurrentMillis();
			this.h = (int) this.i;
			this.sendPacket(new PacketPlayOutKeepAlive(this.h));
		}

		// CraftBukkit start
		// for (int spam; (spam = this.chatThrottle) > 0 &&
		// !chatSpamField.compareAndSet(this, spam, spam - 1); ) ; // Rinny - removed
		/*
		 * Use thread-safe field access instead if (this.chatThrottle > 0) {
		 * --this.chatThrottle; }
		 */
		// CraftBukkit end

		if (this.x > 0) {
			--this.x;
		}

		if (this.player.x() > 0L && this.minecraftServer.getIdleTimeout() > 0 && MinecraftServer.ar()
				- this.player.x() > (long) (this.minecraftServer.getIdleTimeout() * 1000 * 60)) {
			this.disconnect("You have been idle for too long!");
		}
	}

	public NetworkManager b() {
		return this.networkManager;
	}

	public void disconnect(String s) {
		// KigPaper - if already disconnecting, do nothing
		if (disconnecting)
			return;
		// CraftBukkit start - fire PlayerKickEvent
		final String leaveMessage = EnumChatFormat.YELLOW + this.player.getName() + " left the game.";

		final PlayerKickEvent event = new PlayerKickEvent(this.server.getPlayer(this.player), s, leaveMessage);

		if (this.server.getServer().isRunning()) {
			this.server.getPluginManager().callEvent(event);
		}

		if (event.isCancelled()) {
			// Do not kick the player
			return;
		}
		// KigPaper - mark the player as disconnecting, so all other disconnect requests
		// are ignored.
		disconnecting = true;
		// Send the possibly modified leave message
		s = event.getReason();
		// CraftBukkit end
		final ChatComponentText chatcomponenttext = new ChatComponentText(s);

		this.networkManager.handle(new PacketPlayOutKickDisconnect(chatcomponenttext),
				new GenericFutureListener[] { new PlayerConnectionFuture(this, chatcomponenttext) });
		this.a(chatcomponenttext); // CraftBukkit - Process quit immediately
		this.networkManager.g();
	}

	public void a(PacketPlayInSteerVehicle packetplayinsteervehicle) {
		this.player.a(packetplayinsteervehicle.c(), packetplayinsteervehicle.d(), packetplayinsteervehicle.e(),
				packetplayinsteervehicle.f());
	}

	public void a(PacketPlayInFlying packetplayinflying) {
		// CraftBukkit start - Check for NaN
		if (Double.isNaN(packetplayinflying.x) || Double.isNaN(packetplayinflying.y)
				|| Double.isNaN(packetplayinflying.z) || Double.isNaN(packetplayinflying.stance)) {
			logger.warn(player.getName() + " was caught trying to crash the server with an invalid position.");
			getPlayer().kickPlayer("NaN in position (Hacking?)"); // Spigot "Nope" -> Descriptive reason
			return;
		}
		// CraftBukkit end
		final WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

		// this.g = true;
		if (!this.player.viewingCredits && !this.player.dead) {
			double packetY;

			if (!this.checkMovement) {
				packetY = packetplayinflying.d() - this.z;
				if (packetplayinflying.c() == this.y && packetY * packetY < 0.01D && packetplayinflying.e() == this.q) {
					this.checkMovement = true;
				}
			}

			// CraftBukkit start - fire PlayerMoveEvent
			final Player player = this.getPlayer();
			// Spigot Start
			if (!hasMoved) {
				Location curPos = player.getLocation();
				lastPosX = curPos.getX();
				lastPosY = curPos.getY();
				lastPosZ = curPos.getZ();
				lastYaw = curPos.getYaw();
				lastPitch = curPos.getPitch();
				hasMoved = true;
			}
			// Spigot End
			final Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw, lastPitch); // Get
																														// the
																														// Players
																														// previous
																														// Event
																														// location.
			final Location to = player.getLocation().clone(); // Start off the To location as the Players current
																// location.
			final boolean wasOnGround = this.player.onGround;

			// If the packet contains movement information then we update the To location
			// with the correct XYZ.
			if (packetplayinflying.hasPos && !(packetplayinflying.hasPos && packetplayinflying.y == -999.0D
					&& packetplayinflying.stance == -999.0D)) {
				to.setX(packetplayinflying.x);
				to.setY(packetplayinflying.y);
				to.setZ(packetplayinflying.z);
			}

			// If the packet contains look information then we update the To location with
			// the correct Yaw & Pitch.
			if (packetplayinflying.hasLook) {
				to.setYaw(packetplayinflying.yaw);
				to.setPitch(packetplayinflying.pitch);
			}

			if (this.checkMovement) {
				// Prevent 40 event-calls for less than a single pixel of movement >.>
				final double delta = Math.pow(this.lastPosX - to.getX(), 2) + Math.pow(this.lastPosY - to.getY(), 2) + Math.pow(this.lastPosZ - to.getZ(), 2);
				final float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());

				if (deltaAngle > 7.5f) {
					this.lastYaw = to.getYaw();
					this.lastPitch = to.getPitch();

					final PlayerHeadMoveEvent headMoveEvent = new PlayerHeadMoveEvent(player, from, to);
					this.server.getPluginManager().callEvent(headMoveEvent);

					// If the event is cancelled we move the player back to their old location.
					if (headMoveEvent.isCancelled()) {
						this.player.playerConnection.sendPacket(new PacketPlayOutPosition(from.getX(),
								from.getY() + 1.6200000047683716D, from.getZ(), from.getYaw(), from.getPitch(), false));
					} else {
						final Location oldTo = to.clone();
						if (!oldTo.equals(headMoveEvent.getTo())) {
							this.player.getBukkitEntity().teleport(headMoveEvent.getTo(),
									PlayerTeleportEvent.TeleportCause.UNKNOWN);
						}
					}
				}

				if (delta > 1f / 256) {
					this.lastPosX = to.getX();
					this.lastPosY = to.getY();
					this.lastPosZ = to.getZ();

					// Skip the first time we do this
					// if (true) { // Spigot - don't skip any move events
					final PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
					this.server.getPluginManager().callEvent(event);

					// If the event is cancelled we move the player back to their old location.
					if (event.isCancelled()) {
						this.player.playerConnection.sendPacket(new PacketPlayOutPosition(from.getX(),
								from.getY() + 1.6200000047683716D, from.getZ(), from.getYaw(), from.getPitch(), false));
						return;
					} else {
						final Location oldTo = to.clone(); // PaperSpigot
						/*
						 * If a Plugin has changed the To destination then we teleport the Player there
						 * to avoid any 'Moved wrongly' or 'Moved too quickly' errors. We only do this
						 * if the Event was not cancelled.
						 */
						if (!oldTo.equals(event.getTo()) && !event.isCancelled()) { // PaperSpigot
							this.player.getBukkitEntity().teleport(event.getTo(),
									PlayerTeleportEvent.TeleportCause.UNKNOWN);
							return;
						}
					}

					/*
					 * Check to see if the Players Location has some how changed during the call of
					 * the event. This can happen due to a plugin teleporting the player instead of
					 * using .setTo()
					 */
					if (!from.equals(this.getPlayer().getLocation()) && this.justTeleported) {
						this.justTeleported = false;
						return;
					}
					// }
				}
				// CraftBukkit end
				double d1;
				double d2;
				double d3;

				final boolean ground = packetplayinflying.i();
				if (this.player.vehicle != null) {
					float f = this.player.yaw;
					float f1 = this.player.pitch;

					this.player.vehicle.ac();
					d1 = this.player.locX;
					d2 = this.player.locY;
					d3 = this.player.locZ;
					if (packetplayinflying.k()) {
						f = packetplayinflying.g();
						f1 = packetplayinflying.h();
					}

					this.player.onGround = ground;
					if (this.networkManager.getVersion() < 107) {
			            this.player.i(); 
					}
					this.player.V = 0.0F;
					this.player.setLocation(d1, d2, d3, f, f1);
					if (this.player.vehicle != null) {
						this.player.vehicle.ac();
					}

					this.minecraftServer.getPlayerList().d(this.player);
					if (this.checkMovement) {
						this.y = this.player.locX;
						this.z = this.player.locY;
						this.q = this.player.locZ;
					}

					worldserver.playerJoinedWorld(this.player);
					return;
				}

				if (this.player.isSleeping()) {
					if (this.networkManager.getVersion() < 107) {
			            this.player.i();
					}
					this.player.setLocation(this.y, this.z, this.q, this.player.yaw, this.player.pitch);
					worldserver.playerJoinedWorld(this.player);
					return;
				}

				packetY = this.player.locY;
				this.y = this.player.locX;
				this.z = this.player.locY;
				this.q = this.player.locZ;
				d1 = this.player.locX;
				d2 = this.player.locY;
				d3 = this.player.locZ;
				float f2 = this.player.yaw;
				float f3 = this.player.pitch;

				if (packetplayinflying.j() && packetplayinflying.d() == -999.0D && packetplayinflying.f() == -999.0D) {
					packetplayinflying.a(false);
				}

				double d4;

				if (packetplayinflying.j()) {
					d1 = packetplayinflying.c();
					d2 = packetplayinflying.d();
					d3 = packetplayinflying.e();
					d4 = packetplayinflying.f() - packetplayinflying.d();
					if (!this.player.isSleeping() && (d4 > 1.65D || d4 < 0.1D)) {
						this.disconnect("Illegal stance");
						logger.warn(this.player.getName() + " had an illegal stance: " + d4);
						return;
					}

					if (Math.abs(packetplayinflying.c()) > 3.2E7D || Math.abs(packetplayinflying.e()) > 3.2E7D) {
						this.disconnect("Illegal position");
						return;
					}
				}

				if (packetplayinflying.k()) {
					f2 = packetplayinflying.g();
					f3 = packetplayinflying.h();
				}

				if (this.networkManager.getVersion() < 107) {
					this.player.i();
				}
				this.player.V = 0.0F;
				this.player.setLocation(this.y, this.z, this.q, f2, f3);
				if (!this.checkMovement) {
					return;
				}

				d4 = d1 - this.player.locX;
				final double d5 = d2 - this.player.locY;
				final double d6 = d3 - this.player.locZ;
				// CraftBukkit start - min to max
				/*
				 * final double d7 = Math.max(Math.abs(d4), Math.abs(this.player.motX)); final
				 * double d8 = Math.max(Math.abs(d5), Math.abs(this.player.motY)); final double
				 * d9 = Math.max(Math.abs(d6), Math.abs(this.player.motZ)); // CraftBukkit end
				 * double d10 = d7 * d7 + d8 * d8 + d9 * d9;
				 * 
				 * // Spigot: make "moved too quickly" limit configurable // TODO: fix the
				 * rollback due to SLAB if (d10 >
				 * org.spigotmc.SpigotConfig.movedTooQuicklyThreshold && this.player.onGround &&
				 * this.player.noDamageTicks == 0 && (!this.minecraftServer.N() ||
				 * !this.minecraftServer.M().equals(this.player.getName()))) { // CraftBukkit -
				 * Added this.checkMovement condition to solve this check being triggered by
				 * teleports logger.warn(this.player.getName() + " moved too quickly! " + d4 +
				 * "," + d5 + "," + d6 + " (" + d7 + ", " + d8 + ", " + d9 + ")");
				 * this.a(this.y, this.z, this.q, this.player.yaw, this.player.pitch); return; }
				 */

				final float f4 = 0.0625F;
				final boolean blocksEmptyBefore = worldserver
						.getCubes(this.player,
								this.player.boundingBox.clone().shrink((double) f4, (double) f4, (double) f4))
						.isEmpty();

				if (this.player.onGround && !packetplayinflying.i() && d5 > 0.0D) {
					this.player.bj();
				}

				// ABOVE HERE IS BEFORE THE MOVEMENT SERVER ACTION

				this.player.move(d4, d5, d6);
				this.player.onGround = ground;
				this.player.checkMovement(d4, d5, d6);
				final double d11 = d5;

				// BELOW HERE IS AFTER THE MOVEMENT SERVER ACTION

				/*
				 * d4 = d1 - this.player.locX; d5 = d2 - this.player.locY; if (d5 > -0.5D || d5
				 * < 0.5D) { d5 = 0.0D; }
				 * 
				 * d6 = d3 - this.player.locZ; d10 = d4 * d4 + d5 * d5 + d6 * d6; boolean flag1
				 * = false;
				 * 
				 * // Spigot: make "moved wrongly" limit configurable if (d10 >
				 * org.spigotmc.SpigotConfig.movedWronglyThreshold && this.player.onGround &&
				 * !this.player.isSleeping() && !this.player.playerInteractManager.isCreative()
				 * && this.player.noDamageTicks == 0) { flag1 = true;
				 * logger.warn(this.player.getName() + " moved wrongly!"); }
				 */
				final boolean velocityChanged = this.player.velocityChanged;

				this.player.setLocation(d1, d2, d3, f2, f3);
				if (!velocityChanged) {
					final boolean blocksEmptyAfter = worldserver
							.getCubes(this.player,
									this.player.boundingBox.clone().shrink((double) f4, (double) f4, (double) f4))
							.isEmpty();

					// TODO: that's what makes block collision
					if (blocksEmptyBefore && !blocksEmptyAfter && !this.player.isSleeping()) {
						this.a(this.y, this.z, this.q, f2, f3);
						return;
					}
				}

				final AxisAlignedBB axisalignedbb = this.player.boundingBox.clone()
						.grow((double) f4, (double) f4, (double) f4).a(0.0D, -0.55D, 0.0D);
				final boolean touchingAir = !worldserver.c(axisalignedbb);
				if (this.player.vehicle == null && !this.minecraftServer.getAllowFlight()
						&& !this.player.abilities.canFly && !this.player.onGround && touchingAir && !velocityChanged) { // CraftBukkit
																														// -
																														// check
																														// abilities
																														// instead
																														// of
																														// creative
																														// mode
																														// //
																														// Rinny
																														// fix
																														// kicked
																														// for
																														// flying
																														// while
																														// combo
																														// in
																														// practice
					if (d11 >= -0.03125D) {
						++this.f;
						if (this.f > 80) {
							final String msg = String.format("%s was caught using Fly",
									new Object[] { this.player.getName() });
							this.server.getPluginManager().callEvent(
									new AntiCheatEvent(this.player.getBukkitEntity(), AntiCheatEvent.Type.FLY, msg));
							return;
						}
					}
				} else {
					this.f = 0;
				}

				this.player.onGround = ground;
				this.minecraftServer.getPlayerList().d(this.player);
				if (wasOnGround != this.player.onGround) {
					org.bukkit.event.player.PlayerOnGroundEvent onGroundEvent = new org.bukkit.event.player.PlayerOnGroundEvent(
							player, this.player.onGround);
					this.server.getPluginManager().callEvent(onGroundEvent);
				}
				this.player.b(this.player.locY - packetY, ground);
			} else if (this.e % 20 == 0) {
				this.a(this.y, this.z, this.q, this.player.yaw, this.player.pitch);
			}
		}
	}

	public void a(final double d0, final double d1, final double d2, final float f, final float f1) {
		// CraftBukkit start - Delegate to teleport(Location)
		final Player player = this.getPlayer();
		final PlayerTeleportEvent event = new PlayerTeleportEvent(player, player.getLocation(),
				new Location(this.getPlayer().getWorld(), d0, d1, d2, f, f1),
				PlayerTeleportEvent.TeleportCause.UNKNOWN);
		this.server.getPluginManager().callEvent(event);
		final Location to = event.isCancelled() ? event.getFrom() : event.getTo();
		this.teleport(to);
	}

	public void teleport(Location dest) {
		final double d0 = dest.getX();
		final double d1 = dest.getY();
		final double d2 = dest.getZ();
		float f = dest.getYaw();
		float f1 = dest.getPitch();

		// TODO: make sure this is the best way to address this.
		if (Float.isNaN(f)) {
			f = this.player.yaw; // Rinny replaced 0
		}
		if (Float.isNaN(f1)) {
			f1 = this.player.pitch; // Rinny replaced 0
		}

		this.lastPosX = d0;
		this.lastPosY = d1;
		this.lastPosZ = d2;
		this.lastYaw = f;
		this.lastPitch = f1;
		this.justTeleported = true;
		// CraftBukkit end

		this.checkMovement = false;
		this.y = d0;
		this.z = d1;
		this.q = d2;
		final World oldWorld = this.player.getWorld();

		this.player.setLocation(d0, d1, d2, f, f1);
		this.player.playerConnection
				.sendPacket(new PacketPlayOutPosition(d0, d1 + 1.6200000047683716D, d2, f, f1, false));
		if (this.player.bossBarMessage != null && oldWorld != dest.getWorld()) { // TODO: check if it does shit
			final String message = this.player.bossBarMessage;
			this.player.removeBossBar();
			this.player.setBossBar(message, this.player.bossBarHealth);
		}
	}

	public void a(PacketPlayInBlockDig packetplayinblockdig) {
		if (this.player.dead)
			return; // CraftBukkit
		final WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

		this.player.resetIdleTimer();
		switch (packetplayinblockdig.g()) {
			case 4: {
				// CraftBukkit start - limit how quickly items can be dropped
				// If the ticks aren't the same then the count starts from 0 and we update the
				// lastDropTick.
				if (this.lastDropTick != MinecraftServer.currentTick) {
					this.dropCount = 0;
					this.lastDropTick = MinecraftServer.currentTick;
				} else {
					// Else we increment the drop count and check the amount.
					this.dropCount++;
					if (this.dropCount >= 10) { // Rinny - lower to 20
						logger.warn(this.player.getName() + " dropped their items too quickly!");
						this.disconnect("You dropped your items too quickly (Hacking?)");
						return;
					}
				}
				// CraftBukkit end
				this.player.a(false);
				return;
			}
			case 3: {
				this.player.a(true);
				return;
			}
			case 5: {
				this.player.bA();
				return;
			}
			default: {
				break;
			}
		}
		boolean flag = false;

		if (packetplayinblockdig.g() == 0 || packetplayinblockdig.g() == 1 || packetplayinblockdig.g() == 2) {
			flag = true;
		}

		final int i = packetplayinblockdig.c();
		final int j = packetplayinblockdig.d();
		final int k = packetplayinblockdig.e();

		if (flag) {
			final double d0 = this.player.locX - ((double) i + 0.5D);
			final double d1 = this.player.locY - ((double) j + 0.5D) + 1.5D;
			final double d2 = this.player.locZ - ((double) k + 0.5D);
			final double d3 = d0 * d0 + d1 * d1 + d2 * d2;

			if (d3 > 36.0D) {
				if (d3 < MathHelper.pow2(player.viewDistance * 16) && worldserver.isChunkLoaded(i >> 4, k >> 4)) { // nPaper
																													// check
																													// only
																													// in
																													// player's
																													// view
																													// distance
					this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(i, j, k, worldserver)); // Paper
																													// -
																													// Fix
																													// block
																													// break
																													// desync
				}
				return;
			}
			if (j >= this.minecraftServer.getMaxBuildHeight()) {
				return;
			}
		}
		switch (packetplayinblockdig.g()) {
		case 0 -> {
			if (!this.minecraftServer.a(worldserver, i, j, k, this.player)) {
				this.player.playerInteractManager.dig(i, j, k, packetplayinblockdig.f());
			} else {
				// CraftBukkit start - fire PlayerInteractEvent
				CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, i, j, k,
						packetplayinblockdig.f(), this.player.inventory.getItemInHand());
				this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(i, j, k, worldserver));
				// Update any tile entity data for this block
				final TileEntity tileentity = worldserver.getTileEntity(i, j, k);
				if (tileentity != null) {
					this.player.playerConnection.sendPacket(tileentity.getUpdatePacket());
				}
				// CraftBukkit end
			}
			return;
		}
		case 2 -> {
			this.player.playerInteractManager.a(i, j, k);
			if (worldserver.getType(i, j, k).getMaterial() != Material.AIR) {
				this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(i, j, k, worldserver));
			}
			return;
		}
		case 1 -> {
			this.player.playerInteractManager.c(i, j, k);
			if (worldserver.getType(i, j, k).getMaterial() != Material.AIR) {
				this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(i, j, k, worldserver));
			}
			return;
		}
		}
	}

	// Spigot start - limit place/interactions
	private long lastPlace = -1;
	private int packets = 0;

	public void a(PacketPlayInBlockPlace packetplayinblockplace) {
		boolean throttled = false;
		// PaperSpigot - Allow disabling the player interaction limiter
		final long timeStamp = packetplayinblockplace.timestamp;
		if (org.github.paperspigot.PaperSpigotConfig.interactLimitEnabled && lastPlace != -1 && timeStamp - lastPlace < 30 && packets++ >= 4) {
			throttled = true;
		} else if (timeStamp - lastPlace >= 30 || lastPlace == -1) {
			lastPlace = timeStamp;
			packets = 0;
		}
		// Spigot end
		final WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

		// CraftBukkit start
		if (this.player.dead)
			return;

		// This is a horrible hack needed because the client sends 2 packets on 'right
		// mouse click'
		// aimed at a block. We shouldn't need to get the second packet if the data is
		// handled
		// but we cannot know what the client will do, so we might still get it
		//
		// If the time between packets is small enough, and the 'signature' similar, we
		// discard the
		// second one. This sadly has to remain until Mojang makes their packets saner.
		// :(
		// -- Grum
		final int face = packetplayinblockplace.getFace(); // Rinny - moved: why cached it later when we can cache it
															// sooner
		if (face == 255) {
			if (packetplayinblockplace.getItemStack() != null
					&& packetplayinblockplace.getItemStack().getItem() == this.lastMaterial && this.lastPacket != null
					&& timeStamp - this.lastPacket < 100) {
				this.lastPacket = null;
				return;
			}
			if (this.networkManager.getVersion() >= 107 && this.lastPacket != null && packetplayinblockplace.timestamp - this.lastPacket.longValue() < 100L) {
		        this.lastPacket = null;
		        return;
			} 
		} else {
			this.lastMaterial = packetplayinblockplace.getItemStack() == null ? null
					: packetplayinblockplace.getItemStack().getItem();
			this.lastPacket = timeStamp;
		}
		// CraftBukkit - if rightclick decremented the item, always send the update
		// packet.
		// this is not here for CraftBukkit's own functionality; rather it is to fix
		// a notch bug where the item doesn't update correctly.
		boolean always = false;
		// CraftBukkit end

		ItemStack itemstack = this.player.inventory.getItemInHand();
		boolean flag = false;
		int x = packetplayinblockplace.c();
		int y = packetplayinblockplace.d();
		int z = packetplayinblockplace.e();
		// final int face = packetplayinblockplace.getFace(); // Rinny - moved up

		this.player.resetIdleTimer();
		if (packetplayinblockplace.getFace() == 255) {
			if (itemstack == null) {
				return;
			}

			// CraftBukkit start
			final int itemstackAmount = itemstack.count;
			// Spigot start - skip the event if throttled
			if (!throttled) {
				final org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory
						.callPlayerInteractEvent(this.player, Action.RIGHT_CLICK_AIR, itemstack);
				if (event.useItemInHand() != Event.Result.DENY) {
					this.player.playerInteractManager.useItem(this.player, this.player.world, itemstack);
				}
			} else if (MinecraftServer.currentTick - this.lastDropTick > 1) { // Nacho - Fix eat while running
				this.player.playerInteractManager.useItem(this.player, this.player.world, itemstack);
			}
			// Spigot end

			// CraftBukkit - notch decrements the counter by 1 in the above method with
			// food,
			// snowballs and so forth, but he does it in a place that doesn't cause the
			// inventory update packet to get sent
			always = (itemstack.count != itemstackAmount) || itemstack.getItem() == Item.getItemOf(Blocks.WATER_LILY);
			// CraftBukkit end
		} else if (y >= this.minecraftServer.getMaxBuildHeight() - 1
				&& (face == 1 || y >= this.minecraftServer.getMaxBuildHeight())) { // Rinny - CRAFTBUKKIT DIDNT use
																					// variable WTF??!?!?!?
			final ChatMessage chatmessage = new ChatMessage("build.tooHigh",
					new Object[] { Integer.valueOf(this.minecraftServer.getMaxBuildHeight()) });

			chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
			this.player.playerConnection.sendPacket(new PacketPlayOutChat(chatmessage));
			flag = true;
		} else {
			// CraftBukkit start - Check if we can actually do something over this large a
			// distance
			final Location eyeLoc = this.getPlayer().getEyeLocation();
			final double reachDistance = Math.pow(eyeLoc.getX() - x, 2) + Math.pow(eyeLoc.getY() - y, 2)
					+ Math.pow(eyeLoc.getZ() - z, 2);
			if (reachDistance > (this.getPlayer().getGameMode() == org.bukkit.GameMode.CREATIVE
					? CREATIVE_PLACE_DISTANCE_SQUARED
					: SURVIVAL_PLACE_DISTANCE_SQUARED)) {
				return;
			}

			if (throttled || !this.player.playerInteractManager.interact(this.player, worldserver, itemstack, x, y, z,
					face, packetplayinblockplace.h(), packetplayinblockplace.i(), packetplayinblockplace.j())) { // Spigot
																													// -
																													// skip
																													// the
																													// event
																													// if
																													// throttled
				always = true; // force PacketPlayOutSetSlot to be sent to client to update ItemStack count
				// this.sendPacket(new
				// PacketPlayOutSetSlot(this.player.activeContainer.windowId,
				// this.player.inventory.itemInHandIndex, itemstack));
			}
			// CraftBukkit end

			flag = true;
		}

		if (flag) {
			this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(x, y, z, worldserver));
			switch (face) {
			case 0 -> --y;
			case 1 -> ++y;
			case 2 -> --z;
			case 3 -> ++z;
			case 4 -> --x;
			case 5 -> ++x;
			}
			this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(x, y, z, worldserver));
		}

		itemstack = this.player.inventory.getItemInHand();
		if (itemstack != null && itemstack.count == 0) {
			this.player.inventory.items[this.player.inventory.itemInHandIndex] = null;
			itemstack = null;
		}

		if (itemstack == null || itemstack.n() == 0) {
			this.player.g = true;
			this.player.inventory.items[this.player.inventory.itemInHandIndex] = ItemStack
					.b(this.player.inventory.items[this.player.inventory.itemInHandIndex]);
			final Slot slot = this.player.activeContainer.getSlot((IInventory) this.player.inventory,
					this.player.inventory.itemInHandIndex);

			this.player.activeContainer.b();
			this.player.g = false;
			// CraftBukkit - CHECK IF NEEDED -- new if structure might not need 'always'.
			// Kept it in for now, but may be able to remove in future > Rinny removed
			// always var
			if (!ItemStack.matches(this.player.inventory.getItemInHand(), packetplayinblockplace.getItemStack())
					|| always) {
				this.sendPacket(new PacketPlayOutSetSlot(this.player.activeContainer.windowId, slot.rawSlotIndex,
						this.player.inventory.getItemInHand()));
			}
		}
	}

	public void a(IChatBaseComponent ichatbasecomponent) {
		// CraftBukkit start - Rarely it would send a disconnect line twice
		if (this.processedDisconnect) {
			return;
		}
		this.processedDisconnect = true;
		// CraftBukkit end
		logger.info(this.player.getName() + " lost connection: " + ichatbasecomponent.c()); // CraftBukkit - Don't
																							// toString the component
		this.minecraftServer.az();
		// CraftBukkit start - Replace vanilla quit message handling with our own.
		/*
		 * ChatMessage chatmessage = new ChatMessage("multiplayer.player.left", new
		 * Object[] { this.player.getScoreboardDisplayName()});
		 * 
		 * chatmessage.getChatModifier().setColor(EnumChatFormat.YELLOW);
		 * this.minecraftServer.getPlayerList().sendMessage(chatmessage);
		 */

		this.player.n();
		final String quitMessage = this.minecraftServer.getPlayerList().disconnect(this.player, ichatbasecomponent.c());
		if ((quitMessage != null) && (quitMessage.length() > 0)) {
			this.minecraftServer.getPlayerList().sendMessage(CraftChatMessage.fromString(quitMessage));
		}
		// CraftBukkit end
		if (this.minecraftServer.N() && this.player.getName().equals(this.minecraftServer.M())) {
			logger.info("Stopping singleplayer server as player logged out");
			this.minecraftServer.safeShutdown();
		}
	}

	public void sendPacket(Packet packet) {
		if (packet == null || this.processedDisconnect) {
			return;
		}
		if (packet instanceof PacketPlayOutEntityVelocity && player.vehicle != null) { // Rinny - don't send Velocity
																						// when riding
			return;
		}
		// Spigot start - protocol patch
		if (NetworkManager.a(networkManager).attr(NetworkManager.protocolVersion).get() >= 17) {
			if (packet instanceof PacketPlayOutWindowItems items) { // Rinny
				if (player.activeContainer instanceof ContainerEnchantTable
						&& player.activeContainer.windowId == items.a) {
					final ItemStack[] old = items.b;
					items.b = new ItemStack[old.length + 1];
					items.b[0] = old[0];
					System.arraycopy(old, 1, items.b, 2, old.length - 1);
					items.b[1] = new ItemStack(Items.INK_SACK, 3, 4);
				}
			} else if (packet instanceof PacketPlayOutSetSlot items) { // Rinny
				if (player.activeContainer instanceof ContainerEnchantTable
						&& player.activeContainer.windowId == items.a) {
					if (items.b >= 1) {
						items.b++;
					}
				}
			}
		}
		if (this.networkManager.getVersion() >= 107) {
	        if (packet instanceof PacketPlayOutWindowItems) {
	          PacketPlayOutWindowItems items = (PacketPlayOutWindowItems)packet;
	          if (this.player.activeContainer instanceof ContainerBrewingStand && this.player.activeContainer.windowId == items.a) {
	            ItemStack[] old = items.b;
	            items.b = new ItemStack[old.length + 1];
	            System.arraycopy(old, 0, items.b, 0, 4);
	            System.arraycopy(old, 4, items.b, 5, old.length - 4);
	            items.b[4] = new ItemStack(Items.BLAZE_POWDER);
	          } 
	        } else if (packet instanceof PacketPlayOutSetSlot) {
	          PacketPlayOutSetSlot items = (PacketPlayOutSetSlot)packet;
	          if (this.player.activeContainer instanceof ContainerBrewingStand && this.player.activeContainer.windowId == items.a && items.b >= 4)
	            items.b++; 
	        }  
	    } 
		// Spigot end
		if (packet instanceof PacketPlayOutChat packetplayoutchat) { // Rinny
			final EnumChatVisibility enumchatvisibility = this.player.getChatFlags();

			if (enumchatvisibility == EnumChatVisibility.HIDDEN
					|| enumchatvisibility == EnumChatVisibility.SYSTEM && !packetplayoutchat.d()) {
				return;
			}
		}

		// CraftBukkit start
		if (packet instanceof PacketPlayOutSpawnPosition spawnpacket) {
			this.player.compassTarget = new Location(this.getPlayer().getWorld(), spawnpacket.x, spawnpacket.y,
					spawnpacket.z);
		}

		// TODO: REMOVE IF IT DONT WORK (DOWN)
		/*
		 * if (packet instanceof PacketPlayOutEntityMetadata) { if
		 * (getPlayer().canSeeInvisibles()) { final PacketPlayOutEntityMetadata
		 * metadataPacket = (PacketPlayOutEntityMetadata)packet; final Entity entity =
		 * this.player.world.getEntity(metadataPacket.a); if (entity instanceof
		 * EntityLiving) { for (int i = 0; i < metadataPacket.b.size(); i++) { final
		 * WatchableObject wo = (WatchableObject) metadataPacket.b.get(i); if (wo.a() ==
		 * 0 && wo.b() instanceof Byte) { final byte entityFlags =
		 * ((Byte)wo.b()).byteValue(); if ((entityFlags & 0x20) != 0) {
		 * PacketPlayOutEntityMetadata replacedMetadataPacket = new
		 * PacketPlayOutEntityMetadata(); replacedMetadataPacket.a = metadataPacket.a;
		 * replacedMetadataPacket.b = new ArrayList<WatchableObject>(metadataPacket.b);
		 * replacedMetadataPacket.b.set(i, new WatchableObject(wo.c(), wo.a(),
		 * Byte.valueOf((byte)(entityFlags & 0xFFFFFFDF)))); packet =
		 * replacedMetadataPacket; } break; } } } } }
		 */
		// TODO: REMOVE IF IT DONT WORK (UP)
		// CraftBukkit end

		try {
			this.networkManager.handle(packet);
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.a(throwable, "Sending packet");
			CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Packet being sent");

			crashreportsystemdetails.a("Packet class", new CrashReportConnectionPacketClass(this, packet));
			throw new ReportedException(crashreport);
		}
	}

	public void a(PacketPlayInHeldItemSlot packetplayinhelditemslot) {
		// CraftBukkit start > Rinny - CraftBukkit Execution Time: 9000 to 13000 nano
		// sec;
		// Rinny Execution Time: 900 to 3000 nano sec
		if (this.player.dead)
			return;
		final int newSlot = packetplayinhelditemslot.c(); // Rinny - cache newSlot
		if (newSlot >= 0 && newSlot < PlayerInventory.getHotbarSize()) {
			final int currentSlot = this.player.inventory.itemInHandIndex; // Rinny - cache currentSlot
			final PlayerItemHeldEvent event = new PlayerItemHeldEvent(this.getPlayer(), currentSlot, newSlot);
			this.server.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				this.sendPacket(new PacketPlayOutHeldItemSlot(currentSlot));
				this.player.resetIdleTimer();
				return;
			}
			// CraftBukkit end

			this.player.inventory.prevItemInHandIndex = currentSlot; // PandaSpigot
			this.player.inventory.itemInHandIndex = newSlot;
			// Rinny end
			this.player.resetIdleTimer();

			// PandaSpigot start - ensure correct attack damage on slot change
			ItemStack prevItemStack = this.player.inventory.items[currentSlot];
			if (prevItemStack != null) {
				for (AttributeModifier attributemodifier : prevItemStack.D().get("generic.attackDamage")) {
					this.player.getAttributeInstance(GenericAttributes.e).b(attributemodifier); // remove attack damage
																								// from previous held
																								// item
				}
			}
			ItemStack inHand = this.player.inventory.getItemInHand();
			if (inHand != null) {
				this.player.getAttributeMap().a(inHand.D()); // apply attack damage from current held item
			}
			// PandaSpigot end
		} else {
			logger.warn(this.player.getName() + " tried to set an invalid carried item");
			this.disconnect("Invalid hotbar selection (Hacking?)"); // CraftBukkit //Spigot "Nope" -> Descriptive reason
		}
	}

	public void a(PacketPlayInChat packetplayinchat) {
		if (this.player.dead || this.player.getChatFlags() == EnumChatVisibility.HIDDEN) { // CraftBukkit - dead men
																							// tell no tales
			final ChatMessage chatmessage = new ChatMessage("chat.cannotSend");

			chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
			this.sendPacket(new PacketPlayOutChat(chatmessage));
		} else {
			this.player.resetIdleTimer();
			String s = StringUtils.normalizeSpace(packetplayinchat.c());

			// Rinny start
			if (!isValidMessage(s)) {
				handleInvalidMessage(packetplayinchat);
				return;
			}
			// Rinny end

			// CraftBukkit start
			if (!packetplayinchat.a()) {
				try {
					this.minecraftServer.server.playerCommandState = true;
					this.handleCommand(s);
				} finally {
					this.minecraftServer.server.playerCommandState = false;
				}
			} else if (s.isEmpty() || s.isBlank()) {
				logger.warn(this.player.getName() + " tried to send an empty message");
			} else if (getPlayer().isConversing()) {
				// Rinny start
				final String message = s;
				processAsync(() -> {
					getPlayer().acceptConversationInput(message);
					return null;
				});
				// Rinny end
			} else if (this.player.getChatFlags() == EnumChatVisibility.SYSTEM) { // Re-add "Command Only" flag check
				final ChatMessage chatmessage = new ChatMessage("chat.cannotSend");

				chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
				this.sendPacket(new PacketPlayOutChat(chatmessage));
			} else /* if (true) */ {
				this.chat(s, true);
				// CraftBukkit end - the below is for reference. :)
				/*
				 * } else { ChatMessage chatmessage1 = new ChatMessage("chat.type.text", new
				 * Object[] { this.player.getScoreboardDisplayName(), s});
				 * 
				 * this.minecraftServer.getPlayerList().sendMessage(chatmessage1, false);
				 */
			}
		}
	}

	private static boolean isValidMessage(String message) {
		for (int i = 0; i < message.length(); i++) {
			if (!SharedConstants.isAllowedChatCharacter(message.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private void handleInvalidMessage(PacketPlayInChat packetplayinchat) {
		if (packetplayinchat.a()) {
			processAsync(() -> {
				PlayerConnection.this.disconnect("Illegal characters in chat");
				return null;
			});
		} else {
			this.disconnect("Illegal characters in chat");
		}
	}

	private void processAsync(Callable<Void> task) {
		Waitable<Void> waitable = new Waitable<>() {
			@Override
			protected Void evaluate() {
				try {
					task.call();
				} catch (Exception e) {
					throw new RuntimeException("Error during task execution", e);
				}
				return null;
			}
		};

		this.minecraftServer.processQueue.add(waitable);

		try {
			waitable.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			throw new RuntimeException("Error processing asynchronous task", e);
		}
	}

	// CraftBukkit start - add method
	public void chat(String s, boolean async) {
		if (s.isEmpty() || s.isBlank() || this.player.getChatFlags() == EnumChatVisibility.HIDDEN) {
			return;
		}
		if (!async && s.startsWith("/")) {
			this.handleCommand(s);
			return;
		}
		// Rinny start - removed until needed or proven useful
		/*
		 * if (this.player.getChatFlags() == EnumChatVisibility.SYSTEM) { return; }
		 */
		// Rinny end
		final Player player = this.getPlayer();

		final AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet());
		this.server.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}
		s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
		Object formattedMessage = event.getFormattedMessage();

		minecraftServer.console.sendMessage(s);
		if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
			for (EntityPlayer recipient : minecraftServer.getPlayerList().players) {
				if (formattedMessage instanceof TextComponent msg) {
					((CraftPlayer) recipient.bukkitEntity).spigot().sendMessage(msg);
				} else {
					recipient.sendMessage(CraftChatMessage.fromString(s));
				}
			}
		} else {
			for (Player recipient : event.getRecipients()) {
				if (formattedMessage instanceof TextComponent msg) {
					recipient.spigot().sendMessage(msg);
				} else {
					recipient.sendMessage(s);
				}
			}
		}
	}
	// CraftBukkit end

	private void handleCommand(String s) {
		// CraftBukkit start - whole method
		if (org.spigotmc.SpigotConfig.logCommands)
			logger.info(this.player.getName() + " issued server command: " + s);

		final CraftPlayer player = this.getPlayer();

		final PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, s, new LazyPlayerSet());
		this.server.getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			return;
		}

		try {
			if (this.server.dispatchCommand(event.getPlayer(), event.getMessage().substring(1))) {
				return;
			}
		} catch (org.bukkit.command.CommandException ex) {
			player.sendMessage(
					org.bukkit.ChatColor.RED + "An internal error occurred while attempting to perform this command");
			java.util.logging.Logger.getLogger(PlayerConnection.class.getName()).log(java.util.logging.Level.SEVERE,
					null, ex);
			return;
		}
		// this.minecraftServer.getCommandHandler().a(this.player, s);
		// CraftBukkit end
	}

	// TODO shortcut
	public void a(PacketPlayInArmAnimation packetplayinarmanimation) {
		if (this.player.dead)
			return; // CraftBukkit
		this.player.resetIdleTimer();
		if (packetplayinarmanimation.d() == 1) {
			// CraftBukkit start - Raytrace to look for 'rogue armswings'
			final float f = 1.0F;
			final float f1 = this.player.lastPitch + (this.player.pitch - this.player.lastPitch) * f;
			final float f2 = this.player.lastYaw + (this.player.yaw - this.player.lastYaw) * f;
			final double d0 = this.player.lastX + (this.player.locX - this.player.lastX) * (double) f;
			final double d1 = this.player.lastY + (this.player.locY - this.player.lastY) * (double) f + 1.62D
					- (double) this.player.height;
			final double d2 = this.player.lastZ + (this.player.locZ - this.player.lastZ) * (double) f;
			final Vec3D vec3d = Vec3D.a(d0, d1, d2);

			final float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
			final float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
			final float f5 = -MathHelper.cos(-f1 * 0.017453292F);
			final float f6 = MathHelper.sin(-f1 * 0.017453292F);
			final float f7 = f4 * f5;
			final float f8 = f3 * f5;
			final double d3 = player.playerInteractManager.getGameMode() == EnumGamemode.CREATIVE ? 5.0D : 4.5D; // Spigot
			final Vec3D vec3d1 = vec3d.add((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
			final MovingObjectPosition movingobjectposition = this.player.world.rayTrace(vec3d, vec3d1, false);

			if (movingobjectposition == null || movingobjectposition.type != EnumMovingObjectType.BLOCK) {
				CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_AIR,
						this.player.inventory.getItemInHand());
			}
			
			this.player.ba();
		}
	}

	public void a(PacketPlayInEntityAction packetplayinentityaction) {
		// CraftBukkit start
		if (this.player.dead)
			return;

		this.player.resetIdleTimer();
		switch (packetplayinentityaction.d()) {
		case 1, 2 -> {
			PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getPlayer(),
					packetplayinentityaction.d() == 1);
			this.server.getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				return;
			}
			break;
		}
		case 4, 5 -> {
			PlayerToggleSprintEvent event = new PlayerToggleSprintEvent(this.getPlayer(),
					packetplayinentityaction.d() == 4);
			this.server.getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				return;
			}
			break;
		}
		default -> {
			break;
		}
		}
		/*
		 * if (packetplayinentityaction.d() == 1 || packetplayinentityaction.d() == 2) {
		 * PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getPlayer(),
		 * packetplayinentityaction.d() == 1);
		 * this.server.getPluginManager().callEvent(event);
		 * 
		 * if (event.isCancelled()) { return; } }
		 * 
		 * if (packetplayinentityaction.d() == 4 || packetplayinentityaction.d() == 5) {
		 * PlayerToggleSprintEvent event = new PlayerToggleSprintEvent(this.getPlayer(),
		 * packetplayinentityaction.d() == 4);
		 * this.server.getPluginManager().callEvent(event);
		 * 
		 * if (event.isCancelled()) { return; } }
		 */
		// CraftBukkit end

		switch (packetplayinentityaction.d()) {
		case 1 -> this.player.setSneaking(true);
		case 2 -> this.player.setSneaking(false);
		case 4 -> {
			this.player.setSprinting(true);
			this.player.setExtraKnockback(true);
		}
		case 5 -> {
			this.player.setSprinting(false);
			this.player.setExtraKnockback(false);
		}
		case 3 -> this.player.a(false, true, true);
		case 6 -> {
			if (this.player.vehicle != null && this.player.vehicle instanceof EntityHorse) {
				((EntityHorse) this.player.vehicle).w(packetplayinentityaction.e());
			}
		}
		case 7 -> {
			if (this.player.vehicle != null && this.player.vehicle instanceof EntityHorse) {
				((EntityHorse) this.player.vehicle).g(this.player);
			}
		}
		}

		/*
		 * if (packetplayinentityaction.d() == 1) { this.player.setSneaking(true); }
		 * else if (packetplayinentityaction.d() == 2) { this.player.setSneaking(false);
		 * } else if (packetplayinentityaction.d() == 4) {
		 * this.player.setSprinting(true); this.player.setExtraKnockback(true); } else
		 * if (packetplayinentityaction.d() == 5) { this.player.setSprinting(false);
		 * this.player.setExtraKnockback(false); } else if (packetplayinentityaction.d()
		 * == 3) { this.player.a(false, true, true); //this.checkMovement = false; //
		 * CraftBukkit - this is handled in teleport } else if
		 * (packetplayinentityaction.d() == 6) { if (this.player.vehicle != null &&
		 * this.player.vehicle instanceof EntityHorse) { ((EntityHorse)
		 * this.player.vehicle).w(packetplayinentityaction.e()); } } else if
		 * (packetplayinentityaction.d() == 7 && this.player.vehicle != null &&
		 * this.player.vehicle instanceof EntityHorse) { ((EntityHorse)
		 * this.player.vehicle).g(this.player); }
		 */
	}

	// TODO: shortcut
	public void a(PacketPlayInUseEntity packetplayinuseentity) {
		if (packetplayinuseentity.c() == null)
			return; // Spigot - protocol patch
		if (this.player.dead)
			return; // CraftBukkit
		final WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
		final Entity entity = packetplayinuseentity.a((World) worldserver);
		// Spigot Start
		if (entity == player) {
			disconnect("Cannot interact with self!");
			return;
		}
		// Spigot End

		this.player.resetIdleTimer();
		if (entity != null) {
			final boolean flag = this.player.hasLineOfSight(entity);
			double d0 = 36.0D;

			if (!flag) {
				d0 = 9.0D;
			}

			if (this.player.f(entity) < d0) {
				final ItemStack itemInHand = this.player.inventory.getItemInHand(); // CraftBukkit
				switch (packetplayinuseentity.c()) {
				case INTERACT -> {
					// CraftBukkit start
					final PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(this.getPlayer(),
							entity.getBukkitEntity());
					this.server.getPluginManager().callEvent(event);

					// Rinny start - dont repeat code
					if ((event.isCancelled() || this.player.inventory.getItemInHand() == null
							|| (this.player.inventory.getItemInHand().getItem() != Items.LEASH
									&& this.player.inventory.getItemInHand().getItem() != Items.NAME_TAG
									&& this.player.inventory.getItemInHand().getItem() != Item
											.getItemOf(Blocks.CHEST)))) {
						final boolean triggerTagOrChestUpdate = itemInHand != null
								&& (itemInHand.getItem() == Items.NAME_TAG && entity instanceof EntityInsentient
										|| itemInHand.getItem() == Item.getItemOf(Blocks.CHEST)
												&& entity instanceof EntityHorse);
						final boolean triggerLeashUpdate = itemInHand != null && itemInHand.getItem() == Items.LEASH
								&& entity instanceof EntityInsentient;

						if (triggerLeashUpdate) {
							this.sendPacket(new PacketPlayOutAttachEntity(1, entity,
									((EntityInsentient) entity).getLeashHolder()));
						}
						if (triggerTagOrChestUpdate) {
							this.sendPacket(new PacketPlayOutEntityMetadata(entity, true));
						}
					}
					// Rinny end
					if (event.isCancelled()) {
						return;
					}
					// CraftBukkit end

					this.player.q(entity);

					// CraftBukkit start
					if (itemInHand != null && itemInHand.count <= -1) {
						this.player.updateInventory(this.player.activeContainer);
					}
					// CraftBukkit end
					return;
				}
				case ATTACK -> {
					if (entity instanceof EntityItem || entity instanceof EntityExperienceOrb
							|| entity instanceof EntityArrow || entity == this.player) {
						this.disconnect("Attempting to attack an invalid entity");
						this.minecraftServer
								.warning("Player " + this.player.getName() + " tried to attack an invalid entity");
						return;
					}
					
					if (this.player.isBlocking()) {
						this.player.bA();
					}

					this.player.attack(entity);

					// CraftBukkit start
					if (itemInHand != null && itemInHand.count <= -1) {
						this.player.updateInventory(this.player.activeContainer);
					}
					// CraftBukkit end
					return;
				}
				}
			}
		}
	}

	public void a(PacketPlayInClientCommand packetplayinclientcommand) {
		this.player.resetIdleTimer();
		final EnumClientCommand enumclientcommand = packetplayinclientcommand.c();

		switch (ClientCommandOrdinalWrapper.a[enumclientcommand.ordinal()]) {
		case 1:
			if (this.player.viewingCredits) {
				this.minecraftServer.getPlayerList().changeDimension(this.player, 0,
						PlayerTeleportEvent.TeleportCause.END_PORTAL); // CraftBukkit - reroute logic through custom
																		// portal management
			} else if (this.player.r().getWorldData().isHardcore()) {
				if (this.minecraftServer.N() && this.player.getName().equals(this.minecraftServer.M())) {
					this.player.playerConnection.disconnect("You have died. Game over, man, it\'s game over!");
					this.minecraftServer.U();
				} else {
					final GameProfileBanEntry gameprofilebanentry = new GameProfileBanEntry(this.player.getProfile(),
							(Date) null, "(You just lost the game)", (Date) null, "Death in Hardcore");

					this.minecraftServer.getPlayerList().getProfileBans().add(gameprofilebanentry);
					this.player.playerConnection.disconnect("You have died. Game over, man, it\'s game over!");
				}
			} else {
				if (this.player.getHealth() > 0.0F) {
					return;
				}

				this.player = this.minecraftServer.getPlayerList().moveToWorld(this.player, 0, false);
			}
			return;
		case 2:
			this.player.getStatisticManager().a(this.player);
			return;
		case 3:
			this.player.a((Statistic) AchievementList.f);
			return;
		}
	}

	public void a(PacketPlayInCloseWindow packetplayinclosewindow) {
		if (this.player.dead)
			return; // CraftBukkit

		CraftEventFactory.handleInventoryCloseEvent(this.player); // CraftBukkit

		this.player.m();
	}

	public void a(PacketPlayInWindowClick packetplayinwindowclick) {
		if (this.player.dead)
			return; // CraftBukkit

		this.player.resetIdleTimer();
		if (!this.player.activeContainer.a(this.player))
			return; // PaperSpigot - check if player is able to use this container
		if (this.player.activeContainer.windowId == packetplayinwindowclick.c()
				&& this.player.activeContainer.c(this.player)) {
			// CraftBukkit start - Call InventoryClickEvent
			if (packetplayinwindowclick.d() < -1 && packetplayinwindowclick.d() != -999) {
				return;
			}

			InventoryView inventory = this.player.activeContainer.getBukkitView();
			// Spigot start - protocol patch
			if (this.networkManager.getVersion() >= 17)
				if (this.player.activeContainer instanceof ContainerEnchantTable) {
					if (packetplayinwindowclick.slot == 1) {
						this.player.updateInventory(this.player.activeContainer);
						return;
					} 
					if (packetplayinwindowclick.slot > 1)
						packetplayinwindowclick.slot--; 
				}  
			if (this.networkManager.getVersion() >= 107) {
				if (this.player.activeContainer instanceof ContainerBrewingStand) {
					if (packetplayinwindowclick.slot == 4) {
						this.player.updateInventory(this.player.activeContainer);
						return;
					} 
					if (packetplayinwindowclick.slot > 4)
						packetplayinwindowclick.slot--; 
				} 
				if (packetplayinwindowclick.c() == 0 && packetplayinwindowclick.slot == 45) {
					this.player.updateInventory(this.player.activeContainer);
					return;
				} 
			} 
			// Spigot end
			SlotType type = CraftInventoryView.getSlotType(inventory, packetplayinwindowclick.d());

			InventoryClickEvent event = null;
			ClickType click = ClickType.UNKNOWN;
			InventoryAction action = InventoryAction.UNKNOWN;

			ItemStack itemstack = null;

			if (packetplayinwindowclick.d() == -1) {
				type = SlotType.OUTSIDE; // override
				click = packetplayinwindowclick.e() == 0 ? ClickType.WINDOW_BORDER_LEFT : ClickType.WINDOW_BORDER_RIGHT;
				action = InventoryAction.NOTHING;
			} else if (packetplayinwindowclick.h() == 0) {
				if (packetplayinwindowclick.e() == 0) {
					click = ClickType.LEFT;
				} else if (packetplayinwindowclick.e() == 1) {
					click = ClickType.RIGHT;
				}
				if (packetplayinwindowclick.e() == 0 || packetplayinwindowclick.e() == 1) {
					action = InventoryAction.NOTHING; // Don't want to repeat ourselves
					if (packetplayinwindowclick.d() == -999) {
						if (player.inventory.getCarried() != null) {
							action = packetplayinwindowclick.e() == 0 ? InventoryAction.DROP_ALL_CURSOR
									: InventoryAction.DROP_ONE_CURSOR;
						}
					} else {
						Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.d());
						if (slot != null) {
							ItemStack clickedItem = slot.getItem();
							ItemStack cursor = player.inventory.getCarried();
							if (clickedItem == null) {
								if (cursor != null) {
									action = packetplayinwindowclick.e() == 0 ? InventoryAction.PLACE_ALL
											: InventoryAction.PLACE_ONE;
								}
							} else if (slot.isAllowed(player)) {
								if (cursor == null) {
									action = packetplayinwindowclick.e() == 0 ? InventoryAction.PICKUP_ALL
											: InventoryAction.PICKUP_HALF;
								} else if (slot.isAllowed(cursor)) {
									if (clickedItem.doMaterialsMatch(cursor) && ItemStack.equals(clickedItem, cursor)) {
										int toPlace = packetplayinwindowclick.e() == 0 ? cursor.count : 1;
										toPlace = Math.min(toPlace, clickedItem.getMaxStackSize() - clickedItem.count);
										toPlace = Math.min(toPlace,
												slot.inventory.getMaxStackSize() - clickedItem.count);
										if (toPlace == 1) {
											action = InventoryAction.PLACE_ONE;
										} else if (toPlace == cursor.count) {
											action = InventoryAction.PLACE_ALL;
										} else if (toPlace < 0) {
											action = toPlace != -1 ? InventoryAction.PICKUP_SOME
													: InventoryAction.PICKUP_ONE; // this happens with oversized stacks
										} else if (toPlace != 0) {
											action = InventoryAction.PLACE_SOME;
										}
									} else if (cursor.count <= slot.getMaxStackSize()) {
										action = InventoryAction.SWAP_WITH_CURSOR;
									}
								} else if (cursor.getItem() == clickedItem.getItem()
										&& (!cursor.usesData() || cursor.getData() == clickedItem.getData())
										&& ItemStack.equals(cursor, clickedItem)) {
									if (clickedItem.count >= 0) {
										if (clickedItem.count + cursor.count <= cursor.getMaxStackSize()) {
											// As of 1.5, this is result slots only
											action = InventoryAction.PICKUP_ALL;
										}
									}
								}
							}
						}
					}
				}
			} else if (packetplayinwindowclick.h() == 1) {
				if (packetplayinwindowclick.e() == 0) {
					click = ClickType.SHIFT_LEFT;
				} else if (packetplayinwindowclick.e() == 1) {
					click = ClickType.SHIFT_RIGHT;
				}
				if (packetplayinwindowclick.e() == 0 || packetplayinwindowclick.e() == 1) {
					if (packetplayinwindowclick.d() < 0) {
						action = InventoryAction.NOTHING;
					} else {
						Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.d());
						if (slot != null && slot.isAllowed(this.player) && slot.hasItem()) {
							action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
						} else {
							action = InventoryAction.NOTHING;
						}
					}
				}
			} else if (packetplayinwindowclick.h() == 2) {
				if (packetplayinwindowclick.e() >= 0 && packetplayinwindowclick.e() < 9) {
					click = ClickType.NUMBER_KEY;
					Slot clickedSlot = this.player.activeContainer.getSlot(packetplayinwindowclick.d());
					if (clickedSlot.isAllowed(player)) {
						ItemStack hotbar = this.player.inventory.getItem(packetplayinwindowclick.e());
						boolean canCleanSwap = hotbar == null
								|| (clickedSlot.inventory == player.inventory && clickedSlot.isAllowed(hotbar)); // the
																													// slot
																													// will
																													// accept
																													// the
																													// hotbar
																													// item
						if (clickedSlot.hasItem()) {
							if (canCleanSwap) {
								action = InventoryAction.HOTBAR_SWAP;
							} else {
								int firstEmptySlot = player.inventory.getFirstEmptySlotIndex();
								if (firstEmptySlot > -1) {
									action = InventoryAction.HOTBAR_MOVE_AND_READD;
								} else {
									action = InventoryAction.NOTHING; // This is not sane! Mojang: You should test for
																		// other slots of same type
								}
							}
						} else if (!clickedSlot.hasItem() && hotbar != null && clickedSlot.isAllowed(hotbar)) {
							action = InventoryAction.HOTBAR_SWAP;
						} else {
							action = InventoryAction.NOTHING;
						}
					} else {
						action = InventoryAction.NOTHING;
					}
					// Special constructor for number key
					event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.d(), click, action,
							packetplayinwindowclick.e());
				}
			} else if (packetplayinwindowclick.h() == 3) {
				if (packetplayinwindowclick.e() == 2) {
					click = ClickType.MIDDLE;
					if (packetplayinwindowclick.d() < 0) { // Paper - Issue GH-404
						action = InventoryAction.NOTHING;
					} else {
						Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.d());
						if (slot != null && slot.hasItem() && player.abilities.canInstantlyBuild
								&& player.inventory.getCarried() == null) {
							action = InventoryAction.CLONE_STACK;
						} else {
							action = InventoryAction.NOTHING;
						}
					}
				} else {
					click = ClickType.UNKNOWN;
					action = InventoryAction.UNKNOWN;
				}
			} else if (packetplayinwindowclick.h() == 4) {
				if (packetplayinwindowclick.d() >= 0) {
					if (packetplayinwindowclick.e() == 0) {
						click = ClickType.DROP;
						Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.d());
						if (slot != null && slot.hasItem() && slot.isAllowed(player) && slot.getItem() != null
								&& slot.getItem().getItem() != Item.getItemOf(Blocks.AIR)) {
							action = InventoryAction.DROP_ONE_SLOT;
						} else {
							action = InventoryAction.NOTHING;
						}
					} else if (packetplayinwindowclick.e() == 1) {
						click = ClickType.CONTROL_DROP;
						Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.d());
						if (slot != null && slot.hasItem() && slot.isAllowed(player) && slot.getItem() != null
								&& slot.getItem().getItem() != Item.getItemOf(Blocks.AIR)) {
							action = InventoryAction.DROP_ALL_SLOT;
						} else {
							action = InventoryAction.NOTHING;
						}
					}
				} else {
					// Sane default (because this happens when they are holding nothing. Don't ask
					// why.)
					click = ClickType.LEFT;
					if (packetplayinwindowclick.e() == 1) {
						click = ClickType.RIGHT;
					}
					action = InventoryAction.NOTHING;
				}
			} else if (packetplayinwindowclick.h() == 5) {
				itemstack = this.player.activeContainer.clickItem(packetplayinwindowclick.d(),
						packetplayinwindowclick.e(), 5, this.player);
			} else if (packetplayinwindowclick.h() == 6) {
				click = ClickType.DOUBLE_CLICK;
				action = InventoryAction.NOTHING;
				if (packetplayinwindowclick.d() >= 0 && this.player.inventory.getCarried() != null) {
					ItemStack cursor = this.player.inventory.getCarried();
					action = InventoryAction.NOTHING;
					// Quick check for if we have any of the item
					if (inventory.getTopInventory().contains(org.bukkit.Material.getMaterial(Item.getId(cursor.getItem()))) || inventory.getBottomInventory().contains(org.bukkit.Material.getMaterial(Item.getId(cursor.getItem())))) {
						action = InventoryAction.COLLECT_TO_CURSOR;
					}
				}
			}
			// TODO check on updates

			if (packetplayinwindowclick.h() != 5) {
				if (click == ClickType.NUMBER_KEY) {
					event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.d(), click, action, packetplayinwindowclick.e());
				} else {
					event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.d(), click, action);
				}

				org.bukkit.inventory.Inventory top = inventory.getTopInventory();
				if (packetplayinwindowclick.d() == 0 && top instanceof CraftingInventory) {
					org.bukkit.inventory.Recipe recipe = ((CraftingInventory) top).getRecipe();
					if (recipe != null) {
						if (click == ClickType.NUMBER_KEY) {
							event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.d(), click, action, packetplayinwindowclick.e());
						} else {
							event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.d(), click, action);
						}
					}
				}

				server.getPluginManager().callEvent(event);

				switch (event.getResult()) {
				case ALLOW:
				case DEFAULT:
					itemstack = this.player.activeContainer.clickItem(packetplayinwindowclick.d(),
							packetplayinwindowclick.e(), packetplayinwindowclick.h(), this.player);
					break;
				case DENY:
					/*
					 * Needs enum constructor in InventoryAction if (action.modifiesOtherSlots()) {
					 * 
					 * } else { if (action.modifiesCursor()) {
					 * this.player.playerConnection.sendPacket(new Packet103SetSlot(-1, -1,
					 * this.player.inventory.getCarried())); } if (action.modifiesClicked()) {
					 * this.player.playerConnection.sendPacket(new
					 * Packet103SetSlot(this.player.activeContainer.windowId,
					 * packet102windowclick.slot,
					 * this.player.activeContainer.getSlot(packet102windowclick.slot).getItem())); }
					 * }
					 */
					switch (action) {
					// Modified other slots
					case PICKUP_ALL:
					case MOVE_TO_OTHER_INVENTORY:
					case HOTBAR_MOVE_AND_READD:
					case HOTBAR_SWAP:
					case COLLECT_TO_CURSOR:
					case UNKNOWN:
						this.player.updateInventory(this.player.activeContainer);
						break;
					// Modified cursor and clicked
					case PICKUP_SOME:
					case PICKUP_HALF:
					case PICKUP_ONE:
					case PLACE_ALL:
					case PLACE_SOME:
					case PLACE_ONE:
					case SWAP_WITH_CURSOR:
						this.player.playerConnection
								.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.player.inventory.getCarried()));
						this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(
								this.player.activeContainer.windowId, packetplayinwindowclick.d(),
								this.player.activeContainer.getSlot(packetplayinwindowclick.d()).getItem()));
						break;
					// Modified clicked only
					case DROP_ALL_SLOT:
					case DROP_ONE_SLOT:
						this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(
								this.player.activeContainer.windowId, packetplayinwindowclick.d(),
								this.player.activeContainer.getSlot(packetplayinwindowclick.d()).getItem()));
						break;
					// Modified cursor only
					case DROP_ALL_CURSOR:
					case DROP_ONE_CURSOR:
					case CLONE_STACK:
						this.player.playerConnection
								.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.player.inventory.getCarried()));
						break;
					// Nothing
					case NOTHING:
						break;
					}
					return;
				}
			}
			// CraftBukkit end

			if (ItemStack.matches(packetplayinwindowclick.g(), itemstack)) {
				this.player.playerConnection.sendPacket(
						new PacketPlayOutTransaction(packetplayinwindowclick.c(), packetplayinwindowclick.f(), true));
				this.player.g = true;
				this.player.activeContainer.b();
				this.player.broadcastCarriedItem();
				this.player.g = false;
			} else {
				this.n.a(this.player.activeContainer.windowId, Short.valueOf(packetplayinwindowclick.f()));
				this.player.playerConnection.sendPacket(
						new PacketPlayOutTransaction(packetplayinwindowclick.c(), packetplayinwindowclick.f(), false));
				this.player.activeContainer.a(this.player, false);
				ArrayList arraylist = new ArrayList();

				for (int i = 0; i < this.player.activeContainer.c.size(); ++i) {
					arraylist.add(((Slot) this.player.activeContainer.c.get(i)).getItem());
				}

				this.player.a(this.player.activeContainer, arraylist);

				// CraftBukkit start - Send a Set Slot to update the crafting result slot
				if (type == SlotType.RESULT && itemstack != null) {
					this.player.playerConnection
							.sendPacket(new PacketPlayOutSetSlot(this.player.activeContainer.windowId, 0, itemstack));
				}
				// CraftBukkit end
			}
		}
	}

	public void a(PacketPlayInEnchantItem packetplayinenchantitem) {
		this.player.resetIdleTimer();
		if (this.player.activeContainer.windowId == packetplayinenchantitem.c()
				&& this.player.activeContainer.c(this.player)) {
			this.player.activeContainer.a(this.player, packetplayinenchantitem.d());
			this.player.activeContainer.b();
		}
	}

	public void a(PacketPlayInSetCreativeSlot packetplayinsetcreativeslot) {
		if (this.player.playerInteractManager.isCreative()) {
			final boolean flag = packetplayinsetcreativeslot.c() < 0;
			ItemStack itemstack = packetplayinsetcreativeslot.getItemStack();
			final boolean flag1 = packetplayinsetcreativeslot.c() >= 1
					&& packetplayinsetcreativeslot.c() < 36 + PlayerInventory.getHotbarSize();
			// CraftBukkit - Add invalidItems check
			boolean flag2 = itemstack == null || itemstack.getItem() != null && (!invalidItems.contains(Item.getId(itemstack.getItem())) || !org.spigotmc.SpigotConfig.filterCreativeItems); // Spigot
			boolean flag3 = itemstack == null || itemstack.getData() >= 0 && itemstack.count <= 64 && itemstack.count > 0;

			// CraftBukkit start - Call click event
			if (flag || (flag1 && !ItemStack.matches(
					this.player.defaultContainer.getSlot(packetplayinsetcreativeslot.c()).getItem(),
					packetplayinsetcreativeslot.getItemStack()))) { // Insist on valid slot
				final org.bukkit.entity.HumanEntity player = this.player.getBukkitEntity();
				final InventoryView inventory = new CraftInventoryView(player, player.getInventory(),
						this.player.defaultContainer);
				final org.bukkit.inventory.ItemStack item = CraftItemStack
						.asBukkitCopy(packetplayinsetcreativeslot.getItemStack());

				SlotType type = SlotType.QUICKBAR;
				if (flag) {
					type = SlotType.OUTSIDE;
				} else if (packetplayinsetcreativeslot.c() < 36) {
					type = (packetplayinsetcreativeslot.c() >= 5 && packetplayinsetcreativeslot.c() < 9 ? SlotType.ARMOR
							: SlotType.CONTAINER);
				}
				final InventoryCreativeEvent event = new InventoryCreativeEvent(inventory, type,
						flag ? -999 : packetplayinsetcreativeslot.c(), item);
				server.getPluginManager().callEvent(event);

				itemstack = CraftItemStack.asNMSCopy(event.getCursor());

				switch (event.getResult()) {
				case ALLOW:
					// Plugin cleared the id / stacksize checks
					flag2 = flag3 = true;
					break;
				case DEFAULT:
					break;
				case DENY:
					// Reset the slot
					if (packetplayinsetcreativeslot.c() >= 0) {
						this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(
								this.player.defaultContainer.windowId, packetplayinsetcreativeslot.c(),
								this.player.defaultContainer.getSlot(packetplayinsetcreativeslot.c()).getItem()));
						this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, null));
					}
					return;
				}
			}
			// CraftBukkit end

			if (flag1 && flag2 && flag3) {
				this.player.defaultContainer.setItem(packetplayinsetcreativeslot.c(),
						(itemstack == null ? (ItemStack) null : itemstack));
				this.player.defaultContainer.a(this.player, true);
			} else if (flag && flag2 && flag3 && this.x < 200) {
				this.x += 20;
				final EntityItem entityitem = this.player.drop(itemstack, true);

				if (entityitem != null) {
					entityitem.e();
				}
				// Spigot start - protocol patch
			} else {
				if (flag1) {
					player.playerConnection.sendPacket(new PacketPlayOutSetSlot(0, packetplayinsetcreativeslot.c(),
							player.defaultContainer.getSlot(packetplayinsetcreativeslot.c()).getItem()));
				}
			}
			// Spigot end
		}
	}

	public void a(PacketPlayInTransaction packetplayintransaction) {
		if (this.player.dead)
			return; // CraftBukkit
		if (!this.player.activeContainer.a(this.player))
			return; // PaperSpigot - check if player is able to use this container
		final Short oshort = (Short) this.n.get(this.player.activeContainer.windowId);

		if (oshort != null && packetplayintransaction.d() == oshort.shortValue()
				&& this.player.activeContainer.windowId == packetplayintransaction.c()
				&& !this.player.activeContainer.c(this.player)) {
			this.player.activeContainer.a(this.player, true);
		}
	}

	public void a(PacketPlayInUpdateSign packetplayinupdatesign) {
		if (this.player.dead)
			return; // CraftBukkit

		this.player.resetIdleTimer();
		final WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

		if (worldserver.isLoaded(packetplayinupdatesign.c(), packetplayinupdatesign.d(), packetplayinupdatesign.e())) {
			final TileEntity tileentity = worldserver.getTileEntity(packetplayinupdatesign.c(),
					packetplayinupdatesign.d(), packetplayinupdatesign.e());

			if (tileentity instanceof TileEntitySign tileentitysign) { // Rinny
				if (!tileentitysign.a() || tileentitysign.b() != this.player) {
					this.minecraftServer
							.warning("Player " + this.player.getName() + " just tried to change non-editable sign");
					this.sendPacket(new PacketPlayOutUpdateSign(packetplayinupdatesign.c(), packetplayinupdatesign.d(),
							packetplayinupdatesign.e(), tileentitysign.lines)); // CraftBukkit
					return;
				}
			}

			int i;
			int j;

			for (j = 0; j < 4; ++j) {
				boolean flag = true;
				packetplayinupdatesign.f()[j] = packetplayinupdatesign.f()[j].replaceAll("\uF700", "")
						.replaceAll("\uF701", ""); // Spigot - Mac OSX sends weird chars

				if (packetplayinupdatesign.f()[j].length() > 15) {
					flag = false;
				} else {
					for (i = 0; i < packetplayinupdatesign.f()[j].length(); ++i) {
						if (!SharedConstants.isAllowedChatCharacter(packetplayinupdatesign.f()[j].charAt(i))) {
							flag = false;
						}
					}
				}

				if (!flag) {
					packetplayinupdatesign.f()[j] = "!?";
				}
			}

			if (tileentity instanceof TileEntitySign tileentitysign) { // Rinny
				j = packetplayinupdatesign.c();
				int k = packetplayinupdatesign.d();

				i = packetplayinupdatesign.e();
				// CraftBukkit start
				final Player player = this.server.getPlayer(this.player);
				final SignChangeEvent event = new SignChangeEvent(
						(org.bukkit.craftbukkit.block.CraftBlock) player.getWorld().getBlockAt(j, k, i),
						this.server.getPlayer(this.player), packetplayinupdatesign.f());
				this.server.getPluginManager().callEvent(event);

				if (!event.isCancelled()) {
					tileentitysign.lines = org.bukkit.craftbukkit.block.CraftSign.sanitizeLines(event.getLines());
					tileentitysign.isEditable = false;
				}
				// System.arraycopy(packetplayinupdatesign.f(), 0, tileentitysign1.lines, 0, 4);
				// CraftBukkit end

				tileentitysign.update();
				worldserver.notify(j, k, i);
			}
		}
	}

	public void a(PacketPlayInKeepAlive packetplayinkeepalive) {
		if (packetplayinkeepalive.c() == this.h) {
			final short pingTime = (short) (this.getCurrentMillis() - this.i);
			final short averagePing = this.calculateAveragePingWithHistory(pingTime);
			this.player.ping = averagePing;
		}
	}

	// TODO: shortcut
	private final short[] pingHistory = new short[5];
	private short historyIndex = 0;
	private short historySum = 0;
	private short historyCount = 0;

	private short calculateAveragePingWithHistory(short newPing) {
		// use a moving average filter to smooth out sudden changes in ping
		historySum -= pingHistory[historyIndex];
		historySum += newPing;
		pingHistory[historyIndex] = newPing;
		historyIndex = (short) ((historyIndex + 1) % pingHistory.length);
		historyCount = (short) Math.min(historyCount + 1, pingHistory.length);
		final short averagePing = (short) (historySum / historyCount);

		// add a small penalty for high ping values to encourage better network
		// performance
		final short penalty = (short) (Math.max(0, averagePing - 100) / 10);
		return (short) (newPing + penalty);
	}

	private long getCurrentMillis() { // Paper - OBFHELPER
		return System.nanoTime() / 1000000L;
	}

	public void a(PacketPlayInAbilities packetplayinabilities) {
		// CraftBukkit start
		if (this.player.abilities.canFly && this.player.abilities.isFlying != packetplayinabilities.isFlying()) {
			final PlayerToggleFlightEvent event = new PlayerToggleFlightEvent(this.server.getPlayer(this.player),
					packetplayinabilities.isFlying());
			this.server.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				this.player.abilities.isFlying = packetplayinabilities.isFlying(); // Actually set the player's flying
																					// status
				return;
			}
			this.player.updateAbilities(); // Tell the player their ability was reverted
		}
		// CraftBukkit end
	}

	public void a(PacketPlayInTabComplete packetplayintabcomplete) {
		final Set<String> hashSet = new TreeSet<String>(
				this.minecraftServer.a(this.player, packetplayintabcomplete.c()));
		final String[] array = hashSet.toArray(new String[hashSet.size()]);
		this.player.playerConnection.sendPacket(new PacketPlayOutTabComplete(array));
	}

	public void a(PacketPlayInSettings packetplayinsettings) {
		this.player.a(packetplayinsettings);
	}

	public void a(PacketPlayInCustomPayload packetplayincustompayload) {
		// CraftBukkit start - Ignore empty payloads
		if (packetplayincustompayload.length <= 0) {
			return;
		}
		// CraftBukkit end

		PacketDataSerializer packetdataserializer;
		ItemStack itemstack;
		ItemStack itemstack1;

		if ("MC|BEdit".equals(packetplayincustompayload.c())) {
			packetdataserializer = new PacketDataSerializer(Unpooled.wrappedBuffer(packetplayincustompayload.e()),
					networkManager.getVersion()); // Spigot - protocol patch

			try {
				itemstack = packetdataserializer.c();
				if (itemstack != null) {
					if (!ItemBookAndQuill.a(itemstack.getTag())) {
						throw new IOException("Invalid book tag!");
					}

					itemstack1 = this.player.inventory.getItemInHand();
					if (itemstack1 == null) {
						return;
					}

					if (itemstack.getItem() == Items.BOOK_AND_QUILL && itemstack.getItem() == itemstack1.getItem()) {
						itemstack1 = new ItemStack(Items.BOOK_AND_QUILL);
						itemstack1.a("pages", (NBTBase) itemstack.getTag().getList("pages", 8));
						CraftEventFactory.handleEditBookEvent(player, itemstack1); // CraftBukkit
					}

					return;
				}
				// CraftBukkit start
			} catch (Exception exception) {
				logger.error("Couldn\'t handle book info", exception);
				this.disconnect("Invalid book data!");
				return;
				// CraftBukkit end
			} finally {
				packetdataserializer.release();
			}

			return;
		} else if ("MC|BSign".equals(packetplayincustompayload.c())) {
			packetdataserializer = new PacketDataSerializer(Unpooled.wrappedBuffer(packetplayincustompayload.e()),
					networkManager.getVersion()); // Spigot - protocol patch

			try {
				itemstack = packetdataserializer.c();
				if (itemstack != null) {
					if (!ItemWrittenBook.a(itemstack.getTag())) {
						throw new IOException("Invalid book tag!");
					}

					itemstack1 = this.player.inventory.getItemInHand();
					if (itemstack1 == null) {
						return;
					}

					if (itemstack.getItem() == Items.WRITTEN_BOOK && itemstack1.getItem() == Items.BOOK_AND_QUILL) {
						itemstack1 = new ItemStack(Items.WRITTEN_BOOK);
						itemstack1.a("author", (NBTBase) (new NBTTagString(this.player.getName())));
						itemstack1.a("title", (NBTBase) (new NBTTagString(
								itemstack.getTag().getString("title").replace("\u00A7", ""))));
						final NBTTagList pages = new NBTTagList();
						for (int i = 0; i < itemstack.getTag().getList("pages", 8).size(); i++) {
							pages.add(new NBTTagString(
									itemstack.getTag().getList("pages", 8).getString(i).replace("\u00A7", "")));
						}
						itemstack1.a("pages", (NBTBase) pages);
						itemstack1.setItem(Items.WRITTEN_BOOK);
						CraftEventFactory.handleEditBookEvent(player, itemstack1); // CraftBukkit
					}

					return;
				}
				// CraftBukkit start
			} catch (Throwable exception1) {
				logger.error("Couldn\'t sign book", exception1);
				this.disconnect("Invalid book data!");
				// CraftBukkit end
				return;
			} finally {
				packetdataserializer.release();
			}

			return;
		} else {
			int i;
			DataInputStream datainputstream;

			if ("MC|TrSel".equals(packetplayincustompayload.c())) {
				try {
					datainputstream = new DataInputStream(new ByteArrayInputStream(packetplayincustompayload.e()));
					i = datainputstream.readInt();
					final Container container = this.player.activeContainer;

					if (container instanceof ContainerMerchant) {
						((ContainerMerchant) container).e(i);
					}
					// CraftBukkit start
				} catch (Throwable exception2) {
					logger.error("Couldn\'t select trade", exception2);
					this.disconnect("Invalid trade data!");
					// CraftBukkit end
				}
			} else if ("MC|AdvCdm".equals(packetplayincustompayload.c())) {
				if (!this.minecraftServer.getEnableCommandBlock()) {
					this.player.sendMessage(new ChatMessage("advMode.notEnabled", new Object[0]));
				} else if (this.player.a(2, "") && this.player.abilities.canInstantlyBuild) {
					packetdataserializer = new PacketDataSerializer(
							Unpooled.wrappedBuffer(packetplayincustompayload.e()));

					try {
						byte b0 = packetdataserializer.readByte();
						CommandBlockListenerAbstract commandblocklistenerabstract = null;

						if (b0 == 0) {
							final TileEntity tileentity = this.player.world.getTileEntity(
									packetdataserializer.readInt(), packetdataserializer.readInt(),
									packetdataserializer.readInt());

							if (tileentity instanceof TileEntityCommand) {
								commandblocklistenerabstract = ((TileEntityCommand) tileentity).getCommandBlock();
							}
						} else if (b0 == 1) {
							final Entity entity = this.player.world.getEntity(packetdataserializer.readInt());

							if (entity instanceof EntityMinecartCommandBlock) {
								commandblocklistenerabstract = ((EntityMinecartCommandBlock) entity).getCommandBlock();
							}
						}

						final String s = packetdataserializer.c(packetdataserializer.readableBytes());

						if (commandblocklistenerabstract != null) {
							commandblocklistenerabstract.setCommand(s);
							commandblocklistenerabstract.e();
							this.player.sendMessage(new ChatMessage("advMode.setCommand.success", new Object[] { s }));
						}
						// CraftBukkit start
					} catch (Throwable exception3) {
						logger.error("Couldn\'t set command block", exception3);
						this.disconnect("Invalid CommandBlock data!");
						// CraftBukkit end
					} finally {
						packetdataserializer.release();
					}
				} else {
					this.player.sendMessage(new ChatMessage("advMode.notAllowed", new Object[0]));
				}
			} else if ("MC|Beacon".equals(packetplayincustompayload.c())) {
				if (this.player.activeContainer instanceof ContainerBeacon containerbeacon) { // Rinny
					try {
						datainputstream = new DataInputStream(new ByteArrayInputStream(packetplayincustompayload.e()));
						i = datainputstream.readInt();
						final int j = datainputstream.readInt();
						final Slot slot = containerbeacon.getSlot(0);

						if (slot.hasItem()) {
							slot.a(1);
							final TileEntityBeacon tileentitybeacon = containerbeacon.e();

							tileentitybeacon.d(i);
							tileentitybeacon.e(j);
							tileentitybeacon.update();
						}
						// CraftBukkit start
					} catch (Throwable exception4) {
						logger.error("Couldn\'t set beacon", exception4);
						this.disconnect("Invalid beacon data!");
						// CraftBukkit end
					}
				}
			} else if ("MC|ItemName".equals(packetplayincustompayload.c())
					&& this.player.activeContainer instanceof ContainerAnvil containeranvil) { // Rinny
				if (packetplayincustompayload.e() != null && packetplayincustompayload.e().length >= 1) {
					final String s1 = SharedConstants.a(new String(packetplayincustompayload.e(), Charsets.UTF_8));

					if (s1.length() <= 30) {
						containeranvil.a(s1);
					}
				} else {
					containeranvil.a("");
				}
			}
			// CraftBukkit start
			else if (packetplayincustompayload.c().equals("REGISTER")) {
				try {
					final String channels = new String(packetplayincustompayload.e(), "UTF8");
					for (String channel : channels.split("\0")) {
						getPlayer().addChannel(channel);
					}
				} catch (UnsupportedEncodingException ex) {
					throw new AssertionError(ex);
				}
			} else if (packetplayincustompayload.c().equals("UNREGISTER")) {
				try {
					final String channels = new String(packetplayincustompayload.e(), "UTF8");
					for (String channel : channels.split("\0")) {
						getPlayer().removeChannel(channel);
					}
				} catch (UnsupportedEncodingException ex) {
					throw new AssertionError(ex);
				}
			} else {
				server.getMessenger().dispatchIncomingMessage(player.getBukkitEntity(), packetplayincustompayload.c(),
						packetplayincustompayload.e());
			}
			// CraftBukkit end
		}
	}

	public void a(EnumProtocol enumprotocol, EnumProtocol enumprotocol1) {
		if (enumprotocol1 != EnumProtocol.PLAY) {
			throw new IllegalStateException("Unexpected change in protocol!");
		}
	}
	
	public void handleVehicleMove(PacketPlayInVehicleMove packet) {
	    if (!Doubles.isFinite(packet.x) || !Doubles.isFinite(packet.y) || !Doubles.isFinite(packet.z) || !Floats.isFinite(packet.yaw) || !Floats.isFinite(packet.pitch)) {
	    	disconnect("Invalid move vehicle packet received");
	    	return;
	    } 
	    Entity vehicle = this.player.vehicle;
	    if (vehicle == null) {
	    	return; 
	    }
	    double currentX = vehicle.locX;
	    double currentY = vehicle.locY;
	    double currentZ = vehicle.locZ;
	    double dx = packet.x - currentX;
	    double dy = packet.y - currentY;
	    double dz = packet.z - currentZ;
	    /*double moveSquared = dx * dx + dy * dy + dz * dz;
	    if (moveSquared > SpigotConfig.movedTooQuicklyThreshold) {
	      c.warn(this.player.getName() + " moves their vehicle (" + vehicle.getClass().getSimpleName() + ") too quickly! " + dx + ", " + dy + ", " + dz);
	      sendPacket((Packet)new PacketPlayOutVehicleMove(vehicle));
	      return;
	    } */
	    boolean currentlyInBlock = !vehicle.world.getCubes(vehicle, vehicle.boundingBox.shrink(0.0625D, 0.0625D, 0.0625D)).isEmpty();
	    boolean movedWrongly = false;
	    vehicle.move(dx, dy, dz);
	    /*if (vehicle.e(packet.x, packet.y, packet.z) > SpigotConfig.movedWronglyThreshold) {
	      c.warn(this.player.getName() + " moves their vehicle (" + vehicle.getClass().getSimpleName() + ") wrongly!");
	      movedWrongly = true;
	    } */
	    vehicle.setLocation(packet.x, packet.y, packet.z, packet.yaw, packet.pitch);
	    boolean destInBlock = !vehicle.world.getCubes(vehicle, vehicle.boundingBox.shrink(0.0625D, 0.0625D, 0.0625D)).isEmpty();
	    if (!currentlyInBlock && (movedWrongly || destInBlock)) {
	    	vehicle.setLocation(currentX, currentY, currentZ, packet.yaw, packet.pitch);
	    	sendPacket((Packet)new PacketPlayOutVehicleMove(vehicle));
	    	return;
	    } 
	    this.minecraftServer.getPlayerList().d(this.player);
	  }

	public void handleUseItem(PacketPlayInUseItem packet) {
		if (packet.hand == 1) {
			if (this.player.isBlocking()) {
				this.player.shieldBlocking = true;
			}
		} else {
			PacketPlayInBlockPlace place = new PacketPlayInBlockPlace();
			place.a = -1;
			place.b = -1;
			place.c = -1;
			place.d = 255;
			a(place);
		}
	}

	// CraftBukkit start - Add "isDisconnected" method
	public boolean isDisconnected() {
		return !this.player.joining && !this.networkManager.isConnected() || this.processedDisconnect;
	}
	// CraftBukkit end
}
