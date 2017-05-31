/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.adapter.file;

import org.apache.calcite.util.Source;
import org.apache.calcite.util.Sources;

import org.jsoup.select.Elements;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;

/**
 * Unit tests for FileReader.
 */

public class FileReaderTest {

  private static final Source CITIES_SOURCE =
      Sources.url("http://en.wikipedia.org/wiki/List_of_United_States_cities_by_population");

  private static final Source STATES_SOURCE =
      Sources.url(
          "http://en.wikipedia.org/wiki/List_of_states_and_territories_of_the_United_States");

  /** Converts a path that is relative to the module into a path that is
   * relative to where the test is running. */
  public static String file(String s) {
    if (new File("file").exists()) {
      return "file/" + s;
    } else {
      return s;
    }
  }

  /** Tests {@link FileReader} URL instantiation - no path. */
  @Test public void testFileReaderUrlNoPath() throws FileReaderException {
    Assume.assumeTrue(FileSuite.hazNetwork());
    FileReader t = new FileReader(STATES_SOURCE);
    t.refresh();
  }

  /** Tests {@link FileReader} URL instantiation - with path. */
  @Ignore("[CALCITE-1789] Wikipedia format change breaks file adapter test")
  @Test public void testFileReaderUrlWithPath() throws FileReaderException {
    Assume.assumeTrue(FileSuite.hazNetwork());
    FileReader t =
        new FileReader(CITIES_SOURCE,
            "#mw-content-text > table.wikitable.sortable", 0);
    t.refresh();
  }

  /** Tests {@link FileReader} URL fetch. */
  @Test public void testFileReaderUrlFetch() throws FileReaderException {
    Assume.assumeTrue(FileSuite.hazNetwork());
    FileReader t =
        new FileReader(STATES_SOURCE,
            "#mw-content-text > table.wikitable.sortable", 0);
    int i = 0;
    for (Elements row : t) {
      i++;
    }
    assertThat(i, is(51));
  }

  /** Tests failed {@link FileReader} instantiation - malformed URL. */
  @Test public void testFileReaderMalUrl() throws FileReaderException {
    try {
      final Source badSource = Sources.url("bad" + CITIES_SOURCE.path());
      fail("expected exception, got " + badSource);
    } catch (RuntimeException e) {
      assertThat(e.getCause(), instanceOf(MalformedURLException.class));
      assertThat(e.getCause().getMessage(), is("unknown protocol: badhttp"));
    }
  }

  /** Tests failed {@link FileReader} instantiation - bad URL. */
  @Test(expected = FileReaderException.class)
  public void testFileReaderBadUrl() throws FileReaderException {
    final String uri =
        "http://ex.wikipedia.org/wiki/List_of_United_States_cities_by_population";
    FileReader t = new FileReader(Sources.url(uri), "table:eq(4)");
    t.refresh();
  }

  /** Tests failed {@link FileReader} instantiation - bad selector. */
  @Test(expected = FileReaderException.class)
  public void testFileReaderBadSelector() throws FileReaderException {
    final Source source =
        Sources.file(null, file("target/test-classes/tableOK.html"));
    FileReader t = new FileReader(source, "table:eq(1)");
    t.refresh();
  }

  /** Test {@link FileReader} with static file - headings. */
  @Test public void testFileReaderHeadings() throws FileReaderException {
    final Source source =
        Sources.file(null, file("target/test-classes/tableOK.html"));
    FileReader t = new FileReader(source);
    Elements headings = t.getHeadings();
    assertTrue(headings.get(1).text().equals("H1"));
  }

  /** Test {@link FileReader} with static file - data. */
  @Test public void testFileReaderData() throws FileReaderException {
    final Source source =
        Sources.file(null, file("target/test-classes/tableOK.html"));
    FileReader t = new FileReader(source);
    Iterator<Elements> i = t.iterator();
    Elements row = i.next();
    assertTrue(row.get(2).text().equals("R0C2"));
    row = i.next();
    assertTrue(row.get(0).text().equals("R1C0"));
  }

  /** Tests {@link FileReader} with bad static file - headings. */
  @Test public void testFileReaderHeadingsBadFile() throws FileReaderException {
    final Source source =
        Sources.file(null, file("target/test-classes/tableNoTheadTbody.html"));
    FileReader t = new FileReader(source);
    Elements headings = t.getHeadings();
    assertTrue(headings.get(1).text().equals("H1"));
  }

  /** Tests {@link FileReader} with bad static file - data. */
  @Test public void testFileReaderDataBadFile() throws FileReaderException {
    final Source source =
        Sources.file(null, file("target/test-classes/tableNoTheadTbody.html"));
    FileReader t = new FileReader(source);
    Iterator<Elements> i = t.iterator();
    Elements row = i.next();
    assertTrue(row.get(2).text().equals("R0C2"));
    row = i.next();
    assertTrue(row.get(0).text().equals("R1C0"));
  }

  /** Tests {@link FileReader} with no headings static file - data. */
  @Test public void testFileReaderDataNoTh() throws FileReaderException {
    final Source source =
        Sources.file(null, file("target/test-classes/tableNoTH.html"));
    FileReader t = new FileReader(source);
    Iterator<Elements> i = t.iterator();
    Elements row = i.next();
    assertTrue(row.get(2).text().equals("R0C2"));
  }

  /** Tests {@link FileReader} iterator with static file, */
  @Test public void testFileReaderIterator() throws FileReaderException {
    System.out.println(new File("").getAbsolutePath());
    final Source source =
        Sources.file(null, file("target/test-classes/tableOK.html"));
    FileReader t = new FileReader(source);
    Elements row = null;
    for (Elements aT : t) {
      row = aT;
    }
    assertFalse(row == null);
    assertTrue(row.get(1).text().equals("R2C1"));
  }

}

// End FileReaderTest.java