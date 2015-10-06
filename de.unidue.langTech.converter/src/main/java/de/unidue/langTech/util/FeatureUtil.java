package de.unidue.langTech.util;

import java.lang.Character.UnicodeBlock;
import java.util.HashSet;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class FeatureUtil
{
    public static boolean isTrash(String word)
    {
        boolean wwwPrefix = word.startsWith("www.");
        boolean httpPrefix = word.startsWith("http");
        boolean atPrefix = word.startsWith("@");
        boolean gatter = word.startsWith("#");

        boolean punctuation = word.matches("[!\\?\\.,:;]+");

        // special compositions
        boolean isMoneyValue = word.matches("\\.\\$[0-9]");

        // assumes that before the at-char letters or digits occur that helps us
        // to ignore all the
        // cases with name.lastname and name_lastname etc.
        boolean isEmail = word.matches("[0-9A-Za-z]+@[0-9A-Za-z]+\\.[a-z]+");
        return wwwPrefix || httpPrefix || atPrefix || gatter || isNumber(word) || punctuation
                || isMoneyValue || isEmail;
    }

    public static boolean isNumber(String word)
    {
        // 7.30 3:00 323 40% 1-3 1/2 3+4 3*4
        return word.matches("[0-9:\\.%-/\\+\\*]+");
    }

    public static boolean isLatin(char c)
    {
        return Character.UnicodeBlock.of(c) == UnicodeBlock.BASIC_LATIN;
    }

    public static boolean isCapital(char c)
    {
        return c >= 'A' && c <= 'Z';
    }

    public static boolean isLowerCase(char c)
    {
        return c >= 'a' && c <= 'z';
    }

    public static boolean isNumber(char c)
    {
        return c >= '0' && c <= '9';
    }

    public static boolean isNoNumberAndNoCharacter(char c)
    {
        return !isLowerCase(c) && !isNumber(c) && !isCapital(c);
    }

    public static boolean isCharacter(char c)
    {
        return isLowerCase(c) || isCapital(c);
    }

    public static String makeBagOfChars(String string)
    {
        StringBuilder bagOfChar = new StringBuilder();
        Set<String> set = new HashSet<String>();
        for (char c : string.toCharArray()) {
            if (set.contains(c + "")) {
                continue;
            }
            set.add(c + "");
            bagOfChar.append(c);
        }
        return bagOfChar.toString();
    }

    public static Feature wrapAsFeature(String featureName, boolean b)
    {
        if (b){
            return new Feature(featureName, 1);
        }
        
        return new Feature(featureName, 0);
    }

    public static Feature wrapAsFeature(String featureName, String text)
    {
        return new Feature(featureName, text);
    }

    public static boolean isSmiley(String aToken)
    {
        boolean isHorizontalSmiley = aToken.matches("[:8x]{0,1}[)(-PpD\\]\\|]+");
        boolean isVerticalSmiley = aToken
                .matches("[~\\^<>@\\$oOTmx_'(]+[\\s\\._]*[~\\^<>@\\$OoTmx_')]+");
        return isHorizontalSmiley || isVerticalSmiley;
    }

    public static boolean containsNumber(String aToken)
    {
        for (char c : aToken.toCharArray()) {
            if (isNumber(c)) {
                return true;
            }
        }

        return false;
    }

    public static boolean containsSymbol(String aToken, char symbol)
    {
        for (char c : aToken.toCharArray()) {
            if (c == symbol) {
                return true;
            }
        }

        return false;
    }
}
