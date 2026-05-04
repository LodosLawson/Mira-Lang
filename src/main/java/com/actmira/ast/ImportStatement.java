package com.actmira.ast;

public class ImportStatement implements Statement {
    private final String path;

    public ImportStatement(String path) {
        this.path = path;
    }

    public String getPath() { return path; }

    @Override public void statementNode() {}
    @Override public String tokenLiteral() { return "import"; }
    @Override public String string() { return toString(); }
    @Override public String toString() { return "import \"" + path + "\";"; }
}
