package org.kisses.core;

import org.elasticsearch.node.NodeValidationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public class AbstractEsTest {

  protected static Path pathHome;
  protected static KissES es;

  @BeforeClass
  public static void init() throws NodeValidationException {
    pathHome = Paths.get("/tmp/kisses");
    es = KissES.embedded("org.kisses.core", pathHome.toString());
  }

  @AfterClass
  public static void clean() throws IOException {
    es.close();
    Files.walkFileTree(pathHome, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

}
