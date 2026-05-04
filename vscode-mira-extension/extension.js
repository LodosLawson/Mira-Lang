const vscode = require('vscode');
const path = require('path');

/**
 * @param {vscode.ExtensionContext} context
 */
function activate(context) {
    console.log('Mira Language Extension activated');

    let disposable = vscode.commands.registerCommand('mira.runFile', function () {
        const editor = vscode.window.activeTextEditor;
        if (!editor) {
            vscode.window.showErrorMessage('No active Mira file to run.');
            return;
        }

        const document = editor.document;
        const filePath = document.fileName;
        
        if (document.languageId !== 'mira') {
            vscode.window.showErrorMessage('This is not a Mira file.');
            return;
        }

        // Save file before running
        document.save().then(() => {
            let terminal = vscode.window.terminals.find(t => t.name === 'Mira Compiler');
            if (!terminal) {
                terminal = vscode.window.createTerminal('Mira Compiler');
            }
            terminal.show();

            // Eklentinin kendi kurulu olduğu dizin (Self-Contained)
            const extPath = context.extensionPath;
            const binClasses = path.join(extPath, "bin", "classes");
            const binLib = path.join(extPath, "bin", "lib", "*");

            // Java motorunu eklentinin içinden çağırıyoruz!
            const command = \`java -cp "\${binClasses};\${binLib}" com.actmira.Main "\${filePath}"\`;
            terminal.sendText(command);
        });
    });

    context.subscriptions.push(disposable);
}

function deactivate() {}

module.exports = {
    activate,
    deactivate
}
