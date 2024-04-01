import java.util.*;


public class Parser {
    private Lexer lexer;
    private Lexer.Token currentToken;

    private Map<String, String> symbolTable = new HashMap<>(); // To store variable types
    private Map<String, Object> valueTable = new HashMap<>(); // To store variable values


    public Parser(Lexer lexer, Map<String, String> symbolTable, Map<String, Object> valueTable) {
        this.lexer = lexer;
        this.currentToken = lexer.getNextToken();
        this.symbolTable = symbolTable;
        this.valueTable = valueTable;
    }


    private void eat(Lexer.TokenType expectedType, String expectedValue) {
        if (currentToken.type == expectedType && currentToken.value.equals(expectedValue)) {
            currentToken = lexer.getNextToken();
        } else {
            throw new RuntimeException("Syntax error: Expected token " + expectedType + " with value '" + expectedValue +
                    "' but found " + currentToken.type + " with value '" + currentToken.value + "'");
        }
    }

    private void parseVariableDeclaration() {
        String varType = currentToken.value;
        eat(Lexer.TokenType.KEYWORD, varType);

        if (currentToken.type == Lexer.TokenType.EOF) {
            return;
        }

        while (currentToken.type == Lexer.TokenType.IDENTIFIER || currentToken.value.equals("=")) {
            if (currentToken.type == Lexer.TokenType.IDENTIFIER) {
                String varName = currentToken.value;
                symbolTable.put(varName, varType);
                eat(Lexer.TokenType.IDENTIFIER, varName);
                if (currentToken.value.equals("=")) {
                    eat(Lexer.TokenType.SYMBOL, "=");
                    if (currentToken.type == Lexer.TokenType.LITERAL) {
                        String value = new String(currentToken.value);
                        eat(Lexer.TokenType.LITERAL, value);
                        if (value.startsWith("'") && value.endsWith("'")) {
                            if (varType.equals("INT") || varType.equals("FLOAT")) {
                                throw new RuntimeException("Semantic error: " + varType + " variable " + varName + " cannot hold a character literal");
                            }
                            value = value.substring(1, value.length() - 1); // remove the enclosing single quotes
                        } else {
                            if (varType.equals("CHAR")) {
                                throw new RuntimeException("Semantic error: CHAR variable " + varName + " must be assigned a character literal");
                            }
                        }
                        if (varType.equals("INT")) {
                            if (!value.matches("[+-]?\\d+")) {
                                throw new RuntimeException("Semantic error: INT variable " + varName + " can only hold an integer number");
                            }
                            valueTable.put(varName, Integer.parseInt(value));
                        } else if(varType.equals("FLOAT")) {
                            if (!value.matches("[+-]?([0-9]*[.])?[0-9]+")) {
                                throw new RuntimeException("Semantic error: FLOAT variable " + varName + " can only hold a floating-point number");
                            }
                            valueTable.put(varName, Float.parseFloat(value));
                        } else if(varType.equals("CHAR")){
                            if (value.length() != 1) {
                                throw new RuntimeException("Semantic error: CHAR variable " + varName + " can only hold a single character");
                            }
                            valueTable.put(varName, value);
                        } else if(varType.equals("BOOL")){
                            if (!value.equals("\"TRUE\"") && !value.equals("\"FALSE\"")) {
                                throw new RuntimeException("Semantic error: BOOL variable " + varName + " can only hold \"TRUE\" or \"FALSE\"");
                            }
                            valueTable.put(varName, Boolean.parseBoolean(value.substring(1, value.length() - 1).toLowerCase()));
                        }
                    } else if (currentToken.type == Lexer.TokenType.IDENTIFIER) {
                        String valueVarName = currentToken.value;
                        if (!symbolTable.containsKey(valueVarName) || !valueTable.containsKey(valueVarName)) {
                            throw new RuntimeException("Semantic error: Variable " + valueVarName + " has not been initialized properly");
                        }
                        if (!symbolTable.get(valueVarName).equals(varType)) {
                            throw new RuntimeException("Semantic error: Variable " + valueVarName + " is not of type " + varType);
                        }
                        valueTable.put(varName, valueTable.get(valueVarName));
                        eat(Lexer.TokenType.IDENTIFIER, valueVarName);
                    }
                    if (currentToken.value.equals(",")) {
                        eat(Lexer.TokenType.SYMBOL, ",");
                    }
                } else if (currentToken.value.equals("=")) {
                    throw new RuntimeException("Syntax error: Unexpected token '='");
                }

                // Check for end of input or next keyword
                if (currentToken.type == Lexer.TokenType.EOF || (currentToken.type == Lexer.TokenType.KEYWORD && currentToken.value.equals("DISPLAY"))) {
                    break;
                }
            }
        }
        System.out.println("symbolTable: " + symbolTable);
        System.out.println("valueTable: " + valueTable);
    }

