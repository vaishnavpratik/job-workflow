package com.test;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WorkFlow {

    private static Map<Integer, Node> steps = null;

    public static Map<Integer, Node> getInstance(){
        if(steps == null)
            steps = readJSONFile();
        return steps;
    }

    public static void main(String[] args){

        DirectedGraph<Integer, DefaultEdge> DAG = createDAG(getInstance());

        traverseDAG(DAG);

    }

    public  static DirectedGraph<Integer, DefaultEdge> createDAG(Map<Integer, Node> steps){

        DirectedGraph<Integer, DefaultEdge> graph =
                new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);

        try{

            if(steps != null){

                for (Map.Entry<Integer, Node> entry : steps.entrySet()) {
                    graph.addVertex(entry.getValue().getId());
                }

                for (Map.Entry<Integer, Node> entry : steps.entrySet()) {
                    Node tempNode = entry.getValue();
                    if("null".equalsIgnoreCase(tempNode.getParent()) || tempNode.getParent().isEmpty()) {
                        System.out.println("I'm the root "+ tempNode.getId());
                    }else
                        graph.addEdge(Integer.parseInt(entry.getValue().getParent()), entry.getValue().getId());
                }

            }else {
                return null;
            }

        }catch(IllegalArgumentException e){
            System.out.println(e.getMessage());
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return graph;
    }

    public static void traverseDAG(DirectedGraph<Integer, DefaultEdge> graph){

        GraphIterator<Integer, DefaultEdge> iterator =
                new BreadthFirstIterator<Integer, DefaultEdge>(graph);

        while (iterator.hasNext()) {
            String command = getStepById(iterator.next().toString()).getCommand();
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
                int isFine = process.waitFor();
                if(isFine > 0) {
                    System.out.println("Command error " + command + " shutting down workflow ");
                    System.exit(1);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error occurred while executing " + command + " shutting down workflow ");
                System.exit(1);
            }catch (InterruptedException e){
                e.printStackTrace();
                System.out.println("Execution interrupted " + command + " shutting down workflow ");
                System.exit(1);
            }
        }

    }

    public static Map<Integer, Node> readJSONFile(){

        Node[] nodes = null;
        Map<Integer, Node> map = new HashMap<Integer, Node>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            nodes = mapper.readValue(new File("res/workflow.json"), Node[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(Node node: nodes){
            map.put(node.getId(), node);
        }

        return map;
    }


    public static Node getStepById(String id){

        Node step = null;

        Map<Integer, Node> steps = getInstance();

        if(steps.containsKey(Integer.parseInt(id))){
            step = steps.get(Integer.parseInt(id));
        }

        return step;
    }

}
