/*
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
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

class AsyncExecutor {

    protected boolean isBlockedUntilEvent = false;
    protected int result = 0;
    private int outputParam = 0;

    private synchronized void complete() {
        while (isBlockedUntilEvent) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
    }

    public synchronized boolean complete( boolean result ) {
        this.result = result ? 0 : 1;
        complete();
        return 0 == this.result;
    }

    private int getResult() {
        return result;
    }

    private int getOutputParam() {
        return outputParam;
    }

    public synchronized void unblockOnEvent(int result, int outputParam) {
        if( isBlockedUntilEvent ) {
            isBlockedUntilEvent = false;
            this.result = result;
            this.outputParam = outputParam;
            notify();
        }
    }

    static private AsyncExecutor nullAsyncExecutor = null;

    static public AsyncExecutor getNullInstance() {
        if( null == nullAsyncExecutor ) {
            nullAsyncExecutor = new NullInstance();
        }
        return nullAsyncExecutor;
    }

    static private class NullInstance extends AsyncExecutor {
        public boolean complete(boolean result) {
            return result;
        }

        public void unblockOnEvent(int result, int outputParam) {
        }
    }
}
