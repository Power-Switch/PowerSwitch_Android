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

package eu.power_switch.gui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.backup.Backup;
import eu.power_switch.backup.BackupHandler;
import eu.power_switch.exception.backup.BackupNotFoundException;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.shared.log.Log;

/**
 * Adapter to visualize Backup items in RecyclerView
 * <p/>
 * Created by Markus on 27.07.2015.
 */
public class BackupRecyclerViewAdapter extends RecyclerView.Adapter<BackupRecyclerViewAdapter.ViewHolder> {
    private RecyclerViewFragment recyclerViewFragment;
    private ArrayList<Backup> backups;
    private Context context;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public BackupRecyclerViewAdapter(RecyclerViewFragment recyclerViewFragment, Context context, ArrayList<Backup> backups) {
        this.recyclerViewFragment = recyclerViewFragment;
        this.backups = backups;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public BackupRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_backup, parent, false);
        return new BackupRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final BackupRecyclerViewAdapter.ViewHolder holder, int position) {
        final Backup backup = backups.get(position);

        holder.backupDate.setText(backup.getDate().toLocaleString());
        holder.backupName.setText(backup.getName());
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            BackupHandler backupHandler = new BackupHandler(context);
                            backupHandler.removeBackup(backup.getName());

                            backups.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                            StatusMessageHandler.showInfoMessage(recyclerViewFragment, R.string.backup_removed, Snackbar.LENGTH_LONG);
                        } catch (BackupNotFoundException e) {
                            Log.e(e);
                            StatusMessageHandler.showInfoMessage(recyclerViewFragment, R.string.backup_not_found, Snackbar.LENGTH_LONG);
                        } catch (Exception e) {
                            StatusMessageHandler.showErrorMessage(recyclerViewFragment, e);
                        }
                    }
                }).setNeutralButton(android.R.string.cancel, null).setTitle(context.getString(R.string
                        .are_you_sure))
                        .setMessage(R.string.remove_backup_message);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        if (position == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return backups.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView backupName;
        public TextView backupDate;
        public FloatingActionButton deleteButton;
        public LinearLayout footer;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.backupName = (TextView) itemView.findViewById(R.id.txt_backup_name);
            this.backupDate = (TextView) itemView.findViewById(R.id.txt_backup_date);
            this.deleteButton = (FloatingActionButton) itemView.findViewById(R.id.delete_backup_fab);
            deleteButton.setImageDrawable(IconicsHelper.getDeleteIcon(context, android.R.color.white));
            this.footer = (LinearLayout) itemView.findViewById(R.id.list_footer);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(itemView, getLayoutPosition());
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(itemView, getLayoutPosition());
                    }
                    return true;
                }
            });
        }
    }
}
