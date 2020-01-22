package com.Devlex.iWifiFileTransfer;

/*
Changes:
3/22/17 - set wwroot and tmpdir;  added: wait/notify

*/

//package com.Devlex.iWifiFileTransfer;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.commons.io.FileUtils;

import android.util.Log;

import java.util.*;


import com.Devlex.iWifiFileTransfer.misc.AsyncTask;
import com.Devlex.iWifiFileTransfer.model.DocumentInfo;

import static android.content.ContentValues.TAG;
import static com.Devlex.iWifiFileTransfer.S.htm;


import static com.Devlex.iWifiFileTransfer.WF.context;
import static com.Devlex.iWifiFileTransfer.WF.copy;
import static com.Devlex.iWifiFileTransfer.WF.isWritable;


public class SWS extends NH {
  /**    public abstract void onDocumentPicked(DocumentInfo doc);

   * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
   */

  public static String msg;

    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {{
      put("css", "application/octet-stream");
      put("htm","application/x-texinfo");
      put("html", "application/x-texinfo");
      put("xml", "text/xml");
      put("java", "text/x-java-source, text/java");
      put("txt", "application/x-texinfo");
      put("asc", "application/x-texinfo");
      put("gif", "image/gif");
      put("jpg", "image/jpeg");
      put("jpeg", "image/jpeg");
      put("png", "image/png");
      put("mp3", "audio/mpeg");
      put("m3u", "audio/mpeg-url");
      put("mp4", "video/mp4");
      put("ogv", "video/ogg");
      put("flv", "video/x-flv");
      put("mov", "video/quicktime");
      put("swf", "application/x-shockwave-flash");
      put("js", "application/javascript");
      put("pdf", "application/pdf");
      put("doc", "application/msword");
      put("ogg", "application/x-ogg");
      put("zip", "application/octet-stream");
      put("exe", "application/octet-stream");
      put("class", "application/octet-stream");
      put("ai", "application/postscript");
      put("aif", "audio/x-aiff");
      put("aifc", "audio/x-aiff");
      put("aiff", "audio/x-aiff");
      put("asc", "application/x-texinfo");
      put("asf", "video/x.ms.asf");
      put("asx", "video/x.ms.asx");
      put("au", "audio/basic");
      put("avi", "video/x-msvideo");
      put("bcpio", "application/x-bcpio");
      put("bin", "application/octet-stream");
      put("cab", "application/x-cabinet");
      put("cdf", "application/x-netcdf");
      put("class", "application/java-vm");
      put("cpio", "application/x-cpio");
      put("cpt", "application/mac-compactpro");
      put("crt", "application/x-x509-ca-cert");
      put("csh", "application/x-csh");
      put("css", "text/css");
      put("csv", "text/comma-separated-values");
      put("dcr", "application/x-director");
      put("dir", "application/x-director");
      put("dll", "application/x-msdownload");
      put("dms", "application/octet-stream");
      put("doc", "application/msword");
      put("dtd", "application/xml-dtd");
      put("dvi", "application/x-dvi");
      put("dxr", "application/x-director");
      put("eps", "application/postscript");
      put("etx", "text/x-setext");
      put("exe", "application/octet-stream");
      put("ez", "application/andrew-inset");
      put("docx", "application/octet-stream");
      put("gif", "image/gif");
      put("gtar", "application/x-gtar");
      put("gz", "application/gzip");
      put("gzip", "application/gzip");
      put("hdf", "application/x-hdf");
      put("htc", "text/x-component");
      put("hqx", "application/mac-binhex40");
      put("html", "application/x-texinfo");
      put("htm", "application/x-texinfo");
      put("ice", "x-conference/x-cooltalk");
      put("ief", "image/ief");
      put("iges", "model/iges");
      put("igs", "model/iges");
      put("jar", "application/java-archive");
      put("java", "application/x-texinfo");
      put("jnlp", "application/x-java-jnlp-file");
      put("jpeg", "image/jpeg");
      put("jpe", "image/jpeg");
      put("jpg", "image/jpeg");
      put("js", "application/x-javascript");
      put("jsp", "application/x-texinfo");
      put("kar", "audio/midi");
      put("latex", "application/x-latex");
      put("lha", "application/octet-stream");
      put("lzh", "application/octet-stream");
      put("man", "application/x-troff-man");
      put("mathml", "application/mathml+xml");
      put("me", "application/x-troff-me");
      put("mesh", "model/mesh");
      put("mid", "audio/midi");
      put("midi", "audio/midi");
      put("mif", "application/vnd.mif");
      put("mol", "chemical/x-mdl-molfile");
      put("movie", "video/x-sgi-movie");
      put("mov", "video/quicktime");
      put("mp2", "audio/mpeg");
      put("mp3", "audio/mpeg");
      put("mpeg", "video/mpeg");
      put("mpe", "video/mpeg");
      put("mpga", "audio/mpeg");
      put("mpg", "video/mpeg");
      put("ms", "application/x-troff-ms");
      put("msh", "model/mesh");
      put("msi", "application/octet-stream");
      put("nc", "application/x-netcdf");
      put("oda", "application/oda");
      put("ogg", "application/ogg");
      put("pbm", "image/x-portable-bitmap");
      put("pdb", "chemical/x-pdb");
      put("pdf", "application/pdf");
      put("pgm", "image/x-portable-graymap");
      put("pgn", "application/x-chess-pgn");
      put("png", "image/png");
      put("pnm", "image/x-portable-anymap");
      put("ppm", "image/x-portable-pixmap");
      put("ppt", "application/vnd.ms-powerpoint");
      put("ps", "application/postscript");
      put("qt", "video/quicktime");
      put("ra", "audio/x-pn-realaudio");
      put("ra", "audio/x-realaudio");
      put("ram", "audio/x-pn-realaudio");
      put("ras", "image/x-cmu-raster");
      put("rdf", "application/rdf+xml");
      put("rgb", "image/x-rgb");
      put("rm", "audio/x-pn-realaudio");
      put("roff", "application/x-troff");
      put("rpm", "application/x-rpm");
      put("rpm", "audio/x-pn-realaudio");
      put("rtf", "application/rtf");
      put("rtx", "text/richtext");
      put("ser", "application/java-serialized-object");
      put("sgml", "text/sgml");
      put("sgm", "text/sgml");
      put("sh", "application/x-sh");
      put("shar", "application/x-shar");
      put("silo", "model/mesh");
      put("sit", "application/x-stuffit");
      put("skd", "application/x-koan");
      put("skm", "application/x-koan");
      put("skp", "application/x-koan");
      put("skt", "application/x-koan");
      put("smi", "application/smil");
      put("smil", "application/smil");
      put("snd", "audio/basic");
      put("spl", "application/x-futuresplash");
      put("src", "application/x-wais-source");
      put("sv4cpio", "application/x-sv4cpio");
      put("sv4crc", "application/x-sv4crc");
      put("svg", "image/svg+xml");
      put("swf", "application/x-shockwave-flash");
      put("t", "application/x-troff");
      put("tar", "application/x-tar");
      put("tar.gz", "application/x-gtar");
      put("tcl", "application/x-tcl");
      put("tex", "application/x-tex");
      put("texi", "application/x-texinfo");
      put("texinfo", "application/x-texinfo");
      put("tgz", "application/x-gtar");
      put("tiff", "image/tiff");
      put("tif", "image/tiff");
      put("tr", "application/x-troff");
      put("tsv", "text/tab-separated-values");
      put("txt", "application/x-texinfo");
      put("ustar", "application/x-ustar");
      put("vcd", "application/x-cdlink");
      put("vrml", "model/vrml");
      put("vxml", "application/voicexml+xml");
      put("wav", "audio/x-wav");
      put("wbmp", "image/vnd.wap.wbmp");
      put("wmlc", "application/vnd.wap.wmlc");
      put("wmlsc", "application/vnd.wap.wmlscriptc");
      put("wmls", "text/vnd.wap.wmlscript");
      put("wml", "text/vnd.wap.wml");
      put("wrl", "model/vrml");
      put("wtls-ca-certificate", "application/vnd.wap.wtls-ca-certificate");
      put("xbm", "image/x-xbitmap");
      put("xht", "application/xhtml+xml");
      put("xhtml", "application/xhtml+xml");
      put("xls", "application/vnd.ms-excel");
      put("xml", "application/xml");
      put("xpm", "image/x-xpixmap");
      put("xpm", "image/x-xpixmap");
      put("xsl", "application/xml");
      put("xslt", "application/xslt+xml");
      put("xul", "application/vnd.mozilla.xul+xml");
      put("xwd", "image/x-xwindowdump");
      put("xyz", "chemical/x-xyz");
      put("z", "application/compress");
      put("zip", "application/zip");
      put("apk", "application/octet-stream");

  }};

