package database.tools;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import database.JDBCDataAccessProvider;
import database.internal.Field;
import database.internal.TableName;

class Documentator {

    public static void getTableOverview(PrintStream out, String wiki) {

        out.println("# Relaties\n");
        out.println("De databank bevat volgende tabellen:\n");

        out.println("Relatie | Functie");
        out.println("--------|--------");

        for (TableName t : TableName.values()) {
            out.print(link(t, wiki));
            out.print(" | ");
            if (t.getDoc() != null) {
                out.print(t.getDoc());
            }
            out.println();
        }
    }

    /**
     * Gets the list as "[table](wiki#table), [table1](wiki#table1)" without
     * header of table; Null if no deps.
     * 
     * @param t
     * @param out
     * @param wiki
     * @return
     */
    public static String getReverseDeps(TableName t, String wiki) {
        List<TableName> needers = new ArrayList<>();
        needers.addAll(JDBCDataAccessProvider.getDepsFor(t));
        if (needers.isEmpty()) {
            return null;
        }

        String res = "";
        for (int i = 0; i < needers.size() - 1; i++) {
            res += link(needers.get(i), wiki) + ", ";
        }
        res += link(needers.get(needers.size() - 1), wiki);
        return res;
    }

    public static void reverseDepsTable(PrintStream out, String wiki) {
        out.println("Relatie | Wordt gerefereerd door");
        out.println("--------|-----------------------");
        for (TableName t : TableName.values()) {
            String deps = getReverseDeps(t, wiki);
            if (deps != null) {
                out.println(link(t, wiki) + " | " + deps);
            }
        }
    }

    public static void tableOverview(TableName t, PrintStream out, String wiki) {
        out.println("\n## " + t);
        out.println();
        if (t.getDoc() != null) {
            out.println("**" + t.getDoc() + "**\n");
        }
        if (t.getDetailedDoc() != null) {
            out.println(t.getDetailedDoc() + "\n");
        }

        String revDeps = getReverseDeps(t, wiki);
        if (revDeps != null) {
            out.println("\nDe tabel wordt gerefereerd door " + revDeps);
        }

        out.println();
        out.println("De tabel " + t
                + " heeft de volgende attributen (primaire sleutels in vet):\n");

        Set<Field> ids = new HashSet<>();
        for (Field id : t.getIdFields()) {
            ids.add(id);
        }

        out.println("Naam | Type | Functie");
        out.println("-----|------|--------");
        for (Field f : t.getFields()) {
            if (ids.contains(f)) {
                out.print("**" + f.getNameColumn() + "**");
            } else {
                out.print(f.getNameColumn());
            }

            out.print(" | ");
            if (f.getReference() != null) {
                out.print("**" + link(f.getReference(), wiki) + "**");
            } else {
                out.print(f.getTypeEnum().toString().toLowerCase());
            }

            out.print(" | ");

            if (f.getDocs() != null) {
                out.println(f.getDocs());
            } else {
                out.println();
            }
        }

    }

    private static String link(TableName t, String link) {
        String name = t.toString().toLowerCase().replace("\"", "");
        return "[" + name + "](" + link + "#" + name + ")";
    }

    public static void main(String[] args) {
        PrintStream out = System.out;
        String wiki = "Structuur-databank";

        out.println("# OPGELET\n\n"
                + "Deze wikipagina wordt **automatisch** gegenereerd door Dao:````database.utils.Documentator````. Indien je aanpassingen wilt, pas deze dan aan in TableName of het TabelField.");

        getTableOverview(out, wiki);

        out.println("\n## Dependencies\n");

        out.println("Opgelet: standaard worden tabellen 'cascading' verwijderd.\n");
        reverseDepsTable(out, wiki);

        for (TableName t : TableName.values()) {
            tableOverview(t, out, wiki);
        }
    }

}
