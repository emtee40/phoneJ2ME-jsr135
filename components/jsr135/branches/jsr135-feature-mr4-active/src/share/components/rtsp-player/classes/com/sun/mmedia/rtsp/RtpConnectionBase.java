/*
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.mmedia.rtsp;

import java.io.IOException;
import java.util.Vector;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

public abstract class RtpConnectionBase extends Thread implements Runnable {

    protected static final int MAX_DATAGRAM_SIZE = 4096; // bytes
    protected static final int INITIAL_QUEUE_SIZE = 100; // packets
    protected static final int PACKET_TIMEOUT = 30000; // ms to wait for packet arrival

    public abstract boolean connectionIsAlive();
    public abstract void startListening() throws IOException;
    public abstract void stopListening();
    public abstract RtpPacket receivePacket();

    protected RtspSS ss = null;
    protected int    local_port;

    public RtpConnectionBase(int local_port) {
        this.local_port = local_port;
    }

    public void setSS(RtspSS ss) {
        this.ss = ss;
    }

    public void run() {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI,
                "** RTP thread started...");
        }
        while (connectionIsAlive()) {
            RtpPacket pkt = receivePacket();
            if (null != pkt) {
                if (!enqueuePacket(pkt)) {
                    stopListening();
                    break;
                }
            } else {
                stopListening();
                break;
            }
        }
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI,
                "** RTP thread finished.");
        }
    }

    Vector pkt_queue = new Vector(INITIAL_QUEUE_SIZE);

    public synchronized boolean enqueuePacket(RtpPacket pkt) {
        try {
            pkt_queue.addElement(pkt);
            if (null != ss) ss.packetArrived(pkt);
            notify();
            return true;
        } catch (OutOfMemoryError e) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI,
                    "OutOfMemoryError in RtpConnection.enqueuePacket()");
            }
            return false;
        }
    }

    public synchronized RtpPacket dequeuePacket() throws InterruptedException {
        if (0 == pkt_queue.size()) {
            wait(PACKET_TIMEOUT);
        }
        if (0 != pkt_queue.size()) {
            RtpPacket p = (RtpPacket)pkt_queue.elementAt(0);
            pkt_queue.removeElementAt(0);
            return p;
        } else {
            return null;
        }
    }

    public synchronized int getNumPackets() {
        return pkt_queue.size();
    }
}
