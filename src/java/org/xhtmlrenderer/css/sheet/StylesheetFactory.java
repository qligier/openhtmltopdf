/*
 * StylesheetFactory.java
 * Copyright (c) 2004 Torbj�rn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.css.sheet;

import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import com.steadystate.css.parser.CSSOMParser;

import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;


/**
 * A Factory class for Cascading Style Sheets. Sheets are parsed using a single
 * parser instance for all sheets. Sheets are cached by URI using a LRU test,
 * but timestamp of file is not checked.
 *
 * @author   Torbj�rn Gannholm
 */
// ASK: tested for multi-thread access? (PW 12-11-04)
// TODO: add timestamp check (PW 12-11-04)
public class StylesheetFactory {

    /** Description of the Field */
    private CSSOMParser parser = new CSSOMParser();

    /** Description of the Field */
    private int _cacheCapacity = 16;
    
    /** an LRU cache */
    private java.util.LinkedHashMap _cache =
        new java.util.LinkedHashMap( _cacheCapacity, 0.75f, true ) {
            protected boolean removeEldestEntry( java.util.Map.Entry eldest ) {
                return size() > _cacheCapacity;
            }
        };

    /** Creates a new instance of StylesheetFactory */
    public StylesheetFactory() { }

    /**
     * Description of the Method
     *
     * @param origin  PARAM
     * @param reader  PARAM
     * @return        Returns
     */
    public Stylesheet parse( int origin, java.io.Reader reader ) {
        InputSource is = new InputSource( reader );
        CSSStyleSheet style = null;
        try {
            style = parser.parseStyleSheet( is );
        } catch ( java.io.IOException e ) {
            throw new XRRuntimeException( "IOException on parsing style seet from a Reader; don't know the URI.", e );
        }

        return new Stylesheet( style, origin );
    }

    /**
     * Description of the Method
     *
     * @param origin            PARAM
     * @param styleDeclaration  PARAM
     * @return                  Returns
     */
    public Ruleset parseStyleDeclaration( int origin, String styleDeclaration ) {
        try {
            java.io.StringReader reader = new java.io.StringReader( "* {" + styleDeclaration + "}" );
            InputSource is = new InputSource( reader );
            CSSStyleSheet style = parser.parseStyleSheet( is );
            reader.close();
            return new Ruleset( (CSSStyleRule)style.getCssRules().item( 0 ), Stylesheet.AUTHOR );
        } catch ( Exception ex ) {
            throw new XRRuntimeException("Cannot parse style declaration from string.", ex);
        }
    }

    /**
     * Adds a stylesheet to the factory cache. Will overwrite older entry for 
     * same key.
     *
     * @param key    Key to use to reference sheet later; must be unique in factory.
     * @param sheet  The sheet to cache.
     */
    public void putStylesheet( Object key, Stylesheet sheet ) {
        _cache.put( key, sheet );
    }

    /**
     * Returns a cached sheet by its key; null if no entry for that key.
     *
     * @param key  The key for this sheet; same as key passed to putStylesheet();
     * @return     The stylesheet 
     */
    public Stylesheet getStylesheet( Object key ) {
        return (Stylesheet)_cache.get( key );
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2004/11/15 12:42:23  pdoubleya
 * Across this checkin (all may not apply to this particular file)
 * Changed default/package-access members to private.
 * Changed to use XRRuntimeException where appropriate.
 * Began move from System.err.println to std logging.
 * Standard code reformat.
 * Removed some unnecessary SAC member variables that were only used in initialization.
 * CVS log section.
 *
 *
 */