  /**
   * The distribution licence
   */
  private static final String LICENCE =
          "Copyright (c) 2012-2013 by Paul S. Hawke, 2001,2005-2013 by Jarno Elonen, 2010 by Konstantinos Togias\n"
                  + "\n"
                  + "Redistribution and use in source and binary forms, with or without\n"
                  + "modification, are permitted provided that the following conditions\n"
                  + "are met:\n"
                  + "\n"
                  + "Redistributions of source code must retain the above copyright notice,\n"
                  + "this list of conditions and the following disclaimer. Redistributions in\n"
                  + "binary form must reproduce the above copyright notice, this list of\n"
                  + "conditions and the following disclaimer in the documentation and/or other\n"
                  + "materials provided with the distribution. The name of the author may not\n"
                  + "be used to endorse or promote products derived from this software without\n"
                  + "specific prior written permission. \n"
                  + " \n"
                  + "THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\n"
                  + "IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n"
                  + "OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n"
                  + "IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\n"
                  + "INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\n"
                  + "NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\n"
                  + "DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\n"
                  + "THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n"
                  + "(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\n"
                  + "OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";

  private final File rootDir;
  private final boolean quiet;
    String brand;
    public String html;
    String manufacturer;
    String model;
    MessageDisplayer msgDsplyr;
    String product;

