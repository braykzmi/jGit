//pds 2022
//bmi23
package jGit;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

class Utils {

    public static final int SHA1_LENGTH = 40;
    
    public static String sha1(Object... vals) {
    	//returns SHA-1 hash of vals, which can be any mixture of byte arrays and strings
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (Object val : vals) {
                if (val instanceof byte[]) {
                    md.update((byte[]) val);
                } else if (val instanceof String) {
                    md.update(((String) val).getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new IllegalArgumentException("improper type to sha1");
                }
            }
            Formatter result = new Formatter();
            for (byte b : md.digest()) {
                result.format("%02x", b);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException excp) {
            throw new IllegalArgumentException("System does not support SHA-1");
        }
    }

    public static String sha1(List<Object> vals) {
    	//returns SHA-1 hash of vals
        return sha1(vals.toArray(new Object[vals.size()]));
    }

    public static boolean restrictedDelete(File file) {
    	//deletes file if it exists and is not in directory
    	//true if file was deleted, otherwise false
    	//only works if directory contains .jgitrepo
        if (!(new File(file.getParentFile(), ".jgitrepo")).isDirectory()) {
            throw new IllegalArgumentException("not .jgitrepo working directory");
        }
        if (!file.isDirectory()) {
            return file.delete();
        } else {
            return false;
        }
    }

    public static boolean restrictedDelete(String file) {
        return restrictedDelete(new File(file));
    }

    public static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    
    public static String readContentsAsString(File file) {
        return new String(readContents(file), StandardCharsets.UTF_8);
    }

    public static void writeContents(File file, Object... contents) {
    	//writes files
        try {
            if (file.isDirectory()) {
                throw
                    new IllegalArgumentException("cannot overwrite directory");
            }
            BufferedOutputStream str =
                new BufferedOutputStream(Files.newOutputStream(file.toPath()));
            for (Object obj : contents) {
                if (obj instanceof byte[]) {
                    str.write((byte[]) obj);
                } else {
                    str.write(((String) obj).getBytes(StandardCharsets.UTF_8));
                }
            }
            str.close();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    public static <T extends Serializable> T readObject(File file, Class<T> expectedClass) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException | ClassCastException | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    public static void writeObject(File file, Serializable obj) {
    	//writes OBJ to FILE
        writeContents(file, serialize(obj));
    }

    
    private static final FilenameFilter PLAIN_FILES =  new FilenameFilter() {
            @Override
            //overrides functions within FilenameFilter. filters out all but plain files
            public boolean accept(File dir, String name) {
                return new File(dir, name).isFile();
            }
        };

    public static List<String> plainFilenamesIn(File dir) {
        //returns list of names of all plain files in directory
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    public static List<String> plainFilenamesIn(String dir) {
        return plainFilenamesIn(new File(dir));
    }

    public static File join(String first, String... others) {
    	//concatenation of first and others into File designator
        return Paths.get(first, others).toFile();
    }

    public static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }

    public static byte[] serialize(Serializable obj) {
    	//returns byte array of serialized contents of obj
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            throw error("Internal error serializing commit.");
        }
    }

    public static GitException error(String msg, Object... args) {
    	//returns GitException of msg and args
        return new GitException(String.format(msg, args));
    }

    public static void message(String msg, Object... args) {
    	//prints message composed from msg and args, followed by a new line
        System.out.printf(msg, args);
        System.out.println();
    }
}
