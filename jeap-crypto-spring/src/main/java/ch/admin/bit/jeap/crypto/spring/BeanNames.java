package ch.admin.bit.jeap.crypto.spring;

public final class BeanNames {

    private BeanNames() {
    }

    public static String beanNameFromKeyId(String keyId, String postFix) {
        String keyNameCamelCase = BeanNames.toCamelCaseIdentifier(keyId);
        return keyNameCamelCase + postFix;
    }

    /**
     * Convert a name to a camel-case identifier, removing any invalid name characters, and camel-casing kebab-case names.
     * The first character is guaranteed to be a valid lowercase  identifier start character, a '_' is prepended otherwise.
     * Preserves case in already camel-cased identifiers.
     */
    public static String toCamelCaseIdentifier(String name) {
        StringBuilder str = new StringBuilder();

        boolean nextCharUpperCase = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            // Make sure first char is a valid identifier start, otherwise prepend _
            if (i == 0 && !Character.isJavaIdentifierStart(c)) {
                str.append("_");
            }

            if (!Character.isJavaIdentifierPart(c)) {
                // Skip invalid identifier characters (such as dashes), make the next character upper case
                nextCharUpperCase = true;
            } else {
                if (i == 0) {
                    // First character is always lower case
                    str.append(Character.toLowerCase(c));
                } else if (nextCharUpperCase) {
                    str.append(Character.toUpperCase(c));
                    nextCharUpperCase = false;
                } else {
                    str.append(c);
                }
            }
        }
        return str.toString();
    }
}