  public SWS(MessageDisplayer md, String host, int port, File wwwroot, boolean quiet) {
      super(host, port);
      this.rootDir = wwwroot;
      this.quiet = quiet;
      this.manufacturer = Build.MANUFACTURER;
      this.brand = Build.BRAND;
      this.product = Build.PRODUCT;
      this.model = Build.MODEL;
      msgDsplyr = md; // save reference to where we can show messages
      System.out.println("SWS host="+host +", port="+port + ", root="+wwwroot); //<<<<<<<
  }



  File getRootDir() {
      return rootDir;
  }

  /**
   * URL-encodes everything between "/"-characters. Encodes spaces as '%20' instead of '+'.
   */
  private String encodeUri(String uri) {
      String newUri = "";
      StringTokenizer st = new StringTokenizer(uri, "/ ", true);
      while (st.hasMoreTokens()) {
          String tok = st.nextToken();
          if (tok.equals("/"))
              newUri += "/";
          else if (tok.equals(" "))
              newUri += "%20";
          else {
              try {
                  newUri += URLEncoder.encode(tok, "UTF-8");
              } catch (UnsupportedEncodingException ignored) {
              }
          }
      }
      return newUri;
  }

  /**  =================================================================================================================
   * Serves file from homeDir and its' subdirectories (only). Uses only URI, ignores all headers and HTTP parameters.
   */
  Response serveFile(String uri, Map<String, String> header, File homeDir) {
      Response res = null;

      // Make sure we won't die of an exception later
      if (!homeDir.isDirectory()) {
          res = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NH.MIME_PLAINTEXT, "INTERNAL ERRROR: serveFile(): given homeDir is not a directory: "
                               + homeDir);                        //<<<<
      }
      System.out.println("SWS serveFile() homeDir="+homeDir +", uri="+uri); //<<<<<<<<<<

      if (res == null) {
          // Remove URL arguments
          uri = uri.trim().replace(File.separatorChar, '/');
          if (uri.indexOf('?') >= 0)
              uri = uri.substring(0, uri.indexOf('?'));
          System.out.println("SWS new uri="+uri); //<<<<<<<<<<<<<<

          // Prohibit getting out of current directory
          if (uri.startsWith("src/main") || uri.endsWith("src/main") || uri.contains("../"))
              res = newFixedLengthResponse(Response.Status.FORBIDDEN, NH.MIME_PLAINTEXT, "FORBIDDEN: Won't serve ../ for security reasons.");
      }

      File f = new File(homeDir, uri);
//      System.out.println("homeDir="+homeDir +", uri="+uri); //<<<<<<<<<<
      // f=D:\www\NanoServerTesting\servlet\SaveFile
      if (res == null && !f.exists()) {
          if(uri.contains("StopServer")) {
            res = newFixedLengthResponse(Response.Status.NOT_FOUND, NH.MIME_PLAINTEXT, "Error 999, Server stopping.");
            stop();
            synchronized(waitMonitor) {
               waitMonitor.notify(); //<<<< let main exit
            }
            return res;
          }else {
        	 System.out.println(">>SWS File not found f="+f+"<"); //<<<<<<<<<<<
             System.out.println(">>SWS homeDir="+homeDir +", uri="+uri); //<<<<<<<<<<
             res = newFixedLengthResponse(Response.Status.NOT_FOUND, NH.MIME_PLAINTEXT, "Error 404, file not found.");
          }
      }

      // List the directory, if necessary
      if (res == null && f.isDirectory()) {
          // Browsers get confused without '/' after the
          // directory, send a redirect.
          if (!uri.endsWith("/")) {
              uri += "/";
              res = newFixedLengthResponse(Response.Status.REDIRECT, NH.MIME_HTML,
            		             "<html><body>Redirected: <a href=\"" + uri + "\">" + uri
                                 + "</a></body></html>");
              res.addHeader("Location", uri);
          }

          if (res == null) {
              // First try index.html and index.htm
              if (new File(f, "index.html").exists()) {
                  f = new File(homeDir, uri + "/index.html");
              } else if (new File(f, "index.htm").exists()) {
                  f = new File(homeDir, uri + "/index.htm");
              } else if (f.canRead()) {
                  // No index file, list the directory if it is readable
                  res = newFixedLengthResponse(listDirectory(uri, f));
              } else {
                  res = newFixedLengthResponse(Response.Status.FORBIDDEN, NH.MIME_PLAINTEXT, "FORBIDDEN: No directory listing.");
              }
          }
      }

