
package org.dvbviewer.controller.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * A simple parser for INI files.
 */
public class INIParser {

  private LinkedHashMap<String, Properties> mSections;


  /**
   * Creates a new <code>INIParser</code> instance from the INI file at the
   * given path, which is assumed to be in the <code>UTF-8</code> charset.
   *
   * @param iniString the ini content
   * @throws IOException if there is a problem reading the given String.
   */
  public INIParser(String iniString) throws IOException {
    initFromString(iniString);
  }

  /**
   * Creates a new <code>INIParser</code> instance from the given file.
   * <code>aCharset</code> specifies the character encoding of the file.
   *
   * @param aFile INI file to parse
   * @param aCharset character encoding of file
   * @throws FileNotFoundException if <code>aFile</code> does not exist.
   * @throws IOException if there is a problem reading the given file.
   */
  public INIParser(File aFile, Charset aCharset)
          throws IOException {
    initFromFile(aFile, aCharset);
  }

  /**
   * Creates a new <code>INIParser</code> instance from the given file,
   * which is assumed to be in the <code>UTF-8</code> charset.
   *
   * @param aFile INI file to parse
   * @throws FileNotFoundException if <code>aFile</code> does not exist.
   * @throws IOException if there is a problem reading the given file.
   */
  public INIParser(File aFile) throws IOException {
    initFromFile(aFile, Charset.forName("UTF-8"));
  }

  /**
   * Parses given INI file.
   *
   * @param aFile INI file to parse
   * @param aCharset character encoding of file
   * @throws FileNotFoundException if <code>aFile</code> does not exist.
   * @throws IOException if there is a problem reading the given file.
   */
  private void initFromFile(File aFile, Charset aCharset)
          throws IOException {
    FileInputStream fileStream = new FileInputStream(aFile);
    InputStreamReader inStream = new InputStreamReader(fileStream, aCharset);
    BufferedReader reader = new BufferedReader(inStream);

    initFromReader(reader);
  }

  private void initFromString(String string)
          throws IOException {
    StringReader inStream = new StringReader(string);
    BufferedReader reader = new BufferedReader(inStream);

    initFromReader(reader);
  }

  private void initFromReader(BufferedReader reader) throws IOException {
    mSections = new LinkedHashMap<String, Properties>();
    String currSection = null;
    String line;
    while ((line = reader.readLine()) != null) {
      // skip empty lines and comment lines
      String trimmedLine = line.trim();
      if (trimmedLine.length() == 0 || trimmedLine.startsWith("#")
              || trimmedLine.startsWith(";")) {
        continue;
      }

      // Look for section headers (i.e. "[Section]").
      if (line.startsWith("[")) {
        /*
         * We are looking for a well-formed "[Section]".  If this header is
         * malformed (i.e. "[Section" or "[Section]Moretext"), just skip it
         * and go on to next well-formed section header.
         */
        if (!trimmedLine.endsWith("]") ||
                trimmedLine.indexOf("]") != (trimmedLine.length() - 1)) {
          currSection = null;
          continue;
        }

        // remove enclosing brackets
        currSection = trimmedLine.substring(1, trimmedLine.length() - 1);
        continue;
      }

      // If we haven't found a valid section header, continue to next line
      if (currSection == null) {
        continue;
      }

      StringTokenizer tok = new StringTokenizer(line, "=");
      if (tok.countTokens() < 2) { // looking for value pairs
        continue;
      }

      Properties props = mSections.get(currSection);
      if (props == null) {
        props = new Properties();
        mSections.put(currSection, props);
      }
      props.setProperty(tok.nextToken(), tok.nextToken());
    }

    reader.close();
  }

  /**
   * Returns an iterator over the section names available in the INI file.
   *
   * @return an iterator over the section names
   */
  public Iterator getSections() {
    return mSections.keySet().iterator();
  }

  /**
   * Returns an iterator over the keys available within a section.
   *
   * @param aSection section name whose keys are to be returned
   * @return an iterator over section keys, or <code>null</code> if no
   *          such section exists
   */
  public Iterator getKeys(String aSection) {
    /*
     * Simple wrapper class to convert Enumeration to Iterator
     */
    class PropertiesIterator implements Iterator {
      private Enumeration e;

      public PropertiesIterator(Enumeration aEnum) {
        e = aEnum;
      }

      public boolean hasNext() {
        return e.hasMoreElements();
      }

      public Object next() {
        return e.nextElement();
      }

      public void remove() {
        return;
      }
    }

    Properties props = mSections.get(aSection);
    if (props == null) {
      return null;
    }

    return new PropertiesIterator(props.propertyNames());
  }

  /**
   * Gets the string value for a particular section and key.
   *
   * @param aSection a section name
   * @param aKey the key whose value is to be returned.
   * @return string value of particular section and key
   */
  public String getString(String aSection, String aKey) {
    Properties props = mSections.get(aSection);
    if (props == null) {
      return null;
    }

    return props.getProperty(aKey);
  }

}