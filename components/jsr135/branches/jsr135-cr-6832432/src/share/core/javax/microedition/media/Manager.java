/*
 *
 *  Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License version
 *  2 only, as published by the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License version 2 for more details (a copy is
 *  included at /legal/license.txt).
 *  
 *  You should have received a copy of the GNU General Public License
 *  version 2 along with this work; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA
 *  
 *  Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 *  Clara, CA 95054 or visit www.sun.com if you need additional
 *  information or have any questions.
 */
package javax.microedition.media;

import java.io.IOException;
import java.io.InputStream;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;
import com.sun.mmedia.HighLevelPlayer;
import com.sun.mmedia.TonePlayer;
import com.sun.mmedia.Configuration;
import com.sun.mmedia.protocol.*;
import com.sun.mmedia.DefaultConfiguration;

import javax.microedition.media.protocol.*;

/**
 * <code>Manager</code> is the access point for obtaining
 * system dependent resources such as <code>Players</code>
 * for multimedia processing.
 * <p>
 *
 * A <a href="Player.html"<code>Player</code></a>
 * is an object used to
 * control and render media that
 * is specific to the
 * <a href="#content-type">content type</a>
 * of the data.
 * <p>
 * <code>Manager</code> provides access to an implementation specific
 * mechanism for constructing <code>Players</code>.
 * <p>
 * For convenience, <code>Manager</code> also provides a simplified
 * method to generate simple tones.
 *
 * <h2>Simple Tone Generation</h2>
 *
 * <blockquote>
 * The
 * <a href="#playTone(int, int, int)">
 * <code>playTone</code></a>
 * function is defined to generate
 * tones.  Given the note and duration, the function will
 * produce the specified tone.
 * </blockquote>
 *
 * <h2>Creating Players</h2>
 * <blockquote>
 *
 * <code>Manager</code> provides three methods to create a
 * <code>Player</code> for playing back media:
 * <ul>
 * <li> Create from a media locator.
 * <li> Create from a <code>DataSource</code>.
 * <li> Create from an <code>InputStream</code>.
 * </ul>
 * The <code>Player</code> returned can be used to control the
 * presentation of the media.
 * <p>
 *
 * The simplest way to create a <code>Player</code> is from a
 * <a href="#media-locator">locator in the URI syntax</a>.
 * Given a locator,
 * <a href="#createPlayer(java.lang.String)">
 * <code>createPlayer</code></a>
 * will create a <code>Player</code> suitable to handle the media
 * identified by the locator.
 * <p>
 * Users can also implement a custom <code>DataSource</code> to
 * handle an application-defined protocol.
 * The custom <code>DataSource</code> can
 * be used to create a <code>Player</code> by using the
 * <a href="#createPlayer(javax.microedition.media.protocol.DataSource)">
 * <code>createPlayer</code></a> method.
 * <p>
 * A third version of
 * <a href="#createPlayer(java.io.InputStream, java.lang.String)">
 * <code>createPlayer</code></a>
 * creates a <code>Player</code> from an
 * <code>InputStream</code>.  This can be used to interface with
 * other Java API's which use <code>InputStreams</code> such as
 * the java.io package.  It should be noted that <code>InputStream</code>
 * does not provide the necessary random seeking functionality.  So
 * a <code>Player</code> created from an <code>InputStream</code> may
 * not support random seeking (ala <code>Player.setMediaTime</code>).
 * </blockquote>
 *
 * <h2>System Time Base</h2>
 *
 * <blockquote>
 * All <code>Players</code> need a <code>TimeBase</code>. Many
 * use a system-wide <code>TimeBase</code>, often based on
 * a time-of-day clock.
 * <code>Manager</code> provides access to the system <code>TimeBase</code>
 * through
 * <a href="#getSystemTimeBase()">
 * <code>getSystemTimeBase</code></a>.
 * </blockquote>
 *
 * <a name="content-type"></a>
 * <h2>Content Types</h2>
 * <blockquote>
 * Content types identify the type of media data.  They are
 * defined to be the registered MIME types
 * (<a href=
 * "http://www.iana.org/assignments/media-types/">
 * http://www.iana.org/assignments/media-types/</a>);
 * plus
 * some user-defined types that generally follow the MIME syntax
 * (<a href="ftp://ftp.isi.edu/in-notes/rfc2045.txt">RFC 2045</a>,
 * <a href="ftp://ftp.isi.edu/in-notes/rfc2046.txt">RFC 2046</a>).
 * <p>
 * For example, here are a few common content types:
 * <ol>
 * <li>Wave audio files: <code>audio/x-wav</code>
 * <li>AU audio files: <code>audio/basic</code>
 * <li>MP3 audio files: <code>audio/mpeg</code>
 * <li>MIDI files: <code>audio/midi</code>
 * <li>Tone sequences: <code>audio/x-tone-seq</code>
 * <li>MPEG video files: <code>video/mpeg</code>
 * </ol>
 * </blockquote>
 *
 * <a name="delivery-protocol"></a>
 * <h2>Data Delivery Protocol</h2>
 * <blockquote>
 * A data delivery protocol specifies how media data is
 * delivered to the media processing systems.  Some common
 * protocols are: local file, disk I/O, HTTP, RTP streaming,
 * live media capture etc.
 * <p>
 * <a href="#media-locator">Media locators</a> are used to
 * identify the delivery protocol
 * (as well as the identifier/name of the media).
 * </blockquote>
 *
 * <a name="media-locator"></a>
 * <h2>Media Locator</h2>
 * <blockquote>
 * <a name="media-protocol"></a>
 * Media locators are specified in
 * <a href="http://www.ietf.org/rfc/rfc2396.txt">URI syntax</a>
 * which is defined in the form:
 * <p>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;scheme&gt;:&lt;scheme-specific-part&gt;
 * <p>
 * The "scheme" part of the locator string identifies the name
 * of the protocol being used to deliver the data.
 * <p>
 * Some media specific locator syntax are defined below:
 *
 * <a name="live-capture"></a>
 * <h3>1. Locators for Live-media Capture</h3>
 * The locators for capturing live media are defined
 * by the following syntax in
 * <a href="http://www.ietf.org/rfc/rfc2234">Augmented BNF</a> notations:
 * <p>
 * <pre>
 *     "capture://" device [ "?" media_encodings ]
 * </pre>
 * &nbsp;&nbsp;To identify the type or the specific
 * name of the device:<p>
 * <pre>
 *     device       = "audio" / "video" / "audio_video" / dev_name
 *     dev_name     = alphanumeric
 *     alphanumeric = 1*( ALPHA / DIGIT )
 * </pre>
 * The syntax for specifying the media encodings are defined in
 * <a href="Manager.html#media_encodings">Media Encoding Strings</a>.<br>
 *   <br>
 *   &nbsp;&nbsp;&nbsp;&nbsp;Examples:
 *   <br>
 * <pre>
 *     capture://audio  (default audio)
 *     capture://audio?encoding=pcm   (default audio in PCM format)
 *     capture://devmic0?encoding=pcm&rate=11025&bits=16&channels=1
 *             (audio from a specific device--devmic0)
 *     <br>
 *     capture://video  (default video)
 *     capture://video?encoding=gray8&width=160&height=120
 *     capture://devcam0?encoding=rgb888&width=160&height=120&fps=7
 *     <br>
 *     capture://mydev?myattr=123   (custom device with custom param)
 * </pre>
 *
 * <h3>2. Locators for RTP streaming</h3>
 * <a href="http://www.ietf.org/rfc/rfc1889.txt">RTP</a>
 *  is a public standard for streaming media.  The locator
 * syntax for specifying RTP sessions is:
 * <pre>
 *     "rtp://" address [ ":" port ] [ "/" type ]
 * </pre>
 * &nbsp;&nbsp;&nbsp;&nbsp;where:
 * <pre>
 *     address and port defines the RTP session.  The
 *     address and port usage is similar to the host and port
 *     usage as defined in the <a href="http://www.ietf.org/rfc/rfc2396.txt">URI syntax</a>.
 *     <br>
 *     type = "audio" / "video" / "text"
 * </pre>
 *   <br>
 *   &nbsp;&nbsp;&nbsp;&nbsp;Example:
 *   <br>
 * <pre>
 *     rtp://224.1.2.3:12344/audio
 * </pre>
 *
 * <h3>3. Locators for Radio Tuner</h3>
 * To create a <code>Player</code> to tune into a radio program, the
 * following locator syntax is used:
 * <p>
 * <pre>
 *     "capture://radio" [ "?" tuner_params ]
 * </pre>
 * &nbsp;&nbsp;&nbsp;&nbsp;where:
 * <pre>
 *     tuner_params = tuner_param *( "&" tuner_param )
 *     tuner_param  = "f=" freq /
 *                    "mod=" modulation /
 *                    "st=" stereo_mode /
 *                    "id=" program_id /
 *                    "preset=" preset
 *     freq         = megahertz /
 *                    kilohertz /
 *                    hertz
 *     megahertz    = pos_integer "M" /
 *                    pos_integer "." pos_integer "M"
 *     kilohertz    = pos_integer "k" /
 *                    pos_integer "." pos_integer "k"
 *     hertz        = pos_integer
 *     modulation   = "fm" / "am"
 *     stereo_mode  = "mono" / "stereo" / "auto"
 *     program_id   = alpanumeric ; identifies an FM channel by its
 *                                  program service name (PS) delivered
 *                                  via Radio Data System (RDS)**.
 *     preset       = pos_integer ; predefined tuning number
 * </pre>
 *   ** The RDS specification is available from
 *   <a href="http://bsonline.techindex.co.uk">http://bsonline.techindex.co.uk</a>, id BSEN 50067:1998.<br>
 *   <br>
 *   &nbsp;&nbsp;&nbsp;&nbsp;Examples:
 *   <br>
 * <pre>
 *     capture://radio?f=91.9M&st=auto
 *               (91.9 MHz with automatic stereo setting)
 *     capture://radio?f=558k&mod=am
 *               (558 kHz with amplitude modulation)
 *     capture://radio?id=yleq
 *               (FM channel that has "yleq" as its program service name
 *                delivered via Radia Data System)
 * </pre>
 * </blockquote>
 * <p>
 *
 * <a name="media_encodings"></a>
 * <h2>Media Encoding Strings</h2>
 * <blockquote>
 * There are a few places where media encodings are specified
 * as strings, e.g. in the capture media locator.  Sections A to E
 * define the encoding syntax.
 * <a href="#encodings_rules">Section F</a>
 * defines the rules for how they should be handled.
 * <p>
 *
 * &nbsp;&nbsp;A. Describing media encodings:<p>
 * <pre>
 *     media_encodings = audio_encodings /
 *                       video_encodings /
 *                       mixed_encodings /
 *                       custom_encodings
 * </pre>
 *
 * <a name="audio_encodings"></a>
 * &nbsp;&nbsp;B. Describing the audio encodings:<p>
 * <pre>
 *     audio_encodings = audio_enc_param *( "&" audio_param )
 *     audio_enc_param = "encoding=" audio_enc
 *     audio_enc       = "pcm" / "ulaw" / "gsm" / content_type
 *     audio_param     = "rate=" rate /
 *                       "bits=" bits /
 *                       "channels=" channels /
 *                       "endian=" endian /
 *                       "signed=" signed /
 *                       "type=" audio_type
 *     rate            = "96000" / "48000" / "44100" /
 *                       "22050" / "16000" / "11025" /
 *                       "8000" / other_rate
 *     other_rate      = pos_integer
 *     bits            = "8" / "16" / "24" / other_bits
 *     other_bits      = pos_integer
 *     channels        = pos_integer
 *     endian          = "little" / "big"
 *     signed          = "signed" / "unsigned"
 *     audio_type      = bitrate_variable / other_type
 *     other_type      = alphanumeric
 *     pos_integer     = 1*DIGIT
 *
 *   and
 *     <a href="#content-type">content type</a> is given in the MIME syntax.
 * </pre>
 *   <br>
 *   &nbsp;&nbsp;&nbsp;&nbsp;Example:
 *   <br>
 * <pre>
 *     encoding=pcm&rate=11025&bits=16&channels=1
 * </pre>
 *
 * <a name="video_encodings"></a>
 * &nbsp;&nbsp;C. Describing the video or image encodings:<p>
 * <pre>
 *     video_encodings   = video_enc_param *( "&" video_param )
 *     video_enc_param   = "encoding=" video_enc
 *     video_enc         = "gray8" / "rgb888" / "bgr888" /
 *                         "rgb565" / "rgb555" / "yuv444" /
 *                         "yuv422" / "yuv420" / "jpeg" / "png" /
 *                         content_type
 *     video_param       = "width=" width /
 *                         "height=" height /
 *                         "fps=" fps /
 *                         "colors=" colors /
 *                         "progressive=" progressive /
 *                         "interlaced=" interlaced /
 *                         "type=" video_type
 *     width             = pos_integer
 *     height            = pos_integer
 *     fps               = pos_number
 *     quality           = pos_integer
 *     colors            = "p" colors_in_palette /
 *                       = "rgb" r_bits g_bits b_bits /
 *                       = "gray" gray_bits
 *     colors_in_palette = pos_integer
 *     r_bits            = pos_integer
 *     g_bits            = pos_integer
 *     b_bits            = pos_integer
 *     gray_bits         = pos_integer
 *     progressive       = boolean
 *     video_type        = jfif / exif / other_type
 *     other_type        = alphanumeric
 *     interlaced        = boolean
 *     pos_number        = 1*DIGIT [ "." 1*DIGIT ]
 *     boolean           = "true" / "false"
 *
 *   and
 *     <a href="#content-type">content type</a> is given in the MIME syntax.
 * </pre>
 *   <br>
 *   &nbsp;&nbsp;&nbsp;&nbsp;Examples:
 *   <br>
 * <pre>
 *     encoding=gray8&width=160&height=120
 *     encoding=jpeg&quality=80&progressive=true&type=jfif
 *         (progressive JPEG with quality 80 in jfif format)
 *     encoding=jpeg&type=exif
 *         (JPEG in exif format)
 *     encoding=png&colors=rgb888
 *         (24 bits/pixel PNG)
 *     encoding=rgb888
 *         (raw 24-bit rgb image)
 *     encoding=rgb&colors=rgb888
 *         (raw 24-bit rgb image)
 * </pre>
 *
 * <a name="mixed_params"></a>
 * &nbsp;&nbsp;D. Describing mixed audio and video encodings:<p>
 * <pre>
 *     mixed_encodings = audio_encodings "&" video_encodings
 * </pre>
 *   <br>
 *   &nbsp;&nbsp;&nbsp;&nbsp;Example:
 *   <br>
 * <pre>
 *     encoding=pcm&encoding=gray8&width=160&height=160
 * </pre>
 *
 * &nbsp;&nbsp;E. Describing custom media encodings:<p>
 * <pre>
 *     custom_encodings = custom_enc_param *( "&" param )
 *     custom_enc_param = "encoding=" value
 *     param            = key "=" value
 *     key              = alphanumeric
 *     value            = alphanumeric
 * </pre>
 *
 * <a name="encodings_rules"></a>
 * &nbsp;&nbsp;F. Rules for handling the encodings strings:<p>
 * <ul>
 * <li> If a given parameter is a custom parameter and is not recognizable
 *      by the implementation, the parameter is treated as an illegal
 *      parameter and the method must throw an appropriate Exception
 *      to denote that.
 * <li> If the value for a given parameter is incorrect because it is
 *      syntactically wrong or illegal (e.g. out of range),
 *      the method must throw an appropriate Exception to denote that.
 * </ul>
 *
 * @created    January 13, 2005
 * @see        javax.microedition.media.protocol.DataSource
 * @see        Player
 * @see        TimeBase
 */

