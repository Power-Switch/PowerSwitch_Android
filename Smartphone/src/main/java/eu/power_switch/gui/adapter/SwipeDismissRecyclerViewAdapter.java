/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.adapter;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Markus on 24.07.2017.
 */
public abstract class SwipeDismissRecyclerViewAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> implements ItemTouchHelperAdapter {

    protected final Context context;

    protected OnStartDragListener onStartDragListener;
    protected OnItemMovedListener onItemMovedListener;

    public SwipeDismissRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    public void setOnStartDragListener(OnStartDragListener onStartDragListener) {
        this.onStartDragListener = onStartDragListener;
    }

    public void setOnItemMovedListener(OnItemMovedListener onItemMovedListener) {
        this.onItemMovedListener = onItemMovedListener;
    }

    @Override
    @CallSuper
    public void onBindViewHolder(final T holder, int position) {
        getDragHandle(holder).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    onStartDragListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    protected abstract View getDragHandle(T viewHolder);

    @Override
    @CallSuper
    public void onItemMove(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
        if (onItemMovedListener != null) {
            onItemMovedListener.onItemMoved(fromPosition, toPosition);
        }
    }

    @Override
    @CallSuper
    public void onItemDismiss(int position) {
    }

}
