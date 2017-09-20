package jatx.asta_app;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by jatx on 20.09.17.
 */

public class FileUtils {
    public static String simplifyPath(String path) {
        Deque<String> pathDeterminer = new ArrayDeque<String>();
        String[] pathSplitter = path.split("/");
        StringBuilder absolutePath = new StringBuilder();
        for(String term : pathSplitter){
            if(term == null || term.length() == 0 || term.equals(".")){
            /*ignore these guys*/
            }else if(term.equals("..")){
                if(pathDeterminer.size() > 0){
                    pathDeterminer.removeLast();
                }
            }else{
                pathDeterminer.addLast(term);
            }
        }
        if(pathDeterminer.isEmpty()){
            return "/";
        }
        while(! pathDeterminer.isEmpty()){
            absolutePath.insert(0, pathDeterminer.removeLast());
            absolutePath.insert(0, "/");
        }
        return absolutePath.toString();
    }
}