public final class Manager {

    private static Configuration config = Configuration.getConfiguration();
    private static TonePlayer tonePlayer;
    
    /**
     * The locator to create a tone <code>Player</code>
     * to play back tone sequences.  For example,
     * <pre>
     * try {
     *     Player p = Manager.createPlayer(Manager.TONE_DEVICE_LOCATOR);
     *     p.realize();
     *     ToneControl tc = (ToneControl)p.getControl("ToneControl");
     *     tc.setSequence(mySequence);
     *     p.start();
     * } catch (IOException ioe) {
     * } catch (MediaException me) {}
     * </pre>
     *
     * If a tone sequence is not set on the tone
     * <code>Player</code> via its <code>ToneControl</code>,
     * the <code>Player</code> does not carry any
     * sequence.  <code>getDuration</code> returns 0 for this
     * <code>Player</code>.
     * <p>
     * The content type of the <code>Player</code> created from this
     * locator is <code>audio/x-tone-seq</code>.
     * <p>
     * A <code>Player</code> for this locator may not be supported
     * for all implementations.
     * <p>
     * Value "device://tone" is assigned to <code>TONE_DEVICE_LOCATOR</code>.
     */
    public final static String TONE_DEVICE_LOCATOR = "device://tone";

    /**
     * The locator to create a MIDI <code>Player</code>
     * which gives access to the MIDI device by making
     * {@link javax.microedition.media.control.MIDIControl MIDIControl}
     * available.  For example,
     * <pre>
     * try {
     *     Player p = Manager.createPlayer(Manager.MIDI_DEVICE_LOCATOR);
     *     p.prefetch(); // opens the MIDI device
     *     MIDIControl m = (MIDIControl)p.getControl("MIDIControl");
     * } catch (IOException ioe) {
     * } catch (MediaException me) {}
     * </pre>
     *
     * The MIDI <code>Player</code> returned does not carry any
     * media data.  <code>getDuration</code> returns 0 for this
     * <code>Player</code>.
     * <p>
     * The content type of the <code>Player</code> created from this
     * locator is <code>audio/midi</code>.
     * <p>
     * A <code>Player</code> for this locator may not be supported
     * for all implementations.
     * <p>
     * Value "device://midi" is assigned to <code>MIDI_DEVICE_LOCATOR</code>.
     */
    public final static String MIDI_DEVICE_LOCATOR = "device://midi";
    
