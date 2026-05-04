package com.actmira.ide;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import java.awt.*;

public class TextLineNumber extends JPanel {
    private JTextArea textArea;

    public TextLineNumber(JTextArea textArea) {
        this.textArea = textArea;
        setBackground(new Color(49, 51, 53));
        setForeground(new Color(153, 153, 153));
        setFont(new Font("Monospaced", Font.PLAIN, 16));
        
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { repaint(); }
            public void removeUpdate(DocumentEvent e) { repaint(); }
            public void changedUpdate(DocumentEvent e) { repaint(); }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Calculate the height of a line
        FontMetrics fontMetrics = textArea.getFontMetrics(textArea.getFont());
        int fontHeight = fontMetrics.getHeight();
        int fontAscent = fontMetrics.getAscent();
        int fontDescent = fontMetrics.getDescent();
        
        // Get the viewport of the scroll pane
        Rectangle clip = g.getClipBounds();
        int startOffset = textArea.viewToModel2D(new Point(0, clip.y));
        int endOffset = textArea.viewToModel2D(new Point(0, clip.y + clip.height));

        Element root = textArea.getDocument().getDefaultRootElement();
        int startLine = root.getElementIndex(startOffset);
        int endLine = root.getElementIndex(endOffset);

        int drawY = 0;
        try {
            drawY = (int) textArea.modelToView2D(root.getElement(startLine).getStartOffset()).getY();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Draw the line numbers
        for (int i = startLine; i <= endLine; i++) {
            String lineNumber = String.valueOf(i + 1);
            int stringWidth = fontMetrics.stringWidth(lineNumber);
            int x = getWidth() - stringWidth - 5;
            int y = drawY + fontAscent;
            
            g.drawString(lineNumber, x, y);
            drawY += fontHeight;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int lines = textArea.getLineCount();
        FontMetrics fontMetrics = textArea.getFontMetrics(textArea.getFont());
        int width = fontMetrics.stringWidth(String.valueOf(Math.max(lines, 99))) + 15;
        return new Dimension(width, textArea.getHeight());
    }
}
