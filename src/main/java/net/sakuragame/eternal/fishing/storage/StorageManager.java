package net.sakuragame.eternal.fishing.storage;

import net.sakuragame.eternal.fishing.core.FishAccount;
import net.sakuragame.serversystems.manage.api.database.DataManager;
import net.sakuragame.serversystems.manage.api.database.DatabaseQuery;
import net.sakuragame.serversystems.manage.client.api.ClientManagerAPI;

import java.sql.ResultSet;
import java.util.UUID;

public class StorageManager {

    private final DataManager dataManager;

    public StorageManager() {
        this.dataManager = ClientManagerAPI.getDataManager();
    }

    public void init() {
        for (AccountTable table : AccountTable.values()) {
            table.createTable();
        }
    }

    public FishAccount loadDate(UUID uuid) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return null;

        try (DatabaseQuery query = dataManager.createQuery(AccountTable.JUST_FISHING_ACCOUNT.getTableName(),
                "uid", uid)) {
            ResultSet result = query.getResultSet();
            if (result.next()) {
                int amount = result.getInt("amount");
                return new FishAccount(uuid, amount);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return new FishAccount(uuid);
    }

    public void saveData(UUID uuid, int amount) {
        int uid = ClientManagerAPI.getUserID(uuid);
        if (uid == -1) return;

        dataManager.executeReplace(AccountTable.JUST_FISHING_ACCOUNT.getTableName(),
                new String[]{"uid", "amount"},
                new Object[]{uid, amount}
        );
    }
}