    private static String DS_ERR = "Cannot create a DataSource for: ";
    private static String PL_ERR = "Cannot create a Player for: ";

    /**
     * This private constructor keeps anyone from actually
     * getting a <CODE>Manager</CODE>.
     */
    private Manager() { }


    /**
     * Return the list of supported content types for the given protocol.
     * <p>
     * See <a href="#content-type">content types</a> for the syntax
     * of the content types returned.
     * See <a href="#media-protocol">protocol name</a> for the syntax
     * of the protocol used.
     * <p>
     * For example, if the given <code>protocol</code>
     * is <code>"http"</code>,
     * then the supported content types that can be played back
     * with the <code>http</code> protocol will be returned.
     * <p>
     * If <code>null</code> is passed in as the <code>protocol</code>,
     * all the supported content types for this implementation
     * will be returned.  The returned array must be non-empty.
     * <p>
     * If the given <code>protocol</code> is an invalid or
     * unsupported protocol, then an empty array will be returned.
     *
     * @param  protocol  The input protocol for the supported content types.
     * @return           The list of supported content types for the given protocol.
     */
    public static String[] getSupportedContentTypes(String protocol) {
        return config.getSupportedContentTypes(protocol);        
    }


    /**
     * Return the list of supported protocols given the content
     * type.  The protocols are returned
     * as strings which identify what locators can be used for creating
     * <code>Player</code>'s.
     * <p>
     * See <a href="#media-protocol">protocol name</a> for the syntax
     * of the protocols returned.
     * See <a href="#content-type">content types</a> for the syntax
     * of the content type used.
     * <p>
     * For example, if the given <code>content_type</code>
     * is <code>"audio/x-wav"</code>, then the supported protocols
     * that can be used to play back <code>audio/x-wav</code>
     * will be returned.
     * <p>
     * If <code>null</code> is passed in as the
     * <code>content_type</code>,
     * all the supported protocols for this implementation
     * will be returned.  The returned array must be non-empty.
     * <p>
     * If the given <code>content_type</code> is an invalid or
     * unsupported content type, then an empty array will be returned.
     *
     * @param  content_type  The content type for the supported protocols.
     * @return               The list of supported protocols for the given content type.
     */
    public static String[] getSupportedProtocols(String content_type) {
        return config.getSupportedProtocols(content_type);
    }


