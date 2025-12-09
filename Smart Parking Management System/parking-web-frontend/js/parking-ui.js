// 全局变量
let currentSpotId = null;
let isAdminLoggedIn = false;

/**
 * 页面加载完成后的初始化
 */
window.onload = function() {
    // 初始化数据
    initData();

    // 加载车位信息
    loadParkingSpots();

    // 加载停车记录
    loadParkingRecords();
};

/**
 * 加载并显示车位信息
 */
function loadParkingSpots() {
    const spots = getAllSpots();
    const grid = document.getElementById('parking-grid');
    grid.innerHTML = '';

    let freeCount = 0;

    // 遍历所有车位并创建UI元素
    spots.forEach(spot => {
        const col = document.createElement('div');
        col.className = 'col-md-3 mb-3';

        const spotElement = document.createElement('div');
        // 根据状态设置不同的背景颜色
        const statusClass = spot.status === 'FREE' ? 'bg-success' :
            spot.status === 'OCCUPIED' ? 'bg-danger' : 'bg-warning';

        spotElement.className = `parking-spot ${statusClass} text-white p-3 rounded`;
        spotElement.innerHTML = `<div>车位: ${spot.spotNumber}</div>
                                <div>状态: ${getStatusText(spot.status)}</div>`;

        // 设置点击事件
        spotElement.onclick = function() {
            handleSpotClick(spot);
        };

        col.appendChild(spotElement);
        grid.appendChild(col);

        // 统计空闲车位数量
        if (spot.status === 'FREE') freeCount++;
    });

    // 更新状态显示
    document.getElementById('free-count').textContent = freeCount;
}

/**
 * 加载并显示停车记录
 */
function loadParkingRecords() {
    const records = getAllRecords();
    const tbody = document.getElementById('records-table');
    tbody.innerHTML = '';

    // 按入场时间倒序排序
    records.sort((a, b) => new Date(b.entryTime) - new Date(a.entryTime));

    // 遍历记录并创建表格行
    records.forEach(record => {
        const row = tbody.insertRow();

        row.insertCell(0).textContent = record.recordId || '';
        row.insertCell(1).textContent = record.plateNum || '';
        row.insertCell(2).textContent = record.spotId || '';

        // 格式化时间显示
        const entryTime = record.entryTime ? new Date(record.entryTime).toLocaleString() : '';
        row.insertCell(3).textContent = entryTime;

        const exitTime = record.exitTime ? new Date(record.exitTime).toLocaleString() : '进行中';
        row.insertCell(4).textContent = exitTime;

        // 格式化费用显示
        const payment = record.payment !== null ? `${record.payment.toFixed(2)}元` : '待支付';
        row.insertCell(5).textContent = payment;
    });
}

/**
 * 处理车位点击事件
 * @param {Object} spot 车位对象
 */
function handleSpotClick(spot) {
    currentSpotId = spot.spotId;

    if (spot.status === 'FREE') {
        // 空闲车位，显示入场弹窗
        showInModal(spot);
    } else if (spot.status === 'OCCUPIED') {
        // 已占用车位，显示出场信息
        showOutModal(spot);
    }
}

/**
 * 显示车辆入场弹窗
 * @param {Object} spot 车位对象
 */
function showInModal(spot) {
    document.getElementById('modal-spot-number').textContent = spot.spotNumber;
    document.getElementById('plate-number').value = '';
    document.getElementById('in-modal').style.display = 'block';
}

/**
 * 显示车辆出场弹窗
 * @param {Object} spot 车位对象
 */
function showOutModal(spot) {
    const activeRecord = getActiveRecordBySpotId(spot.spotId);

    if (activeRecord) {
        document.getElementById('out-spot-number').textContent = spot.spotNumber;
        document.getElementById('out-plate-number').textContent = activeRecord.plateNum;
        document.getElementById('out-entry-time').textContent = new Date(activeRecord.entryTime).toLocaleString();

        // 计算停车时长
        const entryTime = new Date(activeRecord.entryTime).getTime();
        const now = new Date().getTime();
        const durationMinutes = Math.floor((now - entryTime) / (1000 * 60));
        const hours = Math.floor(durationMinutes / 60);
        const minutes = durationMinutes % 60;
        document.getElementById('out-duration').textContent = `${hours}小时${minutes}分钟`;

        // 预估费用
        const feeRule = getCurrentFeeRule();
        let estimatedFee = 0;
        if (durationMinutes > feeRule.freeMinutes) {
            const hours = Math.ceil((durationMinutes - feeRule.freeMinutes) / 60);
            estimatedFee = hours * feeRule.basePrice;
            if (estimatedFee > feeRule.dailyCap) {
                estimatedFee = feeRule.dailyCap;
            }
        }
        document.getElementById('out-payment').textContent = estimatedFee.toFixed(2);

        document.getElementById('out-modal').style.display = 'block';
    } else {
        alert('未找到该车位的停车记录');
    }
}

