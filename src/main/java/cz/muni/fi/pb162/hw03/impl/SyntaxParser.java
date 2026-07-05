package cz.muni.fi.pb162.hw03.impl;

import cz.muni.fi.pb162.hw03.impl.parser.tokens.Token;
import cz.muni.fi.pb162.hw03.impl.parser.tokens.Tokenizer;
import cz.muni.fi.pb162.hw03.template.TemplateException;

import java.util.ArrayList;
import java.util.Objects;

public class SyntaxParser {
    private static ReturnType syntaxEvalFor(ArrayList<Token> tokens, int index) throws TemplateException {
        index++;
        if (!(tokens.get(index).getKind() == Token.Kind.NAME)) {
            throw new TemplateException("for expected");
        }
        index = getIndex(tokens, index);
        index++;
        if (!(tokens.get(index).getKind() == Token.Kind.CLOSE)) {
            throw new TemplateException("for not closed");
        }
        index++;
        ReturnType type;

        boolean stopper = true;
        while (stopper) {
            if (tokens.get(index).getKind() == Token.Kind.TEXT) {
                index++;
            }
            if (tokens.get(index).getKind() == Token.Kind.NAME) {
                index++;
            }
            if (tokens.get(index).getKind() == Token.Kind.OPEN) {
                index++;
                switch (tokens.get(index).getKind()) {
                    case NAME -> {
                        continue;
                    }
                    case CMD -> {
                        if (tokens.get(index).cmd().equals("if")) {
                            type = syntaxEvalIf(tokens, index);
                            index = type.i();

                        } else if (tokens.get(index).cmd().equals("done")) {
                            stopper = false;
                        } else if (tokens.get(index).cmd().equals("for")) {
                            type = syntaxEvalFor(tokens, index);
                            index = type.i();

                        } else {
                            throw new TemplateException("unknown command inside for");
                        }
                    }
                    default -> throw new TemplateException("invalid command in brackets");
                }
                index++;
            } else if (tokens.get(index).getKind() == Token.Kind.CLOSE) {
                index++;
            }
        }

        return new ReturnType("", index);
    }

    static int getIndex(ArrayList<Token> tokens, int index) {
        index++;
        if (!(tokens.get(index).getKind() == Token.Kind.IN)) {
            throw new TemplateException("in expected");
        }
        index++;
        if (!(tokens.get(index).getKind() == Token.Kind.NAME)) {
            throw new TemplateException("iterable expected");
        }
        return index;
    }

    private static ReturnType syntaxEvalIf(ArrayList<Token> tokens, int index) throws TemplateException {
        index++;
        if (!(tokens.get(index).getKind() == Token.Kind.NAME)) {
            throw new TemplateException("if expected");
        }
        index++;
        if (!(tokens.get(index).getKind() == Token.Kind.CLOSE)) {
            throw new TemplateException("if not closed");
        }
        boolean stopper = true;
        index++;
        ReturnType type;
        while (stopper) {
            if (tokens.get(index).getKind() == Token.Kind.TEXT) {
                index++;
            }
            if (tokens.get(index).getKind() == Token.Kind.NAME) {
                index++;
            }
            if (tokens.get(index).getKind() == Token.Kind.OPEN) {
                index++;
                switch (tokens.get(index).getKind()) {
                    case NAME -> {
                        continue;
                    }
                    case CMD -> {
                        if (tokens.get(index).cmd().equals("if")) {
                            type = syntaxEvalIf(tokens, index);
                            index = type.i();

                        } else if (tokens.get(index).cmd().equals("done")) {
                            stopper = false;
                        } else if (tokens.get(index).cmd().equals("for")) {
                            type = syntaxEvalFor(tokens, index);
                            index = type.i();

                        } else if (!tokens.get(index).cmd().equals("else")) {
                            throw new TemplateException("unknown command inside if");
                        }
                    }
                    default -> throw new TemplateException("invalid command in brackets");
                }
                index++;
            } else {
                index++;
            }
        }

        return new ReturnType("", index);
    }

    /**
     * method that checks syntax for the parser
     *
     * @param tokenizer
     * @return list of all tokens that are willing to be parsed
     */
    public static ArrayList<Token> syntaxParse(Tokenizer tokenizer) {
        Token token;
        ArrayList<Token> listOfTokens = new ArrayList<>();
        ReturnType type;
        while (!tokenizer.done()) {
            token = tokenizer.consume();
            listOfTokens.add(token);
        }
        for (int i = 0; i < listOfTokens.size(); i++) {
            if (Objects.requireNonNull(listOfTokens.get(i).getKind()) == Token.Kind.CMD) {
                if (listOfTokens.get(i).cmd().equals("if")) {
                    type = syntaxEvalIf(listOfTokens, i);
                    i = type.i();
                } else if (listOfTokens.get(i).cmd().equals("for")) {
                    type = syntaxEvalFor(listOfTokens, i);
                    i = type.i();
                }
            }
        }
        return listOfTokens;
    }
}


