package intellij_extension.utility;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.utility.GroupFileObjectUtility;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupFileObjectUtilityTest {

    private static final Comparator<FileObject> FILE_OBJECT_COMPARATOR = Comparator.comparing(FileObject::getFilename);

    @Test
    void groupByPackageTest() {
        //Create test objects
        Codebase codebase = new Codebase();

        //Set up the Codebase with dummy data
        final String PROJECT_ROOT = "C:\\Users\\Dummy\\my-project";
        codebase.setProjectRootPath(PROJECT_ROOT);
        codebase.createOrGetFileObjectFromPath(PROJECT_ROOT + "\\package1\\myfileA.java");
        codebase.createOrGetFileObjectFromPath(PROJECT_ROOT + "\\package1\\myfileB.java");
        codebase.createOrGetFileObjectFromPath(PROJECT_ROOT + "\\package2\\myfileC.java");
        codebase.createOrGetFileObjectFromPath(PROJECT_ROOT + "\\package3\\myfileD.java");
        codebase.createOrGetFileObjectFromPath(PROJECT_ROOT + "\\package3\\myfileE.java");
        codebase.createOrGetFileObjectFromPath(PROJECT_ROOT + "\\package3\\myfileF.java");
        codebase.createOrGetFileObjectFromPath(PROJECT_ROOT + "\\package3\\package4\\myfileG.java");
        codebase.createOrGetFileObjectFromPath(PROJECT_ROOT + "\\package5\\package6\\myfileH.java");

        Map<String, TreeSet<FileObject>> packageToFileMap = GroupFileObjectUtility.groupByPackage(codebase.getProjectRootPath(), codebase.getActiveFileObjects()); //method being tested

        //Verify that some of the above files were categorized under the correct packages
        assertTrue(packageToFileMap.containsKey("\\package1\\"));
        assertTrue(packageToFileMap.get("\\package1\\").stream()
                .anyMatch(fileObject -> fileObject.getFilename().equals("myfileA.java")));
        assertTrue(packageToFileMap.get("\\package1\\").stream()
                .anyMatch(fileObject -> fileObject.getFilename().equals("myfileB.java")));
        assertEquals(2, packageToFileMap.get("\\package1\\").size());

        assertTrue(packageToFileMap.containsKey("\\package3\\"));
        assertTrue(packageToFileMap.get("\\package3\\").stream()
                .anyMatch(fileObject -> fileObject.getFilename().equals("myfileE.java")));
        assertEquals(3, packageToFileMap.get("\\package3\\").size());

        assertTrue(packageToFileMap.containsKey("\\package3\\package4\\"));
        assertTrue(packageToFileMap.get("\\package3\\package4\\").stream()
                .anyMatch(fileObject -> fileObject.getFilename().equals("myfileG.java")));
        assertEquals(1, packageToFileMap.get("\\package3\\package4\\").size());

        assertTrue(packageToFileMap.containsKey("\\package5\\package6\\"));
        assertTrue(packageToFileMap.get("\\package5\\package6\\").stream()
                .anyMatch(fileObject -> fileObject.getFilename().equals("myfileH.java")));
        assertEquals(1, packageToFileMap.get("\\package5\\package6\\").size());
    }

    @Test
    void groupByCommitsTest() {
        //Create test objects
        Codebase codebase = new Codebase();

        //Set up the Codebase with dummy data
        final String PROJECT_ROOT = "C:\\Users\\Dummy\\my-project";
        codebase.setProjectRootPath(PROJECT_ROOT);
        FileObject fileObject1 = codebase.createOrGetFileObjectFromPath("File1.java");
        FileObject fileObject2 = codebase.createOrGetFileObjectFromPath("File2.java");
        FileObject fileObject3 = codebase.createOrGetFileObjectFromPath("File3.java");
        FileObject fileObject4 = codebase.createOrGetFileObjectFromPath("File4.java");
        FileObject fileObject5 = codebase.createOrGetFileObjectFromPath("File5.java");
        FileObject fileObject6 = codebase.createOrGetFileObjectFromPath("File6.java");

        fileObject1.getCommitHashToHeatObjectMap().put("C1", null);
        fileObject1.getCommitHashToHeatObjectMap().put("C3", null);
        fileObject1.getCommitHashToHeatObjectMap().put("C7", null);

        fileObject2.getCommitHashToHeatObjectMap().put("C1", null);
        fileObject2.getCommitHashToHeatObjectMap().put("C2", null);
        fileObject2.getCommitHashToHeatObjectMap().put("C6", null);
        fileObject2.getCommitHashToHeatObjectMap().put("C8", null);

        fileObject3.getCommitHashToHeatObjectMap().put("C1", null);
        fileObject3.getCommitHashToHeatObjectMap().put("C4", null);
        fileObject3.getCommitHashToHeatObjectMap().put("C6", null);

        fileObject4.getCommitHashToHeatObjectMap().put("C3", null);
        fileObject4.getCommitHashToHeatObjectMap().put("C4", null);
        fileObject4.getCommitHashToHeatObjectMap().put("C6", null);
        fileObject4.getCommitHashToHeatObjectMap().put("C7", null);
        fileObject4.getCommitHashToHeatObjectMap().put("C8", null);

        fileObject5.getCommitHashToHeatObjectMap().put("C2", null);
        fileObject5.getCommitHashToHeatObjectMap().put("C4", null);
        fileObject5.getCommitHashToHeatObjectMap().put("C5", null);

        fileObject6.getCommitHashToHeatObjectMap().put("C2", null);
        fileObject6.getCommitHashToHeatObjectMap().put("C5", null);
        fileObject6.getCommitHashToHeatObjectMap().put("C7", null);
        fileObject6.getCommitHashToHeatObjectMap().put("C8", null);

        Map<String, TreeSet<FileObject>> packageToFileMap = GroupFileObjectUtility.groupByCommit(codebase);

        TreeSet<FileObject> pair1 = new TreeSet<>(FILE_OBJECT_COMPARATOR);
        TreeSet<FileObject> pair2 = new TreeSet<>(FILE_OBJECT_COMPARATOR);
        TreeSet<FileObject> pair3 = new TreeSet<>(FILE_OBJECT_COMPARATOR);

        pair1.addAll(Set.of(fileObject1, fileObject4));
        pair2.addAll(Set.of(fileObject2, fileObject6));
        pair3.addAll(Set.of(fileObject5));

        for (Map.Entry<String, TreeSet<FileObject>> entry : packageToFileMap.entrySet())
        {
            System.out.println("key "+entry.getKey());
            for (FileObject f : entry.getValue())
            {
                System.out.println(f.getFilename());
            }
        }

        assertTrue(packageToFileMap.containsValue(pair1));
        assertTrue(packageToFileMap.containsValue(pair2));
        assertTrue(packageToFileMap.containsValue(pair3));
    }
}