    /**
     * Create a <code>Player</code> from an input locator.
     *
     * @param  locator                       A locator string in URI syntax that describes
     * the media content.
     * @return                               A new <code>Player</code>.
     * @exception  MediaException            Thrown if a <code>Player</code> cannot
     * be created for the given locator.
     * @exception  IOException               Thrown if there was a problem connecting
     * with the source pointed to by the <code>locator</code>.
     */
    public static Player createPlayer(String locator)
         throws IOException, MediaException {

        if (locator == null) {
            throw new IllegalArgumentException();
        }

        String locStr = locator.toLowerCase();

        
        if( !isAudioDeviceFound && ( locStr.equals( MIDI_DEVICE_LOCATOR ) || 
                                     locStr.equals( TONE_DEVICE_LOCATOR ) ) )
        {
            throw new MediaException( "No audio device found. Check your" +
                    " audio driver settings" );
        }

        /* Verify if Protocol is supported */
        String theProtocol = null;
        boolean found = false;
        int idx = locStr.indexOf(':');

        if (idx != -1) {
            theProtocol = locStr.substring(0, idx);
        } else {
            throw new MediaException("Malformed locator");
        }

        if (locStr.startsWith(DefaultConfiguration.RADIO_CAPTURE_LOCATOR))
        {
            if (!config.isRadioSupported())
            {
                throw new MediaException( "Radio Capture is not supported" );
            }
            parseRadioLocatorStr(locator);
        }
        else if (locStr.startsWith(DefaultConfiguration.AUDIO_CAPTURE_LOCATOR) ||
            locStr.startsWith(DefaultConfiguration.VIDEO_CAPTURE_LOCATOR))
        {
            // separate device & encodings
            int encInd = locator.indexOf('?');
            String encStr = null;
            if (encInd > 0) {
                locStr = locStr.substring(0, encInd);
                idx = locator.indexOf("encoding=");
                if (idx != -1) {
                    encStr = locator.substring(idx+9);
                    if (encStr != null) {
                        idx = encStr.indexOf('&');
                        if (idx > 0) {
                            encStr = encStr.substring(0, idx);
                        }
                        encStr = encStr.toLowerCase();
                    }
                }

            }
            found = true;
            String encodings = null;
            if (locStr.equals(DefaultConfiguration.AUDIO_CAPTURE_LOCATOR)) {
                String supported = System.getProperty("supports.audio.capture");
                encodings = System.getProperty("audio.encodings");
                if (supported == null || supported.equals("false") || encodings == null) {
                    found = false;
                }
            } else if (locStr.equals(DefaultConfiguration.VIDEO_CAPTURE_LOCATOR)) {
                String supported = System.getProperty("supports.video.capture");
                encodings = System.getProperty("video.encodings");
                if (supported == null || supported.equals("false") || encodings == null) {
                    found = false;
                }
            }
            if (encStr != null && encodings != null && encodings.indexOf(encStr) == -1) {
                found = false;
            }
            if (!found) {
                throw new MediaException("Player cannot be created for " + locator);
            }
        } else {
            String supportedProtocols[] = getSupportedProtocols(null);
            for (int i = 0; i < supportedProtocols.length && !found; i++) {
                if (theProtocol.equals(supportedProtocols[i])) {
                    found = true;
                }
            }

            if (!found) {
                throw new MediaException("Player cannot be created for " + locator + 
                                        " Unsupported protocol " + theProtocol);
            }
        }

        DataSource ds = createDataSource(locator);
        Player pp = null;

        try {
            pp = createPlayer(ds);
        } catch (MediaException ex) {
            ds.disconnect();
            throw ex;
        } catch (IOException ex) {
            ds.disconnect();
            throw ex;
        }
        return pp;
    }

