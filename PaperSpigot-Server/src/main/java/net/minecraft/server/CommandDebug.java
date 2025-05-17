package net.minecraft.server;

public class CommandDebug extends CommandAbstract {

	@Override
	public String getCommand() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}
	// KEEP THIS CLEAR
	/*private static final Logger a = LogManager.getLogger();

	private long b;

	private int c;

	public String getCommand() {
		return "debug";
	}

	public int a() {
		return 3;
	}*/

	public String c(ICommandListener paramICommandListener) {
		return "commands.debug.usage";
	}

	public void execute(ICommandListener paramICommandListener, String[] paramArrayOfString) {
		/*if (paramArrayOfString.length == 1) {
			if (paramArrayOfString[0].equals("start")) {
				a(paramICommandListener, this, "commands.debug.start", new Object[0]);
				MinecraftServer.getServer().am();
				this.b = MinecraftServer.ar();
				this.c = MinecraftServer.getServer().al();
				return;
			}
			if (paramArrayOfString[0].equals("stop")) {
				if (!(MinecraftServer.getServer()).methodProfiler.a)
					throw new CommandException("commands.debug.notStarted", new Object[0]);
				long l1 = MinecraftServer.ar();
				int i = MinecraftServer.getServer().al();
				long l2 = l1 - this.b;
				int j = i - this.c;
				a(l2, j);
				(MinecraftServer.getServer()).methodProfiler.a = false;
				a(paramICommandListener, this, "commands.debug.stop",
						new Object[] { Float.valueOf((float) l2 / 1000.0F), Integer.valueOf(j) });
				return;
			}
		}
		throw new ExceptionUsage("commands.debug.usage", new Object[0]);*/
	}

	/*private void a(long paramLong, int paramInt) {
		File file = new File(MinecraftServer.getServer().d("debug"),
				"profile-results-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + ".txt");
		file.getParentFile().mkdirs();
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(b(paramLong, paramInt));
			fileWriter.close();
		} catch (Throwable throwable) {
			a.error("Could not save profiler results to " + file, throwable);
		}
	}

	private String b(long paramLong, int paramInt) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("---- Minecraft Profiler Results ----\n");
		stringBuilder.append("// ");
		stringBuilder.append(d());
		stringBuilder.append("\n\n");
		stringBuilder.append("Time span: ").append(paramLong).append(" ms\n");
		stringBuilder.append("Tick span: ").append(paramInt).append(" ticks\n");
		stringBuilder.append("// This is approximately ")
				.append(String.format("%.2f", new Object[] { Float.valueOf(paramInt / (float) paramLong / 1000.0F) }))
				.append(" ticks per second. It should be ").append(20).append(" ticks per second\n\n");
		stringBuilder.append("--- BEGIN PROFILE DUMP ---\n\n");
		a(0, "root", stringBuilder);
		stringBuilder.append("--- END PROFILE DUMP ---\n\n");
		return stringBuilder.toString();
	}

	private void a(int paramInt, String paramString, StringBuilder paramStringBuilder) {
		List<ProfilerInfo> list = (MinecraftServer.getServer()).methodProfiler.b(paramString);
		if (list == null || list.size() < 3)
			return;
		for (byte b = 1; b < list.size(); b++) {
			ProfilerInfo profilerInfo = list.get(b);
			paramStringBuilder.append(String.format("[%02d] ", new Object[] { Integer.valueOf(paramInt) }));
			for (byte b1 = 0; b1 < paramInt; b1++)
				paramStringBuilder.append(" ");
			paramStringBuilder.append(profilerInfo.c);
			paramStringBuilder.append(" - ");
			paramStringBuilder.append(String.format("%.2f", new Object[] { Double.valueOf(profilerInfo.a) }));
			paramStringBuilder.append("%/");
			paramStringBuilder.append(String.format("%.2f", new Object[] { Double.valueOf(profilerInfo.b) }));
			paramStringBuilder.append("%\n");
			if (!profilerInfo.c.equals("unspecified"))
				try {
					a(paramInt + 1, paramString + "." + profilerInfo.c, paramStringBuilder);
				} catch (Exception exception) {
					paramStringBuilder.append("[[ EXCEPTION " + exception + " ]]");
				}
		}
	}

	private static String d() {
		String[] arrayOfString = { "Shiny numbers!", "Am I not running fast enough? :(",
				"I'm working as hard as I can!", "Will I ever be good enough for you? :(", "Speedy. Zoooooom!",
				"Hello world", "40% better than a crash report.", "Now with extra numbers", "Now with less numbers",
				"Now with the same numbers", "You should add flames to things, it makes them go faster!",
				"Do you feel the need for... optimization?", "*cracks redstone whip*",
				"Maybe if you treated it better then it'll have more motivation to work faster! Poor server." };
		try {
			return arrayOfString[(int) (System.nanoTime() % arrayOfString.length)];
		} catch (Throwable throwable) {
			return "Witty comment unavailable :(";
		}
	}

	public List tabComplete(ICommandListener paramICommandListener, String[] paramArrayOfString) {
		if (paramArrayOfString.length == 1)
			return a(paramArrayOfString, new String[] { "start", "stop" });
		return null;
	}*/
}