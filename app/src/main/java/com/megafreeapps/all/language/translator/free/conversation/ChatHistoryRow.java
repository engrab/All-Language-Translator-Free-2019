package com.megafreeapps.all.language.translator.free.conversation;

public class ChatHistoryRow {
    public String from;
    String fromCode;
    String fromText;
    public int id;
    public String to;
    String toCode;
    String toText;

    ChatHistoryRow(int id, String from, String to, String fromText, String toText, String toCode, String fromCode) {
        this.id = id;
        this.from = from;
        this.fromText = fromText;
        this.to = to;
        this.toText = toText;
        this.toCode = toCode;
        this.fromCode = fromCode;
    }
}