    private static void parseRadioLocatorStr( String locator ) throws MediaException
    {
        final int prefixLen = 
                DefaultConfiguration.RADIO_CAPTURE_LOCATOR.length();
        
        if( null == locator )
        {
            throw new MediaException( "radio locator is null" );
        }
        if( !locator.startsWith( DefaultConfiguration.RADIO_CAPTURE_LOCATOR ) )
        {
            throw new MediaException( "bad radio locator" );
        }
        
        if ( locator.length() == prefixLen )
        {
            return;
        }
        
        if( locator.charAt( prefixLen ) != '?' )
        {
            throw new MediaException( "bad radio locator" );
        }
        String params = locator.substring( prefixLen + 1 );
        parseRadioParamsString( params );
    }

    private static void parseRadioParamsString( String params ) throws MediaException
    {
        boolean foundFreq       = false;
        boolean foundMod        = false;
        boolean foundStereoMode = false;
        boolean foundId         = false; 
        boolean foundPreset     = false;
        
        int i = 0, j = -1; 
        do {
            j = params.indexOf( '&', i );
            String param = j < 0 ? params.substring( i ) : params.substring( i,
                    j );
            if( param.startsWith( "f=" ) )
            {
                if( foundFreq )
                {
                    throw new MediaException( "frequency is set more than" +
                            " once in the radio locator string" );
                }
                parseRadioFreqParam( param );
                foundFreq = true;
            }
            else if ( param.startsWith( "mod=" ) )
            {
                if( foundMod )
                {
                    throw new MediaException( "modulation is set more than" +
                            " once in the radio locator string" );
                }
                parseRadioModParam( param );
                foundMod = true;
            }
            else if ( param.startsWith( "st=" ) )
            {
                if( foundStereoMode )
                {
                    throw new MediaException( "stereo mode is set more than" +
                            " once in the radio locator string" );
                }
                parseRadioStereoParam( param );
                foundStereoMode = true;
            }
            else if ( param.startsWith( "id=" ) )
            {
                if( foundId )
                {
                    throw new MediaException( "Program ID is set more than" +
                            " once in the radio locator string" );
                }
                parseRadioIdParam( param );
                foundId = true;
            }
            else if ( param.startsWith( "preset=" ) )
            {
                if( foundPreset )
                {
                    throw new MediaException( "Preset is set more than" +
                            " once in the radio locator string" );
                }
                parseRadioPresetParam( param );
                foundPreset = true;
            }
            else
            {
                throw new MediaException( "Unknown parameter in the" +
                        " radio locator string" );
            }

            i = j + 1;
        } while( j >= 0 );
        
    }
    
