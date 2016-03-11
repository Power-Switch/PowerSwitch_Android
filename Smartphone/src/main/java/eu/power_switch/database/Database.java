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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import eu.power_switch.database.table.action.ActionTable;
import eu.power_switch.database.table.action.ReceiverActionTable;
import eu.power_switch.database.table.action.RoomActionTable;
import eu.power_switch.database.table.action.SceneActionTable;
import eu.power_switch.database.table.apartment.ApartmentGatewayRelationTable;
import eu.power_switch.database.table.apartment.ApartmentGeofenceRelationTable;
import eu.power_switch.database.table.apartment.ApartmentTable;
import eu.power_switch.database.table.gateway.GatewayTable;
import eu.power_switch.database.table.geofence.GeofenceActionTable;
import eu.power_switch.database.table.geofence.GeofenceTable;
import eu.power_switch.database.table.history.HistoryTable;
import eu.power_switch.database.table.receiver.AutoPairTable;
import eu.power_switch.database.table.receiver.DipTable;
import eu.power_switch.database.table.receiver.MasterSlaveTable;
import eu.power_switch.database.table.receiver.ReceiverTable;
import eu.power_switch.database.table.receiver.UniversalButtonTable;
import eu.power_switch.database.table.room.RoomTable;
import eu.power_switch.database.table.scene.SceneItemTable;
import eu.power_switch.database.table.scene.SceneTable;
import eu.power_switch.database.table.sleep_as_android.SleepAsAndroidActionTable;
import eu.power_switch.database.table.timer.TimerActionTable;
import eu.power_switch.database.table.timer.TimerTable;
import eu.power_switch.database.table.timer.TimerWeekdayTable;
import eu.power_switch.database.table.widget.ReceiverWidgetTable;
import eu.power_switch.database.table.widget.RoomWidgetTable;
import eu.power_switch.database.table.widget.SceneWidgetTable;
import eu.power_switch.shared.log.Log;

/**
 * This Class is responsible for initializing and upgrading all Database tables
 */
