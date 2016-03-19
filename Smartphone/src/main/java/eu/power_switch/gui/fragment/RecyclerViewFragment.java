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
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Calendar;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.dialog.UnknownErrorDialog;

/**
 * This is a Fragment that contains a RecyclerView somewhere in its view hierarchy
 * It is used to be able to move possible Floating Action Buttons accordingly when displaying Snackbars
 * It also handles async list updates and displaying error messages
 * <p/>
 * Created by Markus on 25.11.2015.
 */
public abstract class RecyclerViewFragment extends Fragment {

    protected View rootView;
    private LinearLayout layoutLoading;
    private LinearLayout layoutEmpty;
    private LinearLayout layoutError;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        onCreateViewEvent(inflater, container, savedInstanceState);

        layoutLoading = (LinearLayout) rootView.findViewById(R.id.layoutLoading);
        layoutEmpty = (LinearLayout) rootView.findViewById(R.id.layoutEmpty);
        layoutError = (LinearLayout) rootView.findViewById(R.id.layoutError);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);

        onInitialized();

        return rootView;
    }

    protected abstract void onInitialized();

    public void updateListContent() {
        showLoadingAnimation();

        new AsyncTask<Context, Void, RecyclerViewUpdateResult>() {

            @Override
            protected RecyclerViewUpdateResult doInBackground(Context... contexts) {
                try {
                    return new RecyclerViewUpdateResult(refreshListData());
                } catch (Exception e) {
                    return new RecyclerViewUpdateResult(e);
                }
            }

            @Override
            protected void onPostExecute(RecyclerViewUpdateResult result) {
                getRecyclerViewAdapter().notifyDataSetChanged();

                if (result.isSuccess()) {
                    if (result.getElements().size() == 0) {
                        showEmpty();
                    } else {
                        showList();
                    }
                } else {
                    showError(result.getException(), Calendar.getInstance().getTimeInMillis());
                    StatusMessageHandler.showErrorMessage(getActivity(), result.getException());
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getContext());
    }

    protected void showLoadingAnimation() {
        layoutEmpty.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    protected void showList() {
        layoutEmpty.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    protected void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    protected void showError(final Exception e, final long timeInMilliseconds) {
        layoutEmpty.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        layoutLoading.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        layoutError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(UnknownErrorDialog.getNewInstanceIntent(e, timeInMilliseconds));
            }
        });
    }

    protected abstract void onCreateViewEvent(LayoutInflater inflater, @Nullable ViewGroup container,
                                              @Nullable Bundle savedInstanceState);

    public abstract RecyclerView getRecyclerView();

    public abstract RecyclerView.Adapter getRecyclerViewAdapter();

    public abstract List refreshListData() throws Exception;

}