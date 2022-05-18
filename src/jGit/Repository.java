//pds 2022
//bmi23
package jGit;

import static jGit.Utils.*;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class Repository implements Serializable {

    //sets up version control in directory
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File JGIT_DIR = join(CWD, ".jgitrepo");
    public static final File COMMITS_DIR = join(JGIT_DIR, "Commits");
    public static final File STAGES_DIR = join(JGIT_DIR, "Stages");
    public static final File BLOBS_DIR = join(JGIT_DIR, "Blobs");
    public static final File BRANCHES_DIR = join(JGIT_DIR, "Branches");
    
    //address of head file
    public static final File HEAD_FILE = join(JGIT_DIR, "_head");
    
    //address of staged file
    public static final File STAGE_FILE = join(JGIT_DIR, "_stage");
    
    //address of branch files
    public static final File BRANCHES_FILE = join(JGIT_DIR, "_branches");

    
    public static void setupPersistence() {
        if (JGIT_DIR.isDirectory()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        } else {
            // creates directories of .jgitrepo, Master branch, and Master's commits folder
            JGIT_DIR.mkdir();
            COMMITS_DIR.mkdir();
            BLOBS_DIR.mkdir();
            STAGES_DIR.mkdir();
            BRANCHES_DIR.mkdir();

            // stages, creates master branch, and commits initial commit
            Commit stage = new Commit(null);
            saveStage(stage);
            ArrayList<String> branches = new ArrayList<>();
            branches.add("master");
            saveBranches(branches);
            stage.commit("initial commit", new Date(0));
            saveCommit(stage);
            saveBranch(branches.get(0), getHEAD().getID());
        }
    }


    private static void saveCommit(Commit commit) {
    	//writes commit location to head file, stores contents
        String id = commit.getID();
        writeObject(HEAD_FILE, id); 
        writeObject(join(COMMITS_DIR, id), commit);
        String currBranch = getBranches().get(0);
        saveBranch(currBranch, id);
        List<String> stageFiles = plainFilenamesIn(STAGES_DIR);
        for (String stage : stageFiles) {
            join(STAGES_DIR, stage).delete();
        }
        Commit stage = new Commit(id);
        saveStage(stage);
    }


    private static void saveStage(Commit stage) {
    	//writes stage location, stores contents
        String id = Utils.sha1(serialize(stage));
        writeObject(STAGE_FILE, id);
        writeObject(join(STAGES_DIR, id), stage);
    }


    private static void saveBranches(ArrayList<String> branchNames) {
        writeObject(BRANCHES_FILE, branchNames);
    }


    private static void saveBranch(String branchName, String branchFile) {
        writeObject(join(BRANCHES_DIR, branchName), branchFile);
    }


    public static Commit getHEAD() {
        return readObject(join(COMMITS_DIR, readObject(HEAD_FILE, String.class)), Commit.class);
    }


    public static Commit getStage() {
        return readObject(join(STAGES_DIR, readObject(STAGE_FILE, String.class)), Commit.class);
    }


    private static byte[] getBlob(String blobName, File blobsDir) {
        return readContents(join(blobsDir, blobName));
    }


    public static ArrayList<String> getBranches() {
        ArrayList<String> branches = (ArrayList<String>) readObject(BRANCHES_FILE, ArrayList.class);
        return branches;
    }


    public static Commit getBranchCommit(String branch, File git) {
    	//get commits from branch
        File branchesDir = join(git, "Branches");
        String branchID = "";
        try {
            branchID = readObject(join(branchesDir, branch), String.class);
        } catch (java.lang.IllegalArgumentException e) {
        }

        File commitsDir = join(git, "Commits");
        return findCommit(branchID, commitsDir);
    }


    public static void add(String filename) {
    	//stages commit
        Commit stage = getStage();
        stage.add(filename);
        saveStage(stage);
    }

    public static void commit(String message) {
        Commit stage = getStage();
        if (stage.commit(message, new Date())) {
            saveCommit(stage);
        }

        ArrayList<String> branches = getBranches();
        String currBranch = branches.get(0);
        saveBranch(currBranch, stage.getID());
    }

    public static void rm(String filename) {
        Commit stage = getStage();
        stage.remove(filename);
        saveStage(stage);
    }

    public static void log() {
    	//creates and prints log
        Commit head = getHEAD();
        Commit currCommit = head;
        while (currCommit.getParentID() != null) {
            currCommit.printLog();
            currCommit = readObject(join(COMMITS_DIR, currCommit.getParentID()), Commit.class);
        }
        currCommit.printLog();
    }


    public static void globalLog() {
    	//creates and prints global log
        List<String> commitList = plainFilenamesIn(COMMITS_DIR);
        for (String commitID : commitList) {
            File commitAddr = join(COMMITS_DIR, commitID);
            Commit commit = readObject(commitAddr, Commit.class);
            commit.printLog();
        }
    }

    public static void status() {
    	//creates and prints status screen
        Commit stage = getStage();
        ArrayList<String> branches = getBranches();
        ArrayList<String> added = stage.getAdded();
        ArrayList<String> removed = stage.getRemoved();
        TreeMap<String, String> modifications = getModifications(stage);
        Set<String> modifiedFiles = modifications.keySet();
        List<String> untracked = getUntracked(stage);
        Collections.sort(added);
        Collections.sort(removed);
        Collections.sort(untracked);

        System.out.println("=== Branches ===");
        String currBranch = branches.get(0);
        Collections.sort(branches);
        int currBranchPos = branches.indexOf(currBranch);
        branches.remove(currBranch);
        branches.add(currBranchPos, "*" + currBranch);
        for (String branch : branches) {
            System.out.println(branch);
        }
       
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String addName : added) {
            System.out.println(addName);
        }
       
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String removeName : removed) {
            System.out.println(removeName);
        }
        
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");

        for (String filename : modifiedFiles) {
            System.out.println(filename + " " + modifications.get(filename));
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for (String untrackedName : untracked) {
            System.out.println(untrackedName);
        }
       
        System.out.println();
        
    }


    private static TreeMap<String, String> getModifications(Commit stage) {
    	//gets modifications to file
        TreeMap<String, String> modifications = new TreeMap<>();
        Map<String, String> headMap = getHEAD().getFbMap();
        Set<String> headFiles = headMap.keySet();
        Map<String, String> stageMap = stage.getFbMap();
        ArrayList<String> stageAdded = stage.getAdded();
        ArrayList<String> stageRemoved = stage.getRemoved();
        ArrayList<String> currFiles = new ArrayList<>(plainFilenamesIn(CWD));
       
        //files to be checked
        ArrayList<String> checkFiles = new ArrayList<>(); 
        checkFiles.addAll(headFiles);
        
        //removes duplicates
        checkFiles.removeAll(currFiles);
        checkFiles.addAll(currFiles);

        for (String checkFile : checkFiles) {
            if (headFiles.contains(checkFile)) {
                String headContents = readContentsAsString(join(BLOBS_DIR, headMap.get(checkFile)));
                if (currFiles.contains(checkFile)) {
                    String cwdContents = readContentsAsString(join(CWD, checkFile));
                    if (!stageAdded.contains(checkFile) && !cwdContents.equals(headContents)) {
                        modifications.put(checkFile, "(modified)");
                    }
                } else if (!stageRemoved.contains(checkFile)) {
                    modifications.put(checkFile, "(deleted)");
                }
            }
            
            if (stageAdded.contains(checkFile)) {
                String stageContents = readContentsAsString(join(BLOBS_DIR, stageMap.get(checkFile)));
                if (currFiles.contains(checkFile)) {
                    String cwdContents = readContentsAsString(join(CWD, checkFile));
                    if (!cwdContents.equals(stageContents)) {
                        modifications.put(checkFile, "(modified)");
                    }
                } else {
                    modifications.put(checkFile, "(deleted)");
                }
            }
        }
        return modifications;
    }


    private static Commit findCommit(String commitID, File commitsDir) {
        int length = commitID.length();
        List<String> commitList = plainFilenamesIn(commitsDir);
        for (String commitName : commitList) {
            if (commitName.substring(0, length).equals(commitID.substring(0, length))) {
                return readObject(join(commitsDir, commitName), Commit.class);
            }
        }
        return null;
    }


    public static void checkoutFile(String filename) {
        //takes head commit file
        Map<String, String> commitFbMap = getHEAD().getFbMap();
        if (commitFbMap.containsKey(filename)) {
            File commitBlobAddr = join(BLOBS_DIR, commitFbMap.get(filename));
            byte[] commitBlob = readContents(commitBlobAddr);
            writeContents(join(CWD, filename), commitBlob);
            return;
        }
        System.out.println("File does not exist in that commit.");
    }


    public static void checkoutIDFile(String commitID, String filename) {
        //takes file of commit with SHA-1 ID commitID
        Commit commit = findCommit(commitID, COMMITS_DIR);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
            return;
        } else {
            Map<String, String> commitFbMap = commit.getFbMap();
            if (commitFbMap.containsKey(filename)) {
                File commitBlobAddr = join(BLOBS_DIR, commitFbMap.get(filename));
                byte[] commitBlob = readContents(commitBlobAddr);
                writeContents(join(CWD, filename), commitBlob);
                return;
            }
            System.out.println("File does not exist in that commit.");
        }
    }


    public static void checkoutBranch(String branchName) {
    	//navigates to branch, puts branch files into working dir
        branchName = branchName.replace("/", "-");
        ArrayList<String> branches = getBranches();
        String currBranch = branches.get(0);

        if (!branches.contains(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }
        Commit branch = getBranchCommit(branchName, JGIT_DIR);
        String branchID = branch.getID();

        if (currBranch.equals(branchName)) { 
            System.out.println("No need to checkout the current branch.");
            return;
        }

        branches.remove(branchName);
        branches.add(0, branchName);
        saveBranches(branches);
        writeObject(HEAD_FILE, branchID);
        reset(branchID, true, CWD);
    }


    private static ArrayList<String> getUntracked(Commit stage) {
        // gets untracked files -- files in working dir that arent staged nor committed and tracked
        ArrayList<String> currFiles = new ArrayList<String>(plainFilenamesIn(CWD));
        ArrayList<String> trackedFiles = new ArrayList<>();
        trackedFiles.addAll(stage.getAdded());
        trackedFiles.addAll(stage.getFbMap().keySet());
        currFiles.removeAll(trackedFiles);
        return currFiles;
    }


    public static void branch(String branchName) {
    	//creates branch, but stays on current branch, doesn't navigate immediately
        Commit head = getHEAD();
        ArrayList<String> branches = getBranches();
        if (branches.contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        saveBranch(branchName, head.getID());
        branches.add(branchName);
        saveBranches(branches);
    }


    public static void rmBranch(String branchName) {
    	//removes branch if possible, can't remove master branch
        ArrayList<String> branches = getBranches();
        if (!branches.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");

        } else if (branchName.equals(branches.get(0))) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branches.remove(branchName);
        }

        saveBranches(branches);
    }


    public static void reset(String commitName, boolean isCheckout, File workingDir) {
        // resets file to working directory through commit, pretty much  reset --hard in actual git
    	File gitletDir = join(workingDir, ".jgitrepo");
        Commit stage = getStage();
        Map<String, String> stageMap = stage.getFbMap();
        Set<String> stageFiles = stageMap.keySet();
        Commit checkout = findCommit(commitName, join(gitletDir, "Commits"));
        if (checkout == null) { // If no commit with the given id exists
            System.out.println("No commit with that id exists.");
            return;
        }
        
        Map<String, String> checkoutMap = checkout.getFbMap();
        Set<String> checkoutFiles = checkoutMap.keySet();
        List<String> untracked = getUntracked(stage);
        
        //can't reset if there's untracked files
        for (String untrackedFile : untracked) {
            if (checkoutFiles.contains(untrackedFile)) {
                System.out.println("There is an untracked file in the way. delete it, or add and commit it first.");
            }
        }
        
        //removes tracked files that are not present in that commit.
        for (String stageFilename : stageFiles) {
            if (!checkoutFiles.contains(stageFilename)) {
                restrictedDelete(join(workingDir, stageFilename));
            }
        }
        
        //checks out all the files tracked by the given commit.
        for (String checkFilename : checkoutFiles) {
            byte[] commitBlob = readContents(join(join(gitletDir, "Blobs"),
                    checkoutMap.get(checkFilename)));
            writeContents(join(workingDir, checkFilename), commitBlob);
        }
        
        //moves the current branch’s head to that commit node.
        String checkoutID = checkout.getID();
        if (!isCheckout) {
            writeObject(join(gitletDir, "_head"), checkoutID);
            String currBranch = getBranches().get(0);
            saveBranch(currBranch, checkoutID);
        }
        
        //stage is cleared
        stage = new Commit(checkoutID);
        saveStage(stage);
    }


    public static void merge(String mergingBranchName) {
        Commit stage = getStage();
        if (checkStageFails(stage)) {
            return;
        }
        ArrayList<String> branches = getBranches();
        String currBranchName = branches.get(0);
        if (checkBranchFails(currBranchName, mergingBranchName, branches)) {
            return;
        }
        String mergingBranch = getBranchCommit(mergingBranchName, JGIT_DIR).getID();
        String currBranch = getHEAD().getID();
        String splitPoint = getSplitPoint(currBranch, mergingBranch);
        if (checkBranchFails(currBranch, mergingBranch, mergingBranchName, splitPoint)) {
            return;
        }
        Map<String, String> splitPointMap = findCommit(splitPoint, COMMITS_DIR).getFbMap();
        Map<String, String> currBranchMap = findCommit(currBranch, COMMITS_DIR).getFbMap();
        Map<String, String> mergingBranchMap = findCommit(mergingBranch, COMMITS_DIR).getFbMap();
        Set<String> checkoutFiles = mergingBranchMap.keySet();
        List<String> untracked = getUntracked(stage);
        
        
        for (String untrackedFile : untracked) {
            //can't checkout if there are untracked files
            if (checkoutFiles.contains(untrackedFile)) {
                System.out.println("There is an untracked file in the way. delete it, or add and commit it first.");
                return;
            }
        }
        
        ArrayList<Map<String, String>> mergeCommits = new ArrayList<>(Arrays.asList(splitPointMap, currBranchMap, mergingBranchMap));
        ArrayList<String> mergeFiles = getMergeFiles(mergeCommits);
        boolean conflicted = false;
        Commit newStage = new Commit(currBranch, mergingBranch);
        
        //checks for merge conflicts
        for (String mergeFile : mergeFiles) {
        	//gets blob from split point
            String splitPointBlob = splitPointMap.getOrDefault(mergeFile, "");
            //gets blob from current branch
            String currBranchBlob = currBranchMap.getOrDefault(mergeFile, "");
            //gets blob from incoming merge
            String mergingBranchBlob = mergingBranchMap.getOrDefault(mergeFile, "");
            
            boolean inSplitPoint = !splitPointBlob.equals("");
            boolean inCurrBranch = !currBranchBlob.equals("");
            boolean inMergingBranch = !mergingBranchBlob.equals("");
            boolean currBranchModded = !splitPointBlob.equals(currBranchBlob);
            boolean mergingBranchModded = !splitPointBlob.equals(mergingBranchBlob);
            
            if (inSplitPoint && inCurrBranch && inMergingBranch) {
                if (!currBranchModded && mergingBranchModded) {
                    writeContents(join(CWD, mergeFile), getBlob(mergingBranchBlob, BLOBS_DIR));
                    newStage.add(mergeFile);
                }
            }
            
            //writes in conflicts in file
            if (currBranchModded && mergingBranchModded) {
                if (!currBranchBlob.equals(mergingBranchBlob)) {
                    conflicted = true;
                    String currBranchStr = getBranchStr(inCurrBranch, currBranchBlob);
                    String mergingBranchStr = getBranchStr(inMergingBranch, mergingBranchBlob);
                    String conflict = "<<<<<<< HEAD\n" + currBranchStr + "=======\n"
                            + mergingBranchStr + ">>>>>>>\n";
                    writeContents(join(CWD, mergeFile), conflict);
                    newStage.add(mergeFile);
                } 
            }
            if (!inSplitPoint && !inCurrBranch && inMergingBranch) {
                writeContents(join(CWD, mergeFile), getBlob(mergingBranchBlob, BLOBS_DIR));
                newStage.add(mergeFile);
            }
            if (inSplitPoint && !currBranchModded && !inMergingBranch) {
                newStage.remove(mergeFile);
            }
        }
        
        
        saveStage(newStage);
        Repository.commit("Merged " + mergingBranchName + " into " + currBranchName + ".");
        saveBranch(currBranch, newStage.getID());
        saveBranches(branches);
        if (conflicted) {
            System.out.println("Encountered a merge conflict.");
        }
    }


    private static String getBranchStr(boolean inBranch, String branchBlob) {
        String branchStr = "";
        if (inBranch) {
            branchStr = readContentsAsString(join(BLOBS_DIR, branchBlob));
        }
        return branchStr;
    }


    private static boolean checkStageFails(Commit stage) {
        //if there are staged additions or removals present
        if (!stage.getAdded().isEmpty() || !stage.getRemoved().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return true;
        }
        return false;
    }


    private static boolean checkBranchFails(String currBranchName, String mergingBranchName, ArrayList<String> branches) {
        if (!branches.contains(mergingBranchName)) {
            System.out.println("A branch with that name does not exist.");
            return true;
        }
        //if attempting to merge a branch with itself
        if (currBranchName.equals(mergingBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }
        return false;
    }


    private static boolean checkBranchFails(String currBranch, String mergingBranch, String mergingBranchName, String splitPoint) {
        //if the split point is the same commit as the given branch
        if (splitPoint.equals(mergingBranch)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return true;
        }
        //if the split point is the current branch
        if (splitPoint.equals(currBranch)) {
            checkoutBranch(mergingBranchName);
            System.out.println("Current branch fast-forwarded.");
            return true;
        }
        return false;
    }


    private static String getSplitPoint(String currBranch, String mergingBranch) {
        String splitPoint = null;

        //ID of currBranch and all ancestors
        ArrayList<String> currBranchCommits = findAllAncestors(currBranch, COMMITS_DIR);
        //ID of mergingBranch and all ancestors
        ArrayList<String> mergingBranchCommits = findAllAncestors(mergingBranch, COMMITS_DIR);

        currBranchCommits.retainAll(mergingBranchCommits);
        if (!currBranchCommits.isEmpty()) {
            splitPoint = currBranchCommits.get(currBranchCommits.size() - 1);
        }
        return splitPoint;
    }


    private static ArrayList<String> findAllAncestors(String currBranchID, File commitsDir) {
        ArrayList<String> ancestorCommits = new ArrayList<>();
        String parent1 = findCommit(currBranchID, commitsDir).getParentID();
        String parent2 = findCommit(currBranchID, commitsDir).getParentID2();

        if (parent1 != null) {
            ancestorCommits.addAll(findAllAncestors(parent1, commitsDir));
        }
        if (parent2 != null) {
            ancestorCommits.addAll(findAllAncestors(parent2, commitsDir));
        }
        ancestorCommits.add(currBranchID);
        return ancestorCommits;
    }


    private static ArrayList<String> getMergeFiles(ArrayList<Map<String, String>> mergeCommits) {
    	//gets files of merge
        ArrayList<String> mergeFiles = new ArrayList<>();
        for (Map<String, String> mergeCommitMap : mergeCommits) {
            Set<String> mergeCommitFiles = mergeCommitMap.keySet();
            for (String mergeCommitFile : mergeCommitFiles) {
                if (!mergeFiles.contains(mergeCommitFile)) {
                    mergeFiles.add(mergeCommitFile);
                }
            }
        }
        return mergeFiles;
    }
}
