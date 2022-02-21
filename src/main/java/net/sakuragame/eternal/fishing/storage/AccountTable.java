package net.sakuragame.eternal.fishing.storage;

import net.sakuragame.eternal.dragoncore.database.mysql.DatabaseTable;

public enum AccountTable {

    JUST_FISHING_ACCOUNT(new DatabaseTable("justfishing_stat",
            new String[]{
                    "`uid` int NOT NULL PRIMARY KEY",
                    "`amount` int default 0"
            }));

    private final DatabaseTable table;

    AccountTable(DatabaseTable table) {
        this.table = table;
    }

    public String getTableName() {
        return table.getTableName();
    }

    public String[] getColumns() {
        return table.getTableColumns();
    }

    public DatabaseTable getTable() {
        return table;
    }

    public void createTable() {
        table.createTable();
    }
}
