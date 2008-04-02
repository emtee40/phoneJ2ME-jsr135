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

package com.sun.mmedia.protocol;

/**
 * A <CODE>ContentDescriptor</CODE> identifies media data containers.
 *
 * @see SourceStream
 */

public class ContentDescriptor {

    private String encoding;

    /**
     * Obtain a string that represents the content type
     * for this descriptor.
     * If the content type is not known, <code>null</code> is returned.
     *
     * @return The content type.
     */
    public String getContentType() {
	return encoding;
    }

    /** 
     * Create a content descriptor with the specified content type.
     *
     * @param contentType The content type of this descriptor.
     * If <code>contentType</code> is <code>null</code>, the type 
     * of the content is unknown.
     */
    public ContentDescriptor(String contentType) {
	encoding = contentType;
    }
}
