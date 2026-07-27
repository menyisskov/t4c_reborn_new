package com.perso.T4C.i18n;

/** Active game language, used to select translated data files at load time. */
public enum Lang {
    EN, FR;

    public static final String PROPERTY = "t4c.lang";
    public static final String ENVIRONMENT = "T4C_LANG";
    private static volatile Lang active = readInitial();

    public static Lang current() {
        return active;
    }

    public static void set(Lang lang) {
        active = lang == null ? EN : lang;
    }

    /** Selects a language from a command-line/system-property style value. */
    public static void set(String value) {
        if (value == null || value.isBlank()) return;
        try {
            set(Lang.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            // Keep the current language for an invalid user setting.
        }
    }

    private static Lang readInitial() {
        String value = System.getProperty(PROPERTY);
        if (value == null || value.isBlank()) value = System.getenv(ENVIRONMENT);
        if (value != null) {
            try { return Lang.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT)); }
            catch (IllegalArgumentException ignored) { }
        }
        return FR;
    }

    /** Inserts this language's file suffix (e.g. "_fr") before the given path's extension, or returns path unchanged for EN. */
    public String suffixed(String path) {
        if (this == EN) return path;
        String suffix = "_" + name().toLowerCase();
        int dot = path.lastIndexOf('.');
        return dot < 0 ? path + suffix : path.substring(0, dot) + suffix + path.substring(dot);
    }
}