    private static void parseRadioFreqParam( String s )
        throws MediaException
    {
        String param = s;
        if( 'M' == param.charAt( param.length() - 1 ) || 
            'k' == param.charAt( param.length() - 1 ) )
        {
            param = param.substring( 0, param.length() - 1 );
        }
        try {
            if( 0 >= Float.parseFloat( param.substring( 2 ) ) )
            {
                throw new MediaException( "Frequency is not positive" +
                        " in the radio locator string" );
            }
        } catch (NumberFormatException e)
        {
            throw new MediaException( "Frequency is not numeric or too big" +
                    " in the radio locator string" );
        }
    }
    
    private static void parseRadioModParam( String param )
        throws MediaException
    {
        String mod = param.substring( "mod=".length() );
        if( !mod.equals( "am" ) && !mod.equals( "fm" ) )
        {
            throw new MediaException( "Unknown modulation in the" +
                    " radio locator string parameters" );
        }
    }
    
    private static void parseRadioStereoParam( String param )
        throws MediaException
    {
        String mode = param.substring( "st=".length() );
        if( !mode.equals( "mono" ) && 
            !mode.equals( "stereo" ) && 
            !mode.equals( "auto" ))
        {
            throw new MediaException( "Unknown stereo mode in the" +
                    " radio locator string parameters" );
        }
    }

