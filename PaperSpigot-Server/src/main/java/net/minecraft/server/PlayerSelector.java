package net.minecraft.server;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;

public class PlayerSelector {
    private static final Pattern a = Pattern.compile("^@([parf])(?:\\[([\\w=,!-]*)\\])?$");
    private static final Pattern b = Pattern.compile("\\G([-!]?[\\w-]*)(?:$|,)");
    private static final Pattern c = Pattern.compile("\\G(\\w+)=([-!]?[\\w-]*)(?:$|,)");

    public static EntityPlayer getPlayer(ICommandListener icommandlistener, String s) {
        EntityPlayer[] aentityplayer = getPlayers(icommandlistener, s);

        return aentityplayer != null && aentityplayer.length == 1 ? aentityplayer[0] : null;
    }

    public static IChatBaseComponent getPlayerNames(ICommandListener icommandlistener, String s) {
        EntityPlayer[] aentityplayer = getPlayers(icommandlistener, s);

        if (aentityplayer != null && aentityplayer.length != 0) {
            IChatBaseComponent[] aichatbasecomponent = new IChatBaseComponent[aentityplayer.length];

            for (int i = 0; i < aichatbasecomponent.length; ++i) {
                aichatbasecomponent[i] = aentityplayer[i].getScoreboardDisplayName();
            }

            return CommandAbstract.a(aichatbasecomponent);
        }
        return null;
    }

    /*public static EntityPlayer[] getPlayers(ICommandListener icommandlistener, String s) {
        // CraftBukkit start - disable playerselections for ICommandListeners other than command blocks
        if (!(icommandlistener instanceof CommandBlockListenerAbstract)) {
            return null;
        }
        // CraftBukkit end

        Matcher matcher = a.matcher(s);

        if (matcher.matches()) {
            Map map = h(matcher.group(2));
            String s1 = matcher.group(1);
            int i = c(s1);
            int j = d(s1);
            int k = f(s1);
            int l = e(s1);
            int i1 = g(s1);
            int j1 = EnumGamemode.NONE.getId();
            ChunkCoordinates chunkcoordinates = icommandlistener.getChunkCoordinates();
            Map map1 = a(map);
            String s2 = null;
            String s3 = null;
            boolean flag = false;
            
            if (map.containsKey("rm")) {
                i = MathHelper.a((String) map.get("rm"), i);
                flag = true;
            }

            if (map.containsKey("r")) {
                j = MathHelper.a((String) map.get("r"), j);
                flag = true;
            }

            if (map.containsKey("lm")) {
                k = MathHelper.a((String) map.get("lm"), k);
            }

            if (map.containsKey("l")) {
                l = MathHelper.a((String) map.get("l"), l);
            }

            if (map.containsKey("x")) {
                chunkcoordinates.x = MathHelper.a((String) map.get("x"), chunkcoordinates.x);
                flag = true;
            }

            if (map.containsKey("y")) {
                chunkcoordinates.y = MathHelper.a((String) map.get("y"), chunkcoordinates.y);
                flag = true;
            }

            if (map.containsKey("z")) {
                chunkcoordinates.z = MathHelper.a((String) map.get("z"), chunkcoordinates.z);
                flag = true;
            }

            if (map.containsKey("m")) {
                j1 = MathHelper.a((String) map.get("m"), j1);
            }

            if (map.containsKey("c")) {
                i1 = MathHelper.a((String) map.get("c"), i1);
            }

            if (map.containsKey("team")) {
                s3 = (String) map.get("team");
            }

            if (map.containsKey("name")) {
                s2 = (String) map.get("name");
            }

            World world = flag ? icommandlistener.getWorld() : null;
            List list;

            if (!s1.equals("p") && !s1.equals("a")) {
                if (s1.equals("r")) {
                    list = MinecraftServer.getServer().getPlayerList().getPlayersInRange(chunkcoordinates, i, j, 0, j1, k, l, map1, s2, s3, world);
                    Collections.shuffle(list);
                    list = list.subList(0, Math.min(i1, list.size()));
                    return list.isEmpty() ? new EntityPlayer[0] : (EntityPlayer[]) list.toArray(new EntityPlayer[list.size()]);
                } else {
                    return null;
                }
            } else {
                list = MinecraftServer.getServer().getPlayerList().getPlayersInRange(chunkcoordinates, i, j, i1, j1, k, l, map1, s2, s3, world);
                return list.isEmpty() ? new EntityPlayer[0] : (EntityPlayer[]) list.toArray(new EntityPlayer[list.size()]);
            }
        } else {
            return null;
        }
    }*/
    
