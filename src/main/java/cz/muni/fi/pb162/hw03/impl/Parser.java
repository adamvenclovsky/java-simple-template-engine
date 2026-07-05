package cz.muni.fi.pb162.hw03.impl;

import cz.muni.fi.pb162.hw03.impl.parser.tokens.Token;
import cz.muni.fi.pb162.hw03.template.TemplateException;
import cz.muni.fi.pb162.hw03.template.model.TemplateModel;

import java.util.ArrayList;

public class Parser {

    private static SwitchReturnType forSwitch(ArrayList<Token> tokens, TemplateModel model, int index){
        StringBuilder out = new StringBuilder();
        ReturnType type;
        boolean stopper = true;
        switch (tokens.get(index).getKind()) {
            case NAME -> {
                out.append(model.getAsString(tokens.get(index).name()));
                index++;
            }
            case CMD -> {
                if (tokens.get(index).cmd().equals("if")) {
                    type = evalIf(tokens, model, index);
                    index = type.i();
                    out.append(type.str());

                } else if (tokens.get(index).cmd().equals("done")) {
                    stopper = false;
                } else if (tokens.get(index).cmd().equals("for")) {
                    type = evalFor(tokens, model, index);
                    index = type.i();
                    out.append(type.str());

                }
            }
            default -> throw new TemplateException("invalid command in brackets");
        }
        return new SwitchReturnType(stopper,out.toString(),index);
    }
    private static ReturnType evalFor(ArrayList<Token> tokens, TemplateModel model, int index)
            throws TemplateException {
        SwitchReturnType returntype;
        StringBuilder out = new StringBuilder();
        index++;
        String name = tokens.get(index).name();
        index = SyntaxParser.getIndex(tokens, index);
        String iterable = tokens.get(index).name();
        index++;
        index++;
        int checkpoint = index;

        String startName;
        try {
            startName = model.getAsString(name);
        } catch (Exception e) {
            startName = "";
        }
        for (Object curName :
                model.getAsIterable(iterable)) {
            String realName = (String) curName;
            model.put(name, realName);
            boolean stopper = true;
            index = checkpoint;
            while (stopper) {
                if (tokens.get(index).getKind() == Token.Kind.TEXT) {
                    out.append(tokens.get(index).text());
                    index++;
                }
                if (tokens.get(index).getKind() == Token.Kind.NAME) {
                    out.append(model.getAsString(tokens.get(index).name()));
                    index++;
                }
                if (tokens.get(index).getKind() == Token.Kind.OPEN) {
                    index++;
                    returntype = forSwitch(tokens,model,index);
                    stopper = returntype.stopper();
                    out.append(returntype.out());
                    index = returntype.index();
                    index++;
                } else if (tokens.get(index).getKind() == Token.Kind.CLOSE) {
                    index++;
                }
            }
        }
        if (startName.equals("")) {
            model.put(name, null); //maybe wrong
        } else {
            model.put(name, startName);
        }
        return new ReturnType(out.toString(), index);
    }

    private static ReturnType evalIf(ArrayList<Token> tokens, TemplateModel model, int index)
            throws TemplateException {
        StringBuilder out = new StringBuilder();
        index++;
        boolean flag = model.getAsBoolean(tokens.get(index).name());
        index+=2;
        boolean stopper = true;
        ReturnType type;
        while (stopper) {
            if (flag && (tokens.get(index).getKind() == Token.Kind.TEXT)) {
                out.append(tokens.get(index).text());
                index++;
            }
            if (flag && (tokens.get(index).getKind() == Token.Kind.NAME)) {
                out.append(model.getAsString(tokens.get(index).name()));
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
                            type = evalIf(tokens, model, index);
                            index = type.i();
                            if (flag) {
                                out.append(type.str());
                            }
                        } else if (tokens.get(index).cmd().equals("else")) {
                            flag = !flag;
                        } else if (tokens.get(index).cmd().equals("done")) {
                            stopper = false;
                        } else if (tokens.get(index).cmd().equals("for")) {
                            type = evalFor(tokens, model, index);
                            index = type.i();
                            if (flag) {
                                out.append(type.str());
                            }
                        }
                    }
                    default -> throw new TemplateException("invalid command in brackets");
                }
                index++;
            } else {
                index++;
            }
        }
        return new ReturnType(out.toString(), index);
    }

    /**
     * method that takes all tokens and model and creates output
     * @param listOfTokens
     * @param model
     * @return output
     */
    public static String parse(ArrayList<Token> listOfTokens, TemplateModel model) {
        StringBuilder out = new StringBuilder();
        ReturnType type;
        for (int i = 0; i < listOfTokens.size(); i++) {
            switch (listOfTokens.get(i).getKind()) {
                case TEXT:
                    out.append(listOfTokens.get(i).text());
                    break;
                case NAME:
                    out.append(model.getAsString(listOfTokens.get(i).name()));
                    break;
                case CMD:
                    if (listOfTokens.get(i).cmd().equals("if")) {
                        type = evalIf(listOfTokens, model, i);
                        out.append(type.str());
                        i = type.i();
                    } else if (listOfTokens.get(i).cmd().equals("for")) {
                        type = evalFor(listOfTokens, model, i);
                        out.append(type.str());
                        i = type.i();
                    }
                default:break;
            }
        }

        return out.toString();
    }
}
