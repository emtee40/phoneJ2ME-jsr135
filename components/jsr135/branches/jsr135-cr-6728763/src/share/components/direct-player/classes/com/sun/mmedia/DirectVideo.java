/*
 * 
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
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

import java.util.*;
import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.media.Control;
import javax.microedition.media.MediaException;
import javax.microedition.media.control.VideoControl;
import javax.microedition.media.PlayerListener;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

/**
 * Common MIDP/LCDUI-based Video Control
 * it implements VideoControl for DirectPlayer.
 */
class DirectVideo implements VideoControl, MIDPVideoPainter {

    private VideoSource source;

/* Need to revisit: these can swap when device screen orientation changes,
 * so they cannot be final 
 */
/* Need to revisit: native methods return 0 */
    private final int SCREEN_WIDTH = 240;//nGetScreenWidth();
    private final int SCREEN_HEIGHT = 240;//nGetScreenHeight();
    private final int DEFAULT_WIDTH = 80;
    private final int DEFAULT_HEIGHT = 80;
    private final int COLOR_KEY = 0x010101;

    // NOTE: You have to calibrate this value carefully
    //       If you increase this value, fake preview quality goes down but, system overhead decrease
    //       If you decrease this value, fake preview quality goes up but, system overhead increase
    //       If you set this value 0 or negative value, fake preview support still image only
    private final static int FAKE_PREVIEW_INTERVAL = 250;
    
    // Canvas and item reference
    private Canvas canvas;
    private DVItem item;

    // original video size    
    private int sw;
    private int sh;

    // Display position and size + temporary values for fullscreen mode
    private int dx, tmp_dx;
    private int dy, tmp_dy;
    private int dw, tmp_dw;
    private int dh, tmp_dh;

    // Fullscreen mode flag
    private boolean fsmode = false;

    // visible?
    private boolean visible = false;
    private boolean hidden = false;
    private boolean started = false;
    private boolean locationInited = false; // Is location initialized?
    
    // current display mode
    private int displayMode = -1;
    // MMHelper to communicate with Canvas
    private MMHelper mmh = null;
    // Lock
    private Object boundLock = new Object();
    
    private static boolean debug = true;
    
    // member functions /////////////////////////////////////////////

    // this is to suppress the default package-private empty constructor
    private DirectVideo() {}
    
    // the only possible way to instantiate this class
    DirectVideo( VideoSource src, int width, int height )
    {
        source  = src;
        sw      = width;
        sh      = height;
        // initialize default rendering width and height
        if (sw <= 0) dw = DEFAULT_WIDTH;
        else dw = sw;
        if (sh <= 0) dh = DEFAULT_HEIGHT;
        else dh = sh;
    }
    
