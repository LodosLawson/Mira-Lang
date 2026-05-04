package com.actmira.evaluator;

import com.actmira.ast.*;
import com.actmira.lexer.Lexer;
import com.actmira.parser.Parser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Evaluator {

    public static final MiraNull NULL = new MiraNull();
    public static final MiraBoolean TRUE = new MiraBoolean(true);
    public static final MiraBoolean FALSE = new MiraBoolean(false);

    public static MiraObject eval(Node node, Environment env) {
        if (node instanceof Program) {
            return evalProgram((Program) node, env);
        } else if (node instanceof BlockStatement) {
            return evalBlockStatement((BlockStatement) node, env);
        } else if (node instanceof ImportStatement) {
            return evalImport((ImportStatement) node, env);
        } else if (node instanceof ReturnStatement) {
            MiraObject val = eval(((ReturnStatement) node).getReturnValue(), env);
            if (isError(val)) return val;
            return new MiraReturnValue(val);
        } else if (node instanceof ExpressionStatement) {
            return eval(((ExpressionStatement) node).getExpression(), env);
        } else if (node instanceof IntegerLiteral) {
            return new MiraInteger(((IntegerLiteral) node).getValue());
        } else if (node instanceof StringLiteral) {
            return new MiraString(((StringLiteral) node).getValue());
        } else if (node instanceof BooleanLiteral) {
            return ((BooleanLiteral) node).getValue() ? TRUE : FALSE;
        } else if (node instanceof FunctionLiteral) {
            FunctionLiteral func = (FunctionLiteral) node;
            return new MiraFunction(func.getParameters(), func.getBody(), env);
        } else if (node instanceof ArrayLiteral) {
            ArrayLiteral arrayLiteral = (ArrayLiteral) node;
            java.util.List<MiraObject> elements = new java.util.ArrayList<>();
            for (Expression exp : arrayLiteral.getElements()) {
                MiraObject evaluated = eval(exp, env);
                if (isError(evaluated)) return evaluated;
                elements.add(evaluated);
            }
            return new MiraArray(elements);
        } else if (node instanceof IndexExpression) {
            IndexExpression indexExp = (IndexExpression) node;
            MiraObject left = eval(indexExp.getLeft(), env);
            if (isError(left)) return left;
            MiraObject index = eval(indexExp.getIndex(), env);
            if (isError(index)) return index;
            return evalIndexExpression(left, index);
        } else if (node instanceof IfExpression) {
            IfExpression ie = (IfExpression) node;
            MiraObject condition = eval(ie.getCondition(), env);
            if (isError(condition)) return condition;
            
            if (isTruthy(condition)) {
                return eval(ie.getConsequence(), env);
            } else if (ie.getAlternative() != null) {
                return eval(ie.getAlternative(), env);
            } else {
                return NULL;
            }
        } else if (node instanceof WhileExpression) {
            WhileExpression we = (WhileExpression) node;
            MiraObject result = NULL;
            while (true) {
                MiraObject condition = eval(we.getCondition(), env);
                if (isError(condition)) return condition;
                if (!isTruthy(condition)) break;
                
                result = eval(we.getConsequence(), env);
                if (result != null && (result.type().equals("RETURN_VALUE") || result.type().equals("ERROR"))) {
                    return result;
                }
            }
            return result;
        } else if (node instanceof InfixExpression) {
            InfixExpression infix = (InfixExpression) node;
            
            // Special handling for object property access (mock)
            if (infix.getOperator().equals(".")) {
                if (infix.getRight() instanceof CallExpression) {
                    CallExpression rightCall = (CallExpression) infix.getRight();
                    if (rightCall.getFunction() instanceof Identifier) {
                        String methodName = ((Identifier) rightCall.getFunction()).getValue();
                        if (methodName.equals("connect")) return TRUE;
                        if (methodName.equals("disconnect")) return FALSE;
                    }
                }
                return NULL;
            }
            
            MiraObject left = eval(infix.getLeft(), env);
            MiraObject right = eval(infix.getRight(), env);
            return evalInfixExpression(infix.getOperator(), left, right);
        } else if (node instanceof LetStatement) {
            LetStatement let = (LetStatement) node;
            MiraObject val = eval(let.getValue(), env);
            if (isError(val)) return val;
            env.set(let.getName().getValue(), val);
            return val;
        } else if (node instanceof Identifier) {
            return evalIdentifier((Identifier) node, env);
        } else if (node instanceof CallExpression) {
            CallExpression call = (CallExpression) node;
            
            // Handle native print
            if (call.getFunction() instanceof Identifier) {
                Identifier ident = (Identifier) call.getFunction();
                if (ident.getValue().equals("print") && call.getArguments().size() > 0) {
                    return eval(call.getArguments().get(0), env);
                }
                if (ident.getValue().equals("length") && call.getArguments().size() > 0) {
                    MiraObject arg = eval(call.getArguments().get(0), env);
                    if (arg instanceof MiraArray) {
                        return new MiraInteger(((MiraArray)arg).getElements().size());
                    }
                    if (arg instanceof MiraString) {
                        return new MiraInteger(((MiraString)arg).getValue().length());
                    }
                }
                if (ident.getValue().equals("render") && call.getArguments().size() > 0) {
                    MiraObject arg = eval(call.getArguments().get(0), env);
                    if (arg instanceof MiraString && Engine.htmlRenderer != null) {
                        Engine.htmlRenderer.accept(((MiraString)arg).getValue());
                    }
                    return arg;
                }
                // openBrowser(html) — writes to temp file and opens in system default browser
                if (ident.getValue().equals("openBrowser") && call.getArguments().size() > 0) {
                    MiraObject arg = eval(call.getArguments().get(0), env);
                    if (arg instanceof MiraString) {
                        try {
                            java.io.File tmp = java.io.File.createTempFile("mira_scene_", ".html");
                            tmp.deleteOnExit();
                            java.nio.file.Files.writeString(tmp.toPath(), ((MiraString)arg).getValue());
                            java.awt.Desktop.getDesktop().browse(tmp.toURI());
                            // Also update the live preview panel
                            if (Engine.htmlRenderer != null)
                                Engine.htmlRenderer.accept(((MiraString)arg).getValue());
                            return new MiraString("[Opened in browser] " + tmp.getName());
                        } catch (Exception e) {
                            return new MiraError("openBrowser failed: " + e.getMessage());
                        }
                    }
                }

                if (ident.getValue().equals("writeFile") && call.getArguments().size() == 2) {
                    MiraObject pathArg = eval(call.getArguments().get(0), env);
                    MiraObject dataArg = eval(call.getArguments().get(1), env);
                    if (pathArg instanceof MiraString && dataArg instanceof MiraString) {
                        try {
                            java.nio.file.Files.writeString(java.nio.file.Path.of(((MiraString)pathArg).getValue()), ((MiraString)dataArg).getValue());
                            return new MiraInteger(1);
                        } catch (Exception e) {
                            return new MiraError("failed to write file: " + e.getMessage());
                        }
                    }
                }
                if (ident.getValue().equals("readFile") && call.getArguments().size() == 1) {
                    MiraObject pathArg = eval(call.getArguments().get(0), env);
                    if (pathArg instanceof MiraString) {
                        try {
                            String content = java.nio.file.Files.readString(java.nio.file.Path.of(((MiraString)pathArg).getValue()));
                            return new MiraString(content);
                        } catch (Exception e) {
                            return new MiraError("failed to read file: " + e.getMessage());
                        }
                    }
                }
            }
            
            // Handle class mock
            if (call.getFunction() instanceof Identifier && ((Identifier)call.getFunction()).getValue().equals("DatabaseConnection")) {
                 return new MiraString("Mock DB Instance");
            }
            
            MiraObject function = eval(call.getFunction(), env);
            if (isError(function)) return function;

            // If the evaluated function is the built-in sentinel, dispatch by name
            if (function == BUILTIN_SENTINEL && call.getFunction() instanceof Identifier) {
                String name = ((Identifier) call.getFunction()).getValue();
                java.util.List<MiraObject> bargs = new java.util.ArrayList<>();
                for (Expression exp : call.getArguments()) bargs.add(eval(exp, env));

                // openBrowser
                if (name.equals("openBrowser") && !bargs.isEmpty()) {
                    MiraObject a = bargs.get(0);
                    String htmlStr = a.inspect().replaceAll("^\"|\"$", "");
                    if (a instanceof MiraString) htmlStr = ((MiraString)a).getValue();
                    try {
                        java.io.File tmp = java.io.File.createTempFile("mira_scene_", ".html");
                        tmp.deleteOnExit();
                        java.nio.file.Files.writeString(tmp.toPath(), htmlStr);
                        java.awt.Desktop.getDesktop().browse(tmp.toURI());
                        if (Engine.htmlRenderer != null) Engine.htmlRenderer.accept(htmlStr);
                        return new MiraString("[Opened in browser] " + tmp.getName());
                    } catch (Exception e) {
                        return new MiraError("openBrowser failed: " + e.getMessage());
                    }
                }
                // render
                if (name.equals("render") && !bargs.isEmpty()) {
                    MiraObject a = bargs.get(0);
                    if (a instanceof MiraString && Engine.htmlRenderer != null)
                        Engine.htmlRenderer.accept(((MiraString)a).getValue());
                    return a;
                }
                // print — return value (output captured in evalProgram)
                if (name.equals("print") && !bargs.isEmpty()) return bargs.get(0);
                // length
                if (name.equals("length") && !bargs.isEmpty()) {
                    MiraObject a = bargs.get(0);
                    if (a instanceof MiraArray) return new MiraInteger(((MiraArray)a).getElements().size());
                    if (a instanceof MiraString) return new MiraInteger(((MiraString)a).getValue().length());
                }
                // readFile
                if (name.equals("readFile") && !bargs.isEmpty() && bargs.get(0) instanceof MiraString) {
                    try { return new MiraString(java.nio.file.Files.readString(java.nio.file.Path.of(((MiraString)bargs.get(0)).getValue()))); }
                    catch (Exception e) { return new MiraError("readFile failed: " + e.getMessage()); }
                }
                // writeFile
                if (name.equals("writeFile") && bargs.size() >= 2
                        && bargs.get(0) instanceof MiraString && bargs.get(1) instanceof MiraString) {
                    try { java.nio.file.Files.writeString(java.nio.file.Path.of(((MiraString)bargs.get(0)).getValue()), ((MiraString)bargs.get(1)).getValue()); return new MiraInteger(1); }
                    catch (Exception e) { return new MiraError("writeFile failed: " + e.getMessage()); }
                }

                // openNativeWindow(title, w, h, bgHex, commandString)
                if (name.equals("openNativeWindow") && bargs.size() == 5) {
                    try {
                        String title = ((MiraString)bargs.get(0)).getValue();
                        int w = ((MiraInteger)bargs.get(1)).getValue();
                        int h = ((MiraInteger)bargs.get(2)).getValue();
                        String bg = ((MiraString)bargs.get(3)).getValue();
                        String cmds = ((MiraString)bargs.get(4)).getValue();

                        javax.swing.SwingUtilities.invokeLater(() -> {
                            javax.swing.JFrame frame = new javax.swing.JFrame(title);
                            frame.setSize(w, h + 30); // Title bar offset
                            frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                            frame.setLocationRelativeTo(null);

                            javax.swing.JPanel panel = new javax.swing.JPanel() {
                                java.awt.Color shadowColor = null;
                                int shadowBlur = 0; int shadowX = 0; int shadowY = 0;

                                private void drawShape(java.awt.Graphics2D g2, Runnable drawCode) {
                                    if (shadowColor != null) {
                                        java.awt.Paint oldPaint = g2.getPaint();
                                        g2.setColor(shadowColor);
                                        g2.translate(shadowX, shadowY);
                                        drawCode.run();
                                        g2.translate(-shadowX, -shadowY);
                                        g2.setPaint(oldPaint);
                                    }
                                    drawCode.run();
                                }

                                @Override
                                protected void paintComponent(java.awt.Graphics g) {
                                    super.paintComponent(g);
                                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
                                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                                    g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                                    try { g2.setColor(java.awt.Color.decode(bg)); } catch(Exception e){ g2.setColor(java.awt.Color.BLACK); }
                                    g2.fillRect(0, 0, getWidth(), getHeight());

                                    String[] commands = cmds.split(";");
                                    for(String cmd : commands) {
                                        if(cmd.trim().isEmpty()) continue;
                                        String[] p = cmd.split(",");
                                        try {
                                            switch(p[0]) {
                                                case "rect":
                                                    g2.setColor(java.awt.Color.decode(p[5]));
                                                    drawShape(g2, () -> g2.fillRect(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4])));
                                                    break;
                                                case "roundRect":
                                                    g2.setColor(java.awt.Color.decode(p[6]));
                                                    int r = Integer.parseInt(p[5]);
                                                    drawShape(g2, () -> g2.fillRoundRect(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4]), r*2, r*2));
                                                    break;
                                                case "circle":
                                                    g2.setColor(java.awt.Color.decode(p[4]));
                                                    int cr = Integer.parseInt(p[3]);
                                                    drawShape(g2, () -> g2.fillOval(Integer.parseInt(p[1]) - cr, Integer.parseInt(p[2]) - cr, cr*2, cr*2));
                                                    break;
                                                case "line":
                                                    g2.setColor(java.awt.Color.decode(p[5]));
                                                    g2.setStroke(new java.awt.BasicStroke(Integer.parseInt(p[6])));
                                                    drawShape(g2, () -> g2.drawLine(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4])));
                                                    break;
                                                case "text":
                                                    g2.setColor(java.awt.Color.decode(p[3]));
                                                    g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, Integer.parseInt(p[4])));
                                                    drawShape(g2, () -> g2.drawString(p[5], Integer.parseInt(p[1]), Integer.parseInt(p[2])));
                                                    break;
                                                case "textCenter":
                                                    g2.setColor(java.awt.Color.decode(p[3]));
                                                    g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, Integer.parseInt(p[4])));
                                                    int tw = g2.getFontMetrics().stringWidth(p[5]);
                                                    drawShape(g2, () -> g2.drawString(p[5], Integer.parseInt(p[1]) - tw/2, Integer.parseInt(p[2])));
                                                    break;
                                                case "shadow":
                                                    shadowColor = java.awt.Color.decode(p[1]);
                                                    // Convert hex to transparent if length == 9 (#RRGGBBAA)
                                                    if (p[1].length() == 9) {
                                                        int rgba = (int) Long.parseLong(p[1].substring(1), 16);
                                                        int aa = (rgba & 0xFF);
                                                        int rr = (rgba >> 24) & 0xFF;
                                                        int gg = (rgba >> 16) & 0xFF;
                                                        int bb = (rgba >> 8) & 0xFF;
                                                        shadowColor = new java.awt.Color(rr, gg, bb, aa);
                                                    }
                                                    shadowBlur = Integer.parseInt(p[2]);
                                                    shadowX = Integer.parseInt(p[3]);
                                                    shadowY = Integer.parseInt(p[4]);
                                                    break;
                                                case "clearShadow":
                                                    shadowColor = null;
                                                    break;
                                            }
                                        } catch(Exception e) { /* ignore malformed */ }
                                    }
                                }
                            };
                            frame.add(panel);
                            frame.setVisible(true);
                        });
                        return new MiraInteger(1);
                    } catch (Exception e) {
                        return new MiraError("openNativeWindow failed: " + e.getMessage());
                    }
                }
                return NULL;
            }

            java.util.List<MiraObject> args = new java.util.ArrayList<>();
            for (Expression exp : call.getArguments()) {
                args.add(eval(exp, env));
            }

            return applyFunction(function, args);
        }
        return NULL;
    }

    private static MiraObject evalIndexExpression(MiraObject left, MiraObject index) {
        if (left.type().equals("ARRAY") && index.type().equals("INTEGER")) {
            MiraArray arrayObject = (MiraArray) left;
            int idx = ((MiraInteger) index).getValue();
            int max = arrayObject.getElements().size() - 1;
            if (idx < 0 || idx > max) return NULL;
            return arrayObject.getElements().get(idx);
        }
        return new MiraError("index operator not supported: " + left.type());
    }

    private static MiraObject applyFunction(MiraObject fn, java.util.List<MiraObject> args) {
        if (!(fn instanceof MiraFunction)) {
            return new MiraError("not a function: " + fn.type());
        }
        
        MiraFunction function = (MiraFunction) fn;
        Environment extendedEnv = new Environment(function.getEnv());
        
        for (int i = 0; i < function.getParameters().size(); i++) {
            if (i < args.size()) {
                extendedEnv.set(function.getParameters().get(i).getValue(), args.get(i));
            }
        }
        
        MiraObject evaluated = eval(function.getBody(), extendedEnv);
        if (evaluated instanceof MiraReturnValue) {
            return ((MiraReturnValue) evaluated).getValue();
        }
        return evaluated;
    }

    private static MiraObject evalProgram(Program program, Environment env) {
        MiraObject result = null;
        StringBuilder output = new StringBuilder();

        for (Statement stmt : program.getStatements()) {
            if (stmt instanceof ExpressionStatement && ((ExpressionStatement) stmt).getExpression() instanceof CallExpression) {
                CallExpression call = (CallExpression) ((ExpressionStatement) stmt).getExpression();
                if (call.getFunction() instanceof Identifier && ((Identifier)call.getFunction()).getValue().equals("print")) {
                    if (call.getArguments().size() > 0) {
                        MiraObject arg = eval(call.getArguments().get(0), env);
                        output.append(arg.inspect()).append("\n");
                    }
                    continue;
                }
            }
            
            result = eval(stmt, env);
            if (result instanceof MiraReturnValue) {
                return ((MiraReturnValue) result).getValue();
            } else if (result instanceof MiraError) {
                return result;
            }
        }
        
        return new MiraString(output.toString());
    }

    private static MiraObject evalBlockStatement(BlockStatement block, Environment env) {
        MiraObject result = null;
        for (Statement stmt : block.getStatements()) {
            result = eval(stmt, env);
            if (result != null && (result.type().equals("RETURN_VALUE") || result.type().equals("ERROR"))) {
                return result;
            }
        }
        return result != null ? result : NULL;
    }

    private static MiraObject evalInfixExpression(String operator, MiraObject left, MiraObject right) {
        if (left.type().equals("INTEGER") && right.type().equals("INTEGER")) {
            return evalIntegerInfixExpression(operator, (MiraInteger) left, (MiraInteger) right);
        }
        if (operator.equals("+") && (left.type().equals("STRING") || right.type().equals("STRING"))) {
            return new MiraString(left.inspect().replace("\"", "") + right.inspect().replace("\"", ""));
        }
        if (operator.equals("==")) {
            if (left.type().equals("STRING") && right.type().equals("STRING")) {
                return ((MiraString)left).getValue().equals(((MiraString)right).getValue()) ? TRUE : FALSE;
            }
            return left == right ? TRUE : FALSE;
        }
        if (operator.equals("!=")) {
            if (left.type().equals("STRING") && right.type().equals("STRING")) {
                return !((MiraString)left).getValue().equals(((MiraString)right).getValue()) ? TRUE : FALSE;
            }
            return left != right ? TRUE : FALSE;
        }
        return new MiraError("unknown operator: " + left.type() + " " + operator + " " + right.type());
    }

    private static MiraObject evalIntegerInfixExpression(String operator, MiraInteger left, MiraInteger right) {
        int lVal = left.getValue();
        int rVal = right.getValue();

        switch (operator) {
            case "+": return new MiraInteger(lVal + rVal);
            case "-": return new MiraInteger(lVal - rVal);
            case "*": return new MiraInteger(lVal * rVal);
            case "/": return new MiraInteger(lVal / rVal);
            case "<": return lVal < rVal ? TRUE : FALSE;
            case ">": return lVal > rVal ? TRUE : FALSE;
            case "==": return lVal == rVal ? TRUE : FALSE;
            case "!=": return lVal != rVal ? TRUE : FALSE;
            default: return new MiraError("unknown operator: " + left.type() + " " + operator + " " + right.type());
        }
    }

    // Sentinel string used to mark native built-in names
    private static final MiraString BUILTIN_SENTINEL = new MiraString("__builtin__");

    private static MiraObject evalIdentifier(Identifier node, Environment env) {
        MiraObject val = env.get(node.getValue());
        if (val != null) return val;

        // Native built-in functions — these are handled by name in CallExpression;
        // returning a sentinel prevents "identifier not found" when they are used
        // as the function part of a CallExpression (e.g. openBrowser(html)).
        switch (node.getValue()) {
            case "print":
            case "length":
            case "render":
            case "openBrowser":
            case "openNativeWindow":
            case "readFile":
            case "writeFile":
            case "DatabaseConnection":
                return BUILTIN_SENTINEL;
        }

        return new MiraError("identifier not found: " + node.getValue());
    }

    private static boolean isError(MiraObject obj) {
        if (obj != null) {
            return obj.type().equals("ERROR");
        }
        return false;
    }

    private static boolean isTruthy(MiraObject obj) {
        if (obj == NULL) return false;
        if (obj == TRUE) return true;
        if (obj == FALSE) return false;
        if (obj instanceof MiraInteger) {
            return ((MiraInteger) obj).getValue() != 0;
        }
        return true;
    }

    // Tracks already-imported paths to prevent duplicate imports (public so Engine can reset it)
    public static final Set<String> importedPaths = new HashSet<>();

    private static MiraObject evalImport(ImportStatement node, Environment env) {
        String rawPath = node.getPath();
        // Resolve stdlib shorthand: "stdlib/math" -> "stdlib/math.mira"
        if (!rawPath.endsWith(".mira")) rawPath = rawPath + ".mira";

        // Try relative to CWD first, then relative to stdlib/
        Path resolved = Paths.get(rawPath);
        if (!Files.exists(resolved)) {
            resolved = Paths.get("stdlib", rawPath);
        }

        String canonical = resolved.toAbsolutePath().toString();
        if (importedPaths.contains(canonical)) {
            return NULL; // Already imported — skip
        }
        importedPaths.add(canonical);

        try {
            String source = Files.readString(resolved);
            Lexer lexer = new Lexer(source);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            if (!parser.getErrors().isEmpty()) {
                return new MiraError("import parse errors in '" + rawPath + "':\n  "
                    + String.join("\n  ", parser.getErrors()));
            }
            // Evaluate in the SAME environment so definitions are shared
            return eval(program, env);
        } catch (Exception e) {
            return new MiraError("import failed '" + rawPath + "': " + e.getMessage());
        }
    }
}
