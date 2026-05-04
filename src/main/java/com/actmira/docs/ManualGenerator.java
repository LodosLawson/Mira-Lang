package com.actmira.docs;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;

public class ManualGenerator {

    private static PDDocument document;
    private static PDPageContentStream contentStream;
    private static float currentY;
    private static final float MARGIN = 50;
    
    public static void main(String[] args) {
        generatePDF("ACTNverionMira_User_Manual.pdf");
    }

    public static void generatePDF(String filename) {
        try {
            document = new PDDocument();
            addNewPage();
            
            writeTitle("ACTNverionMira: Omni-Language");
            writeSubtitle("Official Detailed User Manual v5.0");
            
            writeParagraph("Welcome to ACTNverionMira. This language was designed to combine the most powerful", false);
            writeParagraph("features of Python, JavaScript, C, and HTML into a single, seamless 'Omni-Language'.", false);
            writeParagraph("It allows for backend logic and frontend UI rendering to exist in the exact same script.", false);
            currentY -= 15;
            
            writeHeader("1. Variables & Global State");
            writeParagraph("Unlike strict languages, semicolons (;) are completely optional in ACTNverionMira.", false);
            writeParagraph("Variables are dynamically typed and defined using the 'let' keyword:", false);
            writeParagraph("  let appName = \"My Web App\"", true);
            writeParagraph("  let version = 5", true);
            writeParagraph("For global variables, use the 'Ustglobal' keyword:", false);
            writeParagraph("  Ustglobal isRunning = true", true);
            currentY -= 15;
            
            writeHeader("2. Arrays (Python-Style Lists)");
            writeParagraph("You can define lists of data using square brackets. Access them via indices.", false);
            writeParagraph("You can also natively query the size of an array using length().", false);
            writeParagraph("  let users = [\"Alice\", \"Bob\", \"Charlie\"]", true);
            writeParagraph("  let size = length(users)", true);
            writeParagraph("  print(\"First user: \" + users[0])", true);
            currentY -= 15;
            
            writeHeader("3. Functions & Math");
            writeParagraph("Functions are defined using the 'fn' keyword. They can accept arguments and return values.", false);
            writeParagraph("  fn calculateScore(points, multiplier) {", true);
            writeParagraph("      let total = points * multiplier", true);
            writeParagraph("      return total", true);
            writeParagraph("  }", true);
            writeParagraph("  let myScore = calculateScore(50, 2)", true);
            currentY -= 15;
            
            writeHeader("4. Control Flow (If / Else)");
            writeParagraph("ACTNverionMira supports standard boolean logic and branching using if/else.", false);
            writeParagraph("  if (score > 10) {", true);
            writeParagraph("      print(\"You won!\")", true);
            writeParagraph("  } else {", true);
            writeParagraph("      print(\"Try again.\")", true);
            writeParagraph("  }", true);
            currentY -= 15;

            writeHeader("5. Loops (While)");
            writeParagraph("You can iterate and repeat blocks of code using while loops. Comparisons", false);
            writeParagraph("such as ==, !=, <, and > are fully supported.", false);
            writeParagraph("  let i = 0", true);
            writeParagraph("  while (i < 3) {", true);
            writeParagraph("      print(\"Count: \" + i)", true);
            writeParagraph("      i = i + 1", true);
            writeParagraph("  }", true);
            currentY -= 15;

            writeHeader("6. Native HTML & Web UI Rendering");
            writeParagraph("One of the most powerful features of ACTNverionMira is native Web View integration.", false);
            writeParagraph("You can construct HTML strings and use the 'render()' command to instantly display", false);
            writeParagraph("a real graphical user interface directly inside the IDE.", false);
            writeParagraph("  let buttonUI = \"<button style='color:red;'>Click Me</button>\"", true);
            writeParagraph("  render(buttonUI)", true);
            writeParagraph("Concatenating logic with UI is simple:", false);
            writeParagraph("  let items = [\"Apple\", \"Banana\"]", true);
            writeParagraph("  let ui = \"<h2>Items: \" + length(items) + \"</h2>\"", true);
            writeParagraph("  render(ui)", true);
            currentY -= 15;

            writeHeader("7. Object-Oriented Programming (Classes)");
            writeParagraph("ACTNverionMira supports full OOP. You can define classes, methods, and instantiate", false);
            writeParagraph("them using the 'new' keyword.", false);
            writeParagraph("  class DatabaseConnection {", true);
            writeParagraph("      fn connect(url) {", true);
            writeParagraph("          return true", true);
            writeParagraph("      }", true);
            writeParagraph("  }", true);
            writeParagraph("  let db = new DatabaseConnection()", true);
            writeParagraph("  print(db.connect(\"localhost\"))", true);
            currentY -= 15;
            
            writeHeader("8. Native File System I/O");
            writeParagraph("Interact seamlessly with the host operating system's files.", false);
            writeParagraph("  writeFile(\"config.txt\", \"data\")", true);
            writeParagraph("  let readData = readFile(\"config.txt\")", true);
            currentY -= 15;

            writeHeader("9. Data Types Overview");
            writeParagraph("ACTNverionMira is a dynamically typed language. The engine handles memory mapping.", false);
            writeParagraph("- INTEGER: Whole numbers without decimals (e.g., 42).", false);
            writeParagraph("- STRING: Text enclosed in double quotes (e.g., \"Web\").", false);
            writeParagraph("- BOOLEAN: true or false keywords.", false);
            writeParagraph("- ARRAY: Lists of any data type, accessible by 0-based index.", false);
            currentY -= 15;
            
            writeHeader("10. Console vs. UI Philosophy");
            writeParagraph("The Omni-Language philosophy allows two forms of output:", false);
            writeParagraph("- print(value): Outputs standard text to the Terminal Console.", false);
            writeParagraph("- render(html): Parses the string as a web component and paints it onto the Web View.", false);
            writeParagraph("This eliminates the need for separate frontend and backend frameworks.", false);
            currentY -= 30;
            
            writeParagraph("---", false);
            writeParagraph("Created natively by the ACTNverionMira Document Compiler Engine.", false);

            if (contentStream != null) {
                contentStream.endText();
                contentStream.close();
            }
            
            document.save(new File(filename));
            document.close();
            System.out.println("Detailed PDF User Manual successfully generated: " + filename);
            
        } catch (IOException e) {
            System.err.println("Error generating PDF: " + e.getMessage());
        }
    }
    