public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PSdatabase.db";
    private static final int DATABASE_VERSION = 14;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            GatewayTable.onCreate(db);

            ReceiverTable.onCreate(db);
            MasterSlaveTable.onCreate(db);
            DipTable.onCreate(db);
            AutoPairTable.onCreate(db);
            UniversalButtonTable.onCreate(db);

            RoomTable.onCreate(db);

            SceneTable.onCreate(db);
            SceneItemTable.onCreate(db);

            GeofenceTable.onCreate(db); // has to be created before ApartmentTable
            GeofenceActionTable.onCreate(db);

            TimerTable.onCreate(db);
            TimerWeekdayTable.onCreate(db);
            TimerActionTable.onCreate(db);

            SleepAsAndroidActionTable.onCreate(db);

            ActionTable.onCreate(db);
            ReceiverActionTable.onCreate(db);
            RoomActionTable.onCreate(db);
            SceneActionTable.onCreate(db);

            HistoryTable.onCreate(db);

            ApartmentGatewayRelationTable.onCreate(db);
            ApartmentGeofenceRelationTable.onCreate(db);
            ApartmentTable.onCreate(db); // has to be created after relational tables

            ReceiverWidgetTable.onCreate(db);
            RoomWidgetTable.onCreate(db);
            SceneWidgetTable.onCreate(db);


            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.beginTransaction();

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

            ApartmentTable.onUpgrade(db, oldVersion, newVersion);
            ApartmentGatewayRelationTable.onUpgrade(db, oldVersion, newVersion);
            ApartmentGeofenceRelationTable.onUpgrade(db, oldVersion, newVersion);

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

            GeofenceTable.onUpgrade(db, oldVersion, newVersion);
            GeofenceActionTable.onUpgrade(db, oldVersion, newVersion);

            TimerTable.onUpgrade(db, oldVersion, newVersion);
            TimerWeekdayTable.onUpgrade(db, oldVersion, newVersion);
            TimerActionTable.onUpgrade(db, oldVersion, newVersion);

            SleepAsAndroidActionTable.onUpgrade(db, oldVersion, newVersion);

            ActionTable.onUpgrade(db, oldVersion, newVersion);
            ReceiverActionTable.onUpgrade(db, oldVersion, newVersion);
            RoomActionTable.onUpgrade(db, oldVersion, newVersion);
            SceneActionTable.onUpgrade(db, oldVersion, newVersion);

            HistoryTable.onUpgrade(db, oldVersion, newVersion);

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
                    // insert data from old timer_action table into ActionTable and TimerActionTable
                    Cursor cursor = db.query("timer_action", new String[]{"_id", "timer_id", "action_type"},
                            null, null, null, null, null);
                    cursor.moveToFirst();

                    while (!cursor.isAfterLast()) {
                        Long actionId = cursor.getLong(0);
                        Long timerId = cursor.getLong(1);
                        String actionType = cursor.getString(2);

                        ContentValues values;
                        // add values to TimerActionTable Relation
                        values = new ContentValues();
                        values.put(TimerActionTable.COLUMN_TIMER_ID, timerId);
                        values.put(TimerActionTable.COLUMN_ACTION_ID, actionId);
                        db.insert(TimerActionTable.TABLE_NAME, null, values);

                        // add values to ActionTable
                        values = new ContentValues();
                        values.put(ActionTable.COLUMN_ID, actionId);
                        values.put(ActionTable.COLUMN_ACTION_TYPE, actionType);
                        db.insert(ActionTable.TABLE_NAME, null, values);

                        // RECEIVER ACTION
                        Cursor cursor1 = db.query("timer_receiver_action", new String[]{"_id", "timer_action_id",
                                ReceiverActionTable.COLUMN_ROOM_ID, ReceiverActionTable.COLUMN_RECEIVER_ID,
                                ReceiverActionTable.COLUMN_BUTTON_ID
                        }, "timer_action_id" + "=" + actionId, null, null, null, null);
                        cursor1.moveToFirst();
                        while (!cursor1.isAfterLast()) {
                            Long roomId = cursor1.getLong(2);
                            Long receiverId = cursor1.getLong(3);
                            Long buttonId = cursor1.getLong(4);

                            values = new ContentValues();
                            values.put(ReceiverActionTable.COLUMN_ACTION_ID, actionId);
                            values.put(ReceiverActionTable.COLUMN_ROOM_ID, roomId);
                            values.put(ReceiverActionTable.COLUMN_RECEIVER_ID, receiverId);
                            values.put(ReceiverActionTable.COLUMN_BUTTON_ID, buttonId);
                            db.insert(ReceiverActionTable.TABLE_NAME, null, values);

                            cursor1.moveToNext();
                        }
                        cursor1.close();

                        // ROOM ACTION
                        cursor1 = db.query("timer_room_action", new String[]{"_id", "timer_action_id",
                                        RoomActionTable.COLUMN_ROOM_ID, RoomActionTable.COLUMN_BUTTON_NAME},
                                "timer_action_id" + "=" + actionId, null, null, null, null);
                        cursor1.moveToFirst();
                        while (!cursor1.isAfterLast()) {
                            Long roomId = cursor1.getLong(2);
                            String buttonName = cursor1.getString(3);

                            values = new ContentValues();
                            values.put(RoomActionTable.COLUMN_ACTION_ID, actionId);
                            values.put(RoomActionTable.COLUMN_ROOM_ID, roomId);
                            values.put(RoomActionTable.COLUMN_BUTTON_NAME, buttonName);
                            db.insert(RoomActionTable.TABLE_NAME, null, values);

                            cursor1.moveToNext();
                        }
                        cursor1.close();

                        // SCENE ACTION
                        cursor1 = db.query("timer_scene_action", new String[]{"_id", "timer_action_id",
                                        SceneActionTable.COLUMN_SCENE_ID},
                                "timer_action_id" + "=" + actionId, null, null, null, null);
                        cursor1.moveToFirst();
                        while (!cursor1.isAfterLast()) {
                            Long sceneId = cursor1.getLong(2);

                            values = new ContentValues();
                            values.put(SceneActionTable.COLUMN_ACTION_ID, actionId);
                            values.put(SceneActionTable.COLUMN_SCENE_ID, sceneId);
                            db.insert(SceneActionTable.TABLE_NAME, null, values);

                            cursor1.moveToNext();
                        }
                        cursor1.close();

                        cursor.moveToNext();
                    }

                    cursor.close();

                    db.execSQL("DROP TABLE IF EXISTS " + "timer_receiver_action");
                    db.execSQL("DROP TABLE IF EXISTS " + "timer_room_action");
                    db.execSQL("DROP TABLE IF EXISTS " + "timer_scene_action");

                    // drop old table
                    db.execSQL("DROP TABLE IF EXISTS timer_action");
                case 9:
                case 10:
                case 11:
                case 12:
                    break;
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(e);
        } finally {
            db.endTransaction();
        }
    }
}