    // Method to get default initial value based on variable type
    private String getDefaultInitialValue(String varType) {
        switch (varType) {
            case "INT":
            case "BOOL":
                return "0"; // Default value for INT and BOOL
            case "CHAR":
                return ""; // Default value for CHAR
            case "FLOAT":
                return "0.0"; // Default value for FLOAT
            default:
                return ""; // Default value for other types (should be handled based on your language's specification)
        }
    }

    private void parseDisplayStatement() {
        eat(Lexer.TokenType.KEYWORD, "DISPLAY");
        eat(Lexer.TokenType.SYMBOL, ":");

        while (currentToken.type != Lexer.TokenType.KEYWORD || !currentToken.value.equals("END CODE")) {
            if (currentToken.type == Lexer.TokenType.EOF) {
                throw new RuntimeException("Syntax error: Unexpected end of input while parsing display statement");
            }

            // Display token value
            if (currentToken.type == Lexer.TokenType.IDENTIFIER) {
                String varName = currentToken.value;
                if (!valueTable.containsKey(varName)) {
                    throw new RuntimeException("Semantic error: Variable " + varName + " is not declared");
                }
                Object value = valueTable.get(varName);
                if (value instanceof Boolean) {
                    System.out.print(((Boolean) value ? "TRUE" : "FALSE"));
                } else {
                    System.out.print(value);
                }
                currentToken = lexer.getNextToken();
            } else if (currentToken.type == Lexer.TokenType.LITERAL) {
                System.out.print(currentToken.value);
                currentToken = lexer.getNextToken();
            } else if (currentToken.type == Lexer.TokenType.SYMBOL && currentToken.value.equals("&")) {
                System.out.print(" ");
                currentToken = lexer.getNextToken(); // Move to the next token
            } else {
                throw new RuntimeException("Syntax error: Unexpected token " + currentToken.value);
            }
        }
    }

    public void parseCodeBlock() {
        eat(Lexer.TokenType.KEYWORD, "BEGIN CODE"); // Consume BEGIN CODE

        // Parse variable declarations
        while (currentToken.type == Lexer.TokenType.KEYWORD &&
                (currentToken.value.equals("INT") ||
                        currentToken.value.equals("CHAR") ||
                        currentToken.value.equals("BOOL") ||
                        currentToken.value.equals("FLOAT"))) {
            parseVariableDeclaration();
        }

        if (currentToken.type == Lexer.TokenType.EOF) {
            throw new RuntimeException("Syntax error: Unexpected end of file");
        }

        // Check for display statement
        boolean displayFound = false;
        while (currentToken.type != Lexer.TokenType.EOF) {
            if (currentToken.type == Lexer.TokenType.KEYWORD && currentToken.value.equals("DISPLAY")) {
                parseDisplayStatement();
                displayFound = true;
                break;
            }
            currentToken = lexer.getNextToken();
        }

        // Throw error if 'DISPLAY' not found
        if (!displayFound) {
            throw new RuntimeException("Syntax error: Expected token KEYWORD with value 'DISPLAY' but found EOF with value 'END OF FILE'");
        }

        // Check for END CODE or EOF
        if (currentToken.type == Lexer.TokenType.KEYWORD && currentToken.value.equals("END CODE")) {
            eat(Lexer.TokenType.KEYWORD, "END CODE"); // Consume END CODE
        } else if (currentToken.type == Lexer.TokenType.EOF) {
            throw new RuntimeException("Syntax error: Expected token KEYWORD with value 'END CODE' but found EOF with value 'END OF FILE'");
        }
    }
}