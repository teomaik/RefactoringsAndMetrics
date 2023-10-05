import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {

    /**
     * Checkout the Project in user.dir
     * for windows and linux
     */
    public static void checkoutGitProject(String pathName, String sha) {
        if (isWindows()) {
            try {
                //Change dir and then clone
                Process proc = Runtime.getRuntime().exec("cmd /c cd " +System.getProperty("user.dir")+ "\\" + pathName
                        + " && git checkout -f " + sha + "");
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String inputLine;
                while ((inputLine = inputReader.readLine()) != null) {
                    System.out.println(inputLine);
                }
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.out.println(errorLine);
                }
                System.out.println("Clone DONE!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                ProcessBuilder pbuilder = new ProcessBuilder("bash", "-c",
                        "cd '" + System.getProperty("user.dir") +"/"+ pathName+"' ; git checkout -f " +sha);
                File err = new File("err.txt");
                pbuilder.redirectError(err);
                Process p = pbuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                BufferedReader reader_2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line_2;
                while ((line_2 = reader_2.readLine()) != null) {
                    System.out.println(line_2);
                }
                System.out.println("Clone DONE!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Find previous commit from the given one
     * @param sha
     */
    public static String findPreviousSha(String sha, String pathName) {
        String previousSha= "";
        if (isWindows()) {
            try {
                Process proc = Runtime.getRuntime().exec("cmd /c cd " +System.getProperty("user.dir")+ "\\" + pathName +
                        " && git rev-list --parents -n 1 " + sha + "");
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String inputLine;
                while ((inputLine = inputReader.readLine()) != null) {
                    System.out.println(inputLine);
                    String prSha = inputLine.replace(sha + " ", "");
                    if (prSha.contains(" ")){
                        previousSha = prSha.split(" ")[prSha.split(" ").length-1];
                    }
                    else {
                        previousSha = inputLine.replace(sha + " ", "");
                    }
                }
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.out.println(errorLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                ProcessBuilder pbuilder = new ProcessBuilder("bash", "-c",
                        "cd '" + System.getProperty("user.dir") +"/"+ pathName+"' ; git rev-list --parents -n 1 " +sha);
                File err = new File("err.txt");
                pbuilder.redirectError(err);
                Process p = pbuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    String prSha = line.replace(sha + " ", "");
                    if (prSha.contains(" ")){
                        previousSha = prSha.split(" ")[prSha.split(" ").length-1];
                    }
                    else {
                        previousSha = line.replace(sha + " ", "");
                    }
                }
                BufferedReader reader_2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line_2;
                while ((line_2 = reader_2.readLine()) != null) {
                    System.out.println(line_2);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return previousSha;
    }

    /**
     * Clone the Project in user.dir
     * for windows and linux
     */
    public static void cloneGitProject(String GitURL) {
        if (isWindows()) {
            try {
                //Change dir and then clone
                Process proc = Runtime.getRuntime().exec("cmd /c cd " +System.getProperty("user.dir")+ " && "
                        + "git clone " + GitURL + "");
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String inputLine;
                while ((inputLine = inputReader.readLine()) != null) {
                    System.out.println(inputLine);
                }
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.out.println(errorLine);
                }
                System.out.println("Clone DONE!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                ProcessBuilder pbuilder = new ProcessBuilder("bash", "-c",
                        "cd '" + System.getProperty("user.dir") + "' ; git clone " + GitURL + "");
                File err = new File("err.txt");
                pbuilder.redirectError(err);
                Process p = pbuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                BufferedReader reader_2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line_2;
                while ((line_2 = reader_2.readLine()) != null) {
                    System.out.println(line_2);
                }
                System.out.println("Clone DONE!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Is Windows the OS
     * @return
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

}
