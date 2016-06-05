package by.bsuir.csan.helpers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpHelper {
    public static ArrayList<String> getMatches (String line, String regExp) {
        Matcher matches = Pattern.compile(regExp).matcher(line);
        ArrayList<String> matchesList  = new ArrayList<>();
        while (matches.find()) {
            matchesList.add(matches.group());
        }
        return matchesList;
    }
}
