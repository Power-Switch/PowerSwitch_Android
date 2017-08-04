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

package eu.power_switch.gui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.event.PreferenceChangedEvent;
import eu.power_switch.event.SceneDataChangedEvent;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.gui.adapter.SceneRecyclerViewAdapter;
import eu.power_switch.gui.animation.SnappingLinearLayoutManager;

/**
 * Fragment holding all scenes
 * <p/>
 * Created by Markus on 07.06.2016.
 */
public class ScenesFragment extends FragmentBase {

    @BindView(R.id.scenes_recyclerView)
    RecyclerView scenesRecyclerView;

    @BindView(R.id.layoutLoading)
    LinearLayout layoutLoading;
    @BindView(R.id.layoutEmpty)
    LinearLayout layoutEmpty;

    private SceneRecyclerViewAdapter sceneRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        layoutEmpty.setVisibility(View.GONE);

        sceneRecyclerViewAdapter = new SceneRecyclerViewAdapter(getActivity(),
                scenesRecyclerView,
                MainActivity.sceneList,
                dataApiHandler,
                wearablePreferencesHandler);
        scenesRecyclerView.setAdapter(sceneRecyclerViewAdapter);

        SnappingLinearLayoutManager layoutManager = new SnappingLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        scenesRecyclerView.setLayoutManager(layoutManager);

        if (MainActivity.isInitialized()) {
            refreshUI();
        }

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_scenes;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onSceneDataChanged(SceneDataChangedEvent e) {
        refreshUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onPreferenceChanged(PreferenceChangedEvent e) {
        refreshUI();
    }

    private void refreshUI() {
        sceneRecyclerViewAdapter.notifyDataSetChanged();

        if (MainActivity.sceneList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            scenesRecyclerView.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            scenesRecyclerView.setVisibility(View.VISIBLE);
        }
        layoutLoading.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (dataApiHandler != null) {
            dataApiHandler.connect();
        }
    }

    @Override
    public void onStop() {
        if (dataApiHandler != null) {
            dataApiHandler.disconnect();
        }

        super.onStop();
    }
}
