import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

public class Main extends JFrame {
    private final DrawingPanel drawingPanel;
    private final JComboBox<String> toolSelector;
    private final JSlider brushSizeSlider;
    private final JButton clearButton, colorButton, undoButton, redoButton;
    private Color currentColor = Color.BLACK;
    private String currentTool = "Brush";
    private int brushSize = 5;

    private final ArrayList<Shape> shapes = new ArrayList<>();
    private final ArrayList<Shape> redoStack = new ArrayList<>();

    public Main() {
        setTitle("Responsive Drawing Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Responsive Frame
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (screenSize.width * 0.8), (int) (screenSize.height * 0.8));
        setLocationRelativeTo(null); // Center on the screen

        // Drawing Panel
        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(50, 50, 60));
        sidebar.setPreferredSize(new Dimension(300, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding around sidebar

        // Tool Selector
        JLabel toolLabel = createLabel("Tool Selector");
        toolSelector = new JComboBox<>(new String[]{"Brush", "Line", "Rectangle", "Oval"});
        toolSelector.addActionListener(e -> currentTool = (String) toolSelector.getSelectedItem());
        styleComboBox(toolSelector);

        // Brush Size Slider
        JLabel brushLabel = createLabel("Brush Size");
        brushSizeSlider = new JSlider(1, 50, brushSize);
        brushSizeSlider.addChangeListener(e -> brushSize = brushSizeSlider.getValue());
        styleSlider(brushSizeSlider);

        // Buttons with Amazing Design
        colorButton = createStyledButton("🎨 Pick Color", new Color(72, 72, 255));
        colorButton.addActionListener(e -> chooseColor());

        undoButton = createStyledButton("↩️ Undo", new Color(60, 179, 113));
        undoButton.addActionListener(e -> undoLastAction());

        redoButton = createStyledButton("↪️ Redo", new Color(255, 140, 0));
        redoButton.addActionListener(e -> redoLastAction());

        clearButton = createStyledButton("🗑️ Clear Canvas", new Color(220, 20, 60));
        clearButton.addActionListener(e -> clearCanvas());

        // Add components to the sidebar
        sidebar.add(toolLabel);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(toolSelector);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(brushLabel);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(brushSizeSlider);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(colorButton);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(undoButton);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(redoButton);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(clearButton);

        add(sidebar, BorderLayout.WEST);

        // Finalize UI
        setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JButton createStyledButton(String text, Color gradientColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, gradientColor.brighter(),
                        getWidth(), getHeight(), gradientColor.darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Hover Effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(Color.YELLOW);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }
        });
        return button;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setMaximumSize(new Dimension(250, 30));
        comboBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    private void styleSlider(JSlider slider) {
        slider.setPreferredSize(new Dimension(250, 50));
    }

    private void chooseColor() {
        Color chosenColor = JColorChooser.showDialog(this, "Pick a Color", currentColor);
        if (chosenColor != null) {
            currentColor = chosenColor;
        }
    }

    private void undoLastAction() {
        if (!shapes.isEmpty()) {
            redoStack.add(shapes.remove(shapes.size() - 1));
            drawingPanel.repaint();
        }
    }

    private void redoLastAction() {
        if (!redoStack.isEmpty()) {
            shapes.add(redoStack.remove(redoStack.size() - 1));
            drawingPanel.repaint();
        }
    }

    private void clearCanvas() {
        shapes.clear();
        redoStack.clear();
        drawingPanel.repaint();
    }

    // Drawing Panel
    private class DrawingPanel extends JPanel {
        private Point startPoint, endPoint;

        public DrawingPanel() {
            setBackground(Color.WHITE);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                    endPoint = startPoint;
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    endPoint = e.getPoint();
                    if (startPoint != null && endPoint != null) {
                        addShape();
                    }
                    startPoint = null;
                    endPoint = null;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    endPoint = e.getPoint();
                    if (currentTool.equals("Brush")) {
                        addShape();
                        startPoint = endPoint;
                    }
                    repaint();
                }
            });
        }

        private void addShape() {
            Shape shape = new Shape(startPoint, endPoint, currentColor, currentTool, brushSize);
            shapes.add(shape);
            redoStack.clear(); // Clear redo stack after new shape
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            for (Shape shape : shapes) {
                g2d.setColor(shape.color);
                g2d.setStroke(new BasicStroke(shape.brushSize));

                switch (shape.tool) {
                    case "Line" -> g2d.drawLine(shape.start.x, shape.start.y, shape.end.x, shape.end.y);
                    case "Rectangle" -> g2d.drawRect(
                            Math.min(shape.start.x, shape.end.x),
                            Math.min(shape.start.y, shape.end.y),
                            Math.abs(shape.start.x - shape.end.x),
                            Math.abs(shape.start.y - shape.end.y)
                    );
                    case "Oval" -> g2d.drawOval(
                            Math.min(shape.start.x, shape.end.x),
                            Math.min(shape.start.y, shape.end.y),
                            Math.abs(shape.start.x - shape.end.x),
                            Math.abs(shape.start.y - shape.end.y)
                    );
                    case "Brush" -> g2d.fillOval(
                            shape.start.x - shape.brushSize / 2,
                            shape.start.y - shape.brushSize / 2,
                            shape.brushSize,
                            shape.brushSize
                    );
                }
            }
        }
    }

    private static class Shape {
        Point start, end;
        Color color;
        String tool;
        int brushSize;

        public Shape(Point start, Point end, Color color, String tool, int brushSize) {
            this.start = start;
            this.end = end;
            this.color = color;
            this.tool = tool;
            this.brushSize = brushSize;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