      try {
          if (res == null) {
              // Get MIME type from file name extension, if possible
              String mime = null;
              int dot = f.getCanonicalPath().lastIndexOf('.');
              if (dot >= 0) {
                  mime = MIME_TYPES.get(f.getCanonicalPath().substring(dot + 1).toLowerCase());
              }
              if (mime == null) {
                  mime = NH.MIME_HTML;
              }

              // Calculate etag
              String etag = Integer.toHexString((f.getAbsolutePath() + f.lastModified() + "" + f.length()).hashCode());

              // Support (simple) skipping:
              long startFrom = 0;
              long endAt = -1;
              String range = header.get("range");
              if (range != null) {
                  if (range.startsWith("bytes=")) {
                      range = range.substring("bytes=".length());
                      int minus = range.indexOf('-');
                      try {
                          if (minus > 0) {
                              startFrom = Long.parseLong(range.substring(0, minus));
                              endAt = Long.parseLong(range.substring(minus + 1));
                          }
                      } catch (NumberFormatException ignored) {
                      }
                  }
              }

              // Change return code and add Content-Range header when skipping is requested
              long fileLen = f.length();
              if (range != null && startFrom >= 0) {
                  if (startFrom >= fileLen) {
                      res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, NH.MIME_PLAINTEXT, "");
                      res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                      res.addHeader("ETag", etag);
                  } else {
                      if (endAt < 0) {
                          endAt = fileLen - 1;
                      }
                      long newLen = endAt - startFrom + 1;
                      if (newLen < 0) {
                          newLen = 0;
                      }

                      final long dataLen = newLen;
                      FileInputStream fis = new FileInputStream(f) {
                          @Override
                          public int available() throws IOException {
                              return (int) dataLen;
                          }
                      };
                      fis.skip(startFrom);

                      res = newChunkedResponse(Response.Status.PARTIAL_CONTENT, mime, fis);
                      res.addHeader("Content-Length", "" + dataLen);
                      res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                      res.addHeader("ETag", etag);
                  }
              } else {
                  if (etag.equals(header.get("if-none-match")))
                      res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                  else {
                      res = newChunkedResponse(Response.Status.OK, mime, new FileInputStream(f));
                      res.addHeader("Content-Length", "" + fileLen);
                      res.addHeader("ETag", etag);
                  }
              }
          }
      } catch (IOException ioe) {
          res = newFixedLengthResponse(Response.Status.FORBIDDEN, NH.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
      }

      res.addHeader("Accept-Ranges", "bytes"); // Announce that the file server accepts partial content requestes
      return res;
  }
    public static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals("mounted");
    }

    public static String getAvailableInternalMemorySize() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        return formatSize(((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize()));
    }

    public static String getTotalInternalMemorySize() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        return formatSize(((long) stat.getBlockCount()) * ((long) stat.getBlockSize()));
    }



    public static String getTotalExternalMemorySize() {
        if (!externalMemoryAvailable()) {
            return null;
        }
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return formatSize(((long) stat.getBlockCount()) * ((long) stat.getBlockSize()));
    }
    public static String formatSize(long size) {
        String suffix = null;
        if (size >= FileUtils.ONE_KB) {
            suffix = "KB";
            size /= FileUtils.ONE_KB;
            if (size >= FileUtils.ONE_KB) {
                suffix = "MB";
                size /= FileUtils.ONE_KB;
            }
        }
        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
        for (int commaOffset = resultBuffer.length() - 3; commaOffset > 0; commaOffset -= 3) {
            resultBuffer.insert(commaOffset, ',');
        }
        if (suffix != null) {
            resultBuffer.append(suffix);
        }
        return resultBuffer.toString();
    }

    //=====================================================================================================
  private String listDirectory(String uri, File f) {
	  System.out.println("SWS listDir uri="+uri + ", f="+f); //<<<<<<<<<<<<<<
      String heading = uri;
      msg = htm + "            <div id=\"tabs-1\" class=\"ui-tabs-panel ui-widget-content ui-corner-all\">\n               <div class=\"menu\">\n                  <ul id=\"navin\">\n                     <!-- CSS Tabs -->\n";
      String pat = this.rootDir + heading;
      String pat2 = "/storage/emulated/0/";
      if (pat.equals(pat2)) {
          msg = msg + "                     <li><a href=\"/\">Internal Storage</a></li>\n                     <li><a href=\"/DCIM/\">My Photos</a></li>\n                     <li><a href=\"/Pictures/\">My Pictures</a></li>\n                     <li><a href=\"/Music/\">My Music</a></li>\n                     <li><a href=\"/DCIM/\">My Videos</a></li>\n";
      } else {
          msg = msg + "                     <li><a href=\"/\">External Storage</a></li>\n";
      }
      msg = msg + "                  </ul>\n               </div>\n               <div class=\"clr\"></div>\n            </div>\n      <div style=\"min-width: 1180px\">\n         <table id=\"maintable\" cellspacing=\"0\" cellpadding=\"0\">\n            <tbody>\n               <tr>\n                  <td id=\"maintable_left\">\n                     <form method=\"POST\" action=\"\" enctype=\"multipart/form-data\" name=\"filelist\">\n                        <input type=\"hidden\" name=\"action\"><input type=\"hidden\" name=\"data_file\"><input type=\"hidden\" name=\"data_currentParams\" value=\"?\"><input type=\"hidden\" name=\"data_filepath\" value=\"/\">\n                        <div class=\"tableContainer ui-corner-all\">\n                           <div>\n                           </div>\n                           <div class=\"bdr ui-corner-all\">\n                              <div id=\"msg_container\">\n                                 <div id=\"msg_div\"></div>\n                              </div>\n                              <table id=\"filetable\" width=\"96%\" cellspacing=\"0\" cellpadding=\"6\" class=\"bdr ui-corner-all\">\n                                 <tbody>\n                                    \n                                    \n                                    \n<br /></html>";
      String up = null;
      if (uri.length() > 1) {
          String u = uri.substring(0, uri.length() - 1);
          int slash = u.lastIndexOf('/');
          if (slash >= 0 && slash < u.length()) {
              up = uri.substring(0, slash + 1);
          }
      }

      List<String> files = Arrays.asList(f.list(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
              return new File(dir, name).isFile();
          }
      }));
      Collections.sort(files);
      List<String> directories = Arrays.asList(f.list(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
              return new File (dir, name).isDirectory();
          }
      }));
      Collections.sort(directories);
      if (up != null || directories.size() + files.size() > 0) {
          int i;
          msg = msg + "       <tr>\n          <th>Directory " + heading + "</th>\n        </tr>\n        <ul>";
          if (up != null || directories.size() > 0) {
              msg = msg + "<section class=\"directories\">";
              if (up != null) {
                  msg = msg + "<tr><td><li><img alt=\"i3\" src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAABmJLR0QAAAAAAAD5Q7t/AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4QQNCAQUZoNcWwAAAnJJREFUOMuVkz9vHFUUxX/nznhNNkoA4wgpdowccIEEEkgB0SAKPhANJS2ioOIT8A0ooKCi5E+TMiBFFAiZWEAix1lZux7Pu4diZ3Z21wsST7oavbn3nXvOue9pdPcdZBMiUIiorAihMLZwpsECXn91l/vffs1/LW0fvI0QmQUQ2DTPJjy/fxuA2XSm8fVrPj2dcGfvJQLmtU6cfRRGWxWZicavvauXj17xnw9/O0Cxq4iWCKEKMGT+ev7oeFrdvFm5tMYJmR3gHBgnEs7TJ0Ufvvc+P/01vdGU/BnFPhGJIqRo7azl/NT2J6B6d+dGe3L/u1WN41tgg02MRpXqO28h6WPD5ygShVAIO3EJ7N/l/ArnFk73UnHpmaad12V/XzK/1Pbde59l+iMU11AYhTqpvU/GRb1UO2FJ6jyfwjkL8YVGh/eOrdgjKhMhwToDk1k87Afvhr1xbsl+VKN4jLQ3nzCrDOaMRJZ6heEKWIJtMgH/HUgTEIilCXpe3B/u5a2DDTl3bCY16CkSpD34MnjUN3Cu7T343NkC+GmNdIYBl+HQEotB1hqzhRIvFAFnNfZkkzcrQJtlroUxntTOPF85uA60PN21hsvNPH8t5zWZU5Ymu4np1SZXwTrJ0xrnzFk2dv5XRldyvYea1WRpukTZ2HUjmCFXBlKwK3BTY7ddouoCz8dOd7dWwgs2V4cCXNZcTn9we/ENpcQCAC8VeWDVB8tXZlFXVFU/6rnxmMM33iQzefzHMU9OTvg/64WdF7l9sE9VVTx88AvaPvpA3DpS7Bya6RnOS1wucWmgNNAOX5cGtw1uL4b/pcGlhdkzRV74HxA84jlioU1hAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDE3LTA0LTEzVDA4OjA0OjIwLTA0OjAwEFAT/AAAACV0RVh0ZGF0ZTptb2RpZnkAMjAxNy0wNC0xM1QwODowNDoyMC0wNDowMGENq0AAAAAASUVORK5CYII=\"><a rel=\"directory\" href=\"" + up + "\"style=\"text-decoration:none;><span class=\"dirname\"><font color=\"#000000\"><b>..</span></a></b></li></td></tr>";
              }
              for (i = 0; i < directories.size(); i++) {
                  String dir = directories.get(i) + "/";
                  File del = new File(dir);
                  msg = msg + "<tr><td><li><img alt=\"i3\" src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAABmJLR0QAAAAAAAD5Q7t/AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH4QQNCAQUZoNcWwAAAnJJREFUOMuVkz9vHFUUxX/nznhNNkoA4wgpdowccIEEEkgB0SAKPhANJS2ioOIT8A0ooKCi5E+TMiBFFAiZWEAix1lZux7Pu4diZ3Z21wsST7oavbn3nXvOue9pdPcdZBMiUIiorAihMLZwpsECXn91l/vffs1/LW0fvI0QmQUQ2DTPJjy/fxuA2XSm8fVrPj2dcGfvJQLmtU6cfRRGWxWZicavvauXj17xnw9/O0Cxq4iWCKEKMGT+ev7oeFrdvFm5tMYJmR3gHBgnEs7TJ0Ufvvc+P/01vdGU/BnFPhGJIqRo7azl/NT2J6B6d+dGe3L/u1WN41tgg02MRpXqO28h6WPD5ygShVAIO3EJ7N/l/ArnFk73UnHpmaad12V/XzK/1Pbde59l+iMU11AYhTqpvU/GRb1UO2FJ6jyfwjkL8YVGh/eOrdgjKhMhwToDk1k87Afvhr1xbsl+VKN4jLQ3nzCrDOaMRJZ6heEKWIJtMgH/HUgTEIilCXpe3B/u5a2DDTl3bCY16CkSpD34MnjUN3Cu7T343NkC+GmNdIYBl+HQEotB1hqzhRIvFAFnNfZkkzcrQJtlroUxntTOPF85uA60PN21hsvNPH8t5zWZU5Ymu4np1SZXwTrJ0xrnzFk2dv5XRldyvYea1WRpukTZ2HUjmCFXBlKwK3BTY7ddouoCz8dOd7dWwgs2V4cCXNZcTn9we/ENpcQCAC8VeWDVB8tXZlFXVFU/6rnxmMM33iQzefzHMU9OTvg/64WdF7l9sE9VVTx88AvaPvpA3DpS7Bya6RnOS1wucWmgNNAOX5cGtw1uL4b/pcGlhdkzRV74HxA84jlioU1hAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDE3LTA0LTEzVDA4OjA0OjIwLTA0OjAwEFAT/AAAACV0RVh0ZGF0ZTptb2RpZnkAMjAxNy0wNC0xM1QwODowNDoyMC0wNDowMGENq0AAAAAASUVORK5CYII=\"><a rel=\"directory\" href=\"" + encodeUri(uri + dir) + "\"style=\"text-decoration:none;><span class=\"dirname\"><font color=\"#000000\"><b>" + dir + "</span></a></b></li></td></tr>";
              }
              msg = msg + "</section>";
          }
          if (files.size() > 0) {
              msg = msg + "<section class=\"files\">";
              for (i = 0; i < files.size(); i++) {
                  String file = files.get(i);
                  msg = msg + "<tr><td><li><a href=\"" + encodeUri(uri + file) + "\"><span class=\"filename\"><font color=\"#000000\"><b>" + file + "</span></a>";
                  long len = new File(f, file).length();
                  msg = msg + "&nbsp;<span class=\"filesize\">(";
                  if (len < FileUtils.ONE_KB) {
                      msg = msg + len + " bytes";
                  } else if (len < FileUtils.ONE_MB) {
                      msg = msg + (len / FileUtils.ONE_KB) + "." + (((len % FileUtils.ONE_KB) / 10) % 100) + " KB";
                  } else {
                      msg = msg + (len / FileUtils.ONE_MB) + "." + (((len % FileUtils.ONE_MB) / 10) % 100) + " MB";
                  }
                  msg = msg + ")</span></li></td></tr>";
              }
              msg = msg + "</section>";
          }
          msg = msg + "</ul>";
      }
      StringBuilder stringBuilder = new StringBuilder();
      String str = this.manufacturer;
      msg += "</tbody>\n                              </table>\n                           </div>\n                        </div>\n                     </form>\n                  </td>\n                  <td id=\"maintable_right\">\n                     <div class=\"ui-corner-all tableContainer\">\n                        <div>\n                           <table class=\"table_headline\">\n                              <tbody>\n                                 <tr>\n                                    <td>"+this.model+"</td>\n                                 </tr>\n                              </tbody>\n                           </table>\n                           <table class=\"bdr1 ui-corner-all\" id=\"statustable\">\n                              <tbody>\n                                 <tr>\n                                    <td colspan=\"3\"><span class=\"text_075em\">Space available</span></td>\n                                 </tr>\n                                 <tr>\n                                    <td><span class=\"text_075em\"><a href=\"/\">Internal Storage: </a>:</span></td>\n                                    <td>\n                                    </td>\n                                    <td><span class=\"text_075em\">"+getAvailableInternalMemorySize()+" / "+getTotalInternalMemorySize()+"</span></td>\n                                 </tr>\n                                 <tr>\n                                    <td colspan=\"3\"><span class=\"text_075em\">&nbsp;</span></td>\n                                 </tr>\n                                 <tr>\n                              </tbody>\n                           </table>\n                        </div>\n                        <form action=\"/\" method=\"post\" enctype=\"multipart/form-data\" name=\"uploadform\" id=\"form-uploader\" onsubmit=\"return updateProgress(&#39;/&#39;);\">\n                           <input type=\"hidden\" name=\"data_bytesAvailable\" value=\"1004908544\"><input type=\"hidden\" name=\"data_currentParams\" value=\"?\">\n                           <table class=\"table_headline\">\n                              <tbody>\n                                 <tr>\n                                    <td>Transfer files to device</td>\n                                 </tr>\n                              </tbody>\n                           <table>   \n       <tbody><tr>\n".toString();

          stringBuilder = new StringBuilder();
          msg +="            <th>Upload </th>\n        </tr><tr><td>\n<input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"2000000000\">\nFile: <input name=\"uploadFile\" type=\"file\"><br>\nPath: <input type=\"text\" name=\"path\" value=\""+this.rootDir+heading+"\"><br>\n<input name=\"gezien\" value=\"ja\" type=\"hidden\">\n</form></td></tr><tr><th><input type=\"submit\" value=\"Start Upload\" name=\"submitButton\"></th></tr>\n".toString();
