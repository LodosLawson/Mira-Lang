package com.actmira.ide;

import com.actmira.evaluator.Engine;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class MiraIDE extends JFrame {

    // === STITCH "Obsidian Flow" exact tokens ===
    static final Color S_BG          = new Color(0x11,0x13,0x17); // surface / background
    static final Color S_ACT         = new Color(0x0C,0x0E,0x12); // surface-container-lowest
    static final Color S_SIDE        = new Color(0x1A,0x1C,0x20); // surface-container-low
    static final Color S_EDITOR      = new Color(0x1E,0x20,0x24); // surface-container
    static final Color S_HIGH        = new Color(0x28,0x2A,0x2E); // surface-container-high
    static final Color S_HIGHEST     = new Color(0x33,0x35,0x39); // surface-container-highest
    static final Color S_BORDER      = new Color(0x42,0x47,0x54); // outline-variant
    static final Color S_OUTLINE     = new Color(0x8C,0x90,0x9F); // outline
    static final Color S_TEXT        = new Color(0xE2,0xE2,0xE8); // on-surface
    static final Color S_TEXT_VAR    = new Color(0xC2,0xC6,0xD6); // on-surface-variant
    static final Color S_PRIMARY     = new Color(0x3B,0x82,0xF6); // primary override
    static final Color S_PRIMARY_DIM = new Color(0xAD,0xC6,0xFF); // primary
    static final Color S_SECONDARY   = new Color(0xA8,0x55,0xF7); // secondary override
    static final Color S_TERTIARY    = new Color(0xF9,0x73,0x16); // tertiary override
    static final Color S_SEL         = new Color(0x26,0x4F,0x78); // selection
    static final Color S_GREEN       = new Color(0x22,0xC5,0x5E); // run/success

    private JTabbedPane editorTabs;
    private JTextArea console;
    private JEditorPane webView;
    private JLabel statusLbl;
    private final Map<Component,File> fileMap = new HashMap<>();
    private final Map<Component,JTextArea> edMap = new HashMap<>();

    public MiraIDE() {
        setTitle("DevIDE - Nova Code Studio");
        setSize(1300, 820);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(S_ACT);
        initUI();
    }

    private void initUI() {
        JPanel root = box(S_ACT, new BorderLayout());
        root.add(titleBar(), BorderLayout.NORTH);
        root.add(body(),     BorderLayout.CENTER);
        root.add(statusBar(),BorderLayout.SOUTH);
        setContentPane(root);
        Engine.htmlRenderer = html -> SwingUtilities.invokeLater(() ->
            webView.setText("<html><body style='background:#1e2024;color:#e2e2e8;font-family:Inter,sans-serif;padding:16px'>" + html + "</body></html>"));
        // Keyboard shortcuts
        bindKey("ctrl N", e -> newFile());
        bindKey("ctrl O", e -> openFile());
        bindKey("ctrl S", e -> saveFile());
        bindKey("F5",     e -> runCode());
        bindKey("ctrl W", e -> closeCurrentTab());
        newTab("Untitled.mira", "");
    }

    private void bindKey(String keyStroke, ActionListener a) {
        String k = keyStroke.replace(" ","-");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(keyStroke), k);
        getRootPane().getActionMap().put(k, new AbstractAction(){
            public void actionPerformed(java.awt.event.ActionEvent e){ a.actionPerformed(e); }
        });
    }

    // ── Title bar (height 38) ─────────────────────────────
    private JPanel titleBar() {
        JPanel p = box(S_BG, new BorderLayout());
        p.setPreferredSize(new Dimension(0, 38));
        p.setBorder(new MatteBorder(0, 0, 1, 0, S_BORDER));

        JLabel logo = lbl("  DevIDE", Font.BOLD, 14, S_PRIMARY);
        p.add(logo, BorderLayout.WEST);

        JTextField srch = new JTextField("  Search files or commands...");
        srch.setBackground(S_EDITOR); srch.setForeground(S_OUTLINE);
        srch.setCaretColor(S_TEXT); srch.setFont(new Font("SansSerif", Font.PLAIN, 12));
        srch.setBorder(new CompoundBorder(new LineBorder(S_BORDER, 1, true), BorderFactory.createEmptyBorder(4,10,4,10)));
        srch.setPreferredSize(new Dimension(340, 26));
        JPanel mid = box(S_BG, new FlowLayout(FlowLayout.CENTER, 0, 6));
        mid.add(srch);
        p.add(mid, BorderLayout.CENTER);

        JPanel right = box(S_BG, new FlowLayout(FlowLayout.RIGHT, 6, 5));
        right.add(btn("Docs",   new Color(0x6F,0x00,0xBE), Color.WHITE, e -> openDocs()));
        right.add(btn("New",    S_HIGH, S_TEXT,    e -> newTab("Untitled.mira","")));
        right.add(btn("Open",   S_HIGH, S_TEXT,    e -> openFile()));
        right.add(btn("Save",   S_HIGH, S_TEXT,    e -> saveFile()));
        right.add(btn("Run  >", S_GREEN,Color.WHITE,e -> runCode()));
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Body ─────────────────────────────────────────────
    private JPanel body() {
        JPanel p = box(S_ACT, new BorderLayout());
        p.add(actBar(), BorderLayout.WEST);
        JSplitPane sc = spl(JSplitPane.HORIZONTAL_SPLIT, sidebar(), center());
        sc.setDividerLocation(260);
        JSplitPane cp = spl(JSplitPane.HORIZONTAL_SPLIT, sc, preview());
        cp.setResizeWeight(0.70);
        p.add(cp, BorderLayout.CENTER);
        return p;
    }

    // ── Activity bar (48px) ───────────────────────────────
    private JPanel actBar() {
        JPanel p = box(S_ACT, null);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(48, 0));
        p.setBorder(new MatteBorder(0,0,0,1, S_BORDER));
        p.add(Box.createVerticalStrut(10));
        p.add(aBtn("F", "Explorer (toggle)",  S_TEXT,       e -> toggleSidebar()));
        p.add(aBtn("S", "Search (Ctrl+F)",     S_OUTLINE,    e -> focusSearch()));
        p.add(aBtn("D", "Documentation",       S_PRIMARY_DIM,e -> openDocs()));
        p.add(aBtn("G", "Git / Refresh Tree",  S_OUTLINE,    e -> refreshTree()));
        p.add(aBtn("X", "Close Tab (Ctrl+W)",  S_OUTLINE,    e -> closeCurrentTab()));
        p.add(Box.createVerticalGlue());
        p.add(aBtn(">", "Run (F5)",            S_GREEN,      e -> runCode()));
        p.add(Box.createVerticalStrut(12));
        return p;
    }

    private JButton aBtn(String ic, String tip, Color fg, ActionListener a) {
        JButton b = new JButton(ic);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setForeground(fg); b.setBackground(S_ACT);
        b.setOpaque(true); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(48,44)); b.setPreferredSize(new Dimension(48,44));
        b.setToolTipText(tip); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ b.setBackground(S_SIDE); }
            public void mouseExited(MouseEvent e) { b.setBackground(S_ACT);  }
        });
        if(a!=null) b.addActionListener(a);
        return b;
    }

    // ── Sidebar (260px) ────────────────────────────────────
    private JPanel sidebar() {
        JPanel p = box(S_BG, new BorderLayout());
        p.setPreferredSize(new Dimension(260, 0));
        p.setBorder(new MatteBorder(0,0,0,1, S_BORDER));

        // section header
        JLabel h = lbl("  EXPLORER", Font.BOLD, 10, S_OUTLINE);
        h.setBorder(BorderFactory.createEmptyBorder(10,4,8,4));
        h.setBackground(S_BG); h.setOpaque(true);
        p.add(h, BorderLayout.NORTH);

        // tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Project");
        populateTree(new File("."), root);
        JTree tree = new JTree(new DefaultTreeModel(root));
        tree.setBackground(S_BG); tree.setForeground(S_TEXT);
        tree.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tree.setRootVisible(false); tree.setRowHeight(24);
        tree.setBorder(BorderFactory.createEmptyBorder(2,8,2,0));
        tree.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                if(e.getClickCount()<2) return;
                DefaultMutableTreeNode n=(DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
                if(n!=null && n.getUserObject() instanceof FNode fn && fn.f.isFile()) openTab(fn.f);
            }
        });
        p.add(scr(tree, S_BG), BorderLayout.CENTER);

        // examples
        String[] ex={
            "-- Examples --",
            "── Language ──────────",
            "1. Hello World",
            "2. Functions",
            "3. Classes & OOP",
            "4. Web UI (render)",
            "5. File I/O",
            "6. Loops & Arrays",
            "── 2D Graphics ───────",
            "7. Canvas Dashboard",
            "8. Particle Animation",
            "── 3D Graphics ───────",
            "9. 3D Solar System",
            "10. 3D City Scene"
        };
        JComboBox<String> cb = new JComboBox<>(ex);
        cb.setBackground(S_BG); cb.setForeground(S_OUTLINE);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 11));
        cb.setBorder(new MatteBorder(1,0,0,0,S_BORDER));
        cb.addActionListener(e->{
            String sel = (String)cb.getSelectedItem();
            if(sel==null||sel.startsWith("--")||sel.startsWith("──")) return;
            loadExampleByName(sel);
            cb.setSelectedIndex(0); // reset
        });
        p.add(cb, BorderLayout.SOUTH);
        return p;
    }

    private void populateTree(File dir, DefaultMutableTreeNode parent) {
        File[] fs = dir.listFiles(); if(fs==null) return;
        for(File f:fs){
            if(f.isDirectory()&&!f.getName().startsWith(".")){
                DefaultMutableTreeNode n=new DefaultMutableTreeNode(f.getName());
                parent.add(n); populateTree(f,n);
            } else if(f.isFile()&&f.getName().endsWith(".mira"))
                parent.add(new DefaultMutableTreeNode(new FNode(f)));
        }
    }
    private record FNode(File f){ public String toString(){ return f.getName(); } }

    // ── Center: tabs + terminal ────────────────────────────
    private JSplitPane center() {
        // editor tabs
        editorTabs = new JTabbedPane();
        editorTabs.setBackground(S_EDITOR);
        editorTabs.setForeground(S_TEXT_VAR);
        editorTabs.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // terminal
        console = new JTextArea("DevIDE Terminal  --  Ready\n");
        console.setBackground(S_ACT); console.setForeground(S_GREEN);
        console.setFont(new Font("Monospaced", Font.PLAIN, 13));
        console.setEditable(false);
        console.setBorder(BorderFactory.createEmptyBorder(8,14,8,8));

        // terminal header (tabs: TERMINAL OUTPUT DEBUG)
        JPanel th = box(S_BG, new BorderLayout());
        th.setBorder(new MatteBorder(1,0,0,0,S_BORDER));
        JPanel tabs = box(S_BG, new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.add(termTab("TERMINAL", true));
        tabs.add(termTab("OUTPUT",   false));
        tabs.add(termTab("DEBUG",    false));
        JButton clr = btn("Clear", S_BG, S_OUTLINE, e->console.setText(""));
        clr.setFont(new Font("SansSerif", Font.PLAIN, 10));
        th.add(tabs, BorderLayout.WEST);
        th.add(clr,  BorderLayout.EAST);

        JPanel term = box(S_ACT, new BorderLayout());
        term.add(th,            BorderLayout.NORTH);
        term.add(scr(console, S_ACT), BorderLayout.CENTER);

        JSplitPane sp = spl(JSplitPane.VERTICAL_SPLIT, editorTabs, term);
        sp.setResizeWeight(0.72);
        return sp;
    }

    private JLabel termTab(String t, boolean active) {
        JLabel l = lbl("  "+t+"  ", Font.BOLD, 10, active ? S_TEXT : S_OUTLINE);
        l.setBorder(new CompoundBorder(
            new MatteBorder(active?2:0,0,0,0, S_PRIMARY),
            BorderFactory.createEmptyBorder(active?6:8,4,6,4)));
        l.setBackground(S_BG); l.setOpaque(true);
        return l;
    }

    // ── Live preview ───────────────────────────────────────
    private JPanel preview() {
        webView = new JEditorPane("text/html",
            "<html><body style='background:#1e2024;color:#424754;font-family:sans-serif;"
           +"text-align:center;padding:60px 20px'>"
           +"<h2 style='color:#3b82f6'>Live Preview</h2>"
           +"<p style='font-size:13px'>Call <code>render(\"...\")</code></p>"
           +"</body></html>");
        webView.setEditable(false);
        JPanel p = box(S_BG, new BorderLayout());
        p.setBorder(new MatteBorder(0,1,0,0,S_BORDER));
        JLabel hdr = lbl("  LIVE PREVIEW", Font.BOLD, 10, S_OUTLINE);
        hdr.setBackground(S_BG); hdr.setOpaque(true);
        hdr.setBorder(BorderFactory.createEmptyBorder(9,4,9,4));
        p.add(hdr, BorderLayout.NORTH);
        p.add(scr(webView, S_EDITOR), BorderLayout.CENTER);
        return p;
    }

    // ── Status bar (22px, blue) ────────────────────────────
    private JPanel statusBar() {
        JPanel p = box(S_PRIMARY, new BorderLayout());
        p.setPreferredSize(new Dimension(0, 22));
        p.setBorder(BorderFactory.createEmptyBorder(2,10,2,10));
        statusLbl = lbl(" Ready  |  MiraIDE v1.0", Font.PLAIN, 11, Color.WHITE);
        p.add(statusLbl, BorderLayout.WEST);
        p.add(lbl("Mira  |  UTF-8  ", Font.PLAIN, 11, Color.WHITE), BorderLayout.EAST);
        return p;
    }

    // ── Tab factory ────────────────────────────────────────
    private void newTab(String title, String content) {
        JTextArea ed = new JTextArea(content);
        ed.setBackground(S_EDITOR); ed.setForeground(S_TEXT);
        ed.setCaretColor(S_PRIMARY_DIM);
        ed.setFont(new Font("Monospaced", Font.PLAIN, 14));
        ed.setTabSize(4); ed.setSelectionColor(S_SEL);
        ed.setBorder(BorderFactory.createEmptyBorder(12,16,12,12));
        JScrollPane sc = scr(ed, S_EDITOR);
        TextLineNumber tln = new TextLineNumber(ed);
        tln.setBackground(S_EDITOR); tln.setForeground(S_BORDER);
        sc.setRowHeaderView(tln);
        editorTabs.addTab(title, sc);
        edMap.put(sc, ed);
        editorTabs.setSelectedComponent(sc);
        // Close button on tab
        int idx = editorTabs.getTabCount() - 1;
        JPanel tabHeader = box(S_EDITOR, new FlowLayout(FlowLayout.LEFT, 4, 0));
        tabHeader.setOpaque(false);
        JLabel tabLbl = new JLabel(title);
        tabLbl.setForeground(S_TEXT_VAR);
        tabLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JButton closeBtn = new JButton("x");
        closeBtn.setFont(new Font("SansSerif", Font.PLAIN, 9));
        closeBtn.setForeground(S_OUTLINE);
        closeBtn.setBackground(S_EDITOR);
        closeBtn.setBorderPainted(false); closeBtn.setFocusPainted(false); closeBtn.setOpaque(false);
        closeBtn.setMargin(new Insets(0,2,0,2));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> closeTab(editorTabs.indexOfTabComponent(tabHeader)));
        closeBtn.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ closeBtn.setForeground(S_TEXT); }
            public void mouseExited(MouseEvent e) { closeBtn.setForeground(S_OUTLINE); }
        });
        tabHeader.add(tabLbl);
        tabHeader.add(closeBtn);
        editorTabs.setTabComponentAt(idx, tabHeader);
        setStatus(title);
    }

    // ── File ops ───────────────────────────────────────────
    private void newFile()  { newTab("Untitled.mira",""); }

    private void openFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Mira (*.mira)","mira"));
        if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) openTab(fc.getSelectedFile());
    }

    private void openTab(File f) {
        try {
            newTab(f.getName(), Files.readString(f.toPath()));
            fileMap.put(editorTabs.getSelectedComponent(), f);
            setStatus("Opened: "+f.getName());
        } catch(Exception ex){ setStatus("Error: "+ex.getMessage()); }
    }

    private void saveFile() {
        Component tab = editorTabs.getSelectedComponent();
        if(tab==null) return;
        File f = fileMap.get(tab);
        if(f==null){
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Mira (*.mira)","mira"));
            if(fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
            f = fc.getSelectedFile();
            if(!f.getName().endsWith(".mira")) f=new File(f.getAbsolutePath()+".mira");
            fileMap.put(tab,f);
            editorTabs.setTitleAt(editorTabs.getSelectedIndex(),f.getName());
        }
        try{ Files.writeString(f.toPath(), edMap.get(tab).getText()); setStatus("Saved: "+f.getName()); }
        catch(Exception ex){ setStatus("Error: "+ex.getMessage()); }
    }

    private void runCode() {
        Component tab = editorTabs.getSelectedComponent();
        if(tab==null||edMap.get(tab)==null) return;
        String code = edMap.get(tab).getText();
        console.setText(">> Running...\n"); setStatus("Executing...");
        new Thread(()->{ String out=Engine.execute(code);
            SwingUtilities.invokeLater(()->{ console.setText(out); setStatus("Done."); });
        }).start();
    }

    private void loadExample(int i) {
        // kept for backward compat
        String[] names={"1. Hello World","2. Functions","3. Classes & OOP","4. Web UI (render)","5. File I/O","6. Loops & Arrays"};
        if(i>=0&&i<names.length) loadExampleByName(names[i]);
    }

    private void loadExampleByName(String name) {
        // Try loading from examples/ folder first
        java.util.Map<String,String> fileMap2 = new java.util.LinkedHashMap<>();
        fileMap2.put("7. Canvas Dashboard",    "examples/canvas_dashboard.mira");
        fileMap2.put("8. Particle Animation",   "examples/canvas_particles.mira");
        fileMap2.put("9. 3D Solar System",      "examples/3d_solar_system.mira");
        fileMap2.put("10. 3D City Scene",       "examples/3d_city.mira");
        if(fileMap2.containsKey(name)) {
            try {
                String code = Files.readString(new File(fileMap2.get(name)).toPath());
                newTab(new File(fileMap2.get(name)).getName(), code);
                setStatus("Loaded: " + name);
            } catch(Exception ex) {
                setStatus("File not found: " + fileMap2.get(name));
            }
            return;
        }
        // Inline examples
        java.util.Map<String,String[]> inline = new java.util.LinkedHashMap<>();
        inline.put("1. Hello World",    new String[]{"HelloWorld.mira",
            "// Hello World\nlet name = \"MiraIDE\";\nlet version = 5;\nprint(name + \" v\" + version);\n"});
        inline.put("2. Functions",      new String[]{"Functions.mira",
            "// Functions\nfn add(x, y) { return x + y; }\nfn greet(n) { return \"Hello, \" + n + \"!\"; }\nprint(add(10, 25));\nprint(greet(\"Developer\"));\n"});
        inline.put("3. Classes & OOP",  new String[]{"Classes.mira",
            "// Classes & OOP\nclass Animal {\n    fn speak(sound) { return \"Says: \" + sound; }\n    fn isAlive() { return true; }\n}\nlet dog = new Animal();\nprint(dog.speak(\"Woof\"));\nprint(dog.isAlive());\n"});
        inline.put("4. Web UI (render)",new String[]{"WebUI.mira",
            "// Web UI\nlet ui = \"<h1 style='color:#3b82f6'>MiraIDE</h1><p style='color:#c2c6d6'>Hello from render()!</p>\";\nrender(ui);\nprint(\"Rendered!\");\n"});
        inline.put("5. File I/O",       new String[]{"FileIO.mira",
            "// File I/O\nwriteFile(\"hello.txt\", \"Hello from MiraIDE!\");\nlet content = readFile(\"hello.txt\");\nprint(content);\n"});
        inline.put("6. Loops & Arrays", new String[]{"Loops.mira",
            "// Loops & Arrays\nlet nums = [1, 2, 3, 4, 5];\nlet i = 0;\nwhile (i < length(nums)) {\n    if (nums[i] == 3) { print(\"Three!\"); }\n    else { print(nums[i]); }\n    i = i + 1;\n}\n"});
        String[] entry = inline.get(name);
        if(entry!=null) newTab(entry[0], entry[1]);
    }

    private void closeCurrentTab() {
        int idx = editorTabs.getSelectedIndex();
        if (idx >= 0) closeTab(idx);
    }

    private void closeTab(int idx) {
        if (idx < 0 || idx >= editorTabs.getTabCount()) return;
        Component tab = editorTabs.getComponentAt(idx);
        edMap.remove(tab);
        fileMap.remove(tab);
        editorTabs.removeTabAt(idx);
        if (editorTabs.getTabCount() == 0) newFile();
    }

    private JSplitPane mainSplitRef; // reference for sidebar toggle

    private void toggleSidebar() {
        // Find and toggle the first horizontal split (sidebar vs center)
        // Walk component tree to find first JSplitPane child of body
        Component body = getContentPane().getComponent(0);
        if (body instanceof JPanel) {
            for (Component c : ((JPanel)body).getComponents()) {
                if (c instanceof JSplitPane sp && sp.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
                    // inner split
                    if (sp.getLeftComponent() instanceof JSplitPane inner) {
                        int loc = inner.getDividerLocation();
                        inner.setDividerLocation(loc > 10 ? 0 : 260);
                    }
                    break;
                }
            }
        }
        setStatus("Toggled sidebar");
    }

    private JTextField searchField;
    private void focusSearch() {
        // Focus the search bar in title bar
        Component nb = getContentPane().getComponent(0);
        if (nb instanceof JPanel titleBar) {
            for (Component c : titleBar.getComponents()) {
                if (c instanceof JPanel mid) {
                    for (Component cc : ((JPanel)mid).getComponents()) {
                        if (cc instanceof JTextField tf) { tf.requestFocusInWindow(); tf.selectAll(); return; }
                    }
                }
            }
        }
        setStatus("Search focused");
    }

    private void refreshTree() {
        setStatus("Refreshed file tree (relaunch IDE to update)");
    }

    private void openDocs() {
        // Check if docs tab already open
        for (int i = 0; i < editorTabs.getTabCount(); i++) {
            if ("Documentation".equals(editorTabs.getTitleAt(i))) {
                editorTabs.setSelectedIndex(i); return;
            }
        }

        JEditorPane docs = new JEditorPane("text/html", buildDocsHtml());
        docs.setEditable(false);
        docs.setBackground(new Color(0x1E,0x20,0x24));
        // Clicking links runs example code
        docs.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                String desc = e.getDescription();
                if (desc != null && desc.startsWith("run:")) {
                    String code = desc.substring(4).replace("\\n", "\n");
                    newTab("example.mira", code);
                }
            }
        });

        JScrollPane sp = scr(docs, new Color(0x1E,0x20,0x24));

        editorTabs.addTab("Documentation", sp);
        int idx = editorTabs.getTabCount() - 1;
        // Tab header with close
        JPanel tabHeader = box(S_EDITOR, new FlowLayout(FlowLayout.LEFT, 4, 0));
        tabHeader.setOpaque(false);
        JLabel tabLbl = new JLabel("Documentation");
        tabLbl.setForeground(S_PRIMARY_DIM);
        tabLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        JButton closeBtn = new JButton("x");
        closeBtn.setFont(new Font("SansSerif", Font.PLAIN, 9));
        closeBtn.setForeground(S_OUTLINE); closeBtn.setBackground(S_EDITOR);
        closeBtn.setBorderPainted(false); closeBtn.setFocusPainted(false); closeBtn.setOpaque(false);
        closeBtn.setMargin(new Insets(0,2,0,2));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(ev -> closeTab(editorTabs.indexOfTabComponent(tabHeader)));
        tabHeader.add(tabLbl); tabHeader.add(closeBtn);
        editorTabs.setTabComponentAt(idx, tabHeader);
        editorTabs.setSelectedIndex(idx);
        setStatus("Documentation opened");
    }

    private String buildDocsHtml() {
        return "<!DOCTYPE html><html><head><style>" +
            "body{background:#1e2024;color:#e2e2e8;font-family:'Segoe UI',Inter,sans-serif;margin:0;padding:0}" +
            ".hero{background:linear-gradient(135deg,#1a1c20 0%,#0c0e12 100%);padding:40px 48px;border-bottom:1px solid #424754}" +
            ".hero h1{color:#adc6ff;font-size:32px;margin:0 0 8px 0;font-weight:700}" +
            ".hero p{color:#8c909f;font-size:14px;margin:0}" +
            ".nav{display:flex;gap:0;background:#0c0e12;border-bottom:1px solid #424754;padding:0 48px}" +
            ".nav a{color:#8c909f;text-decoration:none;padding:12px 20px;font-size:12px;font-weight:600;letter-spacing:.05em;display:inline-block;border-bottom:2px solid transparent}" +
            ".nav a:hover{color:#e2e2e8;border-bottom-color:#3b82f6}" +
            ".body{padding:40px 48px;max-width:900px}" +
            "h2{color:#adc6ff;font-size:20px;font-weight:700;margin:40px 0 12px 0;padding-bottom:8px;border-bottom:1px solid #424754}" +
            "h3{color:#c2c6d6;font-size:15px;font-weight:600;margin:24px 0 8px 0}" +
            "p{color:#c2c6d6;font-size:13px;line-height:1.7;margin:0 0 12px 0}" +
            ".code{background:#0c0e12;border:1px solid #424754;border-radius:6px;padding:16px 20px;margin:12px 0 20px 0;font-family:'JetBrains Mono','Consolas',monospace;font-size:13px;color:#e2e2e8;overflow-x:auto;white-space:pre;line-height:1.7}" +
            ".kw{color:#c586c0}.str{color:#ce9178}.num{color:#b5cea8}.cmt{color:#6a9955}.fn{color:#dcdcaa}.type{color:#4ec9b0}" +
            ".badge{display:inline-block;background:#6f00be;color:#fff;padding:2px 8px;border-radius:4px;font-size:10px;font-weight:700;margin-right:6px;vertical-align:middle}" +
            ".badge-blue{background:#1e3a5f;color:#adc6ff}" +
            ".badge-green{background:#0d3321;color:#22c55e}" +
            ".run-btn{display:inline-block;background:#22c55e;color:#000;padding:4px 14px;border-radius:4px;font-size:11px;font-weight:700;text-decoration:none;margin-left:12px;cursor:pointer}" +
            ".run-btn:hover{background:#16a34a}" +
            ".tip{background:#1a1c20;border-left:3px solid #3b82f6;padding:10px 16px;border-radius:0 6px 6px 0;margin:8px 0 20px 0;font-size:12px;color:#8c909f}" +
            ".grid{display:flex;gap:16px;flex-wrap:wrap;margin-bottom:20px}" +
            ".card{background:#0c0e12;border:1px solid #424754;border-radius:8px;padding:16px;flex:1;min-width:220px}" +
            ".card h4{color:#adc6ff;margin:0 0 6px 0;font-size:13px}" +
            ".card p{color:#8c909f;margin:0;font-size:12px}" +
            "table{width:100%;border-collapse:collapse;margin:12px 0 20px 0;font-size:13px}" +
            "th{background:#0c0e12;color:#8c909f;text-align:left;padding:8px 12px;border:1px solid #424754;font-size:11px;font-weight:600;letter-spacing:.05em}" +
            "td{padding:8px 12px;border:1px solid #2a2d35;color:#c2c6d6}" +
            "td code{background:#0c0e12;padding:2px 6px;border-radius:4px;font-family:monospace;font-size:12px;color:#ce9178}" +
            "tr:hover td{background:#1a1c20}" +
            "</style></head><body>" +

            "<div class='hero'>" +
            "<h1>MiraIDE Documentation</h1>" +
            "<p>Complete reference for the Omni-Language — variables, functions, classes, control flow, I/O, and web rendering.</p>" +
            "</div>" +

            "<div class='body'>" +

            // Overview
            "<h2>Language Overview</h2>" +
            "<div class='grid'>" +
            "<div class='card'><h4>Dynamically Typed</h4><p>No type declarations needed. Variables hold any value.</p></div>" +
            "<div class='card'><h4>Omni-Language</h4><p>Runs on CLI, renders HTML UIs, and reads/writes files — all in one script.</p></div>" +
            "<div class='card'><h4>OOP Ready</h4><p>Full class support with methods and object instantiation via <code>new</code>.</p></div>" +
            "<div class='card'><h4>Turing-Complete</h4><p>if/else, while loops, and recursive functions supported.</p></div>" +
            "</div>" +

            // Variables
            "<h2>1. Variables</h2>" +
            "<p>Use <span class='badge badge-blue'>let</span> for local variables and <span class='badge badge-blue'>Ustglobal</span> for global state.</p>" +
            "<div class='code'><span class='cmt'>// Local variable</span>\n" +
            "<span class='kw'>let</span> name = <span class='str'>\"MiraIDE\"</span>;\n" +
            "<span class='kw'>let</span> version = <span class='num'>5</span>;\n" +
            "<span class='kw'>let</span> active = <span class='kw'>true</span>;\n\n" +
            "<span class='cmt'>// Global variable (accessible everywhere)</span>\n" +
            "<span class='type'>Ustglobal</span> appState = <span class='str'>\"running\"</span>;\n\n" +
            "print(name);       <span class='cmt'>// MiraIDE</span>\n" +
            "print(version);    <span class='cmt'>// 5</span></div>" +
            "<div class='tip'>Variables are reassigned without <code>let</code>: <code>version = 6;</code></div>" +

            // Functions
            "<h2>2. Functions</h2>" +
            "<p>Declare with <span class='badge badge-blue'>fn</span>. Functions support parameters and <span class='badge badge-blue'>return</span> values.</p>" +
            "<div class='code'><span class='kw'>fn</span> <span class='fn'>greet</span>(name) {\n" +
            "    <span class='kw'>let</span> msg = <span class='str'>\"Hello, \"</span> + name + <span class='str'>\"!\"</span>;\n" +
            "    <span class='kw'>return</span> msg;\n" +
            "}\n\n" +
            "<span class='kw'>fn</span> <span class='fn'>add</span>(x, y) { <span class='kw'>return</span> x + y; }\n\n" +
            "print(<span class='fn'>greet</span>(<span class='str'>\"Developer\"</span>));  <span class='cmt'>// Hello, Developer!</span>\n" +
            "print(<span class='fn'>add</span>(<span class='num'>10</span>, <span class='num'>25</span>));           <span class='cmt'>// 35</span></div>" +

            // Classes
            "<h2>3. Classes &amp; Objects</h2>" +
            "<p>Use <span class='badge badge-blue'>class</span> to define blueprints. Instantiate with <span class='badge badge-blue'>new</span>.</p>" +
            "<div class='code'><span class='kw'>class</span> <span class='type'>Animal</span> {\n" +
            "    <span class='kw'>fn</span> <span class='fn'>speak</span>(sound) {\n" +
            "        <span class='kw'>return</span> <span class='str'>\"The animal says: \"</span> + sound;\n" +
            "    }\n" +
            "    <span class='kw'>fn</span> <span class='fn'>isAlive</span>() { <span class='kw'>return</span> <span class='kw'>true</span>; }\n" +
            "}\n\n" +
            "<span class='kw'>let</span> dog = <span class='kw'>new</span> <span class='type'>Animal</span>();\n" +
            "print(dog.<span class='fn'>speak</span>(<span class='str'>\"Woof\"</span>));  <span class='cmt'>// The animal says: Woof</span>\n" +
            "print(dog.<span class='fn'>isAlive</span>());      <span class='cmt'>// true</span></div>" +

            // Control Flow
            "<h2>4. Control Flow — If / Else</h2>" +
            "<p>Standard conditional branching with <span class='badge badge-blue'>if</span> and <span class='badge badge-blue'>else</span>.</p>" +
            "<div class='code'><span class='kw'>let</span> score = <span class='num'>85</span>;\n\n" +
            "<span class='kw'>if</span> (score > <span class='num'>90</span>) {\n" +
            "    print(<span class='str'>\"Grade: A\"</span>);\n" +
            "} <span class='kw'>else if</span> (score > <span class='num'>75</span>) {\n" +
            "    print(<span class='str'>\"Grade: B\"</span>);   <span class='cmt'>// This runs</span>\n" +
            "} <span class='kw'>else</span> {\n" +
            "    print(<span class='str'>\"Grade: C\"</span>);\n" +
            "}</div>" +

            "<h3>Comparison Operators</h3>" +
            "<table><tr><th>Operator</th><th>Meaning</th><th>Example</th></tr>" +
            "<tr><td><code>==</code></td><td>Equal to</td><td><code>x == 10</code></td></tr>" +
            "<tr><td><code>!=</code></td><td>Not equal</td><td><code>x != 0</code></td></tr>" +
            "<tr><td><code>&lt;</code></td><td>Less than</td><td><code>i &lt; 5</code></td></tr>" +
            "<tr><td><code>&gt;</code></td><td>Greater than</td><td><code>score &gt; 90</code></td></tr>" +
            "</table>" +

            // While
            "<h2>5. Loops — While</h2>" +
            "<p>Use <span class='badge badge-blue'>while</span> for iteration. Combine with <span class='badge badge-blue'>if</span> for complex logic.</p>" +
            "<div class='code'><span class='kw'>let</span> i = <span class='num'>0</span>;\n" +
            "<span class='kw'>while</span> (i < <span class='num'>5</span>) {\n" +
            "    <span class='kw'>if</span> (i == <span class='num'>2</span>) {\n" +
            "        print(<span class='str'>\"Two!\"</span>);\n" +
            "    } <span class='kw'>else</span> {\n" +
            "        print(i);\n" +
            "    }\n" +
            "    i = i + <span class='num'>1</span>;\n" +
            "}\n" +
            "<span class='cmt'>// Output: 0, 1, Two!, 3, 4</span></div>" +
            "<div class='tip'>Always update the loop variable inside the body to avoid infinite loops.</div>" +

            // Arrays
            "<h2>6. Arrays</h2>" +
            "<p>Arrays hold ordered collections. Access elements with <code>array[index]</code> (0-based).</p>" +
            "<div class='code'><span class='kw'>let</span> colors = [<span class='str'>\"Red\"</span>, <span class='str'>\"Green\"</span>, <span class='str'>\"Blue\"</span>];\n\n" +
            "print(colors[<span class='num'>0</span>]);          <span class='cmt'>// Red</span>\n" +
            "print(<span class='fn'>length</span>(colors));     <span class='cmt'>// 3</span>\n\n" +
            "<span class='cmt'>// Loop through array</span>\n" +
            "<span class='kw'>let</span> idx = <span class='num'>0</span>;\n" +
            "<span class='kw'>while</span> (idx < <span class='fn'>length</span>(colors)) {\n" +
            "    print(colors[idx]);\n" +
            "    idx = idx + <span class='num'>1</span>;\n" +
            "}</div>" +

            // Built-in Functions
            "<h2>7. Built-in Functions</h2>" +
            "<table><tr><th>Function</th><th>Description</th><th>Example</th></tr>" +
            "<tr><td><code>print(value)</code></td><td>Print to terminal</td><td><code>print(\"Hello\");</code></td></tr>" +
            "<tr><td><code>length(array)</code></td><td>Array length</td><td><code>length([1,2,3])</code> → 3</td></tr>" +
            "<tr><td><code>render(html)</code></td><td>Render HTML in Live Preview</td><td><code>render(\"&lt;h1&gt;Hi&lt;/h1&gt;\")</code></td></tr>" +
            "<tr><td><code>readFile(path)</code></td><td>Read file as string</td><td><code>readFile(\"data.txt\")</code></td></tr>" +
            "<tr><td><code>writeFile(path, data)</code></td><td>Write string to file</td><td><code>writeFile(\"out.txt\", \"Hello\")</code></td></tr>" +
            "</table>" +

            // File I/O
            "<h2>8. File I/O</h2>" +
            "<p>Read and write files directly from Mira scripts. Paths are relative to the IDE working directory.</p>" +
            "<div class='code'><span class='cmt'>// Write to file</span>\n" +
            "<span class='kw'>let</span> data = <span class='str'>\"Name: DevIDE\\nVersion: 5\"</span>;\n" +
            "<span class='fn'>writeFile</span>(<span class='str'>\"config.txt\"</span>, data);\n\n" +
            "<span class='cmt'>// Read it back</span>\n" +
            "<span class='kw'>let</span> content = <span class='fn'>readFile</span>(<span class='str'>\"config.txt\"</span>);\n" +
            "print(content);</div>" +

            // Web UI
            "<h2>9. Web UI — Live Preview</h2>" +
            "<p>Call <span class='badge badge-green'>render(html)</span> to display HTML/CSS in the right-side Live Preview panel.</p>" +
            "<div class='code'><span class='kw'>let</span> title = <span class='str'>\"My Dashboard\"</span>;\n" +
            "<span class='kw'>let</span> items = [<span class='str'>\"Users\"</span>, <span class='str'>\"Orders\"</span>, <span class='str'>\"Revenue\"</span>];\n\n" +
            "<span class='kw'>let</span> cards = <span class='str'>\"\"</span>;\n" +
            "<span class='kw'>let</span> i = <span class='num'>0</span>;\n" +
            "<span class='kw'>while</span> (i < <span class='fn'>length</span>(items)) {\n" +
            "    cards = cards + <span class='str'>\"&lt;div style='background:#1e2024;border:1px solid #424754;border-radius:8px;padding:16px;margin:8px'&gt;\"</span>\n" +
            "          + items[i] + <span class='str'>\"&lt;/div&gt;\"</span>;\n" +
            "    i = i + <span class='num'>1</span>;\n" +
            "}\n\n" +
            "<span class='kw'>let</span> html = <span class='str'>\"&lt;h1 style='color:#3b82f6'&gt;\"</span> + title + <span class='str'>\"&lt;/h1&gt;\"</span> + cards;\n" +
            "<span class='fn'>render</span>(html);</div>" +

            // Quick Reference
            "<h2>10. Quick Reference Card</h2>" +
            "<table><tr><th>Concept</th><th>Syntax</th></tr>" +
            "<tr><td>Variable</td><td><code>let x = 10;</code></td></tr>" +
            "<tr><td>Global</td><td><code>Ustglobal g = true;</code></td></tr>" +
            "<tr><td>Function</td><td><code>fn name(a, b) { return a + b; }</code></td></tr>" +
            "<tr><td>Class</td><td><code>class Foo { fn bar() { return 1; } }</code></td></tr>" +
            "<tr><td>Instantiate</td><td><code>let obj = new Foo();</code></td></tr>" +
            "<tr><td>Method call</td><td><code>obj.bar();</code></td></tr>" +
            "<tr><td>If / Else</td><td><code>if (x &gt; 0) { } else { }</code></td></tr>" +
            "<tr><td>While loop</td><td><code>while (i &lt; 10) { i = i + 1; }</code></td></tr>" +
            "<tr><td>Array</td><td><code>let arr = [1, 2, 3];</code></td></tr>" +
            "<tr><td>Array access</td><td><code>arr[0]</code></td></tr>" +
            "<tr><td>Array length</td><td><code>length(arr)</code></td></tr>" +
            "<tr><td>Print</td><td><code>print(\"Hello\");</code></td></tr>" +
            "<tr><td>Render HTML</td><td><code>render(\"&lt;h1&gt;Hi&lt;/h1&gt;\");</code></td></tr>" +
            "<tr><td>Read file</td><td><code>readFile(\"path.txt\")</code></td></tr>" +
            "<tr><td>Write file</td><td><code>writeFile(\"path.txt\", data)</code></td></tr>" +
            "</table>" +

            // ── STDLIB SECTION ──────────────────────────────────────
            "<h2 style='margin-top:60px;color:#ddb7ff;border-bottom-color:#6f00be'>Standard Libraries</h2>" +
            "<p>Import any library with <span class='badge' style='background:#1e3a5f;color:#adc6ff'>import \"stdlib/name\";</span> at the top of your script. " +
            "All libraries are written in <strong style='color:#adc6ff'>pure Mira language</strong> — no Java, no native code.</p>" +

            // math
            "<div style='background:#0c0e12;border:1px solid #6f00be44;border-radius:10px;padding:24px;margin:24px 0'>" +
            "<div style='display:flex;align-items:center;margin-bottom:14px'>" +
            "<span style='font-size:22px;margin-right:12px'>📐</span>" +
            "<div><strong style='color:#ddb7ff;font-size:16px'>stdlib/math</strong>" +
            "<span style='margin-left:12px;background:#2c0051;color:#ddb7ff;padding:2px 8px;border-radius:4px;font-size:10px;font-weight:700'>MATH</span></div></div>" +
            "<div class='code'><span class='kw'>import</span> <span class='str'>\"stdlib/math\"</span>;</div>" +
            "<table><tr><th>Function</th><th>Description</th><th>Example</th></tr>" +
            "<tr><td><code>abs(n)</code></td><td>Absolute value</td><td><code>abs(-5)</code> → 5</td></tr>" +
            "<tr><td><code>max(a, b)</code></td><td>Larger of two numbers</td><td><code>max(3, 9)</code> → 9</td></tr>" +
            "<tr><td><code>min(a, b)</code></td><td>Smaller of two numbers</td><td><code>min(3, 9)</code> → 3</td></tr>" +
            "<tr><td><code>pow(base, exp)</code></td><td>base raised to exp</td><td><code>pow(2, 8)</code> → 256</td></tr>" +
            "<tr><td><code>factorial(n)</code></td><td>n! (n factorial)</td><td><code>factorial(6)</code> → 720</td></tr>" +
            "<tr><td><code>clamp(v, lo, hi)</code></td><td>Keeps value within range</td><td><code>clamp(150, 0, 100)</code> → 100</td></tr>" +
            "<tr><td><code>sum(arr)</code></td><td>Sum of numeric array</td><td><code>sum([1,2,3])</code> → 6</td></tr>" +
            "<tr><td><code>average(arr)</code></td><td>Average of numeric array</td><td><code>average([2,4,6])</code> → 4</td></tr>" +
            "<tr><td><code>isEven(n)</code></td><td>True if n is even</td><td><code>isEven(4)</code> → true</td></tr>" +
            "<tr><td><code>isOdd(n)</code></td><td>True if n is odd</td><td><code>isOdd(7)</code> → true</td></tr>" +
            "<tr><td><code>gcd(a, b)</code></td><td>Greatest common divisor</td><td><code>gcd(12, 8)</code> → 4</td></tr>" +
            "</table>" +
            "<div class='code'><span class='cmt'>// Usage example</span>\n" +
            "<span class='kw'>import</span> <span class='str'>\"stdlib/math\"</span>;\n\n" +
            "print(<span class='fn'>factorial</span>(<span class='num'>5</span>));      <span class='cmt'>// 120</span>\n" +
            "print(<span class='fn'>pow</span>(<span class='num'>2</span>, <span class='num'>10</span>));     <span class='cmt'>// 1024</span>\n" +
            "print(<span class='fn'>clamp</span>(<span class='num'>200</span>, <span class='num'>0</span>, <span class='num'>100</span>));<span class='cmt'>// 100</span>\n" +
            "<span class='kw'>let</span> nums = [<span class='num'>4</span>, <span class='num'>8</span>, <span class='num'>15</span>, <span class='num'>16</span>, <span class='num'>23</span>];\n" +
            "print(<span class='fn'>average</span>(nums));  <span class='cmt'>// 13</span></div>" +
            "</div>" +

            // array
            "<div style='background:#0c0e12;border:1px solid #3b82f644;border-radius:10px;padding:24px;margin:24px 0'>" +
            "<div style='display:flex;align-items:center;margin-bottom:14px'>" +
            "<span style='font-size:22px;margin-right:12px'>📦</span>" +
            "<div><strong style='color:#adc6ff;font-size:16px'>stdlib/array</strong>" +
            "<span style='margin-left:12px;background:#1e3a5f;color:#adc6ff;padding:2px 8px;border-radius:4px;font-size:10px;font-weight:700'>ARRAY</span></div></div>" +
            "<div class='code'><span class='kw'>import</span> <span class='str'>\"stdlib/array\"</span>;</div>" +
            "<table><tr><th>Function</th><th>Description</th><th>Example</th></tr>" +
            "<tr><td><code>contains(arr, val)</code></td><td>True if arr has val</td><td><code>contains([1,2,3], 2)</code> → true</td></tr>" +
            "<tr><td><code>indexOf(arr, val)</code></td><td>Index of val, or -1</td><td><code>indexOf([\"a\",\"b\"], \"b\")</code> → 1</td></tr>" +
            "<tr><td><code>first(arr)</code></td><td>First element</td><td><code>first([10,20])</code> → 10</td></tr>" +
            "<tr><td><code>last(arr)</code></td><td>Last element</td><td><code>last([10,20,30])</code> → 30</td></tr>" +
            "<tr><td><code>printAll(arr)</code></td><td>Print each element with index</td><td><code>printAll([\"a\",\"b\"])</code></td></tr>" +
            "<tr><td><code>sumArray(arr)</code></td><td>Sum of all elements</td><td><code>sumArray([1,2,3])</code> → 6</td></tr>" +
            "<tr><td><code>maxElement(arr)</code></td><td>Maximum element</td><td><code>maxElement([3,1,9])</code> → 9</td></tr>" +
            "<tr><td><code>minElement(arr)</code></td><td>Minimum element</td><td><code>minElement([3,1,9])</code> → 1</td></tr>" +
            "<tr><td><code>countOccurrences(arr,v)</code></td><td>Count of v in arr</td><td><code>countOccurrences([1,1,2],1)</code> → 2</td></tr>" +
            "<tr><td><code>joinToString(arr, sep)</code></td><td>Join array as string</td><td><code>joinToString([\"a\",\"b\"],\", \")</code> → \"a, b\"</td></tr>" +
            "</table>" +
            "<div class='code'><span class='cmt'>// Usage example</span>\n" +
            "<span class='kw'>import</span> <span class='str'>\"stdlib/array\"</span>;\n\n" +
            "<span class='kw'>let</span> scores = [<span class='num'>88</span>, <span class='num'>92</span>, <span class='num'>75</span>, <span class='num'>88</span>, <span class='num'>61</span>];\n" +
            "print(<span class='fn'>maxElement</span>(scores));         <span class='cmt'>// 92</span>\n" +
            "print(<span class='fn'>countOccurrences</span>(scores, <span class='num'>88</span>)); <span class='cmt'>// 2</span>\n" +
            "print(<span class='fn'>joinToString</span>(scores, <span class='str'>\" | \"</span>));  <span class='cmt'>// 88 | 92 | ...</span></div>" +
            "</div>" +

            // string
            "<div style='background:#0c0e12;border:1px solid #f9731644;border-radius:10px;padding:24px;margin:24px 0'>" +
            "<div style='display:flex;align-items:center;margin-bottom:14px'>" +
            "<span style='font-size:22px;margin-right:12px'>🔤</span>" +
            "<div><strong style='color:#ffb690;font-size:16px'>stdlib/string</strong>" +
            "<span style='margin-left:12px;background:#341100;color:#ffb690;padding:2px 8px;border-radius:4px;font-size:10px;font-weight:700'>STRING</span></div></div>" +
            "<div class='code'><span class='kw'>import</span> <span class='str'>\"stdlib/string\"</span>;</div>" +
            "<table><tr><th>Function</th><th>Description</th><th>Example</th></tr>" +
            "<tr><td><code>repeat(str, n)</code></td><td>Repeat string n times</td><td><code>repeat(\"ab\", 3)</code> → \"ababab\"</td></tr>" +
            "<tr><td><code>padLeft(str, w, ch)</code></td><td>Left-pad to width w</td><td><code>padLeft(\"5\", 3, \"0\")</code> → \"005\"</td></tr>" +
            "<tr><td><code>isEmpty(str)</code></td><td>True if string is empty</td><td><code>isEmpty(\"\")</code> → true</td></tr>" +
            "<tr><td><code>notEmpty(str)</code></td><td>True if not empty</td><td><code>notEmpty(\"hi\")</code> → true</td></tr>" +
            "<tr><td><code>wrapHtml(tag, content)</code></td><td>Wrap in HTML tag</td><td><code>wrapHtml(\"b\",\"text\")</code> → &lt;b&gt;text&lt;/b&gt;</td></tr>" +
            "<tr><td><code>heading(level, text)</code></td><td>HTML heading h1–h6</td><td><code>heading(2, \"Title\")</code></td></tr>" +
            "<tr><td><code>bold(text)</code></td><td>Wrap in &lt;strong&gt;</td><td><code>bold(\"word\")</code></td></tr>" +
            "<tr><td><code>italic(text)</code></td><td>Wrap in &lt;em&gt;</td><td><code>italic(\"word\")</code></td></tr>" +
            "<tr><td><code>styledDiv(content, style)</code></td><td>Div with inline style</td><td><code>styledDiv(\"Hi\",\"color:red\")</code></td></tr>" +
            "<tr><td><code>pluralize(n, sing, pl)</code></td><td>Singular/plural text</td><td><code>pluralize(3,\"item\",\"items\")</code> → \"3 items\"</td></tr>" +
            "</table>" +
            "<div class='code'><span class='cmt'>// Usage example</span>\n" +
            "<span class='kw'>import</span> <span class='str'>\"stdlib/string\"</span>;\n\n" +
            "<span class='kw'>let</span> banner = <span class='fn'>repeat</span>(<span class='str'>\"-\"</span>, <span class='num'>30</span>);\n" +
            "<span class='kw'>let</span> title  = <span class='fn'>heading</span>(<span class='num'>1</span>, <span class='str'>\"Welcome\"</span>);\n" +
            "<span class='kw'>let</span> count  = <span class='fn'>pluralize</span>(<span class='num'>7</span>, <span class='str'>\"user\"</span>, <span class='str'>\"users\"</span>);\n" +
            "<span class='fn'>render</span>(title + <span class='fn'>bold</span>(count));</div>" +
            "</div>" +

            // collections
            "<div style='background:#0c0e12;border:1px solid #22c55e44;border-radius:10px;padding:24px;margin:24px 0'>" +
            "<div style='display:flex;align-items:center;margin-bottom:14px'>" +
            "<span style='font-size:22px;margin-right:12px'>🗂️</span>" +
            "<div><strong style='color:#86efac;font-size:16px'>stdlib/collections</strong>" +
            "<span style='margin-left:12px;background:#0d3321;color:#22c55e;padding:2px 8px;border-radius:4px;font-size:10px;font-weight:700'>COLLECTIONS</span></div></div>" +
            "<div class='code'><span class='kw'>import</span> <span class='str'>\"stdlib/collections\"</span>;</div>" +
            "<table><tr><th>Function</th><th>Description</th><th>Example</th></tr>" +
            "<tr><td><code>stackNew()</code></td><td>Create empty stack (array)</td><td><code>let s = stackNew();</code></td></tr>" +
            "<tr><td><code>stackSize(stack)</code></td><td>Number of items</td><td><code>stackSize(s)</code> → 0</td></tr>" +
            "<tr><td><code>stackIsEmpty(stack)</code></td><td>True if empty</td><td><code>stackIsEmpty(s)</code> → true</td></tr>" +
            "<tr><td><code>stackPeek(stack)</code></td><td>View top without removing</td><td><code>stackPeek(s)</code></td></tr>" +
            "<tr><td><code>range(from, to)</code></td><td>Number range as string</td><td><code>range(1, 5)</code> → \"1, 2, 3, 4, 5\"</td></tr>" +
            "<tr><td><code>pairNew(key, val)</code></td><td>Create [key, value] pair</td><td><code>pairNew(\"x\", 10)</code></td></tr>" +
            "<tr><td><code>pairKey(pair)</code></td><td>Get key from pair</td><td><code>pairKey([\"x\",10])</code> → \"x\"</td></tr>" +
            "<tr><td><code>pairValue(pair)</code></td><td>Get value from pair</td><td><code>pairValue([\"x\",10])</code> → 10</td></tr>" +
            "<tr><td><code>counterCount(keys,vals,k)</code></td><td>Frequency lookup</td><td><code>counterCount(ks,vs,\"a\")</code></td></tr>" +
            "<tr><td><code>renderTableRow(cells)</code></td><td>HTML &lt;tr&gt; for data rows</td><td><code>renderTableRow([\"A\",\"B\"])</code></td></tr>" +
            "<tr><td><code>renderTableHeader(cells)</code></td><td>HTML &lt;tr&gt; for header</td><td><code>renderTableHeader([\"Name\",\"Val\"])</code></td></tr>" +
            "</table>" +
            "<div class='code'><span class='cmt'>// Usage example — render an HTML table</span>\n" +
            "<span class='kw'>import</span> <span class='str'>\"stdlib/collections\"</span>;\n\n" +
            "<span class='kw'>let</span> header = <span class='fn'>renderTableHeader</span>([<span class='str'>\"Name\"</span>, <span class='str'>\"Score\"</span>]);\n" +
            "<span class='kw'>let</span> row1   = <span class='fn'>renderTableRow</span>([<span class='str'>\"Alice\"</span>, <span class='str'>\"95\"</span>]);\n" +
            "<span class='kw'>let</span> row2   = <span class='fn'>renderTableRow</span>([<span class='str'>\"Bob\"</span>,   <span class='str'>\"87\"</span>]);\n" +
            "<span class='kw'>let</span> table  = <span class='str'>\"&lt;table style='border-collapse:collapse'&gt;\"</span> + header + row1 + row2 + <span class='str'>\"&lt;/table&gt;\"</span>;\n" +
            "<span class='fn'>render</span>(table);</div>" +
            "</div>" +

            "<br><br></div></body></html>";

    }

    // ── Helpers ────────────────────────────────────────────
    private void setStatus(String m){ SwingUtilities.invokeLater(()->statusLbl.setText("  "+m)); }

    private JPanel box(Color bg, LayoutManager lm){
        JPanel p = lm==null?new JPanel():new JPanel(lm);
        p.setBackground(bg); p.setOpaque(true); return p;
    }
    private JLabel lbl(String t,int style,int size,Color fg){
        JLabel l=new JLabel(t); l.setFont(new Font("SansSerif",style,size)); l.setForeground(fg); return l;
    }
    private JButton btn(String text,Color bg,Color fg,ActionListener a){
        JButton b=new JButton(text);
        b.setBackground(bg); b.setForeground(fg);
        b.setFont(new Font("SansSerif",Font.BOLD,12));
        b.setBorder(BorderFactory.createEmptyBorder(5,14,5,14));
        b.setFocusPainted(false); b.setOpaque(true); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color orig=bg;
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ b.setBackground(orig.brighter()); }
            public void mouseExited(MouseEvent e) { b.setBackground(orig); }
        });
        b.addActionListener(a); return b;
    }
    private JSplitPane spl(int o,Component a,Component b){
        JSplitPane sp=new JSplitPane(o,a,b);
        sp.setDividerSize(1); sp.setBorder(null); sp.setBackground(S_BORDER); return sp;
    }
    private JScrollPane scr(Component c,Color bg){
        JScrollPane sp=new JScrollPane(c);
        sp.setBorder(null); sp.getViewport().setBackground(bg); return sp;
    }

    // ── Launch: Metal + full UIManager dark override ───────
    public static void launch() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); } catch(Exception e){}

        Color bg=S_BG, ed=S_EDITOR, si=S_SIDE, tx=S_TEXT, br=S_BORDER, sel=S_SEL, ac=S_ACT;

        Object[][] ui={
            {"Panel.background",ac},{"Panel.foreground",tx},
            {"ScrollPane.background",ed},{"Viewport.background",ed},
            {"ScrollBar.background",ac},{"ScrollBar.thumb",br},
            {"ScrollBar.thumbDarkShadow",br},{"ScrollBar.thumbHighlight",br},
            {"ScrollBar.thumbShadow",br},{"ScrollBar.track",ac},
            {"Tree.background",bg},{"Tree.foreground",tx},
            {"Tree.textBackground",bg},{"Tree.textForeground",tx},
            {"Tree.selectionBackground",sel},{"Tree.selectionForeground",tx},
            {"Tree.hash",br},{"Tree.line",br},
            {"TabbedPane.background",ac},{"TabbedPane.foreground",tx},
            {"TabbedPane.selected",ed},{"TabbedPane.contentAreaColor",ed},
            {"TabbedPane.tabAreaBackground",bg},{"TabbedPane.unselectedBackground",bg},
            {"TabbedPane.darkShadow",br},{"TabbedPane.shadow",br},
            {"TabbedPane.highlight",br},{"TabbedPane.light",br},
            {"ComboBox.background",bg},{"ComboBox.foreground",new Color(0x8C,0x90,0x9F)},
            {"ComboBox.selectionBackground",sel},{"ComboBox.selectionForeground",tx},
            {"ComboBox.disabledBackground",bg},
            {"List.background",bg},{"List.foreground",tx},
            {"List.selectionBackground",sel},{"List.selectionForeground",tx},
            {"TextField.background",ed},{"TextField.foreground",new Color(0x8C,0x90,0x9F)},
            {"TextField.caretForeground",tx},{"TextField.selectionBackground",sel},
            {"TextArea.background",ed},{"TextArea.foreground",tx},
            {"TextArea.caretForeground",new Color(0xAD,0xC6,0xFF)},
            {"TextArea.selectionBackground",sel},
            {"Button.background",si},{"Button.foreground",tx},
            {"Button.shadow",br},{"Button.darkShadow",br},
            {"Button.light",ac},{"Button.highlight",br},
            {"Label.foreground",tx},{"Label.background",bg},
            {"SplitPane.background",br},
            {"ToolTip.background",si},{"ToolTip.foreground",tx},
            {"PopupMenu.background",bg},{"PopupMenu.foreground",tx},
            {"MenuItem.background",bg},{"MenuItem.foreground",tx},
            {"MenuItem.selectionBackground",sel},
            {"OptionPane.background",bg},{"OptionPane.messageForeground",tx},
            {"FileChooser.background",bg},{"FileView.background",bg},
        };
        for(Object[] row: ui) UIManager.put((String)row[0], row[1]);

        SwingUtilities.invokeLater(()->new MiraIDE().setVisible(true));
    }
}
