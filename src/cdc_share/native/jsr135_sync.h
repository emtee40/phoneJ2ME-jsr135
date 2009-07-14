/*
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

#ifndef __jsr135_sync_H__
#define __jsr135_sync_H__

#include "javacall_defs.h"

void DoLockAudioMutex();
void UnlockAudioMutex();

#define LockAudioMutex() \
   CVMD_gcSafeExec(_ee, { \
       DoLockAudioMutex(); \
   })

typedef enum AsyncEventType_enum {
    ASYNC_EVENT_MMAPI = 0,
    ASYNC_EVENT_AMMS
} AsyncEventType;

javacall_result mmapi_thread_suspend(int desc, AsyncEventType type, javacall_result *pStatus,
    void **pData);
javacall_result mmapi_thread_resume(int desc, AsyncEventType type, javacall_result status,
    void *data);

#endif
