package net.minecraft.server;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
// CraftBukkit start
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerDateChangeEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.spigotmc.SpigotConfig;

import io.noks.cb.CraftHologram;
import io.noks.utils.CachedSizeConcurrentLinkedQueue;
import jline.console.ConsoleReader;
import joptsimple.OptionSet;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.GameProfileRepository;
import net.minecraft.util.com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.util.com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.util.org.apache.commons.lang3.Validate;

public abstract class MinecraftServer implements ICommandListener, Runnable, IMojangStatistics {
    private static final Logger i = LogManager.getLogger(MinecraftServer.class);
    private static final File a = new File("usercache.json");
    private static MinecraftServer j;
    public Convertable convertable; // CraftBukkit - private final -> public
    public File universe; // CraftBukkit - private final -> public
    private final List<IUpdatePlayerListBox> playerListBox = new ArrayList<IUpdatePlayerListBox>();
    private final ICommandHandler o;
    private ServerConnection p; // Spigot
    private final ServerPing q = new ServerPing();
    private final Random r = new Random();
    private String serverIp;
    private int t = -1;
    public WorldServer[] worldServer;
    private PlayerList playerList;
    private boolean isRunning = true;
    private boolean isStopped;
    private int ticks; public int getTicks() { return this.ticks; }
    protected final Proxy d;
    public String e;
    public int f;
    private boolean onlineMode;
    private boolean spawnAnimals;
    private boolean spawnNPCs;
    private boolean pvpMode;
    private boolean allowFlight;
    private String motd;
    private int E;
    private int F = 0;
    public final long[] g = new long[100];
    public long[][] h;
    private KeyPair G;
    private String H;
    private String I;
    //private boolean L;
    private boolean M;
    private String N = "";
    //private boolean O;
    //private long P;
    //private String Q;
    private boolean R;
    private boolean S;
    private final YggdrasilAuthenticationService T;
    private final MinecraftSessionService U;
    private long V = 0L;
    private final GameProfileRepository W;
    private final UserCache X;

    // CraftBukkit start - add fields
    public List<WorldServer> worlds = new ArrayList<WorldServer>();
    public org.bukkit.craftbukkit.CraftServer server;
    public OptionSet options;
    public org.bukkit.command.ConsoleCommandSender console;
    public org.bukkit.command.RemoteConsoleCommandSender remoteConsole;
    public ConsoleReader reader;
    public static int currentTick = 0; // PaperSpigot - Further improve tick loop
    public final Thread primaryThread;
    public java.util.Queue<Runnable> processQueue = new CachedSizeConcurrentLinkedQueue<Runnable>();
    public int autosavePeriod;
    // CraftBukkit end
    private Date date;

    public MinecraftServer(OptionSet options, Proxy proxy) { // CraftBukkit - signature file -> OptionSet
        net.minecraft.util.io.netty.util.ResourceLeakDetector.setEnabled( false ); // Spigot - disable
        this.X = new UserCache(this, a);
        j = this;
        this.d = proxy;
        // this.universe = file1; // CraftBukkit
        // this.p = new ServerConnection(this); // Spigot
        this.o = new CommandDispatcher();
        // this.convertable = new WorldLoaderServer(file1); // CraftBukkit - moved to DedicatedServer.init
        this.T = new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString());
        this.U = this.T.createMinecraftSessionService();
        this.W = this.T.createProfileRepository();
        // CraftBukkit start
        this.options = options;
        // Try to see if we're actually running in a terminal, disable jline if not
        if (System.console() == null) {
            System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
            org.bukkit.craftbukkit.Main.useJline = false;
        }

        try {
            this.reader = new ConsoleReader(System.in, System.out);
            this.reader.setExpandEvents(false); // Avoid parsing exceptions for uncommonly used event designators
        } catch (Throwable e) {
            try {
                // Try again with jline disabled for Windows users without C++ 2008 Redistributable
                System.setProperty("jline.terminal", "jline.UnsupportedTerminal");
                System.setProperty("user.language", "en");
                org.bukkit.craftbukkit.Main.useJline = false;
                this.reader = new ConsoleReader(System.in, System.out);
                this.reader.setExpandEvents(false);
            } catch (IOException ex) {
                i.warn((String) null, ex);
            }
        }
        Runtime.getRuntime().addShutdownHook(new org.bukkit.craftbukkit.util.ServerShutdownThread(this));

