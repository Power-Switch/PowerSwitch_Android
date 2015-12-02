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

import eu.power_switch.database.table.action.ActionTable;
import eu.power_switch.database.table.action.ReceiverActionTable;
import eu.power_switch.database.table.action.RoomActionTable;
import eu.power_switch.database.table.action.SceneActionTable;
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
    private static final int DATABASE_VERSION = 9;

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

        ActionTable.onCreate(db);
        ReceiverActionTable.onCreate(db);
        RoomActionTable.onCreate(db);
        SceneActionTable.onCreate(db);
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

        ActionTable.onUpgrade(db, oldVersion, newVersion);
        ReceiverActionTable.onUpgrade(db, oldVersion, newVersion);
        RoomActionTable.onUpgrade(db, oldVersion, newVersion);
        SceneActionTable.onUpgrade(db, oldVersion, newVersion);


        switch (oldVersion) {
            case 1:
                db.execSQL("DROP TABLE IF EXISTS " + "widgets");
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                // insert data from old timer_action table into ActionTable
                db.execSQL("INSERT INTO " + ActionTable.TABLE_NAME + "(" + ActionTable.COLUMN_ID + ", " + ActionTable.COLUMN_ACTION_TYPE + ") SELECT " +
                        TimerActionTable.COLUMN_ACTION_ID + ", " + "action_type" + " FROM timer_action;");

                // update timerActions
                // insert data from old timer_action table into TimerActionTable
                db.execSQL("INSERT INTO " + TimerActionTable.TABLE_NAME + "(" + TimerActionTable.COLUMN_ACTION_ID + ", " +
                        TimerActionTable.COLUMN_TIMER_ID + ") " +
                        "SELECT " + "" + "_id, " + TimerActionTable.COLUMN_TIMER_ID + " FROM timer_action;");
                // drop old table
                db.execSQL("DROP TABLE timer_action");
                break;
        }
    }
}
