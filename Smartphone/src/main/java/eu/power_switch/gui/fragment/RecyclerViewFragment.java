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

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.Calendar;
import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;

/**
 * This is a Fragment that contains a RecyclerView somewhere in its view hierarchy
 * It is used to be able to move possible Floating Action Buttons accordingly when displaying Snackbars
 * It also handles async list updates and displaying error messages
 * <p/>
 * Created by Markus on 25.11.2015.
 */
public abstract class RecyclerViewFragment<T> extends Fragment implements LoaderManager.LoaderCallbacks<RecyclerViewUpdateResult<T>> {

    protected View rootView;
    private LinearLayout layoutLoading;
    private FrameLayout layoutEmpty;
    private LinearLayout layoutError;

    private Loader dataLoader;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        onCreateViewEvent(inflater, container, savedInstanceState);

        layoutLoading = rootView.findViewById(R.id.layoutLoading);
        layoutEmpty = rootView.findViewById(R.id.layoutEmpty);
        layoutError = rootView.findViewById(R.id.layoutError);

        // use loaderManager of fragment, so unique ID across app is not required (only across this fragment)
        dataLoader = getLoaderManager().initLoader(0, null, this);

        onInitialized();

        return rootView;
    }

    protected abstract void onInitialized();

    @Override
    public Loader<RecyclerViewUpdateResult<T>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<RecyclerViewUpdateResult<T>>(getContext()) {

            @Override
            public RecyclerViewUpdateResult<T> loadInBackground() {
                try {
                    return new RecyclerViewUpdateResult<>(loadListData());
                } catch (Exception e) {
                    return new RecyclerViewUpdateResult<>(e);
                }
            }

            @Override
            public void deliverResult(RecyclerViewUpdateResult<T> result) {
                onLoadFinished(this, result);
            }

            @Override
            protected void onStopLoading() {
                cancelLoad();
            }

            @Override
            protected void onReset() {
                onStopLoading();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<RecyclerViewUpdateResult<T>> loader, RecyclerViewUpdateResult<T> result) {
        if (result.isSuccess()) {
            if (result.getElements()
                    .size() == 0) {
                showEmpty();
            } else {
                onListDataChanged(result.getElements());
                getRecyclerViewAdapter().notifyDataSetChanged();
                showList();
            }
        } else {
            showError(result.getException(),
                    Calendar.getInstance()
                            .getTimeInMillis());
            StatusMessageHandler.showErrorMessage(getActivity(), result.getException());
        }
    }

    /**
     * This method is called after loader has finished loading data and the result is ready to be delivered
     *
     * @param list loader data result
     */
    protected abstract void onListDataChanged(List<T> list);

    @Override
    public void onLoaderReset(Loader<RecyclerViewUpdateResult<T>> loader) {

    }

    public void updateListContent() {
        showLoadingAnimation();
        dataLoader.forceLoad();
    }

    protected void showLoadingAnimation() {
        layoutEmpty.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.VISIBLE);
        getRecyclerView().setVisibility(View.GONE);
    }

    protected void showList() {
        layoutEmpty.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.GONE);
        getRecyclerView().setVisibility(View.VISIBLE);
    }

    protected void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.GONE);
        getRecyclerView().setVisibility(View.INVISIBLE);
    }

    protected void showError(final Exception e, final long timeInMilliseconds) {
        layoutEmpty.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        layoutLoading.setVisibility(View.GONE);
        getRecyclerView().setVisibility(View.GONE);

        layoutError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatusMessageHandler.showErrorDialog(getContext(), e, timeInMilliseconds);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RecyclerView.LayoutManager layoutManager = getRecyclerView().getLayoutManager();
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            ((StaggeredGridLayoutManager) layoutManager).setSpanCount(getSpanCount());
        } else if (layoutManager instanceof GridLayoutManager) {
            ((GridLayoutManager) layoutManager).setSpanCount(getSpanCount());
        }
    }

    /**
     * Override this Method to set Span Count should be used for different screen resolutions
     *
     * @return span count
     */
    protected abstract int getSpanCount();

    protected abstract void onCreateViewEvent(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    public abstract RecyclerView getRecyclerView();

    public abstract RecyclerView.Adapter getRecyclerViewAdapter();

    public abstract List<T> loadListData() throws Exception;

}