        primaryThread = new ThreadServerApplication(this, "Server thread"); // Moved from main
        this.date = new Date();
    }

    public abstract PropertyManager getPropertyManager();
    // CraftBukkit end

    protected abstract boolean init() throws java.net.UnknownHostException; // CraftBukkit - throws UnknownHostException

    protected void a(String s) {
        if (this.getConvertable().isConvertable(s)) {
            i.info("Converting map!");
            //this.b("menu.convertingLevel");
            this.getConvertable().convert(s, new ConvertProgressUpdater(this));
        }
    }

    /*protected synchronized void b(String s) {
        this.Q = s;
    }*/

    protected void a(String s, String s1, long i, WorldType worldtype, String s2) {
        this.a(s);
        //this.b("menu.loadingLevel");
        this.worldServer = new WorldServer[3];
        // this.h = new long[this.worldServer.length][100]; // CraftBukkit - Removed ticktime arrays
        // IDataManager idatamanager = this.convertable.a(s, true);
        // WorldData worlddata = idatamanager.getWorldData();
        /* CraftBukkit start - Removed worldsettings
        WorldSettings worldsettings;

        if (worlddata == null) {
            worldsettings = new WorldSettings(i, this.getGamemode(), this.getGenerateStructures(), this.isHardcore(), worldtype);
            worldsettings.a(s2);
        } else {
            worldsettings = new WorldSettings(worlddata);
        }

        if (this.L) {
            worldsettings.a();
        }
        // */
        final byte worldCount = 3;

        for (byte j = 0; j < worldCount; ++j) {
            int dimension = 0;
            
            if (j == 1) {
                if (!this.getAllowNether()) {
                	continue;
                }
                dimension = -1;
            }

            if (j == 2) {
                if (!this.server.getAllowEnd()) {
                	continue;
                }
                dimension = 1;
            }

            String worldType = Environment.getEnvironment(dimension).toString().toLowerCase();
            String name = (dimension == 0) ? s : s + "_" + worldType;

            org.bukkit.generator.ChunkGenerator gen = this.server.getGenerator(name);
            WorldSettings worldsettings = new WorldSettings(i, this.getGamemode(), this.getGenerateStructures(), this.isHardcore(), worldtype);
            worldsettings.a(s2);
            WorldServer world;
            if (j == 0) {
                IDataManager idatamanager = new ServerNBTManager(server.getWorldContainer(), s1, true);
                world = new WorldServer(this, idatamanager, s1, dimension, worldsettings, Environment.getEnvironment(dimension), gen);
                this.server.scoreboardManager = new org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager(this, world.getScoreboard());
            } else {
                IDataManager idatamanager = new ServerNBTManager(server.getWorldContainer(), name, true);
                // world =, b0 to dimension, s1 to name, added Environment and gen
                world = new SecondaryWorldServer(this, idatamanager, name, dimension, worldsettings, this.worlds.get(0), Environment.getEnvironment(dimension), gen);
            }

            if (gen != null) {
                world.getWorld().getPopulators().addAll(gen.getDefaultPopulators(world.getWorld()));
            }

            this.server.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(world.getWorld()));

            world.addIWorldAccess(new WorldManager(this, world));
            if (!this.N()) {
                world.getWorldData().setGameType(this.getGamemode());
            }

            this.worlds.add(world);
            this.playerList.setPlayerFileData(this.worlds.toArray(new WorldServer[this.worlds.size()]));
            // CraftBukkit end
        }

        this.a(this.getDifficulty());
        this.g();
    }

    protected void g() {
        //boolean flag = true;
        //boolean flag1 = true;
        //boolean flag2 = true;
        //boolean flag3 = true;
        int i = 0;

        //this.b("menu.generatingTerrain");
        //byte b0 = 0;

        // CraftBukkit start - fire WorldLoadEvent and handle whether or not to keep the spawn in memory
        for (byte m = 0; m < this.worlds.size(); ++m) { // Rinny - use byte instead of int
            WorldServer worldserver = this.worlds.get(m);
            MinecraftServer.i.info("Preparing start region for level " + m + " (Seed: " + worldserver.getSeed() + ")");
            if (!worldserver.getWorld().getKeepSpawnInMemory()) {
                continue;
            }

            ChunkCoordinates chunkcoordinates = worldserver.getSpawn();
            long j = ar();
            i = 0;

            // TODO: use short?
            for (int k = -192; k <= 192 && this.isRunning(); k += 16) {
                for (int l = -192; l <= 192 && this.isRunning(); l += 16) {
                    long i1 = ar();

                    if (i1 - j > 1000L) {
                        this.a_("Preparing spawn area", i * 100 / 625);
                        j = i1;
                    }

                    ++i;
                    worldserver.chunkProviderServer.getChunkAt(chunkcoordinates.x + k >> 4, chunkcoordinates.z + l >> 4);
                }
            }
        }

        for (WorldServer world : this.worlds) {
            this.server.getPluginManager().callEvent(new org.bukkit.event.world.WorldLoadEvent(world.getWorld()));
        }
        // CraftBukkit end
        this.n();
    }

    public abstract boolean getGenerateStructures();

    public abstract EnumGamemode getGamemode();

    public abstract EnumDifficulty getDifficulty();

    public abstract boolean isHardcore();

    public abstract int l();

    public abstract boolean m();

    protected void a_(String s, int i) {
        this.e = s;
        this.f = i;
        // CraftBukkit - Use FQN to work around decompiler issue
        MinecraftServer.i.info(s + ": " + i + "%");
    }

    protected void n() {
        this.e = null;
        this.f = 0;

        this.server.enablePlugins(org.bukkit.plugin.PluginLoadOrder.POSTWORLD); // CraftBukkit
    }

    protected void saveChunks(boolean flag) throws ExceptionWorldConflict { // CraftBukkit - added throws
        if (!this.M) {
            // CraftBukkit start - fire WorldSaveEvent
            // WorldServer[] aworldserver = this.worldServer;
        	final byte i = (byte) this.worlds.size(); // Rinny - use byte instead of int

            for (byte j = 0; j < i; ++j) { // Rinny - use byte instead of int
                WorldServer worldserver = this.worlds.get(j);

                if (worldserver != null) {
                    if (!flag) {
                        MinecraftServer.i.info("Saving chunks for level \'" + worldserver.getWorldData().getName() + "\'/" + worldserver.worldProvider.getName());
                    }

                    worldserver.save(true, (IProgressUpdate) null);
                    worldserver.saveLevel();

                    WorldSaveEvent event = new WorldSaveEvent(worldserver.getWorld());
                    this.server.getPluginManager().callEvent(event);
                    // CraftBukkit end
                }
            }
        }
    }

    public void stop() throws ExceptionWorldConflict { // CraftBukkit - added throws
        if (!this.M) {
            i.info("Stopping server");
            // CraftBukkit start
            if (this.server != null) {
                this.server.disablePlugins();
            }
            // CraftBukkit end

            if (this.ai() != null) {
                this.ai().b();
            }

            if (this.playerList != null) {
                i.info("Saving players");
                this.playerList.savePlayers();
                this.playerList.u();
            }

            if (this.worldServer != null) {
                i.info("Saving worlds");
                this.saveChunks(false);
            
                /* CraftBukkit start - Handled in saveChunks
                for (int i = 0; i < this.worldServer.length; ++i) {
                    WorldServer worldserver = this.worldServer[i];

                    worldserver.saveLevel();
                }
                // CraftBukkit end */
            }
            // Spigot start
            if( org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly )
            {
                i.info("Saving usercache.json");
                this.X.c();
            }
            //Spigot end
        }
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public void c(String s) {
        this.serverIp = s;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void safeShutdown() {
        this.isRunning = false;
    }

    // PaperSpigot start - Further improve tick loop
    private static final byte TPS = 20; // Rinny - use byte intead of int
    //private static final byte SUBTICK_PER_TICK = 2;  // Rinny - Define number of subticks per tick // Rinny - use byte intead of int
    private static final long SEC_IN_NANO = 1_000_000_000;
    private static final long TICK_TIME = SEC_IN_NANO / TPS;
    private static final long MAX_CATCHUP_BUFFER = TICK_TIME * TPS * 60L;
    //private static final int SAMPLE_INTERVAL = 20;
 
    public void run() {
        try {
            if (this.init()) {
                //long i = ar();
                //long j = 0L;

            	// Rinny - removed because bukkit already do it
                //this.q.setMOTD(new ChatComponentText(this.motd));
                //this.q.setServerInfo(new ServerPingServerData("1.7.x/1.8.x", 5));
            	// Rinny end
                if (!SpigotConfig.bungee) { // Rinny - s/o to GonPvP
                	this.loadIcon(this.q);
                }
                
                // Spigot start
                // PaperSpigot start - Further improve tick loop
                //long lastTick = System.nanoTime(), catchupTime = 0, curTime, wait, tickSection = lastTick;
                final long start = System.nanoTime();
                long lastTick = start - TICK_TIME;
                long catchupTime = 0L;
                // PaperSpigot end
                while (this.isRunning) {
                    long curTime = System.nanoTime();
                    // PaperSpigot start - Further improve tick loop
                    long wait = TICK_TIME - (curTime - lastTick);
                    if (wait > 0) {
                        if (catchupTime < 2_000_000L/*2E6*/) {
                            wait += Math.abs(catchupTime);
                        } else if (wait < catchupTime) {
                            catchupTime -= wait;
                            wait = 0;
                        } else {
                            wait -= catchupTime;
                            catchupTime = 0;
                        }
                    }
                    if (wait > 0) {
                        Thread.sleep(wait / 1_000_000);
                        curTime = System.nanoTime();
                        wait = TICK_TIME - (curTime - lastTick);
                    }
                    catchupTime = Math.min(MAX_CATCHUP_BUFFER, catchupTime - wait);
                    // Paperspigot end

                    MinecraftServer.currentTick++;
                    
                    // Rinny start - add subticking
                    /*for (int subtick = 0; subtick < SUBTICK_PER_TICK; subtick++) {
                        //long subtickStartTime = System.nanoTime();
                        
						this.subTick(); // tick everything

                        //long subtickEndTime = System.nanoTime();
                        //long subtickDuration = subtickEndTime - subtickStartTime;
                    }*/
                    // Rinny end
                    lastTick = curTime;
                    this.tickServer();
                    //this.O = true;
                }
                // Spigot end
            } else {
                this.a((CrashReport) null);
            }
        } catch (Throwable throwable) {
            i.error("Encountered an unexpected exception", throwable);
            // Spigot Start
            if ( throwable.getCause() != null ) {
                i.error( "\tCause of unexpected exception was", throwable.getCause() );
            }
            // Spigot End
            final  CrashReport crashreport = (throwable instanceof ReportedException ? this.b(((ReportedException) throwable).a()) : this.b(new CrashReport("Exception in server tick loop", throwable)));
            final File file1 = new File(new File(this.s(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
            
            i.error((crashreport.a(file1) ? "This crash report has been saved to: " + file1.getAbsolutePath() : "We were unable to save this crash report to disk."));
            this.a(crashreport);
        } finally {
            try {
                org.spigotmc.WatchdogThread.doStop();
                this.stop();
                this.isStopped = true;
            } catch (Throwable throwable1) {
                i.error("Exception stopping the server", throwable1);
            } finally {
                // CraftBukkit start - Restore terminal to original settings
                try {
                    this.reader.getTerminal().restore();
                } catch (Exception e) {
                }
                // CraftBukkit end
                this.t();
            }
        }
    }

    private void loadIcon(ServerPing serverping) {
        final File file1 = this.d("server-icon.png");

        if (file1.isFile()) {
            try {
                //final BufferedImage bufferedimage = ImageIO.read(file1);
                //Validate.validState(bufferedimage.getWidth() == bufferedimage.getHeight(), "Width must be equals to the height");
                //Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                //Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                //ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
                //ByteBuf bytebuf1 = Base64.encode(bytebuf);

                //serverping.setFavicon("data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8).replace("\n", ""));
            	final BufferedImage bufferedimage = ImageIO.read(file1);
                Validate.validState(bufferedimage.getWidth() == bufferedimage.getHeight(), "Width must be equals to the height");
                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedimage, "PNG", baos);
                byte[] imageInByte = baos.toByteArray();
                String imageDataString = Base64.getEncoder().encodeToString(imageInByte);
                serverping.setFavicon("data:image/png;base64," + imageDataString);
                // Clear and let GC do his job
                imageInByte = null;
            } catch (Exception exception) {
                i.error("Couldn\'t load server icon", exception);
            }
        }
    }

    protected File s() {
        return new File(".");
    }

    protected void a(CrashReport crashreport) {}

    protected void t() {}

    protected void tickServer() throws ExceptionWorldConflict { // CraftBukkit - added throws
        final long i = System.nanoTime();

        ++this.ticks;
        if (this.R) {
            this.R = false;
        }

        this.v();
        if (i - this.V >= 5000000000L) {
            this.V = i;
            this.q.setPlayerSample(new ServerPingPlayerSample(this.D(), Math.min(this.C(), 12))); // Rinny Math.min
            final GameProfile[] agameprofile = new GameProfile[Math.min(this.C(), 12)];
            final int j = MathHelper.nextInt(this.r, 0, this.C() - agameprofile.length);

            for (int k = 0; k < agameprofile.length; ++k) {
                agameprofile[k] = ((EntityPlayer) this.playerList.players.get(j + k)).getProfile();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            this.q.b().a(agameprofile);
        }

        if (this.autosavePeriod > 0) { // CraftBukkit
            //this.u.savePlayers();
            if ((this.ticks % this.autosavePeriod) == 0) this.playerList.savePlayers();
            // Spigot Start
            // We replace this with saving each individual world as this.saveChunks(...) is broken,
            // and causes the main thread to sleep for random amounts of time depending on chunk activity
            // Also pass flag to only save modified chunks -- PaperSpigot
            server.playerCommandState = true;
            for (World world : worlds) {
                world.getWorld().save(true);
            }
            server.playerCommandState = false;
            // this.saveChunks(true);
            // Spigot End
        }

        this.g[this.ticks % 100] = System.nanoTime() - i;
        org.spigotmc.WatchdogThread.tick(); // Spigot
    }
    
    private void subTick() {
    	// TODO: things to subtick
    	for (byte i = 0; i < this.worlds.size(); ++i) { // Rinny use byte instead of int, server has less than 254 world I promise you
            WorldServer worldserver = this.worlds.get(i);
            if (!playerList.players.isEmpty()) worldserver.getTracker().updatePlayers();
    	}
    }

    public void v() {
        // CraftBukkit start
        this.server.getScheduler().mainThreadHeartbeat(this.ticks);

        // Run tasks that are waiting on processing
        while (!processQueue.isEmpty()) {
            processQueue.remove().run();
        }
        org.bukkit.craftbukkit.chunkio.ChunkIOExecutor.tick();
        // Send time updates to everyone, it will get the right time from the world the player is in.
        if ((this.ticks % 20) == 0) {
        	boolean doDaylight;
        	long dayTime;
        	long worldTime;
	        for (final WorldServer world : this.worlds) {
	            doDaylight = world.getGameRules().getBoolean("doDaylightCycle");
	            dayTime = world.getDayTime();
	            worldTime = world.getTime();
	            
	            final PacketPlayOutUpdateTime worldPacket = new PacketPlayOutUpdateTime(worldTime, dayTime, doDaylight);
	            for (EntityHuman entityhuman : (List<EntityHuman>) world.players) {
	            	if (entityhuman.world != world) {
	            		continue;
	            	}
	            	if (entityhuman instanceof EntityPlayer entityplayer) { // Rinny
	            		long playerTime = entityplayer.getPlayerTime();
	            		PacketPlayOutUpdateTime packet = (playerTime == dayTime) ? worldPacket : new PacketPlayOutUpdateTime(worldTime, playerTime, doDaylight);
	            		entityplayer.playerConnection.sendPacket(packet); // Add support for per player time
	            	}
	            }
	        }
        }

        byte i; // Rinny - use byte instead of int

        for (i = 0; i < this.worlds.size(); ++i) {
            WorldServer worldserver = this.worlds.get(i);
            CrashReport crashreport;

            try {
                worldserver.doTick();
            } catch (Throwable throwable) {
                // Spigot Start
                try {
                    crashreport = CrashReport.a(throwable, "Exception ticking world");
                } catch (Throwable t){
                    throw new RuntimeException("Error generating crash report", t);
                }
                // Spigot End
                worldserver.a(crashreport);
                throw new ReportedException(crashreport);
            }
            try {
                worldserver.tickEntities();
            } catch (Throwable throwable1) {
                // Spigot Start
                try {
                    crashreport = CrashReport.a(throwable1, "Exception ticking world entities");
                } catch (Throwable t){
                    throw new RuntimeException("Error generating crash report", t);
                }
                // Spigot End
                worldserver.a(crashreport);
                throw new ReportedException(crashreport);
            }
            // Rinny - moved to this.subTick();
            if (!playerList.players.isEmpty()) worldserver.getTracker().updatePlayers();
        }

        this.ai().c();
        this.playerList.tick();

        for (i = 0; i < this.playerListBox.size(); ++i) {
            this.playerListBox.get(i).resize();
        }
        
        // Rinny start - add ServerDateChangeEvent
        if ((this.ticks % 200) == 0 && needDayUpdate()) {
        	this.server.getPluginManager().callEvent(new ServerDateChangeEvent(this.date));
        }
        // Rinny end
        
        CraftHologram.tickAll(); // Rinny
    }
    
    private boolean needDayUpdate() {
    	final Date now = new Date();
    	boolean needUpdate = false;
    	if (this.date.getDate() != now.getDate()) {
    		this.date = now;
    		needUpdate = true;
    	}
    	return needUpdate;
    }

    public boolean getAllowNether() {
        return true;
    }

    public void a(IUpdatePlayerListBox iupdateplayerlistbox) {
        this.playerListBox.add(iupdateplayerlistbox);
    }

    public static void main(final OptionSet options) { // CraftBukkit - replaces main(String[] astring)
        DispenserRegistry.b();
        org.spigotmc.ProtocolInjector.inject();

        try {
            DedicatedServer dedicatedserver = new DedicatedServer(options);

            if (options.has("port")) {
                final int port = (Integer) options.valueOf("port");
                if (port > 0) {
                    dedicatedserver.setPort(port);
                }
            }

            if (options.has("universe")) {
                dedicatedserver.universe = (File) options.valueOf("universe");
            }

            if (options.has("world")) {
                dedicatedserver.k((String) options.valueOf("world"));
            }

            dedicatedserver.primaryThread.start();
            // Runtime.getRuntime().addShutdownHook(new ThreadShutdown("Server Shutdown Thread", dedicatedserver));
            // CraftBukkit end
        } catch (Exception exception) {
            i.fatal("Failed to start the minecraft server", exception);
        }
    }

    /*public void x() {
        (new ThreadServerApplication(this, "Server thread")).start(); // CraftBukkit - prevent abuse
    }*/

    public File d(String s) {
        return new File(this.s(), s);
    }

    public void info(String s) {
        i.info(s);
    }

    public void warning(String s) {
        i.warn(s);
    }

    public WorldServer getWorldServer(int i) {
        // CraftBukkit start
        for (WorldServer world : this.worlds) {
            if (world.dimension == i) {
                return world;
            }
        }

        return this.worlds.get(0);
        // CraftBukkit end
    }

    public String y() {
        return this.serverIp;
    }

    public int z() {
        return this.t;
    }

    public String A() {
        return this.motd;
    }

    public String getVersion() {
        return "1.7.x/1.8.x/1.9.x";
    }

    public int C() {
        return this.playerList.getPlayerCount();
    }

    public int D() {
        return this.playerList.getMaxPlayers();
    }

    public String[] getPlayers() {
        return this.playerList.f();
    }

    public GameProfile[] F() {
        return this.playerList.g();
    }

    public String getPlugins() {
        // CraftBukkit start - Whole method
    	final StringBuilder result = new StringBuilder();
    	final org.bukkit.plugin.Plugin[] plugins = server.getPluginManager().getPlugins();

        result.append(server.getName());
        result.append(" on Bukkit ");
        result.append(server.getBukkitVersion());

        if (plugins.length > 0 && this.server.getQueryPlugins()) {
            result.append(": ");

            for (int i = 0; i < plugins.length; i++) {
                if (i > 0) {
                    result.append("; ");
                }

                result.append(plugins[i].getDescription().getName());
                result.append(" ");
                result.append(plugins[i].getDescription().getVersion().replaceAll(";", ","));
            }
        }

        return result.toString();
        // CraftBukkit end
    }

    // CraftBukkit start - fire RemoteServerCommandEvent
    public String g(final String s) { // final parameter
    	final Waitable<String> waitable = new Waitable<String>() {
            @Override
            protected String evaluate() {
                RemoteControlCommandListener.instance.e();
                // Event changes start
                final RemoteServerCommandEvent event = new RemoteServerCommandEvent(MinecraftServer.this.remoteConsole, s);
                MinecraftServer.this.server.getPluginManager().callEvent(event);
                // Event changes end
                final ServerCommand servercommand = new ServerCommand(event.getCommand(), RemoteControlCommandListener.instance);
                MinecraftServer.this.server.dispatchServerCommand(MinecraftServer.this.remoteConsole, servercommand); // CraftBukkit
                // this.o.a(RemoteControlCommandListener.instance, s);
                return RemoteControlCommandListener.instance.f();
            }};
        processQueue.add(waitable);
        try {
            return waitable.get();
        } catch (java.util.concurrent.ExecutionException e) {
            throw new RuntimeException("Exception processing rcon command " + s, e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Maintain interrupted state
            throw new RuntimeException("Interrupted processing rcon command " + s, e);
        }
        // CraftBukkit end
    }

    public boolean isDebugging() {
        return this.getPropertyManager().getBoolean("debug", false); // CraftBukkit - don't hardcode
    }

    public void h(String s) {
        i.error(s);
    }

    public void i(String s) {
        if (this.isDebugging()) {
            i.info(s);
        }
    }

    public String getServerModName() {
        return "CompressedPaper"; // CompressedPaper - CompressedPaper > // CompressedSpigot - CompressedSpigot > // PaperSpigot - PaperSpigot > // Spigot - Spigot > // CraftBukkit - cb > vanilla!
    }

    public CrashReport b(CrashReport crashreport) {
        crashreport.g().a("Profiler Position", new CrashReportProfilerPosition(this));
        if (this.worlds != null && !this.worlds.isEmpty() && this.worlds.get(0) != null) { // CraftBukkit > Rinny - use !isEmpty instead of size > 0
            crashreport.g().a("Vec3 Pool Size", new CrashReportVec3DPoolSize(this));
        }

        if (this.playerList != null) {
            crashreport.g().a("Player Count", new CrashReportPlayerCount(this));
        }

        return crashreport;
    }

    public List<String> a(ICommandListener icommandlistener, String s) {
        return this.server.tabComplete(icommandlistener, s);
    }

    public static MinecraftServer getServer() {
        return j;
    }

    public String getName() {
        return "Server";
    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        i.info(ichatbasecomponent.c());
    }

    public boolean a(int i, String s) {
        return true;
    }

    public ICommandHandler getCommandHandler() {
        return this.o;
    }

    public KeyPair K() {
        return this.G;
    }

    public int L() {
        return this.t;
    }

    public void setPort(int i) {
        this.t = i;
    }

    public String M() {
        return this.H;
    }

    public void j(String s) {
        this.H = s;
    }

    public boolean N() {
        return this.H != null;
    }

    public String O() {
        return this.I;
    }

    public void k(String s) {
        this.I = s;
    }

    public void a(KeyPair keypair) {
        this.G = keypair;
    }

    public void a(EnumDifficulty enumdifficulty) {
        // CraftBukkit start - Use worlds list for iteration
        for (byte j = 0; j < this.worlds.size(); ++j) { // Rinny - use byte instead of int
            WorldServer worldserver = this.worlds.get(j);
            // CraftBukkit end

            if (worldserver != null) {
                if (worldserver.getWorldData().isHardcore()) {
                    worldserver.difficulty = EnumDifficulty.HARD;
                    worldserver.setSpawnFlags(true, true);
                } else if (this.N()) {
                    worldserver.difficulty = enumdifficulty;
                    worldserver.setSpawnFlags(worldserver.difficulty != EnumDifficulty.PEACEFUL, true);
                } else {
                    worldserver.difficulty = enumdifficulty;
                    worldserver.setSpawnFlags(this.getSpawnMonsters(), this.spawnAnimals);
                }
            }
        }
    }

    protected boolean getSpawnMonsters() {
        return true;
    }

    /*public void c(boolean flag) {
        this.L = flag;
    }*/

    public Convertable getConvertable() {
        return this.convertable;
    }

    public void U() {
        this.M = true;
        this.getConvertable().d();

        // CraftBukkit start
        for (byte i = 0; i < this.worlds.size(); ++i) { // Rinny - use byte instead of int
            WorldServer worldserver = this.worlds.get(i);
            // CraftBukkit end

            if (worldserver != null) {
                worldserver.saveLevel();
            }
        }

        this.getConvertable().e(this.worlds.get(0).getDataManager().g()); // CraftBukkit
        this.safeShutdown();
    }

    public String getResourcePack() {
        return this.N;
    }

    public void setTexturePack(String s) {
        this.N = s;
    }

    public void a(MojangStatisticsGenerator mojangstatisticsgenerator) {
        mojangstatisticsgenerator.a("whitelist_enabled", Boolean.valueOf(false));
        mojangstatisticsgenerator.a("whitelist_count", Integer.valueOf(0));
        mojangstatisticsgenerator.a("players_current", Integer.valueOf(this.C()));
        mojangstatisticsgenerator.a("players_max", Integer.valueOf(this.D()));
        mojangstatisticsgenerator.a("players_seen", Integer.valueOf(this.playerList.getSeenPlayers().length));
        mojangstatisticsgenerator.a("uses_auth", Boolean.valueOf(this.onlineMode));
        mojangstatisticsgenerator.a("gui_state", this.ak() ? "enabled" : "disabled");
        mojangstatisticsgenerator.a("run_time", Long.valueOf((ar() - mojangstatisticsgenerator.g()) / 60L * 1000L));
        mojangstatisticsgenerator.a("avg_tick_ms", Integer.valueOf((int) (MathHelper.a(this.g) * 1.0E-6D)));
        int i = 0;

        // CraftBukkit start - use worlds list for iteration
        for (byte j = 0; j < this.worlds.size(); ++j) { // Rinny - use byte instead of int
            WorldServer worldserver = this.worlds.get(j);
            if (worldServer != null) {
                // CraftBukkit end
                WorldData worlddata = worldserver.getWorldData();

                mojangstatisticsgenerator.a("world[" + i + "][dimension]", Integer.valueOf(worldserver.worldProvider.dimension));
                mojangstatisticsgenerator.a("world[" + i + "][mode]", worlddata.getGameType());
                mojangstatisticsgenerator.a("world[" + i + "][difficulty]", worldserver.difficulty);
                mojangstatisticsgenerator.a("world[" + i + "][hardcore]", Boolean.valueOf(worlddata.isHardcore()));
                mojangstatisticsgenerator.a("world[" + i + "][generator_name]", worlddata.getType().name());
                mojangstatisticsgenerator.a("world[" + i + "][generator_version]", Integer.valueOf(worlddata.getType().getVersion()));
                mojangstatisticsgenerator.a("world[" + i + "][height]", Integer.valueOf(this.E));
                mojangstatisticsgenerator.a("world[" + i + "][chunks_loaded]", Integer.valueOf(worldserver.L().getLoadedChunks()));
                ++i;
            }
        }

        mojangstatisticsgenerator.a("worlds", Integer.valueOf(i));
    }

    public void b(MojangStatisticsGenerator mojangstatisticsgenerator) {
        mojangstatisticsgenerator.b("singleplayer", Boolean.valueOf(this.N()));
        mojangstatisticsgenerator.b("server_brand", this.getServerModName());
        mojangstatisticsgenerator.b("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
        mojangstatisticsgenerator.b("dedicated", Boolean.valueOf(this.X()));
    }

    public abstract boolean X();

    public boolean getOnlineMode() {
        return this.server.getOnlineMode(); // CraftBukkit
    }

    public void setOnlineMode(boolean flag) {
        this.onlineMode = flag;
    }

    public boolean getSpawnAnimals() {
        return this.spawnAnimals;
    }

    public void setSpawnAnimals(boolean flag) {
        this.spawnAnimals = flag;
    }

    public boolean getSpawnNPCs() {
        return this.spawnNPCs;
    }

    public void setSpawnNPCs(boolean flag) {
        this.spawnNPCs = flag;
    }

    public boolean getPvP() {
        return this.pvpMode;
    }

    public void setPvP(boolean flag) {
        this.pvpMode = flag;
    }

    public boolean getAllowFlight() {
        return this.allowFlight;
    }

    public void setAllowFlight(boolean flag) {
        this.allowFlight = flag;
    }

    public abstract boolean getEnableCommandBlock();

    public String getMotd() {
        return this.motd;
    }

    public void setMotd(String s) {
        this.motd = s;
    }

    public int getMaxBuildHeight() {
        return this.E;
    }

    public void c(int i) {
        this.E = i;
    }

    public boolean isStopped() {
        return this.isStopped;
    }

    public PlayerList getPlayerList() {
        return this.playerList;
    }

    public void a(PlayerList playerlist) {
        this.playerList = playerlist;
    }

    public void a(EnumGamemode enumgamemode) {
        // CraftBukkit start - use worlds list for iteration
        for (int i = 0; i < this.worlds.size(); ++i) {
            getServer().worlds.get(i).getWorldData().setGameType(enumgamemode);
            // CraftBukkit end
        }
    }

    // Spigot Start
    public ServerConnection getServerConnection()
    {
        return this.p;
    }
    // Spigot End
    public ServerConnection ai() {
        return ( this.p ) == null ? this.p = new ServerConnection( this ) : this.p; // Spigot
    }

    public boolean ak() {
        return false;
    }

    public abstract String a(EnumGamemode enumgamemode, boolean flag);

    public int al() {
        return this.ticks;
    }

    public void am() {
        this.R = true;
    }
    
    private ChunkCoordinates cachedChunkCoordinates = new ChunkCoordinates(0, 0, 0); // Rinny
    public ChunkCoordinates getChunkCoordinates() {
        return this.cachedChunkCoordinates;
    }

    public World getWorld() {
        return this.worlds.get(0); // CraftBukkit
    }

    public int getSpawnProtection() {
        return 16;
    }

    public boolean a(World world, int i, int j, int k, EntityHuman entityhuman) {
        return false;
    }

    public void setForceGamemode(boolean flag) {
        this.S = flag;
    }

    public boolean getForceGamemode() {
        return this.S;
    }

    public Proxy aq() {
        return this.d;
    }

    public static long ar() {
        return System.currentTimeMillis();
    }

    public int getIdleTimeout() {
        return this.F;
    }

    public void setIdleTimeout(int i) {
        this.F = i;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatComponentText(this.getName());
    }

    public boolean at() {
        return true;
    }

    public MinecraftSessionService av() {
        return this.U;
    }

    public GameProfileRepository getGameProfileRepository() {
        return this.W;
    }

    public UserCache getUserCache() {
        return this.X;
    }

    public ServerPing ay() {
        return this.q;
    }

    public void az() {
        this.V = 0L;
    }

    public static Logger getLogger() {
        return i;
    }
}