    private static void parseRadioIdParam( String param )
        throws MediaException
    {
        String id = param.toLowerCase().substring( "id=".length() );
        if( 0 == id.length() )
        {
            throw new MediaException( "Empty Program ID name in" +
                    " the radio locator string parameters" );
        }
        
        for( int i = 0; i < id.length(); i++ )
        {
            char ch = id.charAt( i );
            if( !Character.isLowerCase( ch ) &&
                !Character.isDigit( ch ) )
            {
                throw new MediaException( "Not an alphanumeric Program" +
                        " ID in the radio locator string parameters" );
            }
        }
    }

    private static void parseRadioPresetParam( String param )
        throws MediaException
    {
        String preset = param.substring( "preset=".length() );
        try {
            if( 0 >= Byte.parseByte( preset ) )
            {
                throw new MediaException( "Preset number is not positive" +
                        " in the radio locator string" );
            }
        } catch (NumberFormatException e)
        {
            throw new MediaException( "Preset number is not numeric" +
                    " or too big in the radio locator string" );
        }
    }

    /**
     * Create a <code>Player</code> to play back media from an
     * <code>InputStream</code>.
     * <p>
     * The <code>type</code> argument
     * specifies the content-type of the input media.  If
     * <code>null</code> is given, <code>Manager</code> will
     * attempt to determine the type.  However, since determining
     * the media type is non-trivial for some media types, it
     * may not be feasible in some cases.  The
     * <code>Manager</code> may throw a <code>MediaException</code>
     * to indicate that.
     *
     * @param  stream                        The <code>InputStream</code> that delivers the
     * input media.
     * @param  type                          The <code>ContentType</code> of the media.
     * @return                               A new <code>Player</code>.
     * @exception  MediaException            Thrown if a <code>Player</code> cannot
     * be created for the given stream and type.
     * @exception  IOException               Thrown if there was a problem reading data
     * from the <code>InputStream</code>.
     */
    public static Player createPlayer(InputStream stream, String type)
         throws IOException, MediaException {

        if (stream == null) {
            throw new IllegalArgumentException();
        }

        if (type == null) {
            throw new MediaException(PL_ERR + "cannot determine the media type");
        }
        
        type = type.toLowerCase();

        if( !isAudioDeviceFound && type != null && type.startsWith( "audio" ) )
        {
            throw new MediaException( "No audio device found. Check your" +
                    " audio driver settings" );
        }
            
        // Wrap the input stream with a CommonDS where the input
        // can be handled in a generic way.
        
        CommonDS ds = new CommonDS();
        ds.setInputStream(stream);
        ds.setContentType(type);
        
        try {
            return createPlayer(ds);
        } catch (IOException ex) {
            throw new MediaException(PL_ERR + ex.getMessage());
        }
    }


    /**
     * Play back a tone as specified by a note and its duration.
     * A note is given in the range of 0 to 127 inclusive.  The frequency
     * of the note can be calculated from the following formula:
     * <pre>
     *     SEMITONE_CONST = 17.31234049066755 = 1/(ln(2^(1/12)))
     *     note = ln(freq/8.176)*SEMITONE_CONST
     *     The musical note A = MIDI note 69 (0x45) = 440 Hz.
     * </pre>
     * This call is a non-blocking call. Notice that this method may
     * utilize CPU resources significantly on devices that don't
     * have hardware support for tone generation.
     *
     * @param  note                          Defines the tone of the note as specified by the
     * above formula.
     * @param  duration                      The duration of the tone in milli-seconds.
     * Duration must be positive.
     * @param  volume                        Audio volume range from 0 to 100.  100 represents
     * the maximum
     * volume at the current hardware level.  Setting the volume to a
     * value less
     * than 0 will set the volume to 0.  Setting the volume to greater than
     * 100 will set the volume to 100.
     * @exception  MediaException            Thrown if the tone cannot be played
     * due to a device-related problem.
     */
    public static void playTone(int note, int duration, int volume)
         throws MediaException {
             
        if (note < 0 || note > 127 || duration <= 0) {
            throw new IllegalArgumentException( "Invalid note(" + note +
                                                ") or duration (" + duration + ")" );
        }

        if (volume < 0) {
            volume = 0;
        } else if (volume > 100) {
            volume = 100;
        }

        if (duration == 0 || volume == 0) {
            return;
        }

        if( !isAudioDeviceFound )
        {
            throw new MediaException( "No audio device found. Check your" +
                        " audio driver settings" );
        }

        if (tonePlayer == null) {
            tonePlayer = config.getTonePlayer();
        }
        
        if (tonePlayer != null) {
            int res = 
            tonePlayer.playTone(note, duration, volume);
            
            if( TonePlayer.RESULT_FAIL == res )
            {
                throw new MediaException( "Failed to play tone" );
            }
            
            if( TonePlayer.RESULT_NO_AUDIO_DEVICE == res )
            {
                isAudioDeviceFound = false;
                throw new MediaException( "No audio device found. Check your" +
                        " audio driver settings" );
            }
            
            
        } else {
            throw new MediaException("no tone player");
        }
    }

