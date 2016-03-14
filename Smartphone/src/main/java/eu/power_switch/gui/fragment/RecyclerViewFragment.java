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

package eu.power_switch.gui.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;

import java.util.List;

/**
 * This is a Fragment that contains a RecyclerView somewhere in its view hierarchy
 * It is used to be able to move possible Floating Action Buttons accordingly when displaying Snackbars
 * <p/>
 * Created by Markus on 25.11.2015.
 */
public abstract class RecyclerViewFragment extends Fragment {

    private LinearLayout layoutLoading;
    private CoordinatorLayout contentLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = onCreateViewEvent(inflater, container, savedInstanceState);

        layoutLoading = (LinearLayout) rootView.findViewById(R.id.layoutLoading);
        contentLayout = (CoordinatorLayout) rootView.findViewById(R.id.contentLayout);

        updateListContent();

        return rootView;
    }

    public void updateListContent() {
        showLoadingAnimation();

        new AsyncTask<Context, Void, Exception>() {

            @Override
            protected Exception doInBackground(Context... contexts) {
                try {
                    refreshListData();
                    return null;
                } catch (Exception e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Exception e) {
                getRecyclerViewAdapter().notifyDataSetChanged();

                if (e == null) {
                    showList();
                } else {
                    showError();
                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getContext());
    }

    protected void showLoadingAnimation() {
        layoutLoading.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
    }

    protected void showList() {
        layoutLoading.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
    }

    protected void showError() {
        layoutLoading.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
    }

    protected abstract View onCreateViewEvent(LayoutInflater inflater, @Nullable ViewGroup container,
                                              @Nullable Bundle savedInstanceState);

    public abstract RecyclerView getRecyclerView();

    public abstract RecyclerView.Adapter getRecyclerViewAdapter();

    public abstract List refreshListData() throws Exception;

}