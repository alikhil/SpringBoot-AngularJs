package com.ifnotfalsethentrue.controllers;

import com.ifnotfalsethentrue.models.CheckResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
public class UploadController {

    @RequestMapping(value="/upload", method=RequestMethod.POST)
    public @ResponseBody CheckResult uploadAndCheckStyle(@RequestParam("file") MultipartFile file){
        if (!file.isEmpty()) {
            try {
                String filename = file.getOriginalFilename();
                String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());

                if (!extension.equalsIgnoreCase("java"))
                    return new CheckResult("Choose .java file!");

                if (file.getSize() > 1024 * 1024)
                    return new CheckResult("File must be less than 1MB");

                String path = uploadFile(file);
                ArrayList<String> results = executeCommand("checkstyle-algs4", path);
                replaceStringsInList(filename, path, results);
                deleteGeneratedFiles(path);
                return new CheckResult(results);
            }
            catch (IOException e) {
                e.printStackTrace();
                return new CheckResult(e.getMessage());
            }
        } else {
            return new CheckResult("You failed to upload " + file.getOriginalFilename() + " because the file was empty.");
        }
    }

    @RequestMapping(value="/saveToFile", method=RequestMethod.POST)
    public @ResponseBody CheckResult saveToFileAndCheckStyle(@RequestParam("code") String code) throws IOException {
        if (code.length() != 0) {
            if (code.length() > 1024 * 1024)
                return new CheckResult("File must be less than 1MB.");
            String filename = getFileName(code);
            System.out.println("filename:" + filename);
            String path = saveToFile(code, filename);
            ArrayList<String> results = executeCommand("checkstyle-algs4", path);
            replaceStringsInList(filename, path, results);
            deleteGeneratedFiles(path);
            return new CheckResult(results);
        } else {
            return new CheckResult("You failed to upload code.");
        }
    }

    private String saveToFile(String code, String filename) throws IOException {

        String sRootPath = new File("").getAbsolutePath();
        String filesDirectory = String.format("%1$s%2$sjavaUploads%2$s%3$s", sRootPath,
                getDirSeparator(), UUID.randomUUID().toString());
        String path = filesDirectory + getDirSeparator() + filename;

        boolean dirCreated = new File(filesDirectory).mkdirs();
        if (!dirCreated)
            throw new IOException("Can not create a upload directory : " + filesDirectory);

        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(code);
        writer.close();
        return path;
    }

    private String getFileName(String code) {
        Pattern classPattern = Pattern.compile("public class (\\w+)");
        Matcher m = classPattern.matcher(code);
        System.out.println(code);
      if(m.find())
          return m.group(1) + ".java";
      return "NotFound.java";
    }

    private void replaceStringsInList(String newVal, String path, ArrayList<String> results) {
        for (int i = 0; i < results.size();i++ ) {
            String result = results.get(i);
            results.set(i, result.replace(path, newVal));
        }
    }

    private String getDirSeparator() {
        return System.getProperty("os.name").contains("Windows") ? "\\" : "/";
    }

    @RequestMapping(value = "/checkForBugs", method = RequestMethod.POST)
    public @ResponseBody CheckResult uploadAndCheckForBugs(@RequestParam("file") MultipartFile file){
        if (!file.isEmpty()){
            try {
                String filename = file.getOriginalFilename();
                String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());

                if (!extension.equalsIgnoreCase("class"))
                    return new CheckResult("Choose .class file!");

                if (file.getSize() > 5 *1024 * 1024)
                    return new CheckResult("File must be less than 5MB");

                String path = uploadFile(file);
                ArrayList<String> foundBugs =  executeCommand("findbugs-algs4", path);
                replaceStringsInList(filename, path, foundBugs);
                deleteGeneratedFiles(path);
                return new CheckResult(foundBugs);

            } catch (IOException e) {
                e.printStackTrace();
                return new CheckResult(e.getMessage());
            }
        }
        else {
            return new CheckResult("You failed to upload " + file.getOriginalFilename() + " because the file was empty.");
        }
    }

    private ArrayList<String> executeCommand(String command, String path) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String finalCommand = formatCommand(command, path);
        Process proc = rt.exec(finalCommand);

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));

        String s = null;
        ArrayList<String> result = new ArrayList<String>();
        while ((s = stdInput.readLine()) != null) {
            result.add(s);
        }

        while ((s = stdError.readLine()) != null) {
            result.add(s);
        }
        return result;
    }

    private String formatCommand(String command, String path) {
        String commandPrefix = "";
        if (System.getProperty("os.name").contains("Windows"))
            commandPrefix = "cmd.exe /c ";
        return String.format("%1$s%2$s %3$s", commandPrefix, command, path);
    }

    private void deleteGeneratedFiles(String path) throws IOException {
        String dirName = path.substring(0, path.lastIndexOf(getDirSeparator()));
        deleteDirectory(new File(dirName));
    }

    public static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null != files){
                for(int i = 0; i < files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return(directory.delete());
    }

    private String uploadFile(MultipartFile file) throws IOException {
        String sRootPath = new File("").getAbsolutePath();
        String filesDirectory = String.format("%1$s%2$sjavaUploads%2$s%3$s", sRootPath,
                        getDirSeparator(), UUID.randomUUID().toString());
        String name = filesDirectory + getDirSeparator() + file.getOriginalFilename();

        boolean dirCreated = new File(filesDirectory).mkdirs();
        if (!dirCreated)
            throw new IOException("Can not create a upload directory : " + filesDirectory);

        byte[] bytes = file.getBytes();
        BufferedOutputStream stream =
                new BufferedOutputStream(new FileOutputStream(new File(name)));
        stream.write(bytes);
        stream.close();
        return name;
    }
}
