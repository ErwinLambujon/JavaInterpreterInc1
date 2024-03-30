public class Lexer {
    enum TokenType {
        KEYWORD, IDENTIFIER, LITERAL, SYMBOL, EOF
    }

    // Token class
    static class Token {
        TokenType type;
        String value;

        Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    private String code;
    private int currentPosition;

    public Lexer(String code) {
        this.code = code;
        this.currentPosition = 0;
        skipWhitespace();
    }

    private void skipWhitespace() {
        while (currentPosition < code.length() && Character.isWhitespace(code.charAt(currentPosition))) {
            currentPosition++;
        }
    }

    public Token getNextToken() {
        skipWhitespace();

        if (currentPosition >= code.length()) {
            return new Token(TokenType.EOF, "END OF FILE");
        }

        String substring = code.substring(currentPosition);

        if (substring.startsWith("BEGIN CODE")) {
            currentPosition += 10;
            return new Token(TokenType.KEYWORD, "BEGIN CODE");
        } else if (substring.startsWith("END CODE")) {
            currentPosition += 8;
            return new Token(TokenType.KEYWORD, "END CODE");
        }

        char currentChar = code.charAt(currentPosition);

        if (Character.isDigit(currentChar)) {
            StringBuilder literalBuilder = new StringBuilder();
            while (currentPosition < code.length() && (Character.isDigit(currentChar) || currentChar == '.')) {
                literalBuilder.append(currentChar);
                currentPosition++;
                if (currentPosition < code.length()) {
                    currentChar = code.charAt(currentPosition);
                }
            }
            return new Token(TokenType.LITERAL, literalBuilder.toString());
        }


        if (Character.isLetter(currentChar) || currentChar == '_') {
            StringBuilder identifierBuilder = new StringBuilder();
            while (currentPosition < code.length() &&
                    (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
                identifierBuilder.append(currentChar);
                currentPosition++;
                if (currentPosition < code.length()) {
                    currentChar = code.charAt(currentPosition);
                }
            }
            String identifier = identifierBuilder.toString();
            if (identifier.equals("INT") || identifier.equals("CHAR") || identifier.equals("BOOL") || identifier.equals("FLOAT")
                    || identifier.equals("DISPLAY")) {
                return new Token(TokenType.KEYWORD, identifier);
            } else {
                return new Token(TokenType.IDENTIFIER, identifier);
            }
        } else if (currentChar == '"' || currentChar == '\'') {
            char quote = currentChar;
            StringBuilder literalBuilder = new StringBuilder();
            currentPosition++;
            while (currentPosition < code.length() && code.charAt(currentPosition) != quote) {
                literalBuilder.append(code.charAt(currentPosition));
                currentPosition++;
            }
            currentPosition++;
            return new Token(TokenType.LITERAL, literalBuilder.toString());
        } else if (currentChar == '#' || currentChar == '&' || currentChar == '$' || currentChar == ':' || currentChar == '=' || currentChar == ',') {
            currentPosition++;
            return new Token(TokenType.SYMBOL, String.valueOf(currentChar));
        } else {
            currentPosition++;
            return getNextToken();
        }

    }
}