    private static void addNewPage() throws IOException {
        if (contentStream != null) {
            contentStream.endText();
            contentStream.close();
        }
        PDPage page = new PDPage();
        document.addPage(page);
        contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();
        currentY = 730;
    }
    
    private static void checkPageBreak() throws IOException {
        if (currentY < 50) {
            addNewPage();
        }
    }

    private static void writeTitle(String text) throws IOException {
        checkPageBreak();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
        contentStream.newLineAtOffset(MARGIN, currentY);
        contentStream.showText(text);
        currentY -= 30;
    }
    
    private static void writeSubtitle(String text) throws IOException {
        checkPageBreak();
        contentStream.endText();
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 16);
        contentStream.newLineAtOffset(MARGIN, currentY);
        contentStream.showText(text);
        currentY -= 40;
    }
    
    private static void writeHeader(String text) throws IOException {
        checkPageBreak();
        contentStream.endText();
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
        contentStream.newLineAtOffset(MARGIN, currentY);
        contentStream.showText(text);
        currentY -= 20;
    }
    
    private static void writeParagraph(String text, boolean isCode) throws IOException {
        checkPageBreak();
        contentStream.endText();
        contentStream.beginText();
        if (isCode) {
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 12);
        } else {
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
        }
        contentStream.newLineAtOffset(MARGIN + (isCode ? 20 : 0), currentY);
        contentStream.showText(text);
        currentY -= 15;
    }
}
