/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.tattletale.reporting;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.NestableArchive;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * Blacklisted report
 *
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 */
public class BlackListedReport extends AbstractReport
{
   /** NAME */
   private static final String NAME = "Black listed";

   /** DIRECTORY */
   private static final String DIRECTORY = "blacklisted";

   /** Constructor */
   public BlackListedReport()
   {
      super(DIRECTORY, ReportSeverity.ERROR, NAME, DIRECTORY);
   }

   /**
    * write out the report's content
    *
    * @param bw the writer to use
    * @throws IOException if an error occurs
    */
   public void writeHtmlBodyContent(BufferedWriter bw) throws IOException
   {
      bw.write("<table>" + Dump.newLine());

      bw.write("  <tr>" + Dump.newLine());
      bw.write("     <th>Archive</th>" + Dump.newLine());
      bw.write("     <th>Usage</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      boolean odd = true;

      for (Archive archive : archives)
      {
         String archiveName = archive.getName();
         int finalDot = archiveName.lastIndexOf(".");
         String extension = archiveName.substring(finalDot + 1);

         SortedMap<String, SortedSet<String>> blacklisted = getBlackListedDeps(archive);
         boolean include = false;
         boolean filtered = isFiltered(archive.getName());

         if (blacklisted != null && blacklisted.size() > 0)
         {
            include = true;

            if (!filtered)
            {
               status = ReportStatus.RED;
            }
         }

         if (include)
         {
            if (odd)
            {
               bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
            }
            else
            {
               bw.write("  <tr class=\"roweven\">" + Dump.newLine());
            }
            bw.write("     <td><a href=\"../" + extension + "/" + archiveName + ".html\">"
                  + archive.getName() + "</a></td>" + Dump.newLine());
            bw.write("     <td>");

            bw.write("       <table>" + Dump.newLine());

            for (Map.Entry<String, SortedSet<String>> stringSortedSetEntry :
                  blacklisted.entrySet())
            {

               String pkg = stringSortedSetEntry.getKey();
               SortedSet<String> blpkgs = stringSortedSetEntry.getValue();

               bw.write("      <tr>" + Dump.newLine());

               bw.write("        <td>" + pkg + "</td>" + Dump.newLine());

               if (!filtered)
               {
                  bw.write("       <td>");
               }
               else
               {
                  bw.write("       <td style=\"text-decoration: line-through;\">");
               }

               for (String blp : blpkgs)
               {
                  bw.write(blp + "<br>");
               }

               bw.write("</td>" + Dump.newLine());

               bw.write("      </tr>" + Dump.newLine());
            }

            bw.write("       </table>" + Dump.newLine());

            bw.write("</td>" + Dump.newLine());
            bw.write("  </tr>" + Dump.newLine());

            odd = !odd;


         }

         bw.write("</table>" + Dump.newLine());
      }
   }

   private SortedMap<String, SortedSet<String>> getBlackListedDeps(Archive a)
   {
      SortedMap<String, SortedSet<String>> deps = new TreeMap<String, SortedSet<String>>();
      if (a instanceof NestableArchive)
      {
         NestableArchive na = (NestableArchive) a;
         List<Archive> subArchives = na.getSubArchives();

         for (Archive sa : subArchives)
         {
            deps.putAll(getBlackListedDeps(sa));
         }
      }
      else
      {
         deps.putAll(a.getBlackListedDependencies());
      }
      return deps;
   }

   /**
    * write out the header of the report's content
    *
    * @param bw the writer to use
    * @throws IOException if an error occurs
    */
   public void writeHtmlBodyHeader(BufferedWriter bw) throws IOException
   {
      bw.write("<body>" + Dump.newLine());
      bw.write(Dump.newLine());

      bw.write("<h1>" + NAME + "</h1>" + Dump.newLine());

      bw.write("<a href=\"../index.html\">Main</a>" + Dump.newLine());
      bw.write("<p>" + Dump.newLine());
   }

   /**
    * Create filter
    *
    * @return The filter
    */
   @Override
   protected Filter createFilter()
   {
      return new KeyFilter();
   }
}