/**
 * 关闭弹窗
 * @param {String} modalId 弹窗ID
 */
function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

/**
 * 确认车辆入场
 */
function confirmParkIn() {
    const plateNum = document.getElementById('plate-number').value.trim();

    if (!plateNum) {
        alert('请输入车牌号');
        return;
    }

    // 调用业务逻辑进行入场操作
    const success = parkIn(plateNum, currentSpotId);

    if (success) {
        alert('车辆入场成功');
        closeModal('in-modal');
        // 刷新界面数据
        loadParkingSpots();
        loadParkingRecords();
    } else {
        alert('车位可能已被占用，请刷新页面重试');
    }
}

/**
 * 确认车辆出场
 */
function confirmParkOut() {
    // 调用业务逻辑进行出场操作
    const payment = parkOut(currentSpotId);

    if (payment !== null) {
        alert(`车辆出场成功，费用: ${payment.toFixed(2)} 元`);
        closeModal('out-modal');
        // 刷新界面数据
        loadParkingSpots();
        loadParkingRecords();
    } else {
        alert('未找到该车位的停车记录');
    }
}

/**
 * 管理员登录处理
 */
function adminLogin() {
    const username = document.getElementById('admin-username').value.trim();
    const password = document.getElementById('admin-password').value.trim();
    const messageElement = document.getElementById('login-message');

    // 调用登录验证
    const user = adminLogin(username, password);

    if (user) {
        isAdminLoggedIn = true;
        messageElement.textContent = '登录成功';
        messageElement.className = 'text-success';
        // 显示收费规则设置区域
        document.getElementById('fee-rule-section').style.display = 'block';
        // 加载当前收费规则
        loadFeeRule();
    } else {
        messageElement.textContent = '用户名或密码错误';
        messageElement.className = 'text-danger';
    }
}

/**
 * 加载当前收费规则
 */
function loadFeeRule() {
    const feeRule = getCurrentFeeRule();
    document.getElementById('base-price').value = feeRule.basePrice;
    document.getElementById('free-minutes').value = feeRule.freeMinutes;
    document.getElementById('daily-cap').value = feeRule.dailyCap;
}

/**
 * 更新收费规则
 */
function updateFeeRule() {
    if (!isAdminLoggedIn) {
        alert('请先登录');
        return;
    }

    const basePrice = document.getElementById('base-price').value;
    const freeMinutes = document.getElementById('free-minutes').value;
    const dailyCap = document.getElementById('daily-cap').value;
    const messageElement = document.getElementById('fee-rule-message');

    // 验证输入
    if (isNaN(basePrice) || basePrice < 0) {
        messageElement.textContent = '请输入有效的基础价格';
        messageElement.className = 'text-danger';
        return;
    }

    if (isNaN(freeMinutes) || freeMinutes < 0) {
        messageElement.textContent = '请输入有效的免费时长';
        messageElement.className = 'text-danger';
        return;
    }

    if (isNaN(dailyCap) || dailyCap < 0) {
        messageElement.textContent = '请输入有效的每日上限';
        messageElement.className = 'text-danger';
        return;
    }

    // 创建规则对象
    const newRule = {
        basePrice: basePrice,
        freeMinutes: freeMinutes,
        dailyCap: dailyCap
    };

    // 更新规则
    const success = updateFeeRule(newRule);

    if (success) {
        messageElement.textContent = '规则更新成功';
        messageElement.className = 'text-success';
    } else {
        messageElement.textContent = '规则更新失败';
        messageElement.className = 'text-danger';
    }
}

/**
 * 切换标签页
 * @param {String} tabId 标签页ID
 */
function openTab(tabId) {
    // 隐藏所有标签内容
    const tabPanes = document.querySelectorAll('.tab-pane');
    tabPanes.forEach(pane => pane.classList.remove('active'));

    // 移除所有导航链接的active状态
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => link.classList.remove('active'));

    // 激活选中的标签内容
    document.getElementById(tabId).classList.add('active');

    // 高亮当前点击的导航链接
    if (event && event.target) {
        event.target.classList.add('active');
    }

    // 如果切换到记录标签，刷新数据
    if (tabId === 'records') {
        loadParkingRecords();
    }
}