    /**
     * Is in clipping area?
     */
    private boolean isInClippingArea(Graphics g, int x, int y, int w, int h) {
        int diffx = g.getTranslateX();
        int diffy = g.getTranslateY();
        int clipx = g.getClipX();
        int clipy = g.getClipY();
        int clipw = g.getClipWidth();
        int cliph = g.getClipHeight();

        x += diffx;
        y += diffy;
        clipx += diffx;
        clipy += diffy;

        if (x < clipx) return false;
        if (y < clipy) return false;
        if (x + w > clipx + clipw) return false;
        if (y + h > clipy + cliph) return false;

        if (debug) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI, 
                "isInClippingArea return true - No graphic outside of clipping area"); 
        }  

        return true;
    }

    /**
     *
     */
    private void setTranslatedVideoLocation(Graphics g, int x, int y, int w, int h) {
        int diffx = g.getTranslateX();
        int diffy = g.getTranslateY();
        int px, py, pw, ph;
 
        // Calculate positions
        // And, do a physical clipping
        // Currently, Zoran chipset does not support negative position and exceed position
        px = x + diffx;
        py = y + diffy;
        pw = w;
        ph = h;

        if (px + pw <= 0) {
            return;
        }
        if (py + ph <= 0) {
            return;
        }
        if (px >= SCREEN_WIDTH) {
            return;
        }
        if (py >= SCREEN_HEIGHT) {
            return;
        }
        if (px < 0) {
            pw += px;
            px = 0;
        }
        if (py < 0) {
            ph += py;
            py = 0;
        }
        if (px + pw > SCREEN_WIDTH) {
            pw = SCREEN_WIDTH - px;
        }
        if (py + ph > SCREEN_HEIGHT) {
            ph = SCREEN_HEIGHT - py;
        }

        source.setVideoLocation(px, py, pw, ph);
    }

    /**
     * Prepare direct video rendering surface
     */
    private void prepareVideoSurface(Graphics g, int x, int y, int w, int h) {
        if (debug) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI, 
                "prepareVideoSurface " + x + "," + y + "," + w + "," + h); 
        }    
 
        // Turn off color key
        source.setColorKey( false, COLOR_KEY);
        setTranslatedVideoLocation(g, x, y, w, h);

        source.setVideoVisible( !hidden);
    }

    /**
     * Prepare clipped preview region by using color key masking
     */
    private void prepareClippedPreview(Graphics g, int x, int y, int w, int h) {
        if (source.setColorKey( true, COLOR_KEY)) {
            g.setColor(COLOR_KEY);    // IMPL NOTE - Consider RGB565 conversion
            g.fillRect(x, y, w, h);
            setTranslatedVideoLocation(g, x, y, w, h);
            source.setVideoVisible( !hidden);
        } else {
            source.setVideoVisible( false);
        }
    }

    /**
     * request to repaint
     */
    private void repaint() {
        if (canvas != null) {
            canvas.repaint();
        } else if (item != null) {
            item.forcePaint();
        }
    }

    /**
     * Check mode value
     */
    protected void checkState() {
        if (displayMode == -1) {
            throw new IllegalStateException("initDisplayMode not called yet");
        }
    }

    void start() {
        started = true;
        repaint();
    }

    void stop() {
        started = false;
    }
    
    /**
     * Init display mode
     */
    public Object initDisplayMode(int mode, Object container) {
        if (debug) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI, 
                "initDisplayMode mode=" + mode + ", container=" + container); 
        }     
        
        Object ret = null;
        
        if (displayMode != -1) {
            throw new IllegalStateException("mode already set");
        }
        if (mode != USE_DIRECT_VIDEO && mode != USE_GUI_PRIMITIVE) {
            throw new IllegalArgumentException("unsupported mode");
        }
        if (mode == USE_DIRECT_VIDEO && !Canvas.class.isInstance(container)) {
            throw new IllegalArgumentException("container needs to be a Canvas");
        }
        if (mode == USE_GUI_PRIMITIVE && container != null) {
            if (!(container instanceof String)) {
                throw new IllegalArgumentException("container not valid");
            }
            if (!(container.equals("javax.microedition.lcdui.Item"))) {
                throw new IllegalArgumentException("container not valid");
            }
        }

        if (mode == USE_DIRECT_VIDEO) {
            canvas = (Canvas)container;
            if (mmh == null) {
                mmh = MMHelper.getMMHelper();
                if (mmh == null) {
                    throw new RuntimeException("initDisplayMode: unable to set the display mode");
                }
            }
            if (!canvas.isShown()) {
                hidden = true;
            }
            displayMode = mode;
            // register this direct video handler to MMH
            // MMH used to communicate with Canvas
            mmh.registerPlayer(canvas, this);
            setDisplayLocation(dx, dy);
        } else {
            displayMode = mode;
            item = new DVItem(null);
            ret = (Object)item;
            visible = true;
        }
        
        return ret;
    }

    /**
     * Override method in BasicPlayer to close
     * the <code>Player</code>.
     */
    void close() {
        if (mmh != null && canvas != null) {
            // unregister this direct video handler with MMH
            mmh.unregisterPlayer(canvas, this);
        }
    }
    
    /**
     * Set display location 
     */
    public void setDisplayLocation(int x, int y) {
        if (debug) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI, 
                "setDisplayLocation x=" + x + ",y=" + y); 
        }
        checkState();
        if (displayMode == USE_DIRECT_VIDEO) {

            boolean needRepaint = false;

            synchronized(boundLock) {
                if (fsmode) {
                    tmp_dx = x;
                    tmp_dy = y;
                } else {
                    dx = x;
                    dy = y;
                    needRepaint = ( dw != 0 && dh != 0 );
                }
            }

            if( needRepaint ) {
                repaint();
            }
        }
    }
    
    /**
     * Set display size
     */
    public void setDisplaySize(int width, int height) throws MediaException {
        if (debug) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI, 
                "setDisplaySize w=" + width + ",h=" + height); 
        }

        boolean sizeChanged = false;
        
        checkState();

        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("invalid size ("+width+","+height+")");
        }

        synchronized(boundLock) {
            if (fsmode) {
                tmp_dw = width;
                tmp_dh = height;
            } else {
                sizeChanged = ( dw != width || dh != height );
                dw = width;
                dh = height;
            }
        }

        if (item != null) {
            // this will raise sizeChanged event
            // and sizeChanged shall raise paint event also
            item.setPreferredSize( width, height );
        }
        repaint();

        if (sizeChanged) {
            source.notifyDisplaySizeChange();
        }
    }
    
    public int getDisplayX() {
        return dx;
    }
    
    public int getDisplayY() {
        return dy;
    }

    /**
     * Get actual width of rendering 
     */
    public int getDisplayWidth() {
        checkState();
        return dw;
    }
    
    /**
     * Get actual height of rendering
     */
    public int getDisplayHeight() {
        checkState();
        return dh;
    }

    /**
     * return source video width
     */
    public int getSourceWidth() {
        return sw;
    }
    
    /**
     * return source video height
     */
    public int getSourceHeight() {
        return sh;
    }

    /**
     * set visible or unvisible
     */
    public void setVisible(boolean visible) {
        boolean old = this.visible;
        checkState();
        this.visible = visible;

        if (old != visible) {
            repaint();
        }

        if (visible == false) {
            source.setVideoVisible( false);
        }
    }
    
    public void setDisplayFullScreen(boolean fullScreenMode) throws MediaException {

        checkState();

        synchronized( boundLock ) {
            if( fsmode != fullScreenMode ) {
                if( !source.setVideoFullScreen(fullScreenMode) )
                {
                    throw new MediaException( 
                            "Unable to set full-screen mode" );
                }
                fsmode = fullScreenMode;

                if( fsmode ) {
                    tmp_dx = dx;
                    tmp_dy = dy;
                    tmp_dw = dw;
                    tmp_dh = dh;
                } else {
                    if( tmp_dx != dx || tmp_dy != dy ) {
                        setDisplayLocation( tmp_dx, tmp_dy );
                    }
                    if( tmp_dw != dw || tmp_dh != dh ) {
                        setDisplaySize( tmp_dw, tmp_dh );
                    }
                }
            }
        }
    }

    public byte[] getSnapshot( String imageType ) throws MediaException
    {
        checkState();

        if (null == imageType)
        {
            imageType = System.getProperty("video.snapshot.encodings");
            if (null == imageType)
            {
                throw new MediaException( "No supported snapshot formats found" );
            }
            int spacePos = imageType.indexOf(' ');
            if (spacePos > 0)
            {
                imageType = imageType.substring(0, spacePos);
            }
        }
        else
        {
            String supported = System.getProperty( "video.snapshot.encodings" );
            if (null == supported)
            {
                throw new MediaException( "No supported snapshot formats found" );
            }
            int idx = supported.indexOf( imageType );
            if( -1 == idx )
            {
                throw new MediaException( "Snapshot format ('" +
                                          imageType +
                                          "')is not supported" );
            }
        }


        byte[] data = null;
        
        data = source.getVideoSnapshot(imageType.toLowerCase());
        
        if (null == data)
        {
            throw new MediaException( "Snapshot in '" + imageType + "' format failed." );
        }
        return data;
    }

    /**
     * called from Canvas.paint routine
     * We have to paint direct video region on Canvas
     * Notice: This have to be done before device painting action
     * Zoran ESDK use mask color to draw direct video
     */
    public void paintVideo(Graphics g) {
        int x, y, w, h;

        synchronized(boundLock) {
            x = dx;
            y = dy;
            w = dw;
            h = dh;
        }

        if (debug) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI, 
                "paintVideo x=" + x + ",y=" + y + ",w=" + w + ",h=" + h); 
        }

        if (canvas != null && !canvas.isShown()) {
            hidden = true;
        }
        if (!hidden) {
            boolean isOverlapped = false;
            if (mmh != null) {
                isOverlapped = mmh.isDisplayOverlapped(g);
            }
            if (isOverlapped) {
                prepareClippedPreview(g, x, y, w, h);
            } else if (visible && started) {
                prepareVideoSurface(g, x, y, w, h);
            }
        }
    }

    /**
     * Hide video preview (called from CanvasLFImpl)
     */
    public void hideVideo() {
        if (debug) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI, 
                "hideVideoPreview"); 
        }
        source.setVideoVisible( false);
        hidden = true;
        repaint();
    }

    /**
     * Show video preview (called from CanvasLFImpl)
     */
    public void showVideo() {
        if (debug) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI, 
                "showVideoPreview"); 
        }
        hidden = false;
        repaint();
        // IMPL NOTE - we shouldn't show video immediately: it will appear after drawVideo()
    }
    
    // Inner class ///////////////////////////////////////////////////////////

    /**
     * Support USE_GUI_PRIMITIVE mode
     */
    class DVItem extends CustomItem {

       private MMHelper mmh = null;

        DVItem(String label) {
            super(label);
            mmh = MMHelper.getMMHelper();
        }
        
        void forcePaint() {
            repaint();
        }
        
        protected void paint(Graphics g, int w, int h) {
            if (debug) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI, 
                    "DVItem.paint visible=" + visible); 
            }
            if (!hidden) {
                boolean isOverlapped = false;
                if (mmh != null) {
                    isOverlapped = mmh.isDisplayOverlapped(g);
                }
                if (isOverlapped) {
                    prepareClippedPreview(g, 0, 0, w, h);
                } else if (visible) {
                    prepareVideoSurface(g, 0, 0, w, h);
                }
            }
        }
        
        protected int getMinContentWidth() {
            return 1;
        }
        
        protected int getMinContentHeight() {
            return 1;
        }
        
        protected int getPrefContentWidth(int height) {
            return dw;
        }
        
        protected int getPrefContentHeight(int width) {
            return dh;
        }

        protected void sizeChanged(int w, int h) {
            synchronized(boundLock) {
                dw = w;
                dh = h;
            }
            repaint();
        }

        // Now this function used to control visible state of direct video preview
        // Called from MIDPWindow class
        protected void showNotify() {
            if (debug) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI, "showNotify"); 
            }        
            hidden = false;
            repaint();
        }

        // Now this function used to control visible state of direct video preview
        // Called from MIDPWindow class
        protected void hideNotify() {
            if (debug) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI, "hideNotify"); 
            }        
            source.setVideoVisible( false);
            hidden = true;
            repaint();
        }
    }
}
