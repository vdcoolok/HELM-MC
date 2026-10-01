package dev.helm.setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SettingSearch {

    public static final int EXACT = 0;
    public static final int PREFIX = 1;
    public static final int WORD = 2;
    public static final int CONTAINS = 3;
    public static final int NONE = 4;

    private SettingSearch() {
    }

    public record Hit(SettingCatalogue.Entry entry, int rank) {
    }

    public static List<SettingCatalogue.Entry> rank(List<SettingCatalogue.Entry> entries,
                                                   String query) {
        String wanted = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (wanted.isEmpty()) {
            return entries;
        }
        List<Hit> hits = new ArrayList<>();
        for (SettingCatalogue.Entry entry : entries) {
            int rank = best(entry, wanted);
            if (rank < NONE) {
                hits.add(new Hit(entry, rank));
            }
        }
        hits.sort((first, second) -> {
            if (first.rank() != second.rank()) {
                return Integer.compare(first.rank(), second.rank());
            }
            return first.entry().key().compareToIgnoreCase(second.entry().key());
        });
        List<SettingCatalogue.Entry> ordered = new ArrayList<>(hits.size());
        for (Hit hit : hits) {
            ordered.add(hit.entry());
        }
        return ordered;
    }

    private static int best(SettingCatalogue.Entry entry, String wanted) {
        int byName = NONE;
        for (String name : SettingCatalogue.namesFor(entry)) {
            byName = Math.min(byName, rank(name.toLowerCase(Locale.ROOT), wanted));
        }
        if (byName < NONE) {
            return byName;
        }
        return rank(entry.label().toLowerCase(Locale.ROOT), wanted);
    }

    private static int rank(String name, String wanted) {
        if (name.equals(wanted)) {
            return EXACT;
        }
        if (name.startsWith(wanted)) {
            return PREFIX;
        }
        int at = name.indexOf(wanted);
        if (at < 0) {
            return NONE;
        }
        if (at == 0 || name.charAt(at - 1) == '.' || name.charAt(at - 1) == '_') {
            return WORD;
        }
        return CONTAINS;
    }
}