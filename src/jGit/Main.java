//pds 2022
//bmi23
package jGit;

import static jGit.Utils.join;

import java.io.File;
import java.util.Arrays;

public class Main {
	
	//initializes directory that version control system will be established in
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File JGIT_DIR = join(CWD, ".jgitrepo");


    public static void main(String[] args) {
    	//takes in java jGit.Main ARGS, where ARGS are commands and operands
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        if (!JGIT_DIR.isDirectory() && !firstArg.equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        switch(firstArg) {
            case "init":
            	//initializes directory as jGit version control
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.setupPersistence();
                break;

            case "add":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.add(args[1]);
                break;

            case "commit":
                if (args.length == 1) {
                    System.out.println("Please enter a commit message.");
                    break;
                } else if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                } else if (args[1].isBlank() || args[1].isEmpty()) {
                    System.out.println("Please enter a commit message.");
                    break;
                }
                Repository.commit(args[1]);
                break;

            case "rm":
            	//removes file from stage
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.rm(args[1]);
                break;

            case "log":
            	//prints out log of all tracked commits
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.log();
                break;

            case "global-log":
            	//prints out log but contains all commits ever made, even commits removed/reset
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.globalLog();
                break;

            case "status":
            	//prints out status log
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.status();
                break;

            case "checkout":
            	//3 uses:
            	//1. navigates to branch, replaces files in working directory with branch files
            	//2. takes version of file in HEAD commit and puts it in working directory, replacing old file
            	//3. takes file of commit ID (SHA-1) and puts it in working directory, replacing old file
                if (args.length == 3 && args[1].equals("--")) { 
                    Repository.checkoutFile(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) { 
                    Repository.checkoutIDFile(args[1], args[3]);
                } else if (args.length == 2) { 
                    Repository.checkoutBranch(args[1]);
                } else {
                    System.out.println("Incorrect operands.");
                    break;
                }
                break;

            case "branch":
            	//creates branch
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.branch(args[1]);
                break;

            case "rm-branch":
            	//removes branch
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.rmBranch(args[1]);
                break;

            case "reset":
            	//resets to previous commit
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.reset(args[1], false, CWD);
                break;

            case "merge":
            	//merges two branches
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                Repository.merge(args[1]);
                break;

            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }
}
