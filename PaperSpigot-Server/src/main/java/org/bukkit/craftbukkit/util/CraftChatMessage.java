package org.bukkit.craftbukkit.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.ChatClickable;
import net.minecraft.server.ChatComponentText;
import net.minecraft.server.ChatModifier;
import net.minecraft.server.EnumChatFormat;
import net.minecraft.server.EnumClickAction;
import net.minecraft.server.IChatBaseComponent;

public final class CraftChatMessage {
    private static class StringMessage {
        private static final Map<Character, EnumChatFormat> formatMap;
        private static final Pattern INCREMENTAL_PATTERN = Pattern.compile("(" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + "[0-9a-fk-or])|(\\n)|((?:(?:https?)://)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:[" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + " \\n]|$))))", Pattern.CASE_INSENSITIVE);

        static {
        	ImmutableMap.Builder<Character, EnumChatFormat> builder = ImmutableMap.builder();
            for (EnumChatFormat format : EnumChatFormat.values()) {
                builder.put(Character.toLowerCase(format.getChar()), format);
            }
            formatMap = builder.build();
        }

        private final List<IChatBaseComponent> list = new ArrayList<IChatBaseComponent>();
        private IChatBaseComponent currentChatComponent = new ChatComponentText("");
        private ChatModifier modifier = new ChatModifier();
        private final IChatBaseComponent[] output;
        private int currentIndex;
        private final String message;

        /*private StringMessage(String message) {
            this.message = message;
            if (message == null) {
                output = new IChatBaseComponent[] { currentChatComponent };
                return;
            }
            list.add(currentChatComponent);

            Matcher matcher = INCREMENTAL_PATTERN.matcher(message);
            String match = null;
            while (matcher.find()) {
                int groupId = 0;
                while ((match = matcher.group(++groupId)) == null) {
                    // NOOP
                }
                appendNewComponent(matcher.start(groupId));
                switch (groupId) {
                case 1:
                    EnumChatFormat format = formatMap.get(match.toLowerCase().charAt(1));
                    if (format == EnumChatFormat.RESET) {
                        modifier = new ChatModifier();
                    } else if (format.isFormat()) {
                        switch (format) {
                        case BOLD:
                            modifier.setBold(Boolean.TRUE);
                            break;
                        case ITALIC:
                            modifier.setItalic(Boolean.TRUE);
                            break;
                        case STRIKETHROUGH:
                            modifier.setStrikethrough(Boolean.TRUE);
                            break;
                        case UNDERLINE:
                            modifier.setUnderline(Boolean.TRUE);
                            break;
                        case RANDOM:
                            modifier.setRandom(Boolean.TRUE);
                            break;
                        default:
                            throw new AssertionError("Unexpected message format");
                        }
                    } else { // Color resets formatting
                        modifier = new ChatModifier().setColor(format);
                    }
                    break;
                case 2:
                    currentChatComponent = null;
                    break;
                case 3:
                    if ( !( match.startsWith( "http://" ) || match.startsWith( "https://" ) ) ) {
                        match = "http://" + match;
                    }
                    modifier.setChatClickable(new ChatClickable(EnumClickAction.OPEN_URL, match));
                    appendNewComponent(matcher.end(groupId));
                    modifier.setChatClickable((ChatClickable) null);
                }
                currentIndex = matcher.end(groupId);
            }

            if (currentIndex < message.length()) {
                appendNewComponent(message.length());
            }

            output = list.toArray(new IChatBaseComponent[list.size()]);
        }*/
        private StringMessage(String message) {
            this.message = message;
            if (message == null) {
                output = new IChatBaseComponent[] { currentChatComponent };
                return;
            }

            list.add(currentChatComponent);
            processMessage();
            output = list.toArray(new IChatBaseComponent[0]);
        }

        private void processMessage() {
            Matcher matcher = INCREMENTAL_PATTERN.matcher(message);

            while (matcher.find()) {
                int groupId = determineMatchedGroup(matcher);

                appendNewComponent(matcher.start(groupId));

                switch (groupId) {
                    case 1 -> applyFormat(matcher.group(groupId));
                    case 2 -> currentChatComponent = null; // Line break
                    case 3 -> processUrl(matcher.group(groupId), matcher.end(groupId));
                }

                currentIndex = matcher.end(groupId);
            }

            if (currentIndex < message.length()) {
                appendNewComponent(message.length());
            }
        }

        private int determineMatchedGroup(Matcher matcher) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    return i;
                }
            }
            throw new IllegalStateException("No matching group found");
        }

        private void applyFormat(String match) {
            EnumChatFormat format = formatMap.get(Character.toLowerCase(match.charAt(1)));

            if (format == EnumChatFormat.RESET) {
                modifier = new ChatModifier();
            } else if (format.isFormat()) {
                switch (format) {
                    case BOLD -> modifier.setBold(true);
                    case ITALIC -> modifier.setItalic(true);
                    case STRIKETHROUGH -> modifier.setStrikethrough(true);
                    case UNDERLINE -> modifier.setUnderline(true);
                    case RANDOM -> modifier.setRandom(true);
                    default -> throw new IllegalArgumentException("Unexpected value: " + format);
                }
            } else {
                modifier = new ChatModifier().setColor(format);
            }
        }

        private void processUrl(String match, int endIndex) {
            if (!(match.startsWith("http://") || match.startsWith("https://"))) {
                match = "http://" + match;
            }
            modifier.setChatClickable(new ChatClickable(EnumClickAction.OPEN_URL, match));
            appendNewComponent(endIndex);
            modifier.setChatClickable(null);
        }

        private void appendNewComponent(int index) {
            if (index <= currentIndex) return;

            String text = message.substring(currentIndex, index);
            IChatBaseComponent addition = new ChatComponentText(text).setChatModifier(modifier.clone());

            if (currentChatComponent == null) {
                currentChatComponent = new ChatComponentText("");
                list.add(currentChatComponent);
            }
            currentChatComponent.addSibling(addition);
            currentIndex = index;
        }

        /*private IChatBaseComponent[] getOutput() {
            return output;
        }

        private void appendNewComponent(int index) {
            if (index <= currentIndex) {
                return;
            }
            IChatBaseComponent addition = new ChatComponentText(message.substring(currentIndex, index)).setChatModifier(modifier);
            currentIndex = index;
            modifier = modifier.clone();
            if (currentChatComponent == null) {
                currentChatComponent = new ChatComponentText("");
                list.add(currentChatComponent);
            }
            currentChatComponent.addSibling(addition);
        }*/

        private IChatBaseComponent[] getOutput() {
            return output;
        }
    }

    public static IChatBaseComponent[] fromString(String message) {
        return new StringMessage(message).getOutput();
    }

    private CraftChatMessage() {
    }
}
