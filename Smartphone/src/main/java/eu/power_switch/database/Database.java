/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import eu.power_switch.database.table.gateway.GatewayTable;
import eu.power_switch.database.table.receiver.AutoPairTable;
import eu.power_switch.database.table.receiver.DipTable;
import eu.power_switch.database.table.receiver.MasterSlaveTable;
import eu.power_switch.database.table.receiver.ReceiverTable;
import eu.power_switch.database.table.receiver.UniversalButtonTable;
import eu.power_switch.database.table.room.RoomTable;
import eu.power_switch.database.table.scene.SceneItemTable;
import eu.power_switch.database.table.scene.SceneTable;
import eu.power_switch.database.table.timer.TimerActionTable;
import eu.power_switch.database.table.timer.TimerReceiverActionTable;
import eu.power_switch.database.table.timer.TimerRoomActionTable;
import eu.power_switch.database.table.timer.TimerSceneActionTable;
import eu.power_switch.database.table.timer.TimerTable;
import eu.power_switch.database.table.timer.TimerWeekdayTable;
import eu.power_switch.database.table.widget.ReceiverWidgetTable;
import eu.power_switch.database.table.widget.RoomWidgetTable;
import eu.power_switch.database.table.widget.SceneWidgetTable;

/**
 * This Class is responsible for initializing and upgrading all Database tables
 */
public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PSdatabase.db";
    private static final int DATABASE_VERSION = 8;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        GatewayTable.onCreate(db);

        RoomTable.onCreate(db);

        SceneTable.onCreate(db);
        SceneItemTable.onCreate(db);

        ReceiverTable.onCreate(db);
        MasterSlaveTable.onCreate(db);
        DipTable.onCreate(db);
        AutoPairTable.onCreate(db);
        UniversalButtonTable.onCreate(db);

        ReceiverWidgetTable.onCreate(db);
        RoomWidgetTable.onCreate(db);
        SceneWidgetTable.onCreate(db);

        TimerTable.onCreate(db);
        TimerWeekdayTable.onCreate(db);
        TimerActionTable.onCreate(db);

        TimerReceiverActionTable.onCreate(db);
        TimerRoomActionTable.onCreate(db);
        TimerSceneActionTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        switch (oldVersion) {
            case 1:
                db.execSQL("DROP TABLE IF EXISTS " + "widgets");
            case 2:
            case 3:
            case 4:
            case 5:
                break;

        }

        GatewayTable.onUpgrade(db, oldVersion, newVersion);

        RoomTable.onUpgrade(db, oldVersion, newVersion);

        SceneTable.onUpgrade(db, oldVersion, newVersion);
        SceneItemTable.onUpgrade(db, oldVersion, newVersion);

        ReceiverTable.onUpgrade(db, oldVersion, newVersion);
        MasterSlaveTable.onUpgrade(db, oldVersion, newVersion);
        DipTable.onUpgrade(db, oldVersion, newVersion);
        AutoPairTable.onUpgrade(db, oldVersion, newVersion);
        UniversalButtonTable.onUpgrade(db, oldVersion, newVersion);

        ReceiverWidgetTable.onUpgrade(db, oldVersion, newVersion);
        RoomWidgetTable.onUpgrade(db, oldVersion, newVersion);
        SceneWidgetTable.onUpgrade(db, oldVersion, newVersion);

        TimerTable.onUpgrade(db, oldVersion, newVersion);
        TimerWeekdayTable.onUpgrade(db, oldVersion, newVersion);
        TimerActionTable.onUpgrade(db, oldVersion, newVersion);

        TimerReceiverActionTable.onUpgrade(db, oldVersion, newVersion);
        TimerRoomActionTable.onUpgrade(db, oldVersion, newVersion);
        TimerSceneActionTable.onUpgrade(db, oldVersion, newVersion);
    }

}
