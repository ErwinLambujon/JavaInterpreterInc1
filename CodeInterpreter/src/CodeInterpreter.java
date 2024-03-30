import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CodeInterpreter {
    private static Map<String, String> symbolTable = new HashMap<>(); // To store variable types
    private static Map<String, Object> valueTable = new HashMap<>(); // To store variable values

    public static void interpretFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            StringBuilder codeBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                codeBuilder.append(line).append("\n");
            }
            interpret(codeBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void interpret(String code) {
        Lexer lexer = new Lexer(code);
        Parser parser = new Parser(lexer, symbolTable, valueTable);
        parser.parseCodeBlock();
    }

    public static void main(String[] args) {
        interpretFromFile("D:\\JAVA PRACTICE\\CodeInterpreter\\src\\code.txt");
    }
}
