package database.tools.utils;

import database.tools.logging.Logger;
import database.tools.utils.MVCUtils.GlobalStatus;
import database.tools.utils.MVCUtils.Listener;
import database.tools.utils.MVCUtils.RelationStatus;

public class MVCDefaultListeners {

    public static class ResetListener implements Listener<GlobalStatus> {

        public ResetListener() {
            Logger.out.println("Resetting the database:");
        }

        @Override
        public void onStatusChanged(GlobalStatus source) {

            String prog = MVCUtils.getProg(source.totalRelations,
                    source.getCurrentRelation());
            Logger.out.println(" " + prog + " "
                    + source.getRelationStatus().relationName);
        }
    }

    public static class CSVListener implements Listener<RelationStatus> {

        public CSVListener(String name) {
            Logger.out.println("Loading entries into " + name + " from csv");
            MVCUtils.tic();
        }

        @Override
        public void onStatusChanged(RelationStatus source) {
            if (source.getCurrentEntry() != source.entriesToSave
                    && source.getCurrentEntry() % source.factor != 0) {
                return;
            }
            String bar = MVCUtils.getBar(source.entriesToSave / source.factor,
                    source.getCurrentEntry() / source.factor);
            String prog = MVCUtils.getProg(source.entriesToSave,
                    source.getCurrentEntry());
            Logger.out.print("\r"+bar + " " + prog + " "
                    + source.relationName);
            if(source.getCurrentEntry() == source.entriesToSave){
                MVCUtils.toc(": loading");
            }
        }

    }

}
