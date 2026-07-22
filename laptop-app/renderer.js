const WS_URL = 'ws://localhost:8080';
const laptopKey = 'laptop_' + Math.random().toString(36).substr(2, 6);

let ws = new WebSocket(WS_URL);
let tabletKey = '';
let isConnected = false;

const img = document.getElementById('screenImg');
const canvas = document.getElementById('overlayCanvas');
const ctx = canvas.getContext('2d');
const statusEl = document.getElementById('status');
const connectBtn = document.getElementById('connectBtn');
const disconnectBtn = document.getElementById('disconnectBtn');

ws.binaryType = 'arraybuffer'; // لاستقبال الصور

ws.onopen = () => {
    ws.send(JSON.stringify({ type: 'register', key: laptopKey }));
};

ws.onmessage = (e) => {
    // محاولة فك JSON
    try {
        const data = JSON.parse(e.data);
        handleMessage(data);
    } catch {
        // إذا فشل، فهي صورة (ArrayBuffer)
        displayImage(e.data);
    }
};

function handleMessage(data) {
    switch (data.type) {
        case 'connected':
            isConnected = true;
            statusEl.textContent = '✅ متصل';
            statusEl.className = 'online';
            disconnectBtn.style.display = 'inline-block';
            connectBtn.style.display = 'none';
            alert('✅ تم القبول!');
            break;
        case 'rejected':
            statusEl.textContent = '❌ مرفوض';
            statusEl.className = 'offline';
            alert('❌ رفض الجهاز');
            break;
        case 'disconnected':
            isConnected = false;
            statusEl.textContent = '⛔ غير متصل';
            statusEl.className = 'offline';
            disconnectBtn.style.display = 'none';
            connectBtn.style.display = 'inline-block';
            alert('⛔ قطع التابلت الاتصال');
            break;
    }
}

function displayImage(arrayBuffer) {
    const blob = new Blob([arrayBuffer], { type: 'image/jpeg' });
    const url = URL.createObjectURL(blob);
    img.src = url;
    img.onload = () => {
        canvas.width = img.naturalWidth || 1080;
        canvas.height = img.naturalHeight || 2400;
    };
}

connectBtn.onclick = () => {
    tabletKey = document.getElementById('tabletKey').value.trim();
    if (!tabletKey) return alert('أدخل المفتاح');
    ws.send(JSON.stringify({
        type: 'connect',
        from: laptopKey,
        to: tabletKey
    }));
    statusEl.textContent = '⏳ جاري الطلب...';
    statusEl.className = 'waiting';
};

disconnectBtn.onclick = () => {
    ws.send(JSON.stringify({
        type: 'disconnect',
        from: laptopKey,
        to: tabletKey
    }));
    isConnected = false;
    statusEl.textContent = '⛔ غير متصل';
    statusEl.className = 'offline';
    disconnectBtn.style.display = 'none';
    connectBtn.style.display = 'inline-block';
};

// ✅ إرسال أحداث النقر والتمرير
canvas.addEventListener('click', (e) => {
    if (!isConnected) return;
    const rect = canvas.getBoundingClientRect();
    const scaleX = 1080 / canvas.width;
    const scaleY = 2400 / canvas.height;
    const x = Math.round((e.clientX - rect.left) * scaleX);
    const y = Math.round((e.clientY - rect.top) * scaleY);
    ws.send(JSON.stringify({
        type: 'control',
        to: tabletKey,
        action: 'tap',
        x, y
    }));
});

canvas.addEventListener('wheel', (e) => {
    e.preventDefault();
    if (!isConnected) return;
    const delta = e.deltaY > 0 ? 500 : -500;
    ws.send(JSON.stringify({
        type: 'control',
        to: tabletKey,
        action: 'swipe',
        x1: 540, y1: 1200,
        x2: 540, y2: 1200 + delta
    }));
});