    private static boolean isAudioDeviceFound = true;

    /**
     * MMAPI full specific methods.
     *
     * @param  source              Description of the Parameter
     * @return                     Description of the Return Value
     * @exception  IOException     Description of the Exception
     * @exception  MediaException  Description of the Exception
     */

    /**
     * Create a <code>Player</code> for a <code>DataSource</code>.
     *
     * @param  source                        The <CODE>DataSource</CODE> that provides
     * the media content.
     * @return                               A new <code>Player</code>.
     * @exception  MediaException            Thrown if a <code>Player</code> cannot
     * be created for the given <code>DataSource</code>.
     * @exception  IOException               Thrown if there was a problem connecting
     * with the source.
     */
    public static Player createPlayer(DataSource source)
        throws IOException, MediaException
    {
        if (source == null) {
            throw new IllegalArgumentException();
        }

        String type;

        try {
            type = source.getContentType();
        } catch( IllegalStateException e ) {
            type = null;
        }

        if (type != null) {
            String theProtocol = null;
            String locator = source.getLocator();
            if (locator != null) {
                int idx = locator.indexOf(':');
                if (idx != -1) {
                    theProtocol = locator.substring(0, idx);
                }
            }
            String supportedContentTypes[] = getSupportedContentTypes(theProtocol);
            boolean found = false;
            for(int i=0; i<supportedContentTypes.length && !found; i++) {
                if (type.equals(supportedContentTypes[i])) {
                    found = true;
                }
            }
            if (!found) {
                throw new MediaException("Player cannot be created for " + type);
            }
        }

        HighLevelPlayer p = new HighLevelPlayer(source);

        return p;
    }


    /**
     * Create a <code>DataSource</code> for the specified media
     * identified by a locator.  The <code>DataSource</code>
     * returned can be used to read the media data from the input
     * source.
     * <p>
     * The returned data source is <i>connected</i>;
     * <code>DataSource.connect</code> has been invoked.
     * <p>
     * If no suitable <code>DataSource</code> can be found to
     * handle the input, a <CODE>MediaException</CODE>
     * is thrown.
     *
     * @param  locator             The source protocol for the media data.
     * @return                     A connected <CODE>DataSource</CODE>.
     * @exception  MediaException  Thrown if no <CODE>DataSource</CODE>
     * can be found that supports the given protocol as specified by
     * the locator.
     * @exception  IOException     Thrown if there was a problem connecting
     * with the source (e.g. the source media does not exist).
     */
    private static DataSource createDataSource(String locator)
         throws IOException, MediaException {

        String className = config.getProtocolHandler(BasicDS.getProtocol(locator));

        if (className == null) {
            throw new MediaException(DS_ERR + locator);
        }

        try {

            // ... Try to create a DataSource instance ...
            Class protoClass = Class.forName(className);
            DataSource source = (DataSource) protoClass.newInstance();
            // ... and get it connected ...
            ((BasicDS) source).setLocator(locator);
            if (locator.equals(TONE_DEVICE_LOCATOR)) {
                ((BasicDS) source).setContentType("audio/x-tone-seq");
            } else if (locator.equals(MIDI_DEVICE_LOCATOR)) {
                ((BasicDS) source).setContentType("audio/midi");
            }
            return source;
        } catch (MediaException e) {
            throw e;
        } catch (Exception e) {
            throw new MediaException(DS_ERR + e.getMessage());
        }
    }

    private static TimeBase sysTimeBase = null;

    /**
     * Get the time-base object for the system.
     *
     * @return    The system time base.
     */
    public static TimeBase getSystemTimeBase() {
        if (sysTimeBase == null) {
            sysTimeBase = new SystemTimeBase();
        }

        return sysTimeBase;
    }
}

/**
 * SystemTimeBase is the implementation of the default <CODE>TimeBase</CODE>
 * based on the system clock.
 *
 * @see        TimeBase
 */
class SystemTimeBase implements TimeBase {
    /*
     *  Pick some offset (start-up time) so the system time won't be
     *  so huge.  The huge numbers overflow floating point operations
     *  in some cases.
     */
    private static long offset = System.currentTimeMillis() * 1000L;

    /**
     * This is a straight-forward implementation of a
     * system time base using the system clock.
     *
     * @return    The time value
     */
    public long getTime() {
        return (System.currentTimeMillis() * 1000L) - offset;
    }
}
