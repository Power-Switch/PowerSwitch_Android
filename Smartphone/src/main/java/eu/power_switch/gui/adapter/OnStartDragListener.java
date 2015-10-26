package eu.power_switch.gui.adapter;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Markus on 25.10.2015.
 */
public interface OnStartDragListener {
    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}
