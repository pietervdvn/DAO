package database.tools.utils;

import java.util.ArrayList;
import java.util.List;

import database.tools.logging.Logger;

public class MVCUtils {

    public static interface Listener<T extends EventSource> {

        void onStatusChanged(T source);

    }

    public static class EventSource {
        private List<Listener<?>> listeners = new ArrayList<>();

        @SuppressWarnings("unchecked")
        void ping() {
            for (@SuppressWarnings("rawtypes") Listener l : listeners) {
                l.onStatusChanged(this);
            }
        }

        @SuppressWarnings("rawtypes")
        public void addListener(Listener l) {
            listeners.add(l);
        }
        
        @SuppressWarnings("rawtypes")
        public void addListener(Listener... l){
            for (int i = 0; i < l.length; i++) {
                listeners.add(l[i]);
            }
        }
    }

    public static class RelationStatus extends EventSource {

        public final String relationName;
        public final int entriesToSave;
        public int factor = 1; // the numbers of entries that are loaded at once
        
        
        private int currentEntry = 0;

        public RelationStatus(String relationName, int entriesToSave) {
            this.relationName = relationName;
            this.entriesToSave = entriesToSave;
        }

        public int getCurrentEntry() {
            return currentEntry;
        }

        public void setCurrentEntry(int currentEntry) {
            if (this.currentEntry != currentEntry) {
                this.currentEntry = currentEntry;
                ping();
            }
        }

        @Override
        public String toString() {
            return relationName + " (" + currentEntry + "/" + entriesToSave
                    + ")";
        }
    }

    public static class GlobalStatus extends EventSource {

        public final int totalRelations;
        private int currentRelation = 0;
        private RelationStatus rs;

        public GlobalStatus(int totalRelations) {
            this.totalRelations = totalRelations;
        }

        public int getCurrentRelation() {
            return currentRelation;
        }

        public void setCurrentRelation(int currentRelation, RelationStatus rs) {
            if (this.currentRelation != currentRelation) {
                this.currentRelation = currentRelation;
                this.rs = rs;
                ping();
            }
        }

        public RelationStatus getRelationStatus() {
            return rs;
        }

    }

    public static String getBar(int total, int progress) {
        String r = "[";
        for (int i = 0; i < progress; i++) {
            r += "-";
        }
        for (int i = progress; i < total; i++) {
            r += " ";
        }
        return r + "]";
    }

    public static String getProg(int total, int progress) {
        String t = "" + total;
        String p = "" + progress;
        String extra = "";
        for (int i = p.length(); i < t.length(); i++) {
            extra += " ";
        }
        return extra+"("+p+"/"+t+")";

    }

    public static void printScriptName(String scriptName) {
        Logger.out.println();
        printTilde(scriptName.length() + 6);
        Logger.out.println(" ~ " + scriptName + " ~ ");
        printTilde(scriptName.length() + 6);
        Logger.out.println();
    }

    private static long start;

    public static void tic() {
        start = System.currentTimeMillis();
    }

    public static void toc(String msg) {
        long stop = System.currentTimeMillis();
        int millis = (int) (stop - start);
        Logger.out.println(msg + " took " + (millis / 1000) + "."
                + (millis % 1000) + " sec");
    }

    public static void printTilde(int i) {
        for (int j = 0; j < i; j++) {
            Logger.out.print("~");
        }
        Logger.out.println();
    }

}
