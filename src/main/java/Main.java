import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.Matcher;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;


public class Main {


    public static String runAnalysis(String gitURL){
        //Get url and name
        gitURL= gitURL.replace(".git","");
        String projectName = gitURL.split("/")[gitURL.split("/").length-1];
        String projectPath = System.getProperty("user.dir") +File.separator+ projectName;


        //Get refactorings
        List<CommitBeforeRef> commitBeforeRefs = new ArrayList<>();
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        try {
            Repository repo = gitService.cloneIfNotExists(
                    projectName,
                    gitURL);

            miner.detectAll(repo, "master", new RefactoringHandler() {
                @Override
                public void handle(String commitId, List<Refactoring> refactorings) {
                    if(!refactorings.isEmpty()) {
                        //Create CommitBeforeRef
                        List<String> refactoringTypes = new ArrayList<>();
                        List<String> involvedFilesBeforeRefactoring = new ArrayList<>();
                        for (Refactoring ref: refactorings){
                            refactoringTypes.add(ref.getRefactoringType().toString());
                            for(ImmutablePair<String, String> immutablePair: ref.getInvolvedClassesBeforeRefactoring()){
                                involvedFilesBeforeRefactoring.add(immutablePair.left);
                            }
                        }
                        CommitBeforeRef commitBeforeRef = new CommitBeforeRef(commitId, refactoringTypes, involvedFilesBeforeRefactoring);
                        commitBeforeRefs.add(commitBeforeRef);
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //Get extra information for each refactoring
        System.out.println("Total refactorings: " + commitBeforeRefs.size());
        for( CommitBeforeRef commitBeforeRef: commitBeforeRefs){
            System.out.println("Analyzing "+ commitBeforeRef.refactoringCommit);
            //Save previous SHA
            String previousSha = Utils.findPreviousSha(commitBeforeRef.getRefactoringCommit(),projectName);
            commitBeforeRef.setCommitBeforeRefactoring(previousSha);

            //Checkout previous SHA
            Utils.checkoutGitProject(projectName,previousSha);

            //Analyze commit and save
            Analysis analysis = new Analysis(projectPath);
            analysis.StartAnalysis();
            commitBeforeRef.setAnalysis(analysis);
        }

        //gather data for Csv file
        ArrayList<String> csvLines = new ArrayList();
        csvLines.add("projectName,SHA,file,rank,DSC,WMC,DIT,CC,LCOM,MPC,NOM,RFC,DAC,NOCC,CBO,SIZE1,SIZE2,REFACTORED");
        String cwdPath = System.getProperty("user.dir");

        for( CommitBeforeRef commitBeforeRef: commitBeforeRefs){
            Analysis tempAnalysis = commitBeforeRef.getAnalysis();
            ArrayList<JavaFile> javaFiles = tempAnalysis.getJavaFiles();
            Hashtable<String, String> classes = new Hashtable<String, String>();
            for( JavaFile tempFile: javaFiles){
                String line = projectName + ", "+commitBeforeRef.getCommitBeforeRefactoring();
//File.separator
//                String filePath = tempFile.getPath().replaceFirst((projectPath + File.separator), "");
                String filePath = tempFile.getPath().replace(File.separator, "/");
                filePath = filePath.replaceFirst("/", "");

                line += ","+ filePath;
                line += ","+ tempFile.getRank();
                line += ","+ tempFile.getDSC();
                line += ","+ tempFile.getWMC();
                line += ","+ tempFile.getDIT();
                line += ","+ tempFile.getCC();
                line += ","+ tempFile.getLCOM();
                line += ","+ tempFile.getMPC();
                line += ","+ tempFile.getNOM();
                line += ","+ tempFile.getRFC();
                line += ","+ tempFile.getDAC();
                line += ","+ tempFile.getNOCC();
                line += ","+ tempFile.getCBO();
                line += ","+ tempFile.getSIZE1();
                line += ","+ tempFile.getSIZE2();
                classes.put(filePath, line);

                System.out.println(line);
            }
//            String listString = String.join(", ", refactoredClasses);
//            System.out.println(listString);
//            List<String> refactoredClasses = commitBeforeRef.getInvolvedFilesBeforeRefactoring();

            ArrayList<String> refactoredClasses = new ArrayList<>();
            Set<String> set = new HashSet<>(commitBeforeRef.getInvolvedFilesBeforeRefactoring());
            refactoredClasses.addAll(set);
            for(String refClass: refactoredClasses){
                classes.put(refClass, classes.get(refClass)+",true");
            }

            classes.forEach((k, ln) -> {
                csvLines.add(ln);
            });
        }
        String join = String.join("\n ", csvLines);
        return join;
    }
    public static void main(String[] args) {
        //Get url and name
        ArrayList<String> csvs = new ArrayList<>();
        ArrayList<String> projects = new ArrayList<>();
        projects.add("");
        projects.add("");
        projects.add("");
        projects.add("");
        projects.add("");
        projects.add("");
        projects.add("");
        projects.add("");
        projects.add("");
        projects.add("");

        for(String prj: projects){
            csvs.add(runAnalysis(prj));
        }



        //create csv file
        try {
            FileWriter writer = new FileWriter(new File(System.getProperty("user.dir")+"/data_"+projectName+".csv"));
//            writer.write("projectName,SHA,file,rank,DSC,WMC,DIT,CC,LCOM,MPC,NOM,RFC,DAC,NOCC,CBO,SIZE1,SIZE2,REFACTORED" + System.lineSeparator());

            String listString = String.join("\n ", csvs);
            writer.write(listString);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}