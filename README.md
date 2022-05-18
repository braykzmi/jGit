# jGit - Java Implementation of Version Control System Git
Programming Data Structures 2022 Final Project
Brayden Mi

This project is a version control system implemented in Java. The main functionalities included in this project are:

	1. Saving contents of entire directories of files, otherwise known as commiting

	2. Restoring one or more files or entire commits. Otherwise known as checking out.

	3. Viewing history of commits, otherwise known as viewing the log

	4. Maintain different versions of commits in parallel, otherwise known as branching

	5. Merging branches together

The basis of the program relies on three structures: blobs, trees, and commits
	
	Blobs - Binary large objects, essentially the saved contents of the files

	Trees - Directory structure mapping names to blobs and other sub-trees (subdirectories)

	Commits - Combination of log messages, date, and SHA-1 ID, identifying one version of directory

To use: 

	1. Compile java program, navigate to the package directory (jGit) in cmd and run <javac Commit.java Dumpable.java GitException.java Main.java Repository.java Utils.java>

	2. Navigate out of the package directory to source directory (src) - <cd ..>

	3. Initialize repository <java jGit.Main init>

	4. Input any command (command list below), the source directory is set up as the working version control directory

Commands: in the form of java jGit.Main [Command] [Operands]  <br />
	init 				- initializes a version control system in src directory, creates .jgitrepo directory <br />
	add [filename] 			- adds a copy of the file as it currently exists into the staging area <br />
	commit [message]		- saves a snapshot of tracked file under a SHA-1 ID and commit message [message] <br />
	rm [filename]			- removes file from staging area <br />
	log				- prints out log starting from head commit <br />
	global-log			- prints out log starting from head commit, showing every commit ever made <br />
	status				- displays branches, marking current branch with an asterisk, shows staged files,  <br />
					removed files, modified not yet staged files, and untracked files <br />
	checkout --[filename]		- takes version of file as it exists in the head commit and adds it to the working directory <br />
	checkout [id] --[filename]	- takes version of file of commit id [id] and adds it to the working directory <br />
	checkout [branchname]		- switches branches to [branchname] <br />
	branch [branchname]		- creates branch with name [branchname] <br />
	rm-branch [branchname]		- removes branch with name [branchname] <br />
	reset [id]			- checks out all files at that commit id <br />
	merge [branchname]		- merges branch [branchname] with current branch <br />
