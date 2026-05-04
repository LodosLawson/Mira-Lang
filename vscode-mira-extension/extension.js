const vscode = require('vscode');

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

            // Sizin bilgisayarınızdaki (Mira derleyicisinin) tam dizini:
            const compilerDir = "d:\\\\MacSoftware\\\\ACTEHLILEDICI-ACT-VERSION-5.0+FULLVERSION+ANDROID+KERNEL\\\\ACTNverionMira";

            // Java motorunu direkt olarak kendi dizininden çağırıyoruz!
            const command = `cd "${compilerDir}" && java -cp "target/classes;lib/*" com.actmira.Main "${filePath}"`;
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
