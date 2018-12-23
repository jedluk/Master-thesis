package algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {

    public static List<String> filterPrices(String[] prices) {

        List<String> output = new ArrayList<>();
        // prices can contains gibberish
        String pattern = "^\\d{1,2}([,. ])?\\d{1,2}$";
        Pattern r = Pattern.compile(pattern);
        Matcher m;

        for (String price : prices) {

            price = price.replace(" ","");
            m = r.matcher(price);
            if (m.find()) {

                String found = m.group();
                found = found.replace(",", ".");
                if (found.contains(".")) {

                    switch (found.length()) {
                        case 3:
                            // found format 1.1 -> add 0 to the end
                            output.add(found.concat("0"));
                            break;
                        case 4:
                            // found format 1.11 or 11.1
                            if (found.substring(1, 2).equals(".")) {
                                output.add(found);
                            } else {
                                found = found.replace(".", "");
                                output.add(found.substring(0, 1) + "." + found.substring(1));
                            }
                            break;
                        case 5:
                            // found format 11.11 -> trim from 2nd letter
                            output.add(found.substring(1));
                            break;
                        default:
                            break;
                    }
                } else {
                    switch (found.length()) {
                        case 3:
                            output.add(found.substring(0, 1) + "." + found.substring(1));
                            break;
                        case 4:
                            output.add(found.substring(1, 2) + "." + found.substring(2));
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        return output;
    }
}
