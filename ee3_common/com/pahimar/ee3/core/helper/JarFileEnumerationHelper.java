package com.pahimar.ee3.core.helper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * JarFileEnumerationHelper
 * 
 * This Enumeration class can be used to iterate over single subdirectories of jar files
 * 
 * @author Robotic-Brain
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */
public class JarFileEnumerationHelper implements Enumeration<JarEntry> {
    
    /** Subdirectory to search */
    private String subDir;
    
    /** Jar file object */
    private JarFile jar;
    
    /** Keeps track of real iterator */
    private Enumeration<JarEntry> iter;
    
    /** since we may have to skip some elements: get next element and return previous (see nextElement)*/
    private JarEntry nextElem;
    
    
    /**
     * Initializes Enumerator with given URL
     * resourceURL must point to a subdirectory inside a jar
     * 
     * @param resourceURL (e.g. jar:file:/path/to/jar/myJar.jar!/some/directory/inside/jar/)
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public JarFileEnumerationHelper(URL resourceURL) throws IllegalArgumentException, IOException {
        
        try {
            if (!resourceURL.getProtocol().equals("jar")) {
                throw new IllegalArgumentException("URL protocol is not 'jar:'");
            }
            
            String jarFileString;
            
            // Getting Jar file Object
            if (resourceURL.getPath().indexOf("!") != -1) {
                jarFileString = resourceURL.getPath().substring(5, resourceURL.getPath().indexOf("!"));
                subDir = resourceURL.getPath().substring(resourceURL.getPath().indexOf("!") + 2);
            } else {
                jarFileString = resourceURL.getPath().substring(5);
                subDir = "";
            }
            jar = new JarFile(URLDecoder.decode(jarFileString, "UTF-8"));
            iter = jar.entries();
            
            // prime iterator
            getNext();
            
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    
    /**
     * Advances the internal Enumeration to the next entry
     * skips all entries which are not part of given subdirectory
     */
    private void getNext() {
        
        while (iter.hasMoreElements()) {
            nextElem = iter.nextElement();
            
            // Only walk specific Directory
            if (nextElem.getName().startsWith(subDir)) {
                return;
            }
        }
        
        // if reached this point - no more elements left
        nextElem = null;
    }
    
    /**
     * @return true if more elements available
     */
    @Override
    public boolean hasMoreElements() {
        return (nextElem != null);
    }
    
    
    /*public String nextElement() {
        
        String result = jar.getName() + "/" + nextJarElement().getName();
        LogHelper.log(Level.INFO, "result is: " + result);
        getNext();
        return result;
    }*/
    
    
    /**
     * Returns the next element in given subdirectory
     */
    @Override
    public JarEntry nextElement() {
        
        if (!hasMoreElements()) {
            throw new NoSuchElementException();
        }
        
        JarEntry result = nextElem;
        getNext();
        return result;
    }
    
    /**
     * checks if a given entry represents a directory
     * (only works if entry was returned by nextElement() )
     * 
     * @param entry
     * @return true if directory
     */
    public boolean isEntryDir(JarEntry entry) {
        String path = getRelativePath(entry);
        return (path.trim().isEmpty() || path.indexOf('/') >= 0);
    }
    
    /**
     * checks if a given entry represents a file
     * (only works if entry was returned by nextElement() )
     * 
     * @param entry
     * @return true if file
     */
    public boolean isEntryFile(JarEntry entry) {
        return !isEntryDir(entry);
    }
    
    /**
     * Returns the relative name of this entry - e.g. /some/path/this-is-returned.xml
     * (only works if entry was returned by nextElement() )
     * 
     * @param entry
     * @return relative name - if entry is a file this returns the last part of the path
     */
    public String getRelativePath(JarEntry entry) {
        return entry.getName().substring(subDir.length());
    }
    
}
