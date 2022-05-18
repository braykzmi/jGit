//pds 2022
//bmi23
package jGit;

import static jGit.Utils.*;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class Commit implements Serializable {
    private String message;
    // parent commit address
    private String parentID;
    // parent2 commit address, only for merged Commits
    private String parentID2;
    // date of commit
    private Date timestamp;
    // map of file addresses to blob addresses
    private Map<String, String> fbMap;
    // SHA-1 ID of commit.
    private String ID;
    // added (staged) files
    private ArrayList<String> added;
    // removed files
    private ArrayList<String> removed;


    public Commit(String parentAddr) {
        //commits file to repository
        this.parentID = parentAddr;
        this.parentID2 = null;
        added = new ArrayList<>();
        removed = new ArrayList<>();
        if (parentAddr != null) { // clones fbMap from parent
            Commit parent = readObject(join(Repository.COMMITS_DIR, this.parentID), Commit.class);
            fbMap = parent.getFbMap();
        } else {
            fbMap = new TreeMap<>();
        }
    }

    public Commit(String parent1, String parent2) {
        //commits file to repository
        this.parentID = parent1;
        this.parentID2 = parent2;
        added = new ArrayList<>();
        removed = new ArrayList<>();
        if (parent1 != null) { // clones fbMap from parent
            Commit parent = readObject(join(Repository.COMMITS_DIR, this.parentID), Commit.class);
            fbMap = parent.getFbMap();
        } else {
            fbMap = new TreeMap<>();
        }
    }


    public Boolean commit(String msg, Date time) {
    	// returns false if commit was successful, false if unsuccessful
        if (added.isEmpty() && removed.isEmpty() && !msg.equals("initial commit")) {
            System.out.println("No changes added to the commit.");
            return false;
        }
        this.message = msg;
        this.timestamp = time;
        this.ID = Utils.sha1(serialize(this));
        return true;
    }


    public void add(String filename) {
        // get file's blob's ID and compare with HEAD's file's blob's ID
        File fileAddr = join(Repository.CWD, filename);
        if (fileAddr.isFile()) {
            byte[] fileBlob = readContents(fileAddr);
            String fileBlobID = sha1(fileBlob);
            Commit head = Repository.getHEAD();
            if (head.getFbMap().containsKey(filename)) {
                // if file blob is same as old HEAD blob, then revert to HEAD blob
                String headBlobID = head.getBlobID(filename);
                if (fileBlobID.equals(headBlobID)) {
                    if (added.contains(filename)) {
                        added.remove(filename);
                        fbMap.put(filename, headBlobID);
                    }
                    if (removed.contains(filename)) {
                        removed.remove(filename);
                        fbMap.put(filename, headBlobID);
                    }
                } else {  
                	// adds new blob if contents have been changed
                    saveBlob(fileBlob);
                    fbMap.put(filename, fileBlobID);
                    if (!added.contains(filename)) {
                        added.add(filename);
                    }
                }
            } else { 
            	// adds new blob if HEAD does not have the file
                saveBlob(fileBlob);
                fbMap.put(filename, fileBlobID);
                added.add(filename);
            }
        } else {
            System.out.println("File does not exist.");
        }
    }


    public String getBlobID(String filename) {
        return fbMap.get(filename);
    }


    private String saveBlob(byte[] blob) {
        String blobID = sha1(blob);
        File blobFile = join(Repository.BLOBS_DIR, blobID);
        writeContents(blobFile, blob);
        return blobID;
    }

    public void remove(String filename) {
        // removes file from stage
        File fileAddr = join(Repository.CWD, filename);
        if (added.contains(filename)) {
            added.remove(filename);
            fbMap.remove(filename);
        } else if (fbMap.containsKey(filename)) {
            removed.add(filename);
            fbMap.remove(filename);
            restrictedDelete(fileAddr);
        } else {
            System.out.println("No reason to remove the file.");
        }
    }


    public void printLog() {
        System.out.print("===\n");
        System.out.print("commit " + this.ID + "\n");
        String date;
        if (parentID == null) {
            date = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            String pattern = "EEE MMM dd HH:mm:ss yyyy Z";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            date = simpleDateFormat.format(timestamp);
        }
        System.out.print("Date: " + date + "\n");
        System.out.print(message + "\n");
        System.out.print("\n");
    }


    
    
    public String getMessage() {
        return message;
    }

    public String getParentID() {
        return parentID;
    }

    public String getParentID2() {
        return parentID2;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getFbMap() {
        return fbMap;
    }

    public String getID() {
        return ID;
    }

    public ArrayList<String> getAdded() {
        return added;
    }

    public ArrayList<String> getRemoved() {
        return removed;
    }
}