backpath=heading;


      return msg+= "</tbody></table></div></body>\n      </div>\n</body></html>";
  }
  public static String backpath="";
public static boolean upload=false;
    public static String tempFilename;
  //====================================================================================================================================
  @Override
  public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
      if (method.equals(Method.GET)) {
          if (!this.quiet) {
              String msg = method + " '" + uri + "'\n";
              System.out.print(msg);
              for (String value : header.keySet()) {
                  System.out.println("  HDR: '" + value + "' = '" + header.get(value) + "'");
              }
              for (String value2 : parms.keySet()) {
                  String msg1 = "  PRM: '" + value2 + "' = '" + parms.get(value2) + "'\n";
                  msg = msg + msg1;
                  System.out.print(msg1);
              }
              for (String value22 : files.keySet()) {
                  String msg2 = "  UPLOADED: '" + value22 + "' = '" + files.get(value22) + "'\n";
                  msg = msg + msg2;
                  System.out.print(msg2);
              }
              this.msgDsplyr.showMessage(msg);
          }
          return serveFile(uri, header, getRootDir());
      }
      if (method.equals(Method.POST)) {
          Response response;
          tempFilename = files.get("uploadFile");
          if (tempFilename != null) {

              File file = new File(tempFilename);
              new File(parms.get("path")).mkdirs();
              Log.d(TAG, "TOG=="+ parms.get("path"));

              file.renameTo(new File("/storage/emulated/0/" + parms.get("uploadFile")));
              Log.d(TAG, "TOG file=="+file);
              Log.d(TAG, "TOG file=="+new File(parms.get("uploadFile")));
              String mov= parms.get("path") +"WhatsApp/";

              File copiedfile=new File(parms.get("path") + parms.get("uploadFile"));
              Log.d(TAG, "TOG copiedfile=="+new File(parms.get("uploadFile")));

              //copy(copiedfile, mov, getcontext());
             // response = NH.newFixedLengthResponse(Response.Status.OK, NH.MIME_HTML, "<div style=\"text-align:center; position: absolute;top:50%;width:100%;\">\n    <label id=\"lblStatus\">\n        Upload Done\n    </label>\n</div>\n\n<div style='text-align:center;position:absolute;bottom:10%; width:100%;'>\n<FORM><INPUT Type=\"button\" VALUE=\"Back\" onClick=\"history.go(-1);return true;\"></FORM>");

              /*copy(file, (String) parms.get("path")+"WhatsApp/", WF.context);
              Log.d(TAG, "TOG file=="+file);*/
              try {
                  mov= parms.get("path");

                  //copiedfile=new File((String) parms.get("path")+(String) parms.get("uploadFile"));
                  copiedfile=new File("/storage/emulated/0/"+ parms.get("uploadFile"));
                  File movfile=new File(parms.get("path") + parms.get("uploadFile"));

                  // copy(copiedfile, mov, context);

                  File stringmv=new File("/storage/emulated/0/"+ parms.get("uploadFile"));

                  Log.d(TAG, "TOG MV  stringmv=="+stringmv);

                  if (isWritable(movfile)){
                      stringmv.renameTo(new File(parms.get("path") + parms.get("uploadFile")));
                      response = NH.newFixedLengthResponse(Response.Status.OK, NH.MIME_HTML, "<div style=\"text-align:center; position: absolute;top:50%;width:100%;\">\n    <label id=\"lblStatus\">\n        Upload Done\n    </label>\n</div>\n\n<div style='text-align:center;position:absolute;bottom:10%; width:100%;'>\n<form>\n" +
                              "    <button formaction=\""+backpath+"\">Back!</button>");
                      Log.d(TAG, "TOG MV  uri uri=="+backpath);

                      return response;

                  }


                      //copyFile(copiedfile,movfile,context);
                  else {
                      if (copy(copiedfile, mov, context)) {
                          deleteFile("/storage/emulated/0/", parms.get("uploadFile"));
                          File finalfile = new File(parms.get("path") + parms.get("uploadFile"));

                          if (finalfile.exists() && !finalfile.isDirectory()) {
                              response = NH.newFixedLengthResponse(Response.Status.OK, NH.MIME_HTML, "<div style=\"text-align:center; position: absolute;top:50%;width:100%;\">\n    <label id=\"lblStatus\">\n        Upload Done\n    </label>\n</div>\n\n<div style='text-align:center;position:absolute;bottom:10%; width:100%;'>\n<form>\n" +
                                      "    <button formaction=\""+backpath+"\">Back!</button>");
                              Log.d(TAG, "TOG MV  uri uri=="+backpath);

                              return response;
                          }
                      }
                  }





                  //  copyFile((String) parms.get("path"),tempFilename, ((String) parms.get("path")),pickedDir);
                // }

                 // file.renameTo(new File(((String) parms.get("path")) + ((String) parms.get("uploadFile"))));
                  //moveDocument(docs, true);
                  // ArrayList<DocumentInfo> docs;
                 // DocumentInfo toDoc;
                 // Boolean deleteAfter=true;

              } catch (Exception e) {

                  response = NH.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NH.MIME_HTML, "<div style=\"text-align:center; position: absolute;top:50%;width:100%;\">\n    <label id=\"lblStatus\">\n        Upload Error\n    </label>\n</div>\n\n<div style='text-align:center;position:absolute;bottom:10%; width:100%;'>\n<FORM><INPUT Type=\"button\" VALUE=\"Back\" onClick=\"history.go(-1);return true;\"></FORM>");
                  return response;
              }
          }
          else {
              response = NH.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NH.MIME_HTML, "<div style=\"text-align:center; position: absolute;top:50%;width:100%;\">\n    <label id=\"lblStatus\">\n        Upload Error\n    </label>\n</div>\n\n<div style='text-align:center;position:absolute;bottom:10%; width:100%;'>\n<FORM><INPUT Type=\"button\" VALUE=\"Back\" onClick=\"history.go(-1);return true;\"></FORM>");
              return response;
          }



          }

         // return response;

      return serveFile(uri, header, getRootDir());
  }



    /*public void copyFile(String inputPath, String inputFile, String outputPath, DocumentFile pickedDir) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + inputFile);
            //out = new FileOutputStream(outputPath + inputFile);

            DocumentFile file = pickedDir.createFile("//MIME type", outputPath);
            out = getContentResolver().openOutputStream(file.getUri());

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();


            // write the output file (You have now copied the file)
            out.flush();
            out.close();
        } catch (FileNotFoundException fnfe1) {
    *//* I get the error here *//*
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }*/




  static Object waitMonitor = new Object();

    private class MoveTask extends AsyncTask<Void, Void, Boolean> {
        private final DocumentInfo toDoc;
        private final ArrayList<DocumentInfo> docs;
        private boolean deleteAfter;

        public MoveTask(ArrayList<DocumentInfo> docs, DocumentInfo toDoc, boolean deleteAfter) {

            this.docs = docs;
            this.toDoc = toDoc;
            this.deleteAfter = deleteAfter;
            Log.d(TAG, "TAG  TAG     222TAG ArrayList<DocumentInfo> docs ; ====" + docs + " >>>  " + toDoc + " ======" + deleteAfter);

        }

        @Override
        public Boolean doInBackground() {
            return null;
        }
    }




        /** ====================================================================================
   * Starts as a standalone file server and waits for Enter.
   */
