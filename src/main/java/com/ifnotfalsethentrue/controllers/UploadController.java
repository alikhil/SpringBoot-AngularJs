package com.ifnotfalsethentrue.controllers;

import com.ifnotfalsethentrue.models.CheckResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;


@RestController
public class UploadController {

    @RequestMapping(value="/upload", method=RequestMethod.POST)
    public @ResponseBody CheckResult handleFileUpload(@RequestParam("file") MultipartFile file){
        if (!file.isEmpty()) {
            try {
                String filename = file.getOriginalFilename();
                String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());

                if (!extension.equalsIgnoreCase("java"))
                    return new CheckResult("Choose .java file!");

                if (file.getSize() > 1024 * 1024)
                    return new CheckResult("File must be less than 1MB");

                String path = uploadJavaFile(file);
                ArrayList<String> results = checkStyle(path);
                for (int i = 0; i < results.size();i++ ) {
                    String result = results.get(i);
                    results.set(i, result.replace(path, filename));
                }
                deleteJavaFile(path);
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

    private void deleteJavaFile(String path) throws IOException {
        Files.deleteIfExists(Paths.get(path));
        String dirName = path.substring(0, path.lastIndexOf("\\"));
        Files.deleteIfExists(Paths.get(dirName));
    }

    private static ArrayList<String> checkStyle(String path) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String checkerRunCommand = getChecker(System.getProperty("os.name"));
        Process proc = rt.exec(checkerRunCommand + path);

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

    private static String getChecker(String osName) {
        if (osName.contains("Windows"))
            return "cmd.exe /c checkstyle-algs4 ";
        return "checkstyle-algs4 ";
    }

    private String uploadJavaFile (MultipartFile file) throws IOException {
        String sRootPath = new File("").getAbsolutePath();
        String filesDirectory = sRootPath + "\\javaUploads\\" + UUID.randomUUID().toString();
        String name = filesDirectory + "\\" + file.getOriginalFilename();

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
