package productions;

import common.Geom;
import common.Label;
import common.Type;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.Buffer;

@Service
public class P4 {

    public Graph prepareTestGraph(BufferedImage img) {
        Graph graph = new SingleGraph("P4Test");

        int width = img.getWidth();
        int height = img.getHeight();
        Geom center = new Geom((width-1)/2, (height-1)/2);

                // TODO: Change name to UUID?
        addNode(graph, "1", Type.HYPEREDGE, Label.I, new Geom(center.getX()-7, center.getY()+7));
        addNode(graph, "2", Type.HYPEREDGE, Label.I, new Geom(center.getX()+7, center.getY()+7));
        addNode(graph, "3", Type.HYPEREDGE, Label.I, new Geom(center.getX()-7, center.getY()-7));
        addNode(graph, "4", Type.HYPEREDGE, Label.I, new Geom(center.getX()+7, center.getY()-7));

        // Geom is just for test, should not change during production
        addNode(graph, "5", Type.VERTEX, Label.T, new Geom(center.getX(), center.getY()+10), new Color(0,0,0));
        addNode(graph, "6", Type.VERTEX, Label.T, new Geom(center.getX()+10, center.getY()), new Color(0,0,0));
        addNode(graph, "7", Type.VERTEX, Label.T, new Geom(center.getX(), center.getY()-10), new Color(0,0,0));
        addNode(graph, "8", Type.VERTEX, Label.T, new Geom(center.getX()-10, center.getY()), new Color(0,0,0));

        addNode(graph, "9", Type.HYPEREDGE, Label.FE, new Geom(center.getX()+7, center.getY()));
        addNode(graph, "10", Type.HYPEREDGE, Label.FW, new Geom(center.getX()-7, center.getY()));

        addNode(graph, "11", Type.HYPEREDGE, Label.FN, center);

        addEdge(graph,"1", "5");
        addEdge(graph,"5", "2");
        addEdge(graph,"2", "6");
        addEdge(graph,"6", "4");
        addEdge(graph,"4", "7");
        addEdge(graph,"7", "3");
        addEdge(graph,"3", "8");
        addEdge(graph,"8", "1");

        addEdge(graph,"6", "9");
        addEdge(graph,"8", "10");

        addEdge(graph,"5", "11");
        addEdge(graph,"7", "11");

        return graph;
    }

    private Node removeLowerEdgeAndReturnLowerNode(Graph graph, Node node){
        Edge firstEdge = node.getEdge(0);
        Node firstNode = firstEdge.getNode0();
        firstNode = firstNode!=node?firstNode:firstEdge.getNode1();

        Edge secondEdge = node.getEdge(1);
        Node secondNode = secondEdge.getNode0();
        secondNode = secondNode!=node?secondNode:secondEdge.getNode1();

        if(((Geom)secondNode.getAttribute("geom")).getY()<((Geom)firstNode.getAttribute("geom")).getY()){
            graph.removeEdge(secondEdge);
            return secondNode;
        }
        else{
            graph.removeEdge(firstEdge);
            return firstNode;
        }
    }

    public Graph run(Graph graph, BufferedImage img) {

        Node fn = getNodeByLabel(graph, Label.FN);
        if(fn == null){
            System.out.println("There is no node with label: "+Label.FN);
        }
        else{
            Node lowerNode = removeLowerEdgeAndReturnLowerNode(graph, fn);
            Node upperNode = fn.getNeighborNodeIterator().next();

            String fsId = Integer.toString(Integer.parseInt(fn.getId())+1);
            Geom fnGeom = fn.getAttribute("geom");
            Geom nodeUnderFnGeom = lowerNode.getAttribute("geom");
            Geom nodeAboveFnGeom = upperNode.getAttribute("geom");
            Geom fnNewGeom = new Geom((fnGeom.getX()+nodeAboveFnGeom.getX())/2, (fnGeom.getY()+nodeAboveFnGeom.getY())/2);
            fn.setAttribute("geom", fnNewGeom);
            fn.setAttribute("xy", fnNewGeom.getX(), fnNewGeom.getY());

            addNode(graph, fsId, Type.HYPEREDGE, Label.FS, new Geom((fnGeom.getX()+nodeUnderFnGeom.getX())/2, (fnGeom.getY()+nodeUnderFnGeom.getY())/2));
            addEdge(graph, fsId, lowerNode.getId());
            Color color = getColor(img, fnGeom);
            Node vNode = addNode(graph, "V", Type.HYPEREDGE, Label.V, fnGeom, color);

            vNode.addAttribute("ui.label", "V: "+color.getRed()+" "+color.getGreen()+" "+color.getBlue());

            lowerNode.getNeighborNodeIterator().forEachRemaining(node -> {
                    addEdge(graph, vNode.getId(), node.getId());
            });

            upperNode.getNeighborNodeIterator().forEachRemaining(node -> {
                addEdge(graph, vNode.getId(), node.getId());
            });

            addEdge(graph, vNode.getId(), getNodeByLabel(graph, Label.FW).getId());
            addEdge(graph, vNode.getId(), getNodeByLabel(graph, Label.FE).getId());

        }
        return graph;
    }

    public Node getNodeByLabel(Graph graph, Label label){
        for (Node node : graph) {
            Label currentLabel = node.getAttribute("label");

            if (currentLabel == label)
                return node;
        }
        return null;
    }

    private void addNode(Graph graph, String name, Type type, Label label) {
        Node node = graph.addNode(name);
        node.setAttribute("type", type);
        node.setAttribute("label", label);
    }

    private Color getColor(BufferedImage img, Geom geom) {
        return new Color(img.getRGB(geom.getX(),geom.getY()));
    }

    private Node addNode(Graph graph, String name, Type type, Label label, Geom geom) {
        Node node = graph.addNode(name);
        node.setAttribute("type", type);
        node.setAttribute("label", label);
        node.setAttribute("geom", geom);
        node.setAttribute("xy", geom.getX(), geom.getY());
        return node;
    }

    private Node addNode(Graph graph, String name, Type type, Label label, Geom geom, Color rgb) {
        Node node = addNode(graph, name, type, label, geom);
        node.setAttribute("rgb", rgb);
        return node;
    }

    private void addEdge(Graph graph, String sourceName, String targetName) {
        String name = sourceName + "-" + targetName;
        graph.addEdge(name, sourceName, targetName);
    }

    private void removeEdge(Graph graph, String sourceName, String targetName) {
        graph.removeEdge(sourceName, targetName);
    }
}