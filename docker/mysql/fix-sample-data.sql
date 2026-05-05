USE `elm2`;

UPDATE business
SET businessName = '校园小炒',
    businessAddress = '一食堂东侧档口',
    businessExplain = '现炒盖饭和家常小炒，出餐快',
    remarks = '新店优惠'
WHERE businessId = 1;

UPDATE business
SET businessName = '元气早餐铺',
    businessAddress = '宿舍区北门早餐街',
    businessExplain = '豆浆包子粥品，适合早餐和夜宵',
    remarks = '人气早餐'
WHERE businessId = 2;

UPDATE business
SET businessName = '甜心饮品屋',
    businessAddress = '图书馆南侧商业街',
    businessExplain = '奶茶果茶和小甜品',
    remarks = '第二杯半价'
WHERE businessId = 3;

UPDATE food
SET foodName = '宫保鸡丁盖饭',
    foodExplain = '经典川菜，鸡肉嫩滑配花生米',
    remarks = '招牌菜'
WHERE foodId = 1;

UPDATE food
SET foodName = '鱼香肉丝盖饭',
    foodExplain = '酸甜开胃，下饭神器',
    remarks = '人气TOP'
WHERE foodId = 2;

UPDATE food
SET foodName = '扬州炒饭',
    foodExplain = '粒粒分明，配料丰富',
    remarks = '经典款'
WHERE foodId = 3;

UPDATE food
SET foodName = '鲜肉包',
    foodExplain = '现包现蒸，皮薄馅大',
    remarks = '手工制作'
WHERE foodId = 4;

UPDATE food
SET foodName = '皮蛋瘦肉粥',
    foodExplain = '熬制2小时，养胃暖胃',
    remarks = '推荐'
WHERE foodId = 5;

UPDATE food
SET foodName = '油条豆浆套餐',
    foodExplain = '经典早餐组合',
    remarks = '超值'
WHERE foodId = 6;

UPDATE food
SET foodName = '珍珠奶茶',
    foodExplain = '波霸奶茶，可选甜度',
    remarks = '招牌'
WHERE foodId = 7;

UPDATE food
SET foodName = '水果茶',
    foodExplain = '新鲜水果，现切现做',
    remarks = '清爽'
WHERE foodId = 8;

UPDATE food
SET foodName = '芒果西米露',
    foodExplain = '香甜芒果，口感顺滑',
    remarks = '人气'
WHERE foodId = 9;