/*  
  public static void main(String[] args) {
      // Defaults
      int port = 8080;
      String root = "D:/www/NanoServerTesting/"; //".";        //<<<<<<<<<<<<<<<<<<<<
      System.setProperty("java.io.tmpdir", "D:\\www\\NanoServerTesting\\SavedFiles");  //<<<<<<<<< where to write uploads

      String host = "127.0.0.1";
      File wwwroot = new File(root).getAbsoluteFile();
      boolean quiet = false;

      // Parse command-line, with short and long versions of the options.
      for (int i = 0; i < args.length; ++i) {
          if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--host")) {
              host = args[i + 1];
          } else if (args[i].equalsIgnoreCase("-p") || args[i].equalsIgnoreCase("--port")) {
              port = Integer.parseInt(args[i + 1]);
          } else if (args[i].equalsIgnoreCase("-q") || args[i].equalsIgnoreCase("--quiet")) {
              quiet = true;
          } else if (args[i].equalsIgnoreCase("-d") || args[i].equalsIgnoreCase("--dir")) {
              wwwroot = new File(args[i + 1]).getAbsoluteFile();
          } else if (args[i].equalsIgnoreCase("--licence")) {
              System.out.println(LICENCE + "\n");
              break;
          }
      }

//      ServerRunner.executeInstance(new SWS(host, port, wwwroot, quiet));   //  Hangs on read of System.in
      SWS server =  new SWS(null, host, port, wwwroot, quiet);
      try {
          server.start();
      } catch (IOException ioe) {
          System.err.println("Couldn't start server:\n" + ioe);
          System.exit(-1);
      }


      synchronized(waitMonitor) {
         try {waitMonitor.wait();}catch(Exception x) {x.printStackTrace();}
      }

      try {Thread.sleep(100);}catch(Exception x) {}
      System.out.println("Server stopped.");

  }
*/

}