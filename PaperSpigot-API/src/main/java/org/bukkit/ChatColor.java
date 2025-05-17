package org.bukkit;

import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * All supported color values for chat
 */
public enum ChatColor {
    /**
     * Represents black
     */
    BLACK('0'),
    /**
     * Represents dark blue
     */
    DARK_BLUE('1'),
    /**
     * Represents dark green
     */
    DARK_GREEN('2'),
    /**
     * Represents dark blue (aqua)
     */
    DARK_AQUA('3'),
    /**
     * Represents dark red
     */
    DARK_RED('4'),
    /**
     * Represents dark purple
     */
    DARK_PURPLE('5'),
    /**
     * Represents gold
     */
    GOLD('6'),
    /**
     * Represents gray
     */
    GRAY('7'),
    /**
     * Represents dark gray
     */
    DARK_GRAY('8'),
    /**
     * Represents blue
     */
    BLUE('9'),
    /**
     * Represents green
     */
    GREEN('a'),
    /**
     * Represents aqua
     */
    AQUA('b'),
    /**
     * Represents red
     */
    RED('c'),
    /**
     * Represents light purple
     */
    LIGHT_PURPLE('d'),
    /**
     * Represents yellow
     */
    YELLOW('e'),
    /**
     * Represents white
     */
    WHITE('f'),
    /**
     * Represents magical characters that change around randomly
     */
    MAGIC('k', true),
    /**
     * Makes the text bold.
     */
    BOLD('l', true),
    /**
     * Makes a line appear through the text.
     */
    STRIKETHROUGH('m', true),
    /**
     * Makes the text appear underlined.
     */
    UNDERLINE('n', true),
    /**
     * Makes the text italic.
     */
    ITALIC('o', true),
    /**
     * Resets all previous chat colors or formats.
     */
    RESET('r');

    /**
     * The special character which prefixes all chat colour codes. Use this if
     * you need to dynamically convert colour codes from your custom format.
     */
    public static final char COLOR_CHAR = '\u00A7';
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");
    private static final ImmutableSet<Character> VALID_COLOR_CHARS = ImmutableSet.of(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
            'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd', 'E', 'e', 'F', 'f',
            'K', 'k', 'L', 'l', 'M', 'm', 'N', 'n', 'O', 'o', 'R', 'r'
        );

    private final char code;
    private final boolean isFormat;
    private final String toString;
    private final static ImmutableMap<Character, ChatColor> BY_CHAR;
    static {
        ImmutableMap.Builder<Character, ChatColor> byCharBuilder = ImmutableMap.builder();

        for (ChatColor color : values()) {
            byCharBuilder.put(color.code, color);
        }

        BY_CHAR = byCharBuilder.build();
    }

    private ChatColor(char code) {
        this(code, false);
    }

    private ChatColor(char code, boolean isFormat) {
        this.code = code;
        this.isFormat = isFormat;
        this.toString = new String(new char[] {COLOR_CHAR, code});
    }

    /**
     * Gets the char value associated with this color
     *
     * @return A char value of this color code
     */
    public char getChar() {
        return code;
    }

    @Override
    public String toString() {
        return toString;
    }

    /**
     * Checks if this code is a format code as opposed to a color code.
     */
    public boolean isFormat() {
        return isFormat;
    }

    /**
     * Checks if this code is a color code as opposed to a format code.
     */
    public boolean isColor() {
        return !isFormat && this != RESET;
    }

    /**
     * Gets the color represented by the specified color code
     *
     * @param code Code to check
     * @return Associative {@link org.bukkit.ChatColor} with the given code,
     *     or null if it doesn't exist
     */
    public static ChatColor getByChar(char code) {
        return BY_CHAR.get(code);
    }

    /**
     * Gets the color represented by the specified color code
     *
     * @param code Code to check
     * @return Associative {@link org.bukkit.ChatColor} with the given code,
     *     or null if it doesn't exist
     */
    public static ChatColor getByChar(String code) {
        Validate.notNull(code, "Code cannot be null");
        Validate.isTrue(code.length() > 0, "Code must have at least one char");

        return BY_CHAR.get(code.charAt(0));
    }

    /**
     * Strips the given message of all color codes
     *
     * @param input String to strip of color
     * @return A copy of the input string, without any coloring
     */
    public static String stripColor(final String input) {
        if (input == null) {
            return null;
        }

        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Translates a string using an alternate color code character into a
     * string that uses the internal ChatColor.COLOR_CODE color code
     * character. The alternate color code character will only be replaced if
     * it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
     *
     * @param altColorChar The alternate color code character to replace. Ex: &
     * @param textToTranslate Text containing the alternate color code character.
     * @return Text containing the ChatColor.COLOR_CODE color code character.
     */
    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        /*char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = ChatColor.COLOR_CHAR;
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);*/
    	final int length = textToTranslate.length();
        final char[] b = textToTranslate.toCharArray();
        StringBuilder result = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            if (b[i] == altColorChar && i < length - 1 && VALID_COLOR_CHARS.contains(b[i + 1])) {
                result.append(COLOR_CHAR);
                result.append(Character.toLowerCase(b[i + 1]));
                i++; // skip the next character as it's already processed
            } else {
                result.append(b[i]);
            }
        }

        return result.toString();
    }
    
    /**
     * Translates a string using an alternate color code character into a
     * string that uses the internal ChatColor.COLOR_CODE color code
     * character. The alternate color code character will only be replaced if
     * it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
     *
     * @param altColorChar The alternate color code character to replace. Ex: &
     * @param textArrayToTranslate Text containing the alternate color code character.
     * @return TextArray containing the ChatColor.COLOR_CODE color code character.
     */
    public static String[] translateAlternateColorCodes(char altColorChar, String[] textArrayToTranslate) {
        String[] translatedTexts = new String[textArrayToTranslate.length];

        for (int j = 0; j < textArrayToTranslate.length; j++) {
            translatedTexts[j] = translateAlternateColorCodes(altColorChar, textArrayToTranslate[j]);
        }

        return translatedTexts;
    }

    /**
     * Gets the ChatColors used at the end of the given input string.
     *
     * @param input Input string to retrieve the colors from.
     * @return Any remaining ChatColors to pass onto the next line.
     */
    public static String getLastColors(String input) {
    	StringBuilder result = new StringBuilder(); // Rinny - Use StringBuilder
        final int length = input.length();

        // Search backwards from the end as it is faster
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == COLOR_CHAR && index < length - 1) {
                char c = input.charAt(index + 1);
                ChatColor color = getByChar(c);

                if (color != null) {
                    result.insert(0, color.toString());

                    // Once we find a color or reset we can stop searching
                    if (color.isColor() || color.equals(RESET)) {
                        break;
                    }
                }
            }
        }

        return result.toString();
    }
}