    public static EntityPlayer[] getPlayers(ICommandListener icommandlistener, String s) {
        if (!(icommandlistener instanceof CommandBlockListenerAbstract)) {
        	return null;
        }

        final Matcher matcher = a.matcher(s);
        if (!matcher.matches()) {
        	return null;
        }

        final Map<String, String> map = h(matcher.group(2));
        final String selector = matcher.group(1);

        int minRadius = 0, maxRadius = 0, minLevel = 0, maxLevel = 0, count = Integer.MAX_VALUE;
        int gamemode = EnumGamemode.NONE.getId();
        String name = null, team = null;

        boolean locationSpecified = false;
        ChunkCoordinates coords = icommandlistener.getChunkCoordinates();

        minRadius = parse(map.get("rm"), minRadius);
        maxRadius = parse(map.get("r"), maxRadius);
        minLevel = parse(map.get("lm"), minLevel);
        maxLevel = parse(map.get("l"), maxLevel);

        String x;
        if ((x = map.get("x")) != null) { 
        	coords.x = parse(x, coords.x); 
        	locationSpecified = true; 
        }
        String y;
        if ((y = map.get("y")) != null) { 
        	coords.y = parse(y, coords.y); 
        	locationSpecified = true; 
        }
        String z;
        if ((z = map.get("z")) != null) { 
        	coords.z = parse(z, coords.z); 
        	locationSpecified = true; 
        }

        gamemode = parse(map.get("m"), gamemode);
        count = parse(map.get("c"), count);

        name = map.get("name");
        team = map.get("team");

        final Map<String, Integer> extraArgs = a(map);
        final World world = locationSpecified ? icommandlistener.getWorld() : null;

        List<EntityPlayer> players = MinecraftServer.getServer().getPlayerList().getPlayersInRange(
            coords, minRadius, maxRadius, Integer.MAX_VALUE,
            gamemode, minLevel, maxLevel, extraArgs, name, team, world
        );

        if (players.isEmpty()) return new EntityPlayer[0];

        switch (selector) {
            case "a":
                if (count < players.size()) {
                    players = players.subList(0, count);
                }
                break;
            case "p":
                players.sort(Comparator.comparingDouble(p -> p.getBukkitEntity().getLocation().distanceSquared(
                    new Location(null, coords.x, coords.y, coords.z)
                )));
                players = players.subList(0, Math.min(count, players.size()));
                break;
            case "r": 
                Collections.shuffle(players);
                players = players.subList(0, Math.min(count, players.size()));
                break;
            default:
                return null;
        }

        return players.toArray(new EntityPlayer[0]);
    }

    private static int parse(String value, int fallback) {
        return value != null ? MathHelper.a(value, fallback) : fallback;
    }

    public static Map<String, Integer> a(Map<String, String> map) {
        Map<String, Integer> hmap = new HashMap<>();
        Iterator<String> iterator = map.keySet().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            if (s.startsWith("score_") && s.length() > "score_".length()) {
                String s1 = s.substring("score_".length());

                hmap.put(s1, Integer.valueOf(MathHelper.a(map.get(s), 1)));
            }
        }

        return hmap;
    }

    public static boolean isList(String s) {
        Matcher matcher = a.matcher(s);

        if (matcher.matches()) {
            Map<String, String> map = h(matcher.group(2));
            String s1 = matcher.group(1);
            int i = g(s1);

            String exist = map.get("c"); // Rinny
            if (exist != null) {
                i = MathHelper.a(exist, i);
            }

            return i != 1;
        } else {
            return false;
        }
    }

    public static boolean isPattern(String s, String s1) {
        Matcher matcher = a.matcher(s);

        if (matcher.matches()) {
            String s2 = matcher.group(1);

            return s1 == null || s1.equals(s2);
        } else {
            return false;
        }
    }

    public static boolean isPattern(String s) {
        return isPattern(s, (String) null);
    }

    private static final int g(String s) {
        return s.equals("a") ? 0 : 1;
    }

    private static Map<String, String> h(String s) {
        Map<String, String> hashmap = new HashMap<>();

        if (s == null) {
            return hashmap;
        } else {
            Matcher matcher = b.matcher(s);
            int i = 0;

            int j;

            for (j = -1; matcher.find(); j = matcher.end()) {
                final String s1 = switch (i++) {
	                case 0 -> "x";
	                case 1 -> "y";
	                case 2 -> "z";
	                case 3 -> "r";
	                default -> null;
                };

                if (s1 != null && matcher.group(1).length() > 0) {
                    hashmap.put(s1, matcher.group(1));
                }
            }

            if (j < s.length()) {
                matcher = c.matcher(j == -1 ? s : s.substring(j));

                while (matcher.find()) {
                    hashmap.put(matcher.group(1), matcher.group(2));
                }
            }

            return hashmap;
        }
    }
}
