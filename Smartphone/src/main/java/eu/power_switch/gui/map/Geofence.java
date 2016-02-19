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

package eu.power_switch.gui.map;

import android.support.annotation.ColorInt;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Internal Representation of a Geofence used in MapView component
 * <p/>
 * Created by Markus on 20.01.2016.
 */
public class Geofence {

    /**
     * Marker representing the center point of this Geofence
     */
    private Marker marker;

    /**
     * Circle representing the area of this Geofence
     */
    private Circle circle;

    public Geofence(Marker marker, Circle circle) {
        this.marker = marker;
        this.circle = circle;
    }

    public void setCenter(LatLng latLng) {
        marker.setPosition(latLng);
        circle.setCenter(latLng);
    }

    public void setRadius(double radius) {
        circle.setRadius(radius);
    }

    public void setVisible(boolean visible) {
        marker.setVisible(visible);
        circle.setVisible(visible);
    }

    public void setFillColor(@ColorInt int fillColor) {
        circle.setFillColor(fillColor);
    }

    public void setStrokeColor(@ColorInt int strokeColor) {
        circle.setStrokeColor(strokeColor);
    }

    public void setStrokeWidth(float strokeWidth) {
        circle.setStrokeWidth(strokeWidth);
    }

    public void setZIndex(float zIndex) {
        circle.setZIndex(zIndex);
    }

    public void remove() {
        marker.remove();
        circle.remove();
    }

    public Marker getMarker() {
        return marker;
    }

    public Circle getCircle() {
        return circle;
    }
}
