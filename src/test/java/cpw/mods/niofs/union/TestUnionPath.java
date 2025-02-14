package cpw.mods.niofs.union;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TestUnionPath {
    
    @Test
    void testUnionPath() throws IOException {
        var jarFs = FileSystems.newFileSystem(URI.create("jar:" + Paths.get("src", "test", "resources", "dir1.zip").toUri()), Map.of());
        // First test that our tests succed on jar paths:
        assertAll("Tests failed for jar file system, this should not happen, tests need to be adjusted", () -> runPathTests(jarFs));
        
        var fsp = (UnionFileSystemProvider) FileSystemProvider.installedProviders().stream().filter(fs-> fs.getScheme().equals("union")).findFirst().orElseThrow();
        // Actual base directory is not relevant as we don't access the file system in these tests
        var fs = fsp.newFileSystem((path, base) -> true, Paths.get("src").toAbsolutePath().normalize());
        runPathTests(fs);
    }
    
    private static void runPathTests(FileSystem fs) {
        var relUp = fs.getPath("..");
        var rel0 = fs.getPath("");
        var rel1 = fs.getPath("one");
        var rel2 = fs.getPath("two");
        var rel3 = fs.getPath("three");
        var rel32 = fs.getPath("three", "two");
        var rel123 = fs.getPath("one/two/three");
        var rel1223 = fs.getPath("one/two/./three");
        var rel12up3 = fs.getPath("one/two/../three");
        var rel13 = fs.getPath("one/three");
        var rel13slash = fs.getPath("one/three/");
        var rel1slash3 = fs.getPath("one//three");
        var relUpUp1 = fs.getPath("../../one");
        var relUpUp123 = fs.getPath("../../one/two/three");
        
        var absUp = fs.getPath("/..");
        var abs0 = fs.getPath("/");
        var abs1 = fs.getPath("/one");
        var abs2 = fs.getPath("/two");
        var abs3 = fs.getPath("/three");
        var abs32 = fs.getPath("/", "three", "two");
        var abs123 = fs.getPath("/one/two/three");
        var abs1223 = fs.getPath("/one/two/./three");
        var abs12up3 = fs.getPath("/one/two/../three");
        var abs13 = fs.getPath("/one/three");
        var abs13slash = fs.getPath("/one/three/");
        var abs1slash3 = fs.getPath("/one//three");
        var absUpUp1 = fs.getPath("/../../one");
        var absUpUp123 = fs.getPath("/../../one/two/three");
        
        // General
        assertEquals(rel13, rel13slash);
        assertEquals(rel13, rel1slash3);
        assertEquals(abs13, abs13slash);
        assertEquals(abs13, abs1slash3);
        // Not filtering out empty elements for joining will cause this to fail
        assertFalse(fs.getPath("", "one", "two").isAbsolute());
        
        // toString
        assertEquals("..", relUp.toString());
        assertEquals("", rel0.toString());
        assertEquals("one", rel1.toString());
        assertEquals("two", rel2.toString());
        assertEquals("three", rel3.toString());
        assertEquals("three/two", rel32.toString());
        assertEquals("one/two/three", rel123.toString());
        assertEquals("one/two/./three", rel1223.toString());
        assertEquals("one/two/../three", rel12up3.toString());
        assertEquals("one/three", rel13.toString());
        assertEquals("one/three", rel13slash.toString());
        assertEquals("one/three", rel1slash3.toString());
        assertEquals("../../one", relUpUp1.toString());
        assertEquals("../../one/two/three", relUpUp123.toString());
        assertEquals("/..", absUp.toString());
        assertEquals("/", abs0.toString());
        assertEquals("/one", abs1.toString());
        assertEquals("/two", abs2.toString());
        assertEquals("/three", abs3.toString());
        assertEquals("/three/two", abs32.toString());
        assertEquals("/one/two/three", abs123.toString());
        assertEquals("/one/two/./three", abs1223.toString());
        assertEquals("/one/two/../three", abs12up3.toString());
        assertEquals("/one/three", abs13.toString());
        assertEquals("/one/three", abs13slash.toString());
        assertEquals("/one/three", abs1slash3.toString());
        assertEquals("/../../one", absUpUp1.toString());
        assertEquals("/../../one/two/three", absUpUp123.toString());
        
        // isAbsolute
        assertFalse(relUp.isAbsolute());
        assertFalse(rel0.isAbsolute());
        assertFalse(rel1.isAbsolute());
        assertFalse(rel2.isAbsolute());
        assertFalse(rel3.isAbsolute());
        assertFalse(rel32.isAbsolute());
        assertFalse(rel123.isAbsolute());
        assertFalse(rel1223.isAbsolute());
        assertFalse(rel12up3.isAbsolute());
        assertFalse(rel13.isAbsolute());
        assertFalse(rel13slash.isAbsolute());
        assertFalse(rel1slash3.isAbsolute());
        assertFalse(relUpUp1.isAbsolute());
        assertFalse(relUpUp123.isAbsolute());
        assertTrue(absUp.isAbsolute());
        assertTrue(abs0.isAbsolute());
        assertTrue(abs1.isAbsolute());
        assertTrue(abs2.isAbsolute());
        assertTrue(abs3.isAbsolute());
        assertTrue(abs32.isAbsolute());
        assertTrue(abs123.isAbsolute());
        assertTrue(abs1223.isAbsolute());
        assertTrue(abs12up3.isAbsolute());
        assertTrue(abs13.isAbsolute());
        assertTrue(abs13slash.isAbsolute());
        assertTrue(abs1slash3.isAbsolute());
        assertTrue(absUpUp1.isAbsolute());
        assertTrue(absUpUp123.isAbsolute());
        
        // getParent
        assertEquals(rel3, rel32.getParent());
        assertEquals(rel1, rel123.getParent().getParent());
        assertEquals(rel1, rel1223.getParent().getParent().getParent());
        assertEquals(rel1, rel12up3.getParent().getParent().getParent());
        assertEquals(rel1, rel13.getParent());
        assertNull(rel13.getParent().getParent());
        assertEquals(rel1, rel13slash.getParent());
        assertEquals(rel1, rel1slash3.getParent());
        assertEquals(abs3, abs32.getParent());
        assertEquals(abs1, abs123.getParent().getParent());
        assertEquals(abs1, abs1223.getParent().getParent().getParent());
        assertEquals(abs1, abs12up3.getParent().getParent().getParent());
        assertEquals(abs1, abs13.getParent());
        assertEquals(abs0, abs13.getParent().getParent());
        assertEquals(abs1, abs13slash.getParent());
        assertEquals(abs1, abs1slash3.getParent());
        
        // getNameCount, getName, getFileName, subpath
        testNameParts(fs, relUp, "..");
        testNameParts(fs, rel0, "");
        testNameParts(fs, rel1, "one");
        testNameParts(fs, rel2, "two");
        testNameParts(fs, rel3, "three");
        testNameParts(fs, rel32, "three", "two");
        testNameParts(fs, rel123, "one", "two", "three");
        testNameParts(fs, rel1223, "one", "two", ".", "three");
        testNameParts(fs, rel12up3, "one", "two", "..", "three");
        testNameParts(fs, rel13, "one", "three");
        testNameParts(fs, rel13slash, "one", "three");
        testNameParts(fs, rel1slash3, "one", "three");
        testNameParts(fs, relUpUp1, "..", "..", "one");
        testNameParts(fs, relUpUp123, "..", "..", "one", "two", "three");
        testNameParts(fs, absUp, "..");
        testNameParts(fs, abs0);
        testNameParts(fs, abs1, "one");
        testNameParts(fs, abs2, "two");
        testNameParts(fs, abs3, "three");
        testNameParts(fs, abs32, "three", "two");
        testNameParts(fs, abs123, "one", "two", "three");
        testNameParts(fs, abs1223, "one", "two", ".", "three");
        testNameParts(fs, abs12up3, "one", "two", "..", "three");
        testNameParts(fs, abs13, "one", "three");
        testNameParts(fs, abs13slash, "one", "three");
        testNameParts(fs, abs1slash3, "one", "three");
        testNameParts(fs, absUpUp1, "..", "..", "one");
        testNameParts(fs, absUpUp123, "..", "..", "one", "two", "three");
        
        assertEquals(rel0, rel0.normalize());
        assertEquals(rel1, rel1.normalize());
        assertEquals(rel13, rel13.normalize());
        assertEquals(rel123, rel1223.normalize());
        assertEquals(rel13, rel12up3.normalize());
        assertEquals(relUpUp1, relUpUp1.normalize());
        assertEquals(relUpUp123, relUpUp123.normalize());
        assertEquals(abs0, abs0.normalize());
        assertEquals(abs1, abs1.normalize());
        assertEquals(abs13, abs13.normalize());
        assertEquals(abs123, abs1223.normalize());
        assertEquals(abs13, abs12up3.normalize());
        assertEquals(abs1, absUpUp1.normalize());
        assertEquals(abs123, absUpUp123.normalize());
        
        // resolve
        assertEquals(abs32, rel0.resolve(abs32));
        assertEquals(abs13, relUp.resolve(abs13));
        assertEquals(abs123, rel123.resolve(abs123));
        assertEquals(rel123, rel0.resolve(rel123));
        assertEquals(rel123, rel1.resolve(rel2).resolve(rel3));
        assertEquals(rel32, rel3.resolve(rel0).resolve(rel2));
        assertEquals(abs123, abs0.resolve(rel123));
        assertEquals(abs123, abs1.resolve(rel2).resolve(rel3));
        assertEquals(abs32, abs3.resolve(rel0).resolve(rel2));

        assertThrows(IllegalArgumentException.class, () -> rel1.relativize(abs123));
        assertThrows(IllegalArgumentException.class, () -> abs1.relativize(rel123));
        
        assertEquals(fs.getPath("two/three"), rel1.relativize(rel123));
        assertEquals(fs.getPath("../.."), rel123.relativize(rel1));
        assertEquals(rel1, rel1.relativize(fs.getPath("one/one")));
        assertEquals(rel3, rel1.relativize(rel13));
        assertEquals(fs.getPath("../../one/one"), rel32.relativize(fs.getPath("one/one")));
        assertEquals(fs.getPath("../../one"), rel123.relativize(fs.getPath("one/one")));
        
        assertEquals(fs.getPath("two/three"), abs1.relativize(abs123));
        assertEquals(fs.getPath("../.."), abs123.relativize(abs1));
        assertEquals(rel1, abs1.relativize(fs.getPath("/one/one")));
        assertEquals(rel3, abs1.relativize(abs13));
        assertEquals(fs.getPath("../../one/one"), abs32.relativize(fs.getPath("/one/one")));
        assertEquals(fs.getPath("../../one"), abs123.relativize(fs.getPath("/one/one")));
        
        // getRoot
        assertNull(rel0.getRoot());
        assertNull(rel123.getRoot());
        assertNull(relUpUp1.getRoot());

        assertEquals(abs0, abs0.getRoot());
        assertEquals(abs0, abs123.getRoot());
        assertEquals(abs0, absUpUp1.getRoot());
    }
    
    private static void testNameParts(FileSystem fs, Path path, String... names) {
        // getNameCount
        if (names.length == 0) {
            if (path.isAbsolute()) {
                assertEquals(0, path.getNameCount());
            } else {
                assertEquals(1, path.getNameCount());
                assertEquals(fs.getPath(""), path.getName(0));
            }
        } else {
            assertEquals(names.length, path.getNameCount());
        }
        int nameCount = path.getNameCount();
        
        // getName
        assertThrows(IllegalArgumentException.class, () -> path.getName(-1));
        assertThrows(IllegalArgumentException.class, () -> path.getName(nameCount));
        for (int i = 0; i < names.length; i++) {
            assertEquals(fs.getPath(names[i]), path.getName(i));
        }
        
        // getFileName
        if (names.length == 0 || (names.length == 1 && names[0].isEmpty())) {
            assertNull(path.getFileName());
        } else {
            assertEquals(fs.getPath(names[names.length - 1]), path.getFileName());
        }

        // subpath, startsWith, endsWith
        if (names.length > 0) {
            for (int i = 1; i <= names.length; i++) {
                String[] fromStart = new String[i];
                System.arraycopy(names, 0, fromStart, 0, i);
                String[] fromEnd = new String[i];
                System.arraycopy(names, names.length - i, fromEnd, 0, i);
                if (fs.getPath("", fromStart).isAbsolute()) throw new IllegalStateException(Arrays.toString(fromStart));
                assertEquals(fs.getPath("", fromStart), path.subpath(0, i), path + " subpath(0, " + i + ")");
                assertEquals(fs.getPath("", fromEnd), path.subpath(names.length - i, names.length), path + " subpath(" + (names.length - i) + ", " + names.length + ")");
                
                String absStr = path.isAbsolute() ? "/" : "";
                String oppositeAbsStr = path.isAbsolute() ? "" : "/";
                assertTrue(path.startsWith(fs.getPath(absStr, fromStart)));
                assertTrue(path.endsWith(fs.getPath("", fromEnd)));
                assertFalse(path.startsWith(fs.getPath(oppositeAbsStr, fromStart)));
                if (path.isAbsolute()) {
                    // Absolute paths can end on non-absolute paths
                    assertTrue(path.endsWith(fs.getPath(oppositeAbsStr, fromEnd)));
                } else {
                    assertFalse(path.endsWith(fs.getPath(oppositeAbsStr, fromEnd)));
                }
                // Make values invalid and assert that startsWith and endsWith won't work
                fromStart[0] = "SOMETHINGINVALID";
                fromEnd[0] = "SOMETHINGINVALID";
                assertFalse(path.startsWith(fs.getPath(absStr, fromStart)));
                assertFalse(path.endsWith(fs.getPath(absStr, fromEnd)));
            }
        } else if (path.isAbsolute()) {
            assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 1));
        } else {
            assertEquals(fs.getPath(""), path.subpath(0, 1));
        }
    }
}
