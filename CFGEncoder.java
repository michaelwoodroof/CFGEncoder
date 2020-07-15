// Michael Woodroof

// Imports
import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CFGEncoder {

    public String[] variables;
    public String[] terminals;
    public HashMap<String, ArrayList<String>> rules = new HashMap<String, ArrayList<String>>();
    public String startVariable;

    public static void main(String[] args) {
        // Read File from Argument 0
        if (args.length == 1) {
            // Readfile
            CFGEncoder cfgEncode = new CFGEncoder();
            cfgEncode.readFile(args[0]);

            // Perform Chomsky
            cfgEncode.convertToChomsky();

            ArrayList<String> fRules = cfgEncode.convertRules(cfgEncode.rules);

            // Output
            cfgEncode.output(fRules);

            Scanner sc = new Scanner(System.in);
            System.out.println("Enter a word for the Encoding > ");
            String word = sc.nextLine();


            // Induce Read Loop
            System.out.println();
            System.out.println("Derivation Table : ");
            System.out.println(word);

            String[][] cykTable = cfgEncode.setUpTable(word.length());

            cfgEncode.drawTable(calculateTable(cykTable, word, cfgEncode.rules), word);

            Boolean isValid = cfgEncode.check(cykTable, cfgEncode.startVariable);
            System.out.println();
            if (isValid) {
                System.out.println("Word : " + word + " is valid");
            } else {
                System.out.println("Word : " + word + " is invalid");
            }

        } else {
            System.out.println("Invalid number of arguments : Expected input is : <filepath of encoding document>");
        }

    }

    public static String[][] calculateTable(String[][] table, String word, HashMap<String, ArrayList<String>> rules) {
        String[][] output = table;

        // Set Up First Row
        // Go through each Character
        int counter = 0;
        for (char c : word.toCharArray()) {
            ArrayList<String> possibles = new ArrayList<String>();
            // Iterate HashMap
            HashMap<String, ArrayList<String>> traverseRules = CFGEncoder.deepCopy(rules);
            for (Map.Entry<String, ArrayList<String>> entry : traverseRules.entrySet()) {
                ArrayList<String> values = entry.getValue();
                if (values.contains(Character.toString(c)) && !(possibles.contains(Character.toString(c)))) {
                    possibles.add(entry.getKey());
                }
            }
            // Convert to String
            String assemble = "";
            for (int x = 0; x < possibles.size(); x++) {
                if (x == possibles.size() - 1) {
                    assemble += possibles.get(x);
                } else {
                    assemble += possibles.get(x) + ",";
                }
            }

            output[0][counter] = assemble;
            counter++;
        }

        for (int row = 1; row < table.length; row++) {
            for (int col = 0;col < table[row].length; col++) {
                table[row][col] = validCombos(variableCombos(table, createLetterCombos(row, col, word), word), rules);
            }
        }

        return output;
    }

    public static ArrayList<String> createLetterCombos(int row, int col, String word) {
        ArrayList<String> words = new ArrayList<String>();
        String wordSection = word.substring(col,col + row + 1);

        // Create Combos
        int counter = wordSection.length();
        for (int k = 1; k < wordSection.length(); k++) {
            words.add(wordSection.substring(0, k));
            words.add(wordSection.substring(k, wordSection.length()));
            counter--;
        }

        return words;
    }

    public static String validCombos(ArrayList<String> variableCombinations, HashMap<String, ArrayList<String>> rules) {
        ArrayList<String> valid = new ArrayList<String>();


        for(int i = 0; i < variableCombinations.size(); i ++){
            for(Map.Entry<String, ArrayList<String>> entry : rules.entrySet()) {
                ArrayList<String> values = entry.getValue();
                if(values.contains(variableCombinations.get(i))){
                    valid.add(entry.getKey());
                }
            }
        }

        String output = "";
        String delim = "";
        for (int j = 0; j < valid.size(); j++) {
            if (j == valid.size() - 1) {
                delim = "";
            } else {
                delim = ",";
            }
            output += valid.get(j) + delim;
        }

        return output;
    }

    public static ArrayList<String> variableCombos(String[][] table, ArrayList<String> wordsCombinations, String word) {
        ArrayList<String> finalCombo = new ArrayList<String>();


        for (int x = 0; x < wordsCombinations.size(); x+=2) {

            String elementOne = wordsCombinations.get(x);
            String elementTwo = wordsCombinations.get(x + 1);

            int rowIndexOne = elementOne.length() - 1;
            int rowIndexTwo = elementTwo.length() - 1;
            int colIndexOne = word.indexOf(elementOne);
            int colIndexTwo = word.indexOf(elementTwo);

            String[] comboOne = table[rowIndexOne][colIndexOne].split(",");
            String[] comboTwo = table[rowIndexTwo][colIndexTwo].split(",");

            for (int j = 0; j < comboOne.length; j++) {
                for (int i = 0; i < comboTwo.length; i++) {
                    finalCombo.add("" + comboOne[j] + comboTwo[i]);
                }
            }

        }

        return finalCombo;
    }

    public Boolean check(String[][] table, String startVariable) {
        String value = (table[table.length - 1][0]);
        if (value.indexOf(startVariable) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public String[][] setUpTable(int height) {

        String[][] output = new String[height][];

        int counter = 0;
        for (int i = height; i > 0; i--) {
            output[counter] = new String[i];
            counter++;
        }

        return output;
    }

    public void drawTable(String[][] table, String word) {
        int max = getMaxValue(table);
        for (int row = 0; row < table.length; row++) {
            String[] rowValues = table[row];
            for (int item = 0; item < rowValues.length; item++) {
                // Format
                String value = rowValues[item];
                int totalSpaces = max - value.length();

                // Calculate Spaces
                int firstHalf = (int) totalSpaces / 2;
                int secondHalf = totalSpaces - firstHalf;
                System.out.print("[ ");

                for (int fh = 0; fh < firstHalf; fh++) {
                    System.out.print(" ");
                }

                System.out.print(value);

                for (int sh = 0; sh < secondHalf; sh++) {
                    System.out.print(" ");
                }

                System.out.print(" ]");
            }
            System.out.println("");
        }
    }

    public int getMaxValue(String[][] table) {
        int max = 0;
        for (int i = 0; i < table.length; i++) {
            for (int j=0; j < table[i].length; j++) {
                if (table[i][j].length() > max) {
                    max = table[i][j].length();
                }
            }
        }
        return max;
    }

    public void convertToChomsky() {
        // Add a new Start Variable

        // Assign new Start Variable - Step 1
        // SV is set to S0
        rules.put("S0", new ArrayList<String>(){{
            add(startVariable);
        }});

        startVariable = "S0";

        // Remove Empty States
        removeEmptyStrings(variables, rules);

        // Unit Removal
        unitRemoval(rules);

        // Clean Up Stage
        // Usuable Character Set
        ArrayList<String> charSet = genCharSet(variables);
        cleanUp(charSet, rules);

    }

    // IF Rules refers to Variable that doesn't exist it doesn't remove
    public void removeEmptyStrings(String[] oldVariables, HashMap<String, ArrayList<String>> oldRules) {
        ArrayList<String> removeList = new ArrayList<String>();

        HashMap<String, ArrayList<String>> newRules = CFGEncoder.deepCopy(oldRules);

        // Find Non-Terminal Rules and Add them to Arraylist
        for (Map.Entry<String, ArrayList<String>> entry : newRules.entrySet()) {
            ArrayList<String> values = entry.getValue();
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i).equals("e")) {
                    removeList.add(entry.getKey());
                    rules.get(entry.getKey()).remove(values.get(i));
                }
            }
            // Remove Key if it has no values
            if (rules.get(entry.getKey()).size() == 0) {
                if (!(valueContains(entry.getKey(), rules))) {
                    // Iterate through Variables
                    ArrayList<String> convert = new ArrayList<String>(Arrays.asList(variables));
                    convert.remove(entry.getKey());
                    variables = convert.toArray(new String[convert.size()]);
                    // Remove from Rules
                    rules.remove(entry.getKey());
                }
            }
        }

        HashMap<String, ArrayList<String>> modRules = CFGEncoder.deepCopy(oldRules);
        for (Map.Entry<String, ArrayList<String>> entry : modRules.entrySet()) {
            ArrayList<String> values = entry.getValue();
            for (int j = 0; j < values.size(); j++) {
                if (values.get(j).length() >= 2 && !(values.get(j).equals("e"))) {
                    // Go Through Remove List
                    for (int k = 0; k < removeList.size(); k++) {
                        if (values.get(j).contains(removeList.get(k))) {
                            values.add(values.get(j).replace(removeList.get(k), ""));
                            modRules.put(entry.getKey(), values);
                        }
                    }
                }
            }
        }

        // Adds Empty String
        ArrayList<String> s0 = modRules.get("S0");
        s0.add("e");
        modRules.put("S0", s0);

        rules = modRules;

    }

    public static Boolean valueContains(String searchCriteria, HashMap<String, ArrayList<String>> targetRules) {
        for (Map.Entry<String, ArrayList<String>> entry : targetRules.entrySet()) {
            if (!(entry.getKey().equals(searchCriteria))) {
                ArrayList<String> values = entry.getValue();
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i).indexOf(searchCriteria) >= 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void unitRemoval(HashMap<String, ArrayList<String>> oldRules) {
        HashMap<String, ArrayList<String>> newRules = CFGEncoder.deepCopy(oldRules);
        ArrayList<String> tempVariables = new ArrayList<String>(Arrays.asList(variables));

        for (Map.Entry<String, ArrayList<String>> entry : newRules.entrySet()) {
            ArrayList<String> values = entry.getValue();
            // Check each Rule
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i).length() == 1 && tempVariables.contains(values.get(i))) {
                    // Prevents Duplication
                    values.removeAll(newRules.get(values.get(i)));
                    values.addAll(newRules.get(values.get(i)));

                    values.remove(values.get(i));

                    newRules.put(entry.getKey(), values);

                }
            }
        }

        rules = newRules;
    }

    public void cleanUp(ArrayList<String> charSet, HashMap<String, ArrayList<String>> oldRules) {
        // Variable Declaration
        HashMap<String, ArrayList<String>> newRules = CFGEncoder.deepCopy(oldRules);
        ArrayList<String> tempTerminals = new ArrayList<String>(Arrays.asList(terminals));
        ArrayList<String> tempVariables = new ArrayList<String>(Arrays.asList(variables));

        // Remove Terminals
        for (Map.Entry<String, ArrayList<String>> entry : newRules.entrySet()) {
            ArrayList<String> values = entry.getValue();
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i).length() >= 2) {
                    for (char c : values.get(i).toCharArray()) {
                        if (tempTerminals.contains(Character.toString(c))) {
                            // Check Rule Doesn't Exist Already
                            String key = "";
                            HashMap<String, ArrayList<String>> findKeys = CFGEncoder.deepCopy(newRules);
                            for (Map.Entry<String, ArrayList<String>> search : findKeys.entrySet()) {
                                ArrayList<String> sValues = search.getValue();
                                for (int j = 0; j < sValues.size(); j++) {
                                    if (sValues.get(j).length() == 1 && sValues.get(j).equals(Character.toString(c))) {
                                        key = search.getKey();
                                    }
                                }
                            }
                            if (key.length() == 0) {
                                // Key Doesn't Exist ->
                                findKeys.put(charSet.get(0), new ArrayList<String>(){{
                                    add(Character.toString(c));
                                }});

                                // Logic to Replace Terminal with Variable
                                values.set(i, values.get(i).replace(Character.toString(c), charSet.get(0)));
                                findKeys.put(entry.getKey(), values);

                                charSet.remove(charSet.get(0));
                            } else {
                                // Key Exists ->
                                values.set(i, values.get(i).replace(Character.toString(c), key));
                                findKeys.put(entry.getKey(), values);

                            }
                            newRules = CFGEncoder.deepCopy(findKeys);
                        }
                    }
                }
            }
        }

        rules = newRules;

        // Breakup Complex Rules
        Boolean isComplex = true;

        while (isComplex) {
            isComplex = false;
            HashMap<String, ArrayList<String>> complexRules = CFGEncoder.deepCopy(rules);
            for (Map.Entry<String, ArrayList<String>> entry : complexRules.entrySet()) {
                ArrayList<String> values = entry.getValue();
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i).length() >= 3) {
                        isComplex = true;
                        String latter = values.get(i).substring(1, values.get(i).length());
                        // Check if a Key Points to Latter
                        String key = "";
                        HashMap<String, ArrayList<String>> findKeys = CFGEncoder.deepCopy(complexRules);
                        for (Map.Entry<String, ArrayList<String>> search : findKeys.entrySet()) {
                            ArrayList<String> sValues = search.getValue();
                            for (int j = 0; j < sValues.size(); j++) {
                                if (sValues.get(j).length() == 1 && sValues.get(j).equals(latter)) {
                                    key = search.getKey();
                                }
                            }
                        }

                        if (key.length() == 0) {
                            // Key Doesn't Exist create ->
                            findKeys.put(charSet.get(0), new ArrayList<String>(){{
                                add(latter);
                            }});

                            // Logic to Replace Terminal with Variable
                            values.set(i, values.get(i).replace(latter, charSet.get(0)));
                            findKeys.put(entry.getKey(), values);

                            charSet.remove(charSet.get(0));

                        } else {
                            // Key Exist Assign ->
                            // Key Exists ->
                            values.set(i, values.get(i).replace(latter, key));
                            findKeys.put(entry.getKey(), values);

                        }
                        complexRules = CFGEncoder.deepCopy(findKeys);

                    }
                }
            }
            rules = CFGEncoder.deepCopy(complexRules);
        }

    }

    public static HashMap<String, ArrayList<String>> deepCopy(HashMap<String, ArrayList<String>> original) {
        HashMap<String, ArrayList<String>> copy = new HashMap<String, ArrayList<String>>();
        for (Map.Entry<String, ArrayList<String>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
        }
        return copy;
    }

    public void readFile(String filepath) {

        // Read File and Assignment properties

        try {

            // Variable Declaration
            FileReader fr = new FileReader(filepath);
            BufferedReader br = new BufferedReader(fr);
            int readMode = -1;
            String line;

            // Loop Through textfile
            while ((line = br.readLine()) != null) {
                // Set Readmode
                if (line.equals("[VARIABLES]") || line.equals("[TERMINALS]") || line.equals("[RULES]") || line.equals("[START VARIABLE]")) {
                    if (line.equals("[VARIABLES]")) {
                        readMode = 0;
                    } else if (line.equals("[TERMINALS]")) {
                        readMode = 1;
                    } else if (line.equals("[RULES]")) {
                        readMode = 2;
                    } else if (line.equals("[START VARIABLE]")) {
                        readMode = 3;
                    }
                } else {
                    if (readMode > -1) {
                        // Read Depending on Switch Case
                        switch (readMode) {
                            case 0:
                                variables = line.split(",");
                                break;
                            case 1:
                                terminals = line.split(",");
                                break;
                            case 2:
                                String key = line.substring(0, line.indexOf("-")).trim();
                                String strippedRules = line.substring(line.indexOf(">") + 1, line.length());
                                ArrayList<String> formattedRules = new ArrayList<String>(Arrays.asList(strippedRules.split(Pattern.quote("|"))));

                                formattedRules = trimArr(formattedRules);
                                rules.put(key, formattedRules);

                                break;
                            case 3:
                                startVariable = line.trim();
                                break;
                            default:
                                System.out.println("Error");
                                break;
                        }
                    } else {
                        // Invalid Encoder
                        System.out.println("Invalid encoding document");
                    }
                }
            }

            br.close();

        } catch (Exception e) {
            // Throw Error
            System.out.println("Cannot find or read given file");
        }
    }

    public static ArrayList<String> genCharSet(String[] variables) {
        // Generate all Characters
        ArrayList<String> charSet = new ArrayList<String>();
        char[] alpha = new char[26];
        for (int i = 0; i < 26; i++) {
            charSet.add(String.valueOf((char) (65 + i)));
        }

        // Remove Unneeded Characters
        for (int j = 0; j < variables.length; j++) {
            charSet.remove(variables[j]);
        }

        // Removed as Lowercase e is emtpy String so to prevent confusion
        charSet.remove("E");

        return charSet;
    }

    // For Formatting
    public ArrayList<String> convertRules(HashMap<String, ArrayList<String>> dictionary) {
        ArrayList<String> output = new ArrayList<String>();
        for (Map.Entry<String, ArrayList<String>> entry : dictionary.entrySet()) {
            if (entry.getValue().size() == 1) {
                output.add(entry.getKey() + " -> " + entry.getValue().get(0));
            } else {
                // Iterate
                for (int i = 0; i < entry.getValue().size(); i++) {
                    output.add(entry.getKey() + " -> " + entry.getValue().get(i));
                }

            }
        }
        return output;
    }

    public void output(ArrayList<String> input) {
        System.out.println("Rules : ");
        for (int i = 0; i < input.size(); i++) {
            System.out.println(input.get(i));
        }
    }

    public String toString() {
        String formatted = "Variables : " + readArr(variables, 0) + " Terminals : " + readArr(terminals, 0) + " Rules : " + formattedRules(rules) + " Start Variable : " + startVariable;
        return formatted;
    }

    public ArrayList<String> trimArr(ArrayList<String> arr) {
        for (int i = 0; i < arr.size(); i++) {
            arr.set(i, arr.get(i).trim());
        }
        return arr;
    }

    public String readArr(String[] arr, int mode) {
        String assembled = "";
        String delim = "";
        for (int i = 0; i < arr.length; i++) {
            if (i == arr.length - 1) {
                delim = "";
            } else {
                if (mode == 0) {
                    delim = ",";
                } else {
                    delim = "|";
                }
            }
            assembled += arr[i] + delim;
        }
        return assembled;
    }

    public String formattedRules(HashMap<String, ArrayList<String>> dictionary) {
        String assembled = "";
        for (Map.Entry<String, ArrayList<String>> entry : dictionary.entrySet()) {
            String[] fSet = entry.getValue().toArray(new String[entry.getValue().size()]);
            assembled += "Rule " + entry.getKey() + " - > " + readArr(fSet, 1) + " ";
        }
        return assembled;
    }

}
