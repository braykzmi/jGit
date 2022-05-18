//pds 2022
//bmi23
package jGit;

class GitException extends RuntimeException {

    GitException() {
    	//GitException with no message
    	super();
    }

    GitException(String msg) {
    	//GitException with msg as message
    	super(msg);
    }

}
