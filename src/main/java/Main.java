import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;


public class Main {


    public static void main(String[] args) {
        //Get url and name
        String gitURL = "https://github.com/dimizisis/metrics_calculator";
        gitURL= gitURL.replace(".git","");
        String projectName = gitURL.split("/")[gitURL.split("/").length-1];
        String projectPath = System.getProperty("user.dir") +"/"+ projectName;


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


//        //create csv file
//        try {
//            FileWriter writer = new FileWriter(new File(System.getProperty("user.dir")+"/data_"+projectName+".csv"));
//            writer.write("projectName;PR_id;previousSHA;nextSHA;file;" + System.lineSeparator());
//            writer.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

    }
}