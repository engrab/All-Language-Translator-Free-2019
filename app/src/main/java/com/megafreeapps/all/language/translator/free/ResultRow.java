package com.megafreeapps.all.language.translator.free;

public class ResultRow {
    public String from;
    String fromCode;
    String fromText;
    public int id;
    String nativeText;
    public String to;
    String toCode;
    String toText;

    ResultRow(int id, String from, String to, String fromText, String toText, String toCode, String fromCode, String nativeText) {
        this.id = id;
        this.from = from;
        this.fromText = fromText;
        this.to = to;
        this.toText = toText;
        this.toCode = toCode;
        this.fromCode = fromCode;
        this.nativeText = nativeText;
    }
}
