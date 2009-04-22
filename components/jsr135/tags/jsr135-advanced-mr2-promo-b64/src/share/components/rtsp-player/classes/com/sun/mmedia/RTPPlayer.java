/*
* Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License version
* 2 only, as published by the Free Software Foundation. 
* 
* This program is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* General Public License version 2 for more details (a copy is
* included at /legal/license.txt). 
* 
* You should have received a copy of the GNU General Public License
* version 2 along with this work; if not, write to the Free Software
* Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA 
* 
* Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
* Clara, CA 95054 or visit www.sun.com if you need additional
* information or have any questions. 
*/

package com.sun.mmedia;

import com.sun.mmedia.RTSPPlayer;
import com.sun.mmedia.DirectPlayer;

import javax.microedition.media.PlayerListener;

public class RTPPlayer extends com.sun.mmedia.DirectPlayer
{
    private RTSPPlayer parentPlayer;

    public RTPPlayer(RTSPPlayer parent)
    {
        parentPlayer = parent;
    }

    public void sendEvent( String evt, Object evtData )
    {
        if (null != parentPlayer)
        {
            // IMPL_NOTE:
            //
            // - EOM arrives here from javacall;
            //
            // - VOLUME_CHANGED arrives here from 'our' DirectVolume
            //   because parent RTSPPlayer delegates volume controlling
            //   to child RTPPlayer.
            //
            // These events must be delivered to PlayerListeners of
            // parent RTSPPlayer, so we redirect them. Other events,
            // such as STOPPED or STARTED, must not be forwarded from
            // here, this would be a duplicate because RTSPPlayer
            // generates them by itself.

            if (evt.equals( PlayerListener.END_OF_MEDIA ) ||
                evt.equals( PlayerListener.VOLUME_CHANGED ))
            {
                parentPlayer.sendEvent(evt, evtData);
            }
        }
        else
        {
            super.sendEvent(evt, evtData);
        }
    }
}