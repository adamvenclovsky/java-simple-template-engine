package cz.muni.fi.pb162.hw03.impl;

import cz.muni.fi.pb162.hw03.impl.parser.tokens.Token;
import cz.muni.fi.pb162.hw03.impl.parser.tokens.Tokenizer;
import cz.muni.fi.pb162.hw03.template.FSTemplateEngine;
import cz.muni.fi.pb162.hw03.template.TemplateException;
import cz.muni.fi.pb162.hw03.template.model.TemplateModel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class FSTemplateEngineImpl implements FSTemplateEngine {
    /**
     * constructor
     */
    public FSTemplateEngineImpl() {
        this.tModel = new HashMap<>();
    }

    private final HashMap<String, ArrayList<Token>> tModel;

    @Override
    public void loadTemplate(Path file, Charset cs, String ext) {
        String fileName = file.getFileName().toString();
        StringBuilder out = new StringBuilder();
        String[] split = fileName.split("\\.");
        for (int i = 0; i < split.length - 1; i++) {
            out.append(split[i]);
            if (i < split.length - 2) {
                out.append(".");
            }
        }
        try {
            loadTemplate(out.toString(), Files.readString(file, cs));
        } catch (IOException e) {
            throw new TemplateException("failed reading template from file");
        }

    }

    @Override
    public void loadTemplateDir(Path inDir, Charset cs, String ext) {
        for (File file :
                Objects.requireNonNull(inDir.toFile().listFiles())) {
            if (!file.isDirectory()) {
                loadTemplate(file.toPath(), cs, ext);
            }
        }
    }

    @Override
    public void writeTemplate(String name, TemplateModel model, Path file, Charset cs) {
        String out = evaluateTemplate(name, model);
        PrintWriter writer;
        try {
            writer = new PrintWriter(file.toFile(), cs);
            writer.print(out);
            writer.close();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void writeTemplates(TemplateModel model, Path outDir, Charset cs) {
        for (String name :
                tModel.keySet()) {
            writeTemplate(name, model, outDir.resolve(name), cs);
        }
    }

    @Override
    public void loadTemplate(String name, String text) {
        try {
            tModel.put(name, SyntaxParser.syntaxParse(new Tokenizer(text)));
        } catch (IndexOutOfBoundsException e) {
            throw new TemplateException("wrong syntax");
        }

    }

    @Override
    public Collection<String> getTemplateNames() {
        return tModel.keySet();
    }

    @Override
    public String evaluateTemplate(String name, TemplateModel model) {
        try {
            return Parser.parse(tModel.get(name), model);
        } catch (Exception e) {
            throw new TemplateException("invalid template");
        }

    }
}
