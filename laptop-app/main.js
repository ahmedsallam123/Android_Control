const { app, BrowserWindow } = require('electron');
let mainWindow;

app.whenReady().then(() => {
    mainWindow = new BrowserWindow({
        width: 1000,
        height: 800,
        webPreferences: {
            nodeIntegration: true,
            contextIsolation: false
        },
        icon: __dirname + '/icon.png',
        title: '🖥️ التحكم بالتابلت'
    });
    mainWindow.loadFile('index.html');
    mainWindow.setMenu(null);
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') app.quit();
});

