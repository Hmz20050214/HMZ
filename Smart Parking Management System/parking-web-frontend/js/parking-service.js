// 数据管理和业务逻辑

/**
 * 初始化数据 - 在localStorage中创建初始数据
 */
function initData() {
    // 初始化车位数据
    if (!localStorage.getItem('parkingSpots')) {
        const spots = [];
        for (let i = 1; i <= 20; i++) {
            spots.push({
                spotId: i,
                spotNumber: `A-${String(i).padStart(2, '0')}`,
                status: 'FREE',
                floor: 1
            });
        }
        localStorage.setItem('parkingSpots', JSON.stringify(spots));
    }

    // 初始化停车记录
    if (!localStorage.getItem('parkingRecords')) {
        localStorage.setItem('parkingRecords', JSON.stringify([]));
    }

    // 初始化管理员账户
    if (!localStorage.getItem('adminUsers')) {
        localStorage.setItem('adminUsers', JSON.stringify([
            { userId: 1, username: 'admin', password: 'admin', realName: '系统管理员' }
        ]));
    }

    // 初始化收费规则
    if (!localStorage.getItem('feeRules')) {
        localStorage.setItem('feeRules', JSON.stringify([{
            ruleId: 1,
            basePrice: 10,
            freeMinutes: 30,
            dailyCap: 100
        }]));
    }
}

/**
 * 获取所有车位信息
 * @returns {Array} 车位信息数组
 */
function getAllSpots() {
    return JSON.parse(localStorage.getItem('parkingSpots') || '[]');
}

/**
 * 保存车位数据
 * @param {Array} spots 车位信息数组
 */
function saveSpots(spots) {
    localStorage.setItem('parkingSpots', JSON.stringify(spots));
}

/**
 * 获取所有停车记录
 * @returns {Array} 停车记录数组
 */
function getAllRecords() {
    return JSON.parse(localStorage.getItem('parkingRecords') || '[]');
}

/**
 * 保存停车记录
 * @param {Array} records 停车记录数组
 */
function saveRecords(records) {
    localStorage.setItem('parkingRecords', JSON.stringify(records));
}

/**
 * 车辆入场操作
 * @param {String} plateNum 车牌号
 * @param {Number} spotId 车位ID
 * @returns {Boolean} 是否成功入场
 */
function parkIn(plateNum, spotId) {
    const spots = getAllSpots();
    const spotIndex = spots.findIndex(s => s.spotId === spotId);

    if (spotIndex !== -1 && spots[spotIndex].status === 'FREE') {
        // 更新车位状态为占用
        spots[spotIndex].status = 'OCCUPIED';
        saveSpots(spots);

        // 创建停车记录
        const records = getAllRecords();
        const newRecord = {
            recordId: records.length + 1,
            plateNum: plateNum,
            spotId: spotId,
            entryTime: new Date().toISOString(),
            exitTime: null,
            payment: null
        };
        records.push(newRecord);
        saveRecords(records);

        return true;
    }
    return false;
}

/**
 * 车辆出场操作
 * @param {Number} spotId 车位ID
 * @returns {Number|null} 停车费用，失败返回null
 */
function parkOut(spotId) {
    const records = getAllRecords();
    const recordIndex = records.findIndex(r => r.spotId === spotId && !r.exitTime);

    if (recordIndex === -1) {
        return null; // 未找到停车记录
    }

    // 计算停车费用
    const record = records[recordIndex];
    const exitTime = new Date();
    const entryTime = new Date(record.entryTime);
    const durationMinutes = Math.floor((exitTime - entryTime) / (1000 * 60));

    // 获取收费规则
    const feeRules = JSON.parse(localStorage.getItem('feeRules') || '[]');
    const feeRule = feeRules[feeRules.length - 1] || {
        basePrice: 10,
        freeMinutes: 30,
        dailyCap: 100
    };

    // 计算费用
    let payment = 0;
    if (durationMinutes > feeRule.freeMinutes) {
        const hours = Math.ceil((durationMinutes - feeRule.freeMinutes) / 60);
        payment = hours * feeRule.basePrice;
        if (payment > feeRule.dailyCap) {
            payment = feeRule.dailyCap;
        }
    }

    // 更新停车记录
    record.exitTime = exitTime.toISOString();
    record.payment = payment;
    saveRecords(records);

    // 更新车位状态为空闲
    const spots = getAllSpots();
    const spotIndex = spots.findIndex(s => s.spotId === spotId);
    if (spotIndex !== -1) {
        spots[spotIndex].status = 'FREE';
        saveSpots(spots);
    }

    return payment;
}

/**
 * 获取车位的当前停车记录
 * @param {Number} spotId 车位ID
 * @returns {Object|null} 停车记录对象
 */
function getActiveRecordBySpotId(spotId) {
    const records = getAllRecords();
    return records.find(r => r.spotId === spotId && !r.exitTime) || null;
}

/**
 * 管理员登录验证
 * @param {String} username 用户名
 * @param {String} password 密码
 * @returns {Object|null} 管理员对象或null
 */
function adminLogin(username, password) {
    const users = JSON.parse(localStorage.getItem('adminUsers') || '[]');
    return users.find(u => u.username === username && u.password === password) || null;
}

/**
 * 获取当前收费规则
 * @returns {Object} 收费规则对象
 */
function getCurrentFeeRule() {
    const rules = JSON.parse(localStorage.getItem('feeRules') || '[]');
    return rules[rules.length - 1] || {
        ruleId: 0,
        basePrice: 10,
        freeMinutes: 30,
        dailyCap: 100
    };
}

/**
 * 更新收费规则
 * @param {Object} rule 新的收费规则
 * @returns {Boolean} 是否更新成功
 */
function updateFeeRule(rule) {
    const rules = JSON.parse(localStorage.getItem('feeRules') || '[]');

    // 添加新规则（保留历史规则）
    const newRule = {
        ruleId: rules.length + 1,
        basePrice: parseFloat(rule.basePrice) || 0,
        freeMinutes: parseInt(rule.freeMinutes) || 0,
        dailyCap: parseFloat(rule.dailyCap) || 0
    };

    rules.push(newRule);
    localStorage.setItem('feeRules', JSON.stringify(rules));
    return true;
}

/**
 * 获取状态的中文显示
 * @param {String} status 状态码
 * @returns {String} 中文状态
 */
function getStatusText(status) {
    switch(status) {
        case 'FREE': return '空闲';
        case 'OCCUPIED': return '已占用';
        case 'RESERVED': return '已预约';
        default: return status;
    }
}