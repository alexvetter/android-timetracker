package alexvetter.timetrackr.controller;

import alexvetter.timetrackr.database.AbstractDatabaseHandler;

public interface Controller<T extends AbstractDatabaseHandler> {

    T getDatabaseHandler();
}
