import common.Geom;
import common.Label;
import common.Type;
import org.junit.Test;
import org.graphstream.graph.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import productions.P1;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class P1Test {

    @InjectMocks
    private P1 p1;

    @Test
    public void testP1() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File f = new File(Objects.requireNonNull(classLoader.getResource("colors.jpg")).getFile());
        BufferedImage img = ImageIO.read(f);

        Graph graph = p1.run(img);

        //todo: fix visualization, add all attributes
        graph.display();

        //structure
        assertEquals(5, graph.getNodeCount());
        assertEquals(8, graph.getEdgeCount());
        assertEquals(1, graph.getNodeSet().stream().filter(n -> n.getAttribute("label").equals(Label.I)).count());
        assertEquals(4, graph.getNodeSet().stream().filter(n -> n.getAttribute("label").equals(Label.V)).count());
        assertEquals(4, graph.getEdgeSet().stream().filter(n -> Label.B.equals(n.getAttribute("label"))).count());
        assertEquals(4, graph.getEdgeSet().stream().filter(n -> n.getAttributeCount() == 0).count());
        assertEquals(1, graph.getNodeSet().stream().filter(n -> n.getAttribute("type").equals(Type.HYPEREDGE)).count());
        assertEquals(4, graph.getNodeSet().stream().filter(n -> n.getAttribute("type").equals(Type.VERTEX)).count());
        assertFalse(graph.getNode("5").getAttribute("break"));

        //todo: check connections

        //RGB
        int width  = img.getWidth();
        int height = img.getHeight();

        assertEquals(getColor(img, 0, height - 1), graph.getNode("1").getAttribute("rgb"));
        assertEquals(getColor(img, width -1 , height - 1), graph.getNode("2").getAttribute("rgb"));
        assertEquals(getColor(img, 0, 0), graph.getNode("3").getAttribute("rgb"));
        assertEquals(getColor(img, width - 1, 0), graph.getNode("4").getAttribute("rgb"));


        //geom
        Geom topLeft = graph.getNode("1").getAttribute("geom");
        assertEquals(0,  topLeft.getX());
        assertEquals(height - 1, topLeft.getY());

        Geom topRight = graph.getNode("2").getAttribute("geom");
        assertEquals(width - 1,  topRight.getX());
        assertEquals(height -1 , topRight.getY());

        Geom bottomLeft = graph.getNode("3").getAttribute("geom");
        assertEquals(0,  bottomLeft.getX());
        assertEquals(0, bottomLeft.getY());

        Geom bottomRight = graph.getNode("4").getAttribute("geom");
        assertEquals(width - 1,  bottomRight.getX());
        assertEquals(0, bottomRight.getY());
    }

    private Color getColor(BufferedImage img, int x, int y) {
        return new Color(img.getRGB(x, y));
    